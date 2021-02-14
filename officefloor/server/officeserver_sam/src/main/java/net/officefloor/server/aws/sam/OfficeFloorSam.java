package net.officefloor.server.aws.sam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Handles the {@link APIGatewayProxyRequestEvent}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSam implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	/**
	 * {@link StreamBufferPool}.
	 */
	private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), 10, 1000);

	/**
	 * {@link SamInstance}.
	 */
	private static SamInstance instance;

	/**
	 * Opens the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to open the {@link OfficeFloor}.
	 */
	public synchronized static void open() throws Exception {
		getSamInstance();
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
	 * Obtains the {@link SamInstance}.
	 * 
	 * @return {@link SamInstance}.
	 * @throws Exception If fails to obtain {@link SamInstance}.
	 */
	private synchronized static SamInstance getSamInstance() throws Exception {

		// Determine if have running instance
		if (instance != null) {
			return instance;
		}

		// Instance not running, so start instance
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");
		officeFloor.openOfficeFloor();

		// Capture the instance
		instance = new SamInstance(officeFloor, SamHttpServerImplementation.getSamHttpServerImplementation());

		// Return the instance
		return instance;
	}

	/**
	 * Indicates if log.
	 */
	private final boolean isLog;

	/**
	 * Instantiate.
	 * 
	 * @param isLog Indicates if log.
	 */
	public OfficeFloorSam(boolean isLog) {
		this.isLog = isLog;
	}

	/**
	 * Default constructor required by AWS.
	 */
	public OfficeFloorSam() {
		this(true);
	}

	/**
	 * ======================= RequestHandler =========================
	 */

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		// Obtain the sam instance
		SamInstance sam;
		try {
			sam = getSamInstance();
		} catch (Exception ex) {
			StringWriter buffer = new StringWriter();
			ex.printStackTrace(new PrintWriter(buffer));
			context.getLogger().log("Failed to start OfficeFloor\n\n" + buffer.toString());
			throw new RuntimeException();
		}

		// Obtain the request details
		boolean isSecure = true; // API Gateway always secure
		HttpMethod httpMethod = HttpMethod.getHttpMethod(input.getHttpMethod());
		String path = input.getPath();
		NonMaterialisedHttpHeaders httpHeaders = new SamNonMaterialisedHttpHeaders(input.getMultiValueHeaders());
		ByteSequence entity = new SamEntityByteSequence(input.getBody(), input.getIsBase64Encoded());

		// Update request with query parameters
		Map<String, List<String>> multiQueryParams = input.getMultiValueQueryStringParameters();
		Map<String, String> queryParams = input.getQueryStringParameters();
		if ((multiQueryParams != null) && (multiQueryParams.size() > 0)) {
			StringBuilder pathWithQuery = new StringBuilder(path);
			String separator = "?";
			for (String name : multiQueryParams.keySet()) {
				for (String value : multiQueryParams.get(name)) {
					pathWithQuery.append(separator);
					pathWithQuery.append(name);
					pathWithQuery.append("=");
					pathWithQuery.append(value);
					separator = "&";
				}
			}
			path = pathWithQuery.toString();

		} else if ((queryParams != null) && (queryParams.size() > 0)) {
			StringBuilder pathWithQuery = new StringBuilder(path);
			String separator = "?";
			for (String name : queryParams.keySet()) {
				String value = queryParams.get(name);
				pathWithQuery.append(separator);
				pathWithQuery.append(name);
				pathWithQuery.append("=");
				pathWithQuery.append(value);
				separator = "&";
			}
			path = pathWithQuery.toString();
		}

		// Indicate servicing request
		if (this.isLog) {
			sam.logger.info("OfficeFloor servicing request " + httpMethod + " " + path);
		}

		// Create the response writer
		SamHttpResponseWriter responseWriter = new SamHttpResponseWriter(bufferPool);

		// Create the connection
		final String requestUri = path;
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				sam.location, isSecure, () -> httpMethod, () -> requestUri, HttpVersion.HTTP_1_1, httpHeaders, entity,
				null, null, sam.isIncludeEscalationStackTrace, responseWriter, bufferPool);

		// Service request
		sam.input.service(connection, connection.getServiceFlowCallback());

		// Provide response
		APIGatewayProxyResponseEvent response = responseWriter.getApiGatewayProxyResponseEvent();

		// Indicate services
		if (this.isLog) {
			sam.logger.info("Request " + httpMethod + " " + path + " serviced by OfficeFloor");
		}

		// Return the response
		return response;
	}

	/**
	 * Running {@link OfficeFloor}.
	 */
	private static class SamInstance {

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
		 * Indicates if include stack trace in HTTP response.
		 */
		private final boolean isIncludeEscalationStackTrace;

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloor                 {@link OfficeFloor}.
		 * @param samHttpServerImplementation {@link SamHttpServerImplementation}.
		 */
		private SamInstance(OfficeFloor officeFloor, SamHttpServerImplementation samHttpServerImplementation) {
			this.officeFloor = officeFloor;

			// Load remaining from server implementation
			this.location = samHttpServerImplementation.getHttpServerLocation();
			this.input = samHttpServerImplementation.getInput();
			this.isIncludeEscalationStackTrace = samHttpServerImplementation.isIncludeEscalationStackTrace();
			this.logger = samHttpServerImplementation.getLogger();
		}
	}

}