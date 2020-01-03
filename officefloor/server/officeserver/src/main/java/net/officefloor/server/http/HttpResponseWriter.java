package net.officefloor.server.http;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Writes the {@link HttpResponse}.
 * 
 * @param <B>
 *            Type of buffer.
 * @author Daniel Sagenschneider
 */
public interface HttpResponseWriter<B> {

	/**
	 * Writes the {@link HttpResponse}.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param status
	 *            {@link HttpStatus}.
	 * @param headHttpHeader
	 *            Head {@link WritableHttpHeader} to the linked list of
	 *            {@link WritableHttpHeader} instances for the
	 *            {@link HttpResponse}.
	 * @param headHttpCookie
	 *            Head {@link WritableHttpCookie} to the linked list of
	 *            {@link WritableHttpCookie} instances for the
	 *            {@link HttpResponse}.
	 * @param contentLength
	 *            Number of bytes in the HTTP entity.
	 * @param contentType
	 *            <code>Content-Type</code> of the HTTP entity.
	 * @param contentHeadStreamBuffer
	 *            Head {@link StreamBuffer} to the linked list of
	 *            {@link StreamBuffer} instances containing the
	 *            {@link HttpResponse} entity. May be <code>null</code> if no
	 *            entity content.
	 */
	void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
			WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
			StreamBuffer<B> contentHeadStreamBuffer);

}
