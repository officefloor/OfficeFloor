/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.parameters;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;

/**
 * Utility class aiding in testing HTTP functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTestUtil {

	/**
	 * Creates a {@link HttpRequest} for testing.
	 * 
	 * @param method
	 *            HTTP method (GET, POST).
	 * @param requestUri
	 *            Request URI.
	 * @param body
	 *            Contents of the {@link HttpRequest} body.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 * @return {@link HttpRequest}.
	 */
	public static HttpRequest createHttpRequest(String method,
			String requestUri, String body, String... headerNameValues)
			throws Exception {

		// Transform the body into input buffer stream
		byte[] bodyData = (body == null ? new byte[0] : body.getBytes(Charset
				.forName("UTF-8")));
		BufferStream bufferStream = new BufferStreamImpl(ByteBuffer
				.wrap(bodyData));
		bufferStream.getOutputBufferStream().close(); // all data available
		InputBufferStream inputBufferStream = bufferStream
				.getInputBufferStream();

		// Create the headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		if (body != null) {
			// Include content length if body
			headers.add(new HttpHeaderImpl("content-length", String
					.valueOf(bodyData.length)));
		}
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Return the HTTP request
		return new HttpRequestImpl(method, requestUri, "HTTP/1.1", headers,
				inputBufferStream);
	}

	/**
	 * All access via static methods.
	 */
	private HttpTestUtil() {
	}

}