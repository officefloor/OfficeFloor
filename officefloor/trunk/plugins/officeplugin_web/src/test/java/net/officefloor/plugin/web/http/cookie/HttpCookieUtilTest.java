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
package net.officefloor.plugin.web.http.cookie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.web.http.cookie.HttpCookie;
import net.officefloor.plugin.web.http.cookie.HttpCookieUtil;

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
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse httpResponse = this
			.createMock(HttpResponse.class);

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
	 * Ensure correctly adds {@link HttpCookie} to response {@link HttpHeader}.
	 */
	public void testAddCookieToResponse() throws Exception {

		final long currentTime = System.currentTimeMillis();
		String expireText = this.getExpireText(currentTime);
		HttpHeader header = this.createMock(HttpHeader.class);

		// Create the HTTP cookie
		HttpCookie cookie = new HttpCookie("test", "value");
		cookie.setExpires(currentTime);
		cookie.setPath("/");
		cookie.setDomain(".officefloor.net");

		// Record adding cookie
		this.recordReturn(this.httpResponse, this.httpResponse.getHeaders(),
				new HttpHeader[0]);
		this.recordReturn(this.httpResponse, this.httpResponse.addHeader(
				"set-cookie", "test=\"value\"; expires=" + expireText
						+ "; path=/; domain=.officefloor.net"), header);

		// Add the cookie to HTTP response
		this.replayMockObjects();
		HttpHeader returnedHeader = HttpCookieUtil.addHttpCookie(cookie,
				this.httpResponse);
		this.verifyMockObjects();
		assertSame("Incorrect HTTP header", header, returnedHeader);
	}

	/**
	 * Ensure correctly adds {@link HttpCookie} that replaces an existing
	 * {@link HttpCookie} on the {@link HttpResponse}.
	 */
	public void testAddCookieReplacingExistingHeaderValue() throws Exception {

		final long currentTime = System.currentTimeMillis();
		String expireText = this.getExpireText(currentTime);
		final HttpHeader header = this.createMock(HttpHeader.class);

		// Create the existing cookie header
		HttpCookie existingCookie = new HttpCookie("test", "existing");
		existingCookie.setExpires(currentTime + 1000);
		existingCookie.setPath("/existing");
		existingCookie.setDomain(".existing.officefloor.net");
		HttpHeader existingHeader = new HttpHeaderImpl("set-cookie",
				existingCookie.toHttpResponseHeaderValue());
		HttpHeader anotherHeader = new HttpHeaderImpl("set-cookie",
				"another=cookie");

		// Create the HTTP cookie
		HttpCookie cookie = new HttpCookie("test", "replace");
		cookie.setExpires(currentTime);
		cookie.setPath("/replace");
		cookie.setDomain(".replace.officefloor.net");

		// Record adding cookie (removing existing cookie header)
		this.recordReturn(this.httpResponse, this.httpResponse.getHeaders(),
				new HttpHeader[] { existingHeader, anotherHeader });
		this.httpResponse.removeHeader(existingHeader);
		this.recordReturn(this.httpResponse, this.httpResponse.addHeader(
				"set-cookie", "test=\"replace\"; expires=" + expireText
						+ "; path=/replace; domain=.replace.officefloor.net"),
				header);

		// Add the cookie to HTTP response
		this.replayMockObjects();
		HttpHeader returnedHeader = HttpCookieUtil.addHttpCookie(cookie,
				this.httpResponse);
		this.verifyMockObjects();
		assertSame("Incorrect HTTP header", header, returnedHeader);
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

	/**
	 * Obtains the expire text for the expire time.
	 *
	 * @param expireTime
	 *            Expire time.
	 * @return Expire text.
	 */
	private String getExpireText(long expireTime) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(new Date(expireTime));
	}

}