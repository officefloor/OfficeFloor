package net.officefloor.server.google.function;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * {@link OfficeFloor} {@link HttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpFunction implements HttpFunction {

	/**
	 * {@link StreamBufferPool}.
	 */
	private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), 10, 1000);

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorHttpFunction.class.getName());

	/**
	 * Factory to create the compiled and open {@link OfficeFloor}.
	 */
	@FunctionalInterface
	public static interface HttpFunctionOfficeFloorFactory {

		/**
		 * Compiles and opens the {@link OfficeFloor}.
		 * 
		 * @return Compiled and open {@link OfficeFloor}.
		 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
		 */
		OfficeFloor compileAndOpenOfficeFloor() throws Exception;
	}

	/**
	 * Default {@link OfficeFloorFactory}.
	 */
	private static final HttpFunctionOfficeFloorFactory DEFAULT_HTTP_FUNCTION_OFFICE_FLOOR_FACTORY = () -> {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();
		return officeFloor;
	};

	/**
	 * Default {@link OfficeFloorFactory}.
	 */
	private static HttpFunctionOfficeFloorFactory officeFloorFactory = DEFAULT_HTTP_FUNCTION_OFFICE_FLOOR_FACTORY;

	/**
	 * {@link GoogleFunctionInstance}.
	 */
	private static GoogleFunctionInstance instance;

	/**
	 * Specifies the {@link OfficeFloorFactory}.
	 * 
	 * @param factory {@link OfficeFloorFactory}.
	 */
	public synchronized static void setOfficeFloorFactory(HttpFunctionOfficeFloorFactory factory) {

		// Ensure instance already not created
		if (instance != null) {
			throw new IllegalStateException("Can not set " + HttpFunctionOfficeFloorFactory.class.getSimpleName()
					+ " once " + OfficeFloorHttpFunction.class.getSimpleName() + " active");
		}

		// Specify the factory
		officeFloorFactory = factory;
	}

	/**
	 * <p>
	 * Resets {@link OfficeFloorHttpFunction} for reloading.
	 * <p>
	 * Should only be called in test code.
	 */
	public synchronized static void reset() {

		// Reset
		officeFloorFactory = DEFAULT_HTTP_FUNCTION_OFFICE_FLOOR_FACTORY;
		instance = null;
	}

	/**
	 * Opens the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to open the {@link OfficeFloor}.
	 */
	public synchronized static void open() throws Exception {
		getGoogleFunctionInstance();
	}

	/**
	 * <p>
	 * Closes the {@link OfficeFloor}.
	 * <p>
	 * AWS should just discard instances. Therefore, this is mainly for testing.
	 * 
	 * @throws Exception If fails to close the {@link OfficeFloor}.
	 */
	public synchronized static void close() throws Exception {

		// Do nothing if already closed
		if (instance == null) {
			return;
		}

		// Stop the instance (and discard to open again)
		try {
			instance.officeFloor.close();
		} finally {
			instance = null;
		}
	}

	/**
	 * Obtains the {@link GoogleFunctionInstance}.
	 * 
	 * @return {@link GoogleFunctionInstance}.
	 * @throws Exception If fails to obtain {@link GoogleFunctionInstance}.
	 */
	private synchronized static GoogleFunctionInstance getGoogleFunctionInstance() throws Exception {

		// Determine if have running instance
		if (instance != null) {
			return instance;
		}

		// Instance not running, so start instance
		OfficeFloor officeFloor = officeFloorFactory.compileAndOpenOfficeFloor();

		// Capture the instance
		instance = new GoogleFunctionInstance(officeFloor,
				GoogleFunctionHttpServerImplementation.getGoogleFunctionHttpServerImplementation());

		// Return the instance
		return instance;
	}

	/*
	 * ==================== HttpFunction ========================
	 */

	@Override
	public void service(HttpRequest request, HttpResponse response) throws Exception {

		// Obtain the Google Function instance
		GoogleFunctionInstance googleFunction;
		try {
			googleFunction = getGoogleFunctionInstance();
		} catch (Exception ex) {
			StringWriter buffer = new StringWriter();
			ex.printStackTrace(new PrintWriter(buffer));
			LOGGER.severe("Failed to start OfficeFloor\n\n" + buffer.toString());
			throw new RuntimeException();
		}

		// Obtain the request details
		boolean isSecure = true; // Google Function always secure
		HttpMethod httpMethod = HttpMethod.getHttpMethod(request.getMethod());
		String requestUri = request.getUri();
		NonMaterialisedHttpHeaders httpHeaders = new GoogleFunctionNonMaterialisedHttpHeaders(request.getHeaders());

		// Obtain the request entity
		ByteArrayOutputStream requestEntityBuffer = new ByteArrayOutputStream();
		InputStream requestEntityInput = request.getInputStream();
		for (int value = requestEntityInput.read(); value != -1; value = requestEntityInput.read()) {
			requestEntityBuffer.write(value);
		}
		ByteSequence requestEntity = new ByteArrayByteSequence(requestEntityBuffer.toByteArray());

		// Create the response writer
		GoogleFunctionHttpResponseWriter responseWriter;
		synchronized (response) {
			responseWriter = new GoogleFunctionHttpResponseWriter(response, bufferPool);
		}

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				googleFunction.location, isSecure, () -> httpMethod, () -> requestUri, HttpVersion.HTTP_1_1,
				httpHeaders, requestEntity, googleFunction.serverName, googleFunction.dateHttpHeaderClock,
				googleFunction.isIncludeEscalationStackTrace, responseWriter, bufferPool);

		// Service request
		boolean[] isComplete = new boolean[] { false };
		googleFunction.input.service(connection, (ex) -> {

			// Undertake service completion
			connection.getServiceFlowCallback().run(ex);

			// Notify request processing complete
			synchronized (isComplete) {
				isComplete[0] = true;
				isComplete.notify();
			}
		});

		// Wait until servicing complete
		synchronized (isComplete) {
			while (!isComplete[0]) {
				isComplete.wait(100);
			}
		}

		// Determine if failure
		IOException writeFailure = responseWriter.getWriteFailure();
		if (writeFailure != null) {
			throw writeFailure;
		}
	}

	/**
	 * Running {@link OfficeFloor}.
	 */
	private static class GoogleFunctionInstance {

		/**
		 * {@link OfficeFloor}.
		 */
		private final OfficeFloor officeFloor;

		/**
		 * {@link HttpServerLocation}.
		 */
		private final HttpServerLocation location;

		/**
		 * {@link ExternalServiceInput}.
		 */
		@SuppressWarnings("rawtypes")
		private final ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

		/**
		 * Server {@link HttpHeaderValue}.
		 */
		private final HttpHeaderValue serverName;

		/**
		 * {@link DateHttpHeaderClock}.
		 */
		private final DateHttpHeaderClock dateHttpHeaderClock;

		/**
		 * Indicates if include stack trace in HTTP response.
		 */
		private final boolean isIncludeEscalationStackTrace;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloor                            {@link OfficeFloor}.
		 * @param googleFunctionHttpServerImplementation {@link GoogleFunctionHttpServerImplementation}.
		 */
		private GoogleFunctionInstance(OfficeFloor officeFloor,
				GoogleFunctionHttpServerImplementation googleFunctionHttpServerImplementation) {
			this.officeFloor = officeFloor;

			// Load remaining from server implementation
			this.location = googleFunctionHttpServerImplementation.getHttpServerLocation();
			this.input = googleFunctionHttpServerImplementation.getInput();
			this.serverName = googleFunctionHttpServerImplementation.getServerName();
			this.dateHttpHeaderClock = googleFunctionHttpServerImplementation.getDateHttpHeaderClock();
			this.isIncludeEscalationStackTrace = googleFunctionHttpServerImplementation.isIncludeEscalationStackTrace();
		}
	}

}