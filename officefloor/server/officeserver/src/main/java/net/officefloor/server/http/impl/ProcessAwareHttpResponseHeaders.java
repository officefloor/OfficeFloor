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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link Serializable} {@link HttpResponseHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseHeaders implements HttpResponseHeaders {

	/**
	 * {@link WritableHttpHeader} instances.
	 */
	private final List<WritableHttpHeader> headers = new ArrayList<>(16);

	/**
	 * Obtains the {@link WritableHttpHeader} instances for the
	 * {@link HttpResponseWriter}.
	 * 
	 * @return {@link WritableHttpHeader} instances for the
	 *         {@link HttpResponseWriter}.
	 */
	public List<WritableHttpHeader> getWritableHttpHeaders() {
		return this.headers;
	}

	/*
	 * ====================== HttpResponseHeaders ========================
	 */

	@Override
	public Iterator<HttpHeader> iterator() {
		return new Iterator<HttpHeader>() {

			List<? extends HttpHeader> headers = ProcessAwareHttpResponseHeaders.this.headers;

			int position = 0;

			@Override
			public boolean hasNext() {
				return this.position < this.headers.size();
			}

			@Override
			public HttpHeader next() {

				// Ensure another header
				if (this.position >= this.headers.size()) {
					throw new NoSuchElementException();
				}

				// Return the next header
				HttpHeader header = this.headers.get(this.position);
				this.position++; // increment for next
				return header;
			}
		};
	}

	@Override
	public HttpHeader addHeader(String name, String value) throws IllegalArgumentException {
		WritableHttpHeader header = new WritableHttpHeaderImpl(name, value);
		this.headers.add(header);
		return header;
	}

	@Override
	public void removeHeader(HttpHeader header) {
		this.removeHeader(header);
	}

	@Override
	public void removeHeaders(String name) {
		Iterator<WritableHttpHeader> iterator = this.headers.iterator();
		while (iterator.hasNext()) {
			if (name.equalsIgnoreCase(iterator.next().getName())) {
				iterator.remove();
			}
		}
	}

	@Override
	public HttpHeader getHeader(String name) {
		for (HttpHeader header : this.headers) {
			if (name.equalsIgnoreCase(header.getName())) {
				return header;
			}
		}
		return null; // not found
	}

	@Override
	public Iterable<HttpHeader> getHeaders(String name) {
		return () -> new Iterator<HttpHeader>() {

			List<? extends HttpHeader> headers = ProcessAwareHttpResponseHeaders.this.headers;

			int position = 0;

			@Override
			public boolean hasNext() {

				// Determine if further values
				for (int i = this.position; i < this.headers.size(); i++) {
					if (name.equalsIgnoreCase(this.headers.get(i).getName())) {
						return true;
					}
				}
				return false; // no further headers by name
			}

			@Override
			public HttpHeader next() {

				// Move to next position
				for (; this.position < this.headers.size(); this.position++) {
					HttpHeader header = this.headers.get(this.position);
					if (name.equalsIgnoreCase(header.getName())) {
						return header; // found next header
					}
				}
				throw new NoSuchElementException();
			}
		};
	}

	@Override
	public HttpHeader headerAt(int index) {
		return this.headers.get(index);
	}

	@Override
	public int length() {
		return this.headers.size();
	}

	/**
	 * {@link WritableHttpHeader} implementation.
	 */
	private static class WritableHttpHeaderImpl implements WritableHttpHeader {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * HTTP encoded bytes for the name.
		 */
		private final byte[] nameBytes;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param value
		 *            {@link HttpHeader} value.
		 */
		public WritableHttpHeaderImpl(String name, String value) {
			this.name = name;
			this.value = value;

			// Obtain the writable bytes
			this.nameBytes = this.name.getBytes(ServerHttpConnection.HTTP_CHARSET);
		}

		/*
		 * ================= WritableHttpHeader ==================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public byte[] getNameBytes() {
			return this.nameBytes;
		}
	}

}
