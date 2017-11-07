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
package net.officefloor.web.cookie;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponseBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.state.HttpCookie;

/**
 * Tests the {@link HttpCookieUtil}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCookieTest extends OfficeFrameTestCase {

	/**
	 * Ensure extracts the first {@link HttpCookie} by name.
	 */
	public void testExtractFirst() {

		// Create mock request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		loadCookieHttpHeader(request, "test", "value");
		loadCookieHttpHeader(request, "test", "should only return first found cookie");

		// Extract the http cookie
		HttpCookie cookie = HttpCookie.extractHttpCookie("test", request.build());

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} with a quoted value.
	 */
	public void testExtractQuotedValue() {

		// Create mock request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		loadCookieHttpHeader(request, "test", "\"value\"");

		// Extract the http cookie
		HttpCookie cookie = HttpCookie.extractHttpCookie("test", request.build());

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} with an empty quoted value.
	 */
	public void testExtractEmptyQuotedValue() {

		// Create mock request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		loadCookieHttpHeader(request, "test", "\"\"");

		// Extract the http cookie
		HttpCookie cookie = HttpCookie.extractHttpCookie("test", request.build());

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} from multiple in same
	 * {@link HttpHeader}.
	 */
	public void testExtractFromMuliple() {

		// Create mock request
		MockHttpRequestBuilder setup = MockHttpServer.mockRequest();
		loadCookieHttpHeader(setup, "another", "value");
		loadCookieHttpHeader(setup, "test", "value");
		MockHttpRequestBuilder request = normaliseCookies(setup);

		// Extract the http cookie
		HttpCookie cookie = HttpCookie.extractHttpCookie("test", request.build());

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure able to extract the {@link HttpCookie} from multiple in same
	 * {@link HttpHeader} that contains quoted separators.
	 */
	public void testIgnoreQuotedSeparators() {

		// Create mock request
		MockHttpRequestBuilder setup = MockHttpServer.mockRequest();
		loadCookieHttpHeader(setup, "attribute", "\";\"");
		loadCookieHttpHeader(setup, "cookie", "\",\"");
		loadCookieHttpHeader(setup, "test", "value");
		MockHttpRequestBuilder request = normaliseCookies(setup);

		// Extract the http cookie
		HttpCookie cookie = HttpCookie.extractHttpCookie("test", request.build());

		// Ensure correct cookie
		assertEquals("Incorrect cookie", "value", cookie.getValue());
	}

	/**
	 * Ensure correctly adds {@link HttpCookie} to response {@link HttpHeader}.
	 */
	public void testAddCookieToResponse() throws Exception {

		final long currentTime = System.currentTimeMillis();
		String expireText = this.getExpireText(currentTime);

		// Create the HTTP cookie
		HttpCookie cookie = new HttpCookie("test", "value");
		cookie.setExpires(currentTime);
		cookie.setPath("/");
		cookie.setDomain(".officefloor.net");

		// Add the cookie to HTTP response
		MockHttpResponseBuilder response = MockHttpServer.mockResponse();
		HttpHeader returnedHeader = HttpCookie.addHttpCookie(cookie, response);

		// Ensure correct value added
		final String expectedValue = "test=\"value\"; expires=" + expireText + "; path=/; domain=.officefloor.net";
		assertEquals("Incorrect HTTP header", expectedValue, returnedHeader.getValue());
		assertEquals("Incorrect response", expectedValue,
				response.build().getFirstHeader(HttpCookie.SET_COOKIE.getName()).getValue());
	}

	/**
	 * Ensure correctly adds {@link HttpCookie} that replaces an existing
	 * {@link HttpCookie} on the {@link HttpResponse}.
	 */
	public void testAddCookieReplacingExistingHeaderValue() throws Exception {

		final long currentTime = System.currentTimeMillis();
		String expireText = this.getExpireText(currentTime);

		// Create the existing cookie header
		HttpCookie existingCookie = new HttpCookie("test", "existing");
		existingCookie.setExpires(currentTime + 1000);
		existingCookie.setPath("/existing");
		existingCookie.setDomain(".existing.officefloor.net");

		// Create the HTTP cookie
		HttpCookie cookie = new HttpCookie("test", "replace");
		cookie.setExpires(currentTime);
		cookie.setPath("/replace");
		cookie.setDomain(".replace.officefloor.net");

		// Add the cookie to HTTP response
		MockHttpResponseBuilder response = MockHttpServer.mockResponse();
		HttpCookie.addHttpCookie(existingCookie, response);
		HttpCookie.addHttpCookie(cookie, response);
		List<WritableHttpHeader> headers = response.build().getHttpHeaders();
		assertEquals("Should be just the one header, as replaced", 1, headers.size());
		final String expectedValue = "test=\"replace\"; expires=" + expireText
				+ "; path=/replace; domain=.replace.officefloor.net";
		assertEquals("Incorrect header value", expectedValue, headers.get(0).getValue());
	}

	/**
	 * Loads a {@link HttpHeader} containing the {@link HttpCookie}.
	 *
	 * @param nameValuePairs
	 *            Name value pairs of the {@link HttpCookie} attributes.
	 * @return {@link HttpHeader} containing the {@link HttpCookie}.
	 */
	private static void loadCookieHttpHeader(MockHttpRequestBuilder request, String... nameValuePairs) {
		// Create text of header value
		StringBuilder headerValue = new StringBuilder();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = nameValuePairs[i];
			String value = nameValuePairs[i + 1];
			headerValue.append(name + "=" + value + ";");
		}

		// Add the cookie
		request.header(HttpCookie.COOKIE, headerValue.toString());
	}

	/**
	 * Normalises the cookie values into a single {@link HttpHeader}.
	 * 
	 * @param setup
	 *            Setup {@link MockHttpRequestBuilder} containing the
	 *            {@link HttpCookie} instances.
	 * @return {@link MockHttpRequestBuilder} with {@link HttpCookie} instances
	 *         normalised into the single {@link HttpHeader}.
	 */
	private static MockHttpRequestBuilder normaliseCookies(MockHttpRequestBuilder setup) {
		HttpRequestHeaders headers = setup.build().getHeaders();
		StringBuilder value = new StringBuilder();
		boolean isFirst = true;
		for (HttpHeader header : headers.getHeaders(HttpCookie.COOKIE)) {
			if (!isFirst) {
				value.append(",");
			}
			isFirst = false;
			value.append(header.getValue());
		}
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		request.header(HttpCookie.COOKIE, value.toString());
		return request;
	}

	/**
	 * Obtains the expire text for the expire time.
	 *
	 * @param expireTime
	 *            Expire time.
	 * @return Expire text.
	 */
	private String getExpireText(long expireTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(new Date(expireTime));
	}

}