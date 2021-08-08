/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;

/**
 * Materialising {@link HttpRequestCookies}.
 *
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequestCookies implements HttpRequestCookies {

	/**
	 * {@link HttpRequestHeaders}.
	 */
	private final HttpRequestHeaders headers;

	/**
	 * Head {@link HttpRequestCookie} in linked list of
	 * {@link HttpRequestCookie} instances. May be <code>null</code> to indicate
	 * no {@link HttpRequestCookie} instances.
	 */
	private Head head = null;

	/**
	 * Instantiate.
	 * 
	 * @param headers
	 *            {@link HttpRequestHeaders}.
	 */
	public MaterialisingHttpRequestCookies(HttpRequestHeaders headers) {
		this.headers = headers;
	}

	/**
	 * State of parsing the {@link HttpRequestCookie} instances.
	 */
	private static enum ParseState {
		NEW, NAME, START_VALUE, VALUE
	}

	/**
	 * Obtains the head {@link HttpRequestCookie} of the linked list of
	 * {@link HttpRequestCookie} instances.
	 * 
	 * @return {@link Head}.
	 */
	private Head getHead() {

		// Determine if already loaded cookies
		if (this.head != null) {
			return this.head; // cached
		}

		// Obtain the cookie value
		HttpHeader header = this.headers.getHeader("cookie");
		if (header == null) {
			// No cookies
			this.head = new Head(null, 0);
			return this.head;
		}

		// Load in the cookies
		String content = header.getValue();
		HttpRequestCookieImpl head = null;
		ParseState state = ParseState.NEW;
		int nameStart = -1;
		int nameEnd = -1;
		int valueStart = -1;
		int cookieCount = 0;
		NEXT_CHARACTER: for (int i = 0; i < content.length(); i++) {
			char character = content.charAt(i);

			switch (state) {
			case NEW:
				// Ignore leading spaces (or invalid ;)
				if ((character == ' ') || (character == ';')) {
					continue NEXT_CHARACTER;
				}

				// Starting name
				nameStart = i;
				nameEnd = -1;
				valueStart = -1;
				state = ParseState.NAME;

			case NAME:
				switch (character) {
				case '=':
					// End of name
					nameEnd = i;
					state = ParseState.START_VALUE;
					continue NEXT_CHARACTER;

				case ';':
					// Invalid cookie, so ignore
					state = ParseState.NEW;
					continue NEXT_CHARACTER;
				}
				break;

			case START_VALUE:
				switch (character) {
				case ' ':
					// Ignore leading zero
					continue NEXT_CHARACTER;

				case ';':
					// Invalid cookie (no value), so ignore
					state = ParseState.NEW;
					continue NEXT_CHARACTER;
				}

				// Starting value
				valueStart = i;
				state = ParseState.VALUE;

			case VALUE:
				// Parse until separating ; (or end)
				if (character != ';') {
					continue NEXT_CHARACTER;
				}

				// Found value (so add cookie)
				head = this.createCookie(content, nameStart, nameEnd, valueStart, i, head);
				cookieCount++;

				// Start new cookie
				state = ParseState.NEW;
			}
		}

		// Last value (with no terminating ;)
		if (state == ParseState.VALUE) {
			head = this.createCookie(content, nameStart, nameEnd, valueStart, content.length(), head);
			cookieCount++;
		}

		// Return the head cookie
		this.head = new Head(head, cookieCount);
		return this.head;
	}

	/**
	 * Creates the {@link HttpRequestCookieImpl}.
	 * 
	 * @param content
	 *            Cookie content.
	 * @param nameStart
	 *            Start index of name.
	 * @param nameEnd
	 *            End index of name.
	 * @param valueStart
	 *            Start index of value.
	 * @param valueEnd
	 *            End index of value.
	 * @param next
	 *            Next {@link HttpRequestCookieImpl}. May be <code>null</code>.
	 * @return Created {@link HttpRequestCookieImpl}.
	 */
	private HttpRequestCookieImpl createCookie(String content, int nameStart, int nameEnd, int valueStart, int valueEnd,
			HttpRequestCookieImpl next) {

		// Ignore end spacing of name
		while ((nameEnd > nameStart) && (content.charAt(nameEnd - 1) == ' ')) {
			nameEnd--;
		}
		if (nameEnd == nameStart) {
			return next; // no name, so no cookie
		}

		// Ignore end spacing of value
		while ((valueEnd > valueStart) && (content.charAt(valueEnd - 1) == ' ')) {
			valueEnd--;
		}

		// Determine if ignore quoted value
		if ((content.charAt(valueStart) == '"') && (content.charAt(valueEnd - 1) == '"')) {
			valueStart++;
			valueEnd--;
		}

		// Ensure value
		if (valueEnd <= nameStart) {
			return next; // no value, so no cookie
		}

		// Obtain the name and value
		String name = content.substring(nameStart, nameEnd);
		String value = content.substring(valueStart, valueEnd);

		// Create and return the cookie
		return new HttpRequestCookieImpl(name, value, next);
	}

	/*
	 * ================= HttpRequestCookies ==================
	 */

	@Override
	public Iterator<HttpRequestCookie> iterator() {
		return new Iterator<HttpRequestCookie>() {

			private HttpRequestCookieImpl head = MaterialisingHttpRequestCookies.this.getHead().head;

			@Override
			public boolean hasNext() {
				return (this.head != null);
			}

			@Override
			public HttpRequestCookie next() {
				HttpRequestCookie next = this.head;
				this.head = this.head.next;
				return next;
			}
		};
	}

	@Override
	public HttpRequestCookie getCookie(String name) {

		// Find the cookie
		HttpRequestCookieImpl cookie = this.getHead().head;
		while (cookie != null) {

			// Determine if cookie
			if (name.equalsIgnoreCase(cookie.name)) {
				return cookie;
			}

			// Next
			cookie = cookie.next;
		}

		// As here, no cookie by name
		return null;
	}

	@Override
	public HttpRequestCookie cookieAt(int index) {

		// Obtain the indexed cookie
		HttpRequestCookieImpl cookie = this.getHead().head;
		while (cookie != null) {

			// Determine if found cookie
			if (index == 0) {
				return cookie;
			}

			// Next
			index--;
			cookie = cookie.next;
		}

		// As here, no cookie by index
		throw new NoSuchElementException();
	}

	@Override
	public int length() {
		Head head = this.getHead();
		return head.length;
	}

	/**
	 * Placeholder head to indicate whether the {@link HttpRequestCookie}
	 * instances have been loaded.
	 */
	private static class Head {

		/**
		 * Head {@link HttpRequestCookieImpl}.
		 */
		private final HttpRequestCookieImpl head;

		/**
		 * Length.
		 */
		private final int length;

		/**
		 * Instantiate.
		 * 
		 * @param head
		 *            {@link HttpRequestCookieImpl}.
		 * @param length
		 *            Length.
		 */
		private Head(HttpRequestCookieImpl head, int length) {
			this.head = head;
			this.length = length;
		}
	}

	/**
	 * {@link HttpRequestCookie} implementation.
	 */
	private static class HttpRequestCookieImpl implements HttpRequestCookie {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Next {@link HttpRequestCookieImpl}.
		 */
		private final HttpRequestCookieImpl next;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 * @param next
		 *            Next {@link HttpRequestCookieImpl}. May be
		 *            <code>null</code> for first {@link HttpRequestCookie}.
		 */
		private HttpRequestCookieImpl(String name, String value, HttpRequestCookieImpl next) {
			this.name = name;
			this.value = value;
			this.next = next;
		}

		/*
		 * ================= HttpRequestCookie ===================
		 */

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
