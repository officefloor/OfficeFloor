package net.officefloor.server.http.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpRequestCookies;

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

/**
 * Tests the {@link Serializable} {@link HttpRequestCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequestCookiesTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpRequestCookie} {@link List}.
	 */
	private List<HttpRequestCookie> list = new ArrayList<>();

	/**
	 * {@link SerialisableHttpRequestCookies}.
	 */
	private SerialisableHttpRequestCookies cookies;

	@Override
	protected void setUp() throws Exception {

		// Load the HTTP Cookies
		this.list.add(new MockHttpRequestCookie("one", "1"));
		this.list.add(new MockHttpRequestCookie("two", "2"));
		this.list.add(new MockHttpRequestCookie("three", "3"));
		this.list.add(new MockHttpRequestCookie("four", "4"));

		// Create the Cookies for testing
		this.cookies = new SerialisableHttpRequestCookies(this.list);
	}

	/**
	 * No cookies.
	 */
	public void testNoCookies() {
		HttpRequestCookies cookies = new SerialisableHttpRequestCookies(new ArrayList<>());

		// Ensure no Cookies
		assertEquals("Should indicate no cookies", 0, cookies.length());
		assertFalse("Should be no cookies", cookies.iterator().hasNext());
		assertNull("Should not obtain a cookie", cookies.getCookie("name"));
	}

	/**
	 * Ensure correct number of {@link HttpRequestCookie} instances.
	 */
	public void testLength() {
		assertEquals("Incorrect number of cookies", this.list.size(), this.cookies.length());
	}

	/**
	 * Ensure correct Cookie at index.
	 */
	public void testCookieAt() {
		for (int i = 0; i < this.list.size(); i++) {
			assertHttpRequestCookie(this.list.get(i), this.cookies.cookieAt(i));
		}
	}

	/**
	 * Ensure can iterate over all the {@link HttpHeader} instances.
	 */
	public void testAllCookies() {
		int i = 0;
		for (HttpRequestCookie cookie : this.cookies) {
			assertHttpRequestCookie(this.list.get(i++), cookie);
		}
		assertEquals("Incorrect number of headers", this.list.size(), i);
	}

	/**
	 * Ensure get correct {@link HttpRequestCookie}.
	 */
	public void testGetCookie() {
		for (int i = 0; i < this.list.size(); i++) {
			HttpRequestCookie expected = this.list.get(i);
			assertHttpRequestCookie(expected, this.cookies.getCookie(expected.getName()));
		}
	}

	/**
	 * Ensure can take a copy for {@link Serializable}.
	 */
	public void testCopy() {
		SerialisableHttpRequestCookies copy = new SerialisableHttpRequestCookies(this.cookies);
		int i = 0;
		for (HttpRequestCookie cookie : copy) {
			assertHttpRequestCookie(this.list.get(i), cookie);
			assertNotSame("Should be copy of HTTP Cookie", this.cookies.cookieAt(i), cookie);
			i++;
		}
		assertEquals("Incorrect number of Cookies", this.list.size(), i);
	}

	/**
	 * Ensure can serialise.
	 */
	public void testSerialize() throws Exception {

		// Serialise the HTTP Cookies
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
		outputStream.writeObject(this.cookies);
		outputStream.flush();

		// Materialise the HTTP Cookies
		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		Object object = inputStream.readObject();
		assertTrue("Incorrect object", object instanceof SerialisableHttpRequestCookies);
		SerialisableHttpRequestCookies materialise = (SerialisableHttpRequestCookies) object;

		// Ensure have all the Cookies
		int i = 0;
		for (HttpRequestCookie cookie : materialise) {
			assertHttpRequestCookie(this.list.get(i++), cookie);
		}
	}

	/**
	 * Asserts the {@link HttpRequestCookie}.
	 * 
	 * @param expected
	 *            Expected {@link HttpRequestCookie}.
	 * @param actual
	 *            Actual {@link HttpRequestCookie}.
	 */
	private static void assertHttpRequestCookie(HttpRequestCookie expected, HttpRequestCookie actual) {
		assertEquals("Incorrect name", expected.getName(), actual.getName());
		assertEquals("Incorrect value", expected.getValue(), actual.getValue());
	}

	/**
	 * Mock {@link HttpRequestCookie}.
	 */
	private static class MockHttpRequestCookie implements HttpRequestCookie {

		private final String name;

		private final String value;

		private MockHttpRequestCookie(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

}