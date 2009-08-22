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

import java.util.ArrayList;
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
	 * Ensure extracts the first {@link HttpCookie} by name.
	 */
	public void testExtractFirst() {

		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		headers.add(this.createCookieHttpHeader("test", "value"));
		headers.add(this.createCookieHttpHeader("test",
				"should only return first found cookie"));

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				headers);

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} with a quoted value.
	 */
	public void testExtractQuotedValue() {

		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		headers.add(this.createCookieHttpHeader("test", "\"value\""));

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				headers);

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} with an empty quoted value.
	 */
	public void testExtractEmptyQuotedValue() {

		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		headers.add(this.createCookieHttpHeader("test", "\"\""));

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				headers);

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} from multiple in same
	 * {@link HttpHeader}.
	 */
	public void testExtractFromMuliple() {

		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		HttpHeader one = this.createCookieHttpHeader("another", "value");
		HttpHeader two = this.createCookieHttpHeader("test", "value");
		HttpHeader header = new HttpHeaderImpl(one.getName(), one.getValue()
				+ "," + two.getValue());
		headers.add(header);

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				headers);

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} from multiple in same
	 * {@link HttpHeader} that contains quoted separators.
	 */
	public void testIgnoreQuotedSeparators() {

		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		HttpHeader attributeSeparatorHeader = this.createCookieHttpHeader(
				"attribute", "\";\"");
		HttpHeader cookieSeparatorHeader = this.createCookieHttpHeader(
				"cookie", "\",\"");
		HttpHeader cookieHeader = this.createCookieHttpHeader("test", "value");
		HttpHeader header = new HttpHeaderImpl(cookieHeader.getName(),
				attributeSeparatorHeader.getValue() + ","
						+ cookieSeparatorHeader.getValue() + ","
						+ cookieHeader.getValue());
		headers.add(header);

		// Record
		this.recordReturn(this.httpRequest, this.httpRequest.getHeaders(),
				headers);

		// Extract the http cookie
		this.replayMockObjects();
		HttpCookie cookie = HttpCookieUtil.extractHttpCookie("test",
				this.httpRequest);
		this.verifyMockObjects();

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Creates the {@link HttpHeader} containing the {@link HttpCookie}.
	 *
	 * @param nameValuePairs
	 *            Name value pairs of the {@link HttpCookie} attributes.
	 * @return {@link HttpHeader} containing the {@link HttpCookie}.
	 */
	private HttpHeader createCookieHttpHeader(String... nameValuePairs) {
		// Create text of header value
		StringBuilder headerValue = new StringBuilder();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			headerValue.append(name + "=" + value + ";");
		}
		return new HttpHeaderImpl("cookie", headerValue.toString());
	}
}