/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * {@link HttpResponseWriter} implementation.
 *
 * TODO handle Accept and translating HTTP response for the client.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterImpl implements HttpResponseWriter {

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response;

	/**
	 * Initiate.
	 *
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public HttpResponseWriterImpl(ServerHttpConnection connection) {
		this.response = connection.getHttpResponse();
	}

	/*
	 * =================== HttpResponseWriter =======================
	 */

	@Override
	public void write(String contentEncoding, String contentType,
			Charset charset, ByteBuffer contents) throws IOException {

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
			this.response.removeHeaders(CONTENT_ENCODING_HEADER_NAME);
			this.response.addHeader(CONTENT_ENCODING_HEADER_NAME,
					contentEncoding);
		}

		// Specify the content-type if not blank
		if ((contentType != null) && (contentType.trim().length() > 0)) {
			final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
			this.response.removeHeaders(CONTENT_TYPE_HEADER_NAME);
			contentType = contentType
					+ (charset == null ? "" : "; charset=" + charset.name());
			this.response.addHeader(CONTENT_TYPE_HEADER_NAME, contentType);
		}

		// Write the contents to the response
		this.response.getBody().append(contents);
	}

	@Override
	public void write(String contentType, String contents) throws IOException {

		// Specify the content-type if not blank
		if ((contentType != null) && (contentType.trim().length() > 0)) {
			final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
			this.response.removeHeaders(CONTENT_TYPE_HEADER_NAME);
			this.response.addHeader(CONTENT_TYPE_HEADER_NAME, contentType);
		}

		// Write the contents to the response
		OutputStreamWriter writer = new OutputStreamWriter(this.response
				.getBody().getOutputStream());
		writer.write(contents);
		writer.flush();
	}

}