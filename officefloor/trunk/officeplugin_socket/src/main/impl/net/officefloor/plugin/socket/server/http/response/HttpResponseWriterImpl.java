/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.socket.server.http.response;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link HttpResponseWriter} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterImpl implements HttpResponseWriter {

	/*
	 * =================== HttpResponseWriter =======================
	 */

	@Override
	public void writeContent(ServerHttpConnection connection,
			String contentEncoding, String contentType, ByteBuffer contents)
			throws IOException {

		// Obtain the response
		HttpResponse response = connection.getHttpResponse();

		/*
		 * TODO handle translations should client not accept encoding/type.
		 *
		 * DETAILS: Should interrogate the HttpRequest to obtain the 'Accept-*'
		 * values to then determine if require to translate the input contents
		 * to acceptable encoding/type for client.
		 *
		 * MITIGATION: Likely will be required however keeping it simple at
		 * moment. Also for initial use would only be sending HTTP file which is
		 * likely to have supported defaults that are acceptable to most
		 * clients.
		 */

		// Specify the content-encoding if not blank
		if ((contentEncoding != null) && (contentEncoding.trim().length() > 0)) {
			final String CONTENT_ENCODING_HEADER_NAME = "Content-Encoding";
			response.removeHeaders(CONTENT_ENCODING_HEADER_NAME);
			response.addHeader(CONTENT_ENCODING_HEADER_NAME, contentEncoding);
		}

		// Specify the content-type if not blank
		if ((contentType != null) && (contentType.trim().length() > 0)) {
			final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
			response.removeHeaders(CONTENT_TYPE_HEADER_NAME);
			response.addHeader(CONTENT_TYPE_HEADER_NAME, contentType);
		}

		// Write the contents to the response
		response.getBody().append(contents);
	}
}