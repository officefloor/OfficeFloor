package net.officefloor.server.aws.sam;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * SAM {@link HttpResponseWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class SamHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link ByteBufferWriter}.
	 */
	private static final ByteBufferWriter byteBufferWriter = (buffer, outputStream) -> {
		for (int position = BufferJvmFix.position(buffer); position < BufferJvmFix.limit(buffer); position++) {
			outputStream.write(buffer.get());
		}
	};

	/**
	 * Writes the {@link ByteBuffer} to the {@link OutputStream}.
	 */
	@FunctionalInterface
	private static interface ByteBufferWriter {

		/**
		 * Writes the {@link ByteBuffer} to the {@link OutputStream}.
		 * 
		 * @param buffer      {@link ByteBuffer}.
		 * @param outputSteam {@link OutputStream}.
		 * @throws IOException If fails to write {@link IOException}.
		 */
		void write(ByteBuffer buffer, OutputStream outputSteam) throws IOException;
	}

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Possible {@link Exception}.
	 */
	private volatile RuntimeException exception;

	/**
	 * {@link APIGatewayProxyResponseEvent}.
	 */
	private volatile APIGatewayProxyResponseEvent responseEvent;

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public SamHttpResponseWriter(StreamBufferPool<ByteBuffer> bufferPool) {
		this.bufferPool = bufferPool;
	}

	/**
	 * Obtains the {@link APIGatewayProxyResponseEvent}.
	 * 
	 * @return {@link APIGatewayProxyResponseEvent}.
	 * @throws RuntimeException If fails to write the response.
	 */
	public APIGatewayProxyResponseEvent getApiGatewayProxyResponseEvent() throws RuntimeException {

		// Determine if failure
		RuntimeException ex = this.exception;
		if (ex != null) {
			throw ex;
		}

		// Provide the response
		return this.responseEvent;
	}

	/*
	 * =================== HttpResponseWriter =====================
	 */

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {

		// Create the response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		// Load details to response
		response.setStatusCode(status.getStatusCode());

		// Provide the headers and cookies
		Map<String, String> headers = new HashMap<>();
		while (headHttpHeader != null) {
			headers.put(headHttpHeader.getName(), headHttpHeader.getValue());
			headHttpHeader = headHttpHeader.next;
		}
		while (headHttpCookie != null) {
			headers.put("set-cookie", headHttpCookie.toResponseHeaderValue());
			headHttpCookie = headHttpCookie.next;
		}
		if (contentType != null) {
			headers.put("Content-Type", contentType.getValue());
		}
		if (contentLength > 0) {
			headers.put("Content-Length", String.valueOf(contentLength));
		}
		response.setHeaders(headers);

		// Read entity
		if (contentHeadStreamBuffer != null) {

			// Write the entity
			try {
				Base64Buffer entity = new Base64Buffer();
				OutputStream entityOutputStream = entity.getOutputStream();
				StreamBuffer<ByteBuffer> stream = contentHeadStreamBuffer;
				while (stream != null) {
					if (stream.pooledBuffer != null) {
						// Write the pooled byte buffer
						BufferJvmFix.flip(stream.pooledBuffer);
						byteBufferWriter.write(stream.pooledBuffer, entityOutputStream);

					} else if (stream.unpooledByteBuffer != null) {
						// Write the unpooled byte buffer
						byteBufferWriter.write(stream.unpooledByteBuffer, entityOutputStream);

					} else {
						// Write the file content
						StreamBuffer<ByteBuffer> streamBuffer = bufferPool.getPooledStreamBuffer();
						boolean isWritten = false;
						try {
							ByteBuffer buffer = streamBuffer.pooledBuffer;
							long position = stream.fileBuffer.position;
							long count = stream.fileBuffer.count;
							int bytesRead;
							do {
								BufferJvmFix.clear(buffer);

								// Read bytes
								bytesRead = stream.fileBuffer.file.read(buffer, position);
								position += bytesRead;

								// Setup for bytes
								BufferJvmFix.flip(buffer);
								if (count >= 0) {
									count -= bytesRead;

									// Determine read further than necessary
									if (count < 0) {
										BufferJvmFix.limit(buffer, BufferJvmFix.limit(buffer) - (int) Math.abs(count));
										bytesRead = 0;
									}
								}

								// Write the buffer
								byteBufferWriter.write(buffer, entityOutputStream);
							} while (bytesRead > 0);

							// As here, written file
							isWritten = true;

						} finally {
							streamBuffer.release();

							// Close the file
							if (stream.fileBuffer.callback != null) {
								stream.fileBuffer.callback.complete(stream.fileBuffer.file, isWritten);
							}
						}
					}
					stream = stream.next;
				}

				// Provide entity
				response.setBody(entity.getBase64Text());
				response.setIsBase64Encoded(true);

			} catch (IOException ex) {
				// Failed to handle response
				this.exception = new RuntimeException(ex);
				return; // will propagate failure, no need for further processing
			}
		}

		// Specify as response event
		this.responseEvent = response;
	}

}