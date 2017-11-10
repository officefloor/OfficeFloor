/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockProcessAwareContext;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link HttpResponseCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseCookiesTest extends OfficeFrameTestCase {

	/**
	 * {@link ProcessAwareHttpResponseCookies} to be tested.
	 */
	private final ProcessAwareHttpResponseCookies cookies = new ProcessAwareHttpResponseCookies(
			new MockProcessAwareContext());

	/**
	 * Expire time.
	 */
	private static final String EXPIRE = "Wed, 9 Jun 2021 10:18:14 GMT";

	/**
	 * Expire {@link TemporalAccessor}.
	 */
	private static final TemporalAccessor TEMPORAL_EXPIRE = DateTimeFormatter.RFC_1123_DATE_TIME.parse(EXPIRE);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Add the cookies
		this.cookies.addCookie("name", "1");
		this.addCookie("value", "0", (cookie) -> cookie.setValue("2"));
		this.addCookie("expires", "3", (cookie) -> cookie.setExpires(TEMPORAL_EXPIRE));
		this.addCookie("max-age", "4", (cookie) -> cookie.setMaxAge(1000));
		this.addCookie("domain", "5", (cookie) -> cookie.setDomain("officefloor.net"));
		this.addCookie("path", "6", (cookie) -> cookie.setPath("/path"));
		this.addCookie("secure", "7", (cookie) -> cookie.setSecure(true));
		this.addCookie("http", "8", (cookie) -> cookie.setHttpOnly(true));
		this.addCookie("extension", "9", (cookie) -> cookie.addExtension("extend"));
		this.cookies.addCookie(new HttpRequestCookie() {
			@Override
			public String getName() {
				return "extensions";
			}

			@Override
			public String getValue() {
				return "10";
			}
		}, (init) -> init.addExtension("one").addExtension("two=2").addExtension("three"));
		this.addCookie("all", "0",
				(cookie) -> cookie.setValue("11").setExpires(TEMPORAL_EXPIRE).setMaxAge(10)
						.setDomain("mock.officefloor.net").setPath("/all").setSecure(true).setHttpOnly(true)
						.addExtension("extension"));
	}

	/**
	 * Adds a {@link HttpResponseCookie}.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param attributeConfigurer
	 *            Attribute configurer.
	 */
	private void addCookie(String name, String value,
			Function<HttpResponseCookie, HttpResponseCookie> attributeConfigurer) {
		HttpResponseCookie original = this.cookies.addCookie(name, value);
		HttpResponseCookie updated = attributeConfigurer.apply(original);
		assertSame("Incorrect returned cookie", original, updated);
	}

	/**
	 * Ensure can add {@link HttpRequestCookie} instances and iterate over them.
	 */
	public void testGetHeaders() {

		// Ensure can iterate over all cookies
		assertCookieNames(this.cookies, "name", "value", "expires", "max-age", "domain", "path", "secure", "http",
				"extension", "extensions", "all");

		// Ensure can get by name
		assertCookie(this.cookies.getCookie("name"), "name", "1");
		assertCookie(this.cookies.getCookie("value"), "value", "2");
		assertCookie(this.cookies.getCookie("expires"), "expires", "3", expires(EXPIRE));
		assertCookie(this.cookies.getCookie("max-age"), "max-age", "4", maxAge(1000));
		assertCookie(this.cookies.getCookie("domain"), "domain", "5", domain("officefloor.net"));
		assertCookie(this.cookies.getCookie("path"), "path", "6", path("/path"));
		assertCookie(this.cookies.getCookie("secure"), "secure", "7", secure(true));
		assertCookie(this.cookies.getCookie("http"), "http", "8", httpOnly(true));
		assertCookie(this.cookies.getCookie("extension"), "extension", "9", extension("extend"));
		assertCookie(this.cookies.getCookie("extensions"), "extensions", "10", extension("one"), extension("two=2"),
				extension("three"));
		assertCookie(this.cookies.getCookie("all"), "all", "11", expires(EXPIRE), maxAge(10),
				domain("mock.officefloor.net"), path("/all"), secure(true), httpOnly(true), extension("extension"));
	}

	/**
	 * Ensure can remove {@link HttpResponseCookie}.
	 */
	public void testRemoveCookie() {
		HttpResponseCookie cookie = this.cookies.getCookie("value");

		// Remove the cookie
		assertTrue("Cookie should be removed", this.cookies.removeCookie(cookie));
		assertCookieNames(this.cookies, "name", "expires", "max-age", "domain", "path", "secure", "http", "extension",
				"extensions", "all");

		// Removing the same cookie should have not effect
		assertFalse("Cookie already removed", this.cookies.removeCookie(cookie));
		assertCookieNames(this.cookies, "name", "expires", "max-age", "domain", "path", "secure", "http", "extension",
				"extensions", "all");
	}

	/**
	 * Ensure can remove {@link HttpResponseCookie} instances by
	 * {@link Iterator}.
	 */
	public void testRemoveCookiesByIterator() {

		// Remove via all cookies iterator
		Iterator<HttpResponseCookie> iterator = this.cookies.iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertCookieNames(this.cookies, "value", "expires", "max-age", "domain", "path", "secure", "http", "extension",
				"extensions", "all");
		HttpResponseCookie cookie = iterator.next();
		assertEquals("Incorrect next cookie after removing", "value", cookie.getName());
	}

	/**
	 * Ensure correct writing of {@link WritableHttpHeader}.
	 */
	public void testWrittenHeaderBytes() throws IOException {

		// Obtain writer
		MockStreamBufferPool bufferPool = new MockStreamBufferPool();
		StreamBuffer<ByteBuffer> buffer = bufferPool.getPooledStreamBuffer();

		// Write the headers
		WritableHttpCookie header = this.cookies.getWritableHttpCookie();
		while (header != null) {
			header.write(buffer, bufferPool);
			header = header.next;
		}

		// Obtain the content
		MockStreamBufferPool.releaseStreamBuffers(buffer);
		String content = MockStreamBufferPool.getContent(buffer, ServerHttpConnection.HTTP_CHARSET);

		// Ensure correct content
		StringBuilder expected = new StringBuilder();
		expected.append("set-cookie: name=1\r\n");
		expected.append("set-cookie: value=2\r\n");
		expected.append("set-cookie: expires=3; Expires=" + EXPIRE + "\r\n");
		expected.append("set-cookie: max-age=4; Max-Age=1000\r\n");
		expected.append("set-cookie: domain=5; Domain=officefloor.net\r\n");
		expected.append("set-cookie: path=6; Path=/path\r\n");
		expected.append("set-cookie: secure=7; Secure\r\n");
		expected.append("set-cookie: http=8; HttpOnly\r\n");
		expected.append("set-cookie: extension=9; extend\r\n");
		expected.append("set-cookie: extensions=10; one; two=2; three\r\n");
		expected.append("set-cookie: all=11; Expires=" + EXPIRE
				+ "; Max-Age=10; Domain=mock.officefloor.net; Path=/all; Secure; HttpOnly; extension\r\n");
		assertEquals("Incorrect HTTP headers content", expected.toString(), content);
	}

	private static Attribute expires(String expires) {
		return new Attribute(AttributeType.EXPIRES, DateTimeFormatter.RFC_1123_DATE_TIME.parse(expires));
	}

	private static Attribute maxAge(long maxAge) {
		return new Attribute(AttributeType.MAX_AGE, maxAge);
	}

	private static Attribute domain(String domain) {
		return new Attribute(AttributeType.DOMAIN, domain);
	}

	private static Attribute path(String path) {
		return new Attribute(AttributeType.PATH, path);
	}

	private static Attribute secure(boolean isSecure) {
		return new Attribute(AttributeType.SECURE, isSecure);
	}

	private static Attribute httpOnly(boolean isHttpOnly) {
		return new Attribute(AttributeType.HTTP_ONLY, isHttpOnly);
	}

	private static Attribute extension(String extension) {
		return new Attribute(AttributeType.EXTENSION, extension);
	}

	/**
	 * Asserts the {@link HttpResponseCookie}.
	 * 
	 * @param cookie
	 *            {@link HttpResponseCookie}.
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param attributes
	 *            Expected {@link Attribute} values. Not included
	 *            {@link Attribute} instances are considered to have default
	 *            values.
	 */
	private static void assertCookie(HttpResponseCookie cookie, String name, String value, Attribute... attributes) {

		// Capture the expected response header value
		StringBuilder header = new StringBuilder();
		header.append(name + "=" + value);

		// Load the attributes
		TemporalAccessor expires = null;
		long maxAge = HttpResponseCookie.BROWSER_SESSION_MAX_AGE;
		String domain = null;
		String path = null;
		boolean isSecure = false;
		boolean isHttpOnly = false;
		List<String> extensions = new ArrayList<>();
		for (Attribute attribute : attributes) {
			switch (attribute.type) {
			case EXPIRES:
				expires = (TemporalAccessor) attribute.value;
				header.append(
						"; Expires=" + DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).format(expires));
				break;
			case MAX_AGE:
				maxAge = (Long) attribute.value;
				header.append("; Max-Age=" + maxAge);
				break;
			case DOMAIN:
				domain = (String) attribute.value;
				header.append("; Domain=" + domain);
				break;
			case PATH:
				path = (String) attribute.value;
				header.append("; Path=" + path);
				break;
			case SECURE:
				isSecure = (Boolean) attribute.value;
				if (isSecure) {
					header.append("; Secure");
				}
				break;
			case HTTP_ONLY:
				isHttpOnly = (Boolean) attribute.value;
				if (isHttpOnly) {
					header.append("; HttpOnly");
				}
				break;
			case EXTENSION:
				extensions.add((String) attribute.value);
				header.append("; " + attribute.value);
				break;
			}
		}

		// Assert the attributes
		assertCookie(cookie, name, value, expires, maxAge, domain, path, isSecure, isHttpOnly,
				extensions.toArray(new String[0]));

		// Ensure appropriate to response value
		assertEquals("Cookie should be writable", WritableHttpCookie.class, cookie.getClass());
		assertEquals("Incorrect response value", header.toString(),
				((WritableHttpCookie) cookie).toResponseHeaderValue());
	}

	private static enum AttributeType {
		EXPIRES, MAX_AGE, DOMAIN, PATH, SECURE, HTTP_ONLY, EXTENSION
	}

	private static class Attribute {

		private final AttributeType type;

		private final Object value;

		private Attribute(AttributeType type, Object value) {
			this.type = type;
			this.value = value;
		}
	}

	/**
	 * Asserts the {@link HttpResponseCookie}.
	 * 
	 * @param cookie
	 *            {@link HttpResponseCookie}.
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 * @param expires
	 *            Expires.
	 * @param maxAge
	 *            Max-age.
	 * @param domain
	 *            Domain.
	 * @param path
	 *            Path.
	 * @param isSecure
	 *            Indicates if secure.
	 * @param isHttpOnly
	 *            Indicates if HTTP only.
	 * @param extensions
	 *            Expected extensions.
	 */
	private static void assertCookie(HttpResponseCookie cookie, String name, String value, TemporalAccessor expires,
			long maxAge, String domain, String path, boolean isSecure, boolean isHttpOnly, String... extensions) {
		assertEquals("Incorrect name", name, cookie.getName());
		assertEquals("Incorrect value", value, cookie.getValue());
		if (expires == null) {
			assertEquals("Incorrect expires", expires, cookie.getExpires());
		} else {
			assertEquals("Incorect expires", expires.toString(), cookie.getExpires().toString());
		}
		assertEquals("Incorrect max-age", maxAge, cookie.getMaxAge());
		assertEquals("Incorrect domain", domain, cookie.getDomain());
		assertEquals("Incorrect path", path, cookie.getPath());
		assertEquals("Incorrect secure", isSecure, cookie.isSecure());
		assertEquals("Incorrect HTTP only", isHttpOnly, cookie.isHttpOnly());
		String[] actualExtensions = cookie.getExtensions();
		assertEquals("Incorrect number of extensions", extensions.length, actualExtensions.length);
		for (int i = 0; i < extensions.length; i++) {
			assertEquals("Incorrect extension " + i, extensions[i], actualExtensions[i]);
		}
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param cookies
	 *            {@link Iterator} over the {@link HttpResponseCookie}
	 *            instances.
	 * @param expectedHeaderNames
	 *            Expected {@link HttpResponseCookie} names in order as per
	 *            {@link Iterator}.
	 */
	private static void assertCookieNames(Iterable<? extends HttpResponseCookie> cookies,
			String... expectedCookieNames) {
		assertCookieNames(cookies.iterator(), expectedCookieNames);
	}

	/**
	 * Asserts the Cookie instances.
	 * 
	 * @param cookies
	 *            {@link Iterator} over the {@link HttpResponseCookie}
	 *            instances.
	 * @param expectedCookieNames
	 *            Expected {@link HttpResponseCookie} names in order as per
	 *            {@link Iterator}.
	 */
	private static void assertCookieNames(Iterator<? extends HttpResponseCookie> cookies,
			String... expectedCookieNames) {
		for (int i = 0; i < expectedCookieNames.length; i++) {
			assertTrue("Should have HTTP Cookie " + i, cookies.hasNext());
			HttpResponseCookie cookie = cookies.next();
			assertEquals("Incorrect HTTP Cookie " + i, expectedCookieNames[i], cookie.getName());
		}
		assertFalse("Should be no further Cookies", cookies.hasNext());
	}

}