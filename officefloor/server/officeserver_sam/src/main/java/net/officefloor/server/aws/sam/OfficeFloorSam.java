package net.officefloor.server.aws.sam;

import java.nio.ByteBuffer;

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
public class OfficeFloorSam
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, AutoCloseable {

	/**
	 * {@link StreamBufferPool}.
	 */
	private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), 10, 1000);

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
	 * Instantiated by AWS.
	 * 
	 * @throws Exception If fails to start {@link OfficeFloor}.
	 */
	public OfficeFloorSam() throws Exception {

		// Open OfficeFloor
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.officeFloor = compiler.compile("OfficeFloor");
		this.officeFloor.openOfficeFloor();

		// Capture the servicing details
		SamHttpServerImplementation samHttpServerImplementation = SamHttpServerImplementation
				.getSamHttpServerImplementation();
		this.location = samHttpServerImplementation.getHttpServerLocation();
		this.input = samHttpServerImplementation.getInput();
		this.isIncludeEscalationStackTrace = samHttpServerImplementation.isIncludeEscalationStackTrace();
	}

	/**
	 * ======================= RequestHandler =========================
	 */

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

		// Obtain the request details
		boolean isSecure = true; // API Gateway always secure
		HttpMethod httpMethod = HttpMethod.getHttpMethod(input.getHttpMethod());
		String requestUri = input.getPath();
		NonMaterialisedHttpHeaders httpHeaders = new SamNonMaterialisedHttpHeaders(input.getMultiValueHeaders());
		ByteSequence entity = new SamEntityByteSequence(input.getBody(), input.getIsBase64Encoded());

		// Create the response writer
		SamHttpResponseWriter responseWriter = new SamHttpResponseWriter(bufferPool);

		// Create the connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.location, isSecure, () -> httpMethod, () -> requestUri, HttpVersion.HTTP_1_1, httpHeaders, entity,
				null, null, this.isIncludeEscalationStackTrace, responseWriter, bufferPool);

		// Service request
		this.input.service(connection, connection.getServiceFlowCallback());

		// Provide response
		return responseWriter.getApiGatewayProxyResponseEvent();
	}

	/*
	 * ======================== AutoCloseable ==========================
	 */

	@Override
	public void close() throws Exception {
		this.officeFloor.close();
	}

}