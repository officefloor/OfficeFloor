/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

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
	 * @param entity
	 *            Contents of the {@link HttpRequest} entity.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 * @return {@link HttpRequest}.
	 */
	public static HttpRequest createHttpRequest(String method,
			String requestUri, String entity, String... headerNameValues)
			throws Exception {

		// Obtain the entity data
		byte[] entityData = (entity == null ? new byte[0] : entity
				.getBytes(Charset.forName("UTF-8")));

		// Create the headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		if (entity != null) {
			// Include content length if entity
			headers.add(new HttpHeaderImpl("content-length", String
					.valueOf(entityData.length)));
		}
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the entity input stream
		ServerInputStreamImpl inputStream = new ServerInputStreamImpl(
				new Object());
		inputStream.inputData(entityData, 0, (entityData.length - 1), false);
		HttpEntity httpEntity = new HttpEntityImpl(inputStream);

		// Return the HTTP request
		return new HttpRequestImpl(method, requestUri, "HTTP/1.1", headers,
				httpEntity);
	}

	/**
	 * All access via static methods.
	 */
	private HttpTestUtil() {
	}

}