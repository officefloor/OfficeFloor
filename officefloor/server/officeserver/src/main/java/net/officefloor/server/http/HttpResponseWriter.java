/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
