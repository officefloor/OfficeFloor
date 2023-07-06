package net.officefloor.server.google.function;

import java.nio.ByteBuffer;

import com.google.cloud.functions.HttpResponse;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Google Function {@link HttpResponseWriter}.
 */
public class GoogleFunctionHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response;

	/**
	 * Instantiate.
	 * 
	 * @param response {@link HttpResponse}.
	 */
	public GoogleFunctionHttpResponseWriter(HttpResponse response) {
		this.response = response;
	}

	/*
	 * ================ HttpResponseWriter ====================
	 */

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {

		// TODO implement writing response
		try {
			response.getWriter().write("{\"text\":\"MOCK RESPONSE\"}");
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		
	}

}
