package net.officefloor.server.google.function;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.google.cloud.functions.HttpResponse;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.StreamBufferUtil;

/**
 * Google Function {@link HttpResponseWriter}.
 */
public class GoogleFunctionHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Possible failure.
	 */
	private volatile IOException failure = null;

	/**
	 * Instantiate.
	 * 
	 * @param response   {@link HttpResponse}.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public GoogleFunctionHttpResponseWriter(HttpResponse response, StreamBufferPool<ByteBuffer> bufferPool) {
		this.response = response;
		this.bufferPool = bufferPool;
	}

	/**
	 * Obtains the write failure.
	 * 
	 * @return {@link IOException} for write failure or <code>null</code> if no
	 *         failure.
	 */
	public IOException getWriteFailure() {
		synchronized (this.response) {
			return this.failure;
		}
	}

	/*
	 * ================ HttpResponseWriter ====================
	 */

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {

		// Write the response
		synchronized (this.response) {

			// Load values to response
			this.response.setStatusCode(status.getStatusCode());

			// Load the headers
			WritableHttpHeader header = headHttpHeader;
			while (header != null) {
				this.response.appendHeader(header.getName(), header.getValue());
				header = header.next;
			}

			// Load the cookies
			WritableHttpCookie nextCookie = headHttpCookie;
			while (nextCookie != null) {
				this.response.appendHeader("Set-Cookie", nextCookie.toResponseHeaderValue());
				nextCookie = nextCookie.next;
			}

			// Load the entity
			if (contentType != null) {
				this.response.setContentType(contentType.getValue());
			}

			// Write the response entity
			try {
				OutputStream entity = this.response.getOutputStream();
				StreamBufferUtil.write(contentHeadStreamBuffer, entity, bufferPool);

			} catch (IOException ex) {
				// Capture the failure
				this.failure = ex;
			}
		}
	}

}
