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
package net.officefloor.plugin.socket.server.http.cookie;

import java.net.HttpCookie;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;

/**
 * Tests the {@link HttpCookieUtil}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCookieUtilTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpRequest}.
	 */
	private final HttpRequest httpRequest = this.createMock(HttpRequest.class);

	/**
	 * Ensure able to extract the {@link HttpCookie}.
	 */
	public void testExtractHttpCookie() {

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(), this
				.createHttpHeaders("test", "value"));

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Creates the {@link HttpHeader} instances.
	 *
	 * @param nameValuePairs
	 *            Name value pairs for the {@link HttpHeader} instances.
	 * @return {@link HttpHeader} instances.
	 */
	private List<HttpHeader> createHttpHeaders(String... nameValuePairs) {
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			headers.add(new HttpHeaderImpl("cookie", name + "=\"" + value
					+ "\""));
		}
		return headers;
	}
}