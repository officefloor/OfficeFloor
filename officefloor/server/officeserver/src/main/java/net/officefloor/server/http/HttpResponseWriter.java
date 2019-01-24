/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
