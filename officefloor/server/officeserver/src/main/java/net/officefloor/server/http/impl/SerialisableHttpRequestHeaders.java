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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequestHeaders;

/**
 * {@link Serializable} {@link HttpRequestHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequestHeaders implements HttpRequestHeaders, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link SerialisableHttpHeader} instances.
	 */
	private final HttpHeader[] headers;

	/**
	 * Instantiate from {@link HttpRequestHeaders}.
	 * 
	 * @param httpRequestHeaders
	 *            {@link HttpRequestHeaders}.
	 */
	public SerialisableHttpRequestHeaders(HttpRequestHeaders httpRequestHeaders) {
		this(httpRequestHeaders.length(), httpRequestHeaders);
	}

	/**
	 * Instantiate from {@link List} of {@link HttpHeader} instances.
	 * 
	 * @param headers
	 *            {@link List} of {@link HttpHeader} instances.
	 */
	public SerialisableHttpRequestHeaders(List<HttpHeader> headers) {
		this(headers.size(), headers);
	}

	/**
	 * Loads the {@link HttpHeader} instances.
	 * 
	 * @param length
	 *            Number of {@link HttpHeader} instances.
	 * @param headers
	 *            {@link Iterable} over the {@link HttpHeader} instances.
	 */
	protected SerialisableHttpRequestHeaders(int length, Iterable<HttpHeader> headers) {
		this.headers = new SerialisableHttpHeader[length];
		int index = 0;
		for (HttpHeader header : headers) {
			this.headers[index++] = new SerialisableHttpHeader(header.getName(), header.getValue());
		}
	}

	/*
	 * ================= HttpRequestHeaders ======================
	 */

	@Override
	public Iterator<HttpHeader> iterator() {
		return Arrays.asList(this.headers).iterator();
	}

	@Override
	public HttpHeader getHeader(CharSequence name) {
		final String nameString = name.toString();

		// Search for header by name
		for (int i = 0; i < this.headers.length; i++) {
			HttpHeader header = this.headers[i];
			if (header.getName().equalsIgnoreCase(nameString)) {
				return header;
			}
		}

		// As here, no header by the name
		return null;
	}

	@Override
	public Iterable<HttpHeader> getHeaders(CharSequence name) {
		final String nameString = name.toString();
		return new Iterable<HttpHeader>() {
			@Override
			public Iterator<HttpHeader> iterator() {
				return new Iterator<HttpHeader>() {

					private int index = -1; // index of next

					@Override
					public boolean hasNext() {

						// Easy access to headers
						HttpHeader[] headers = SerialisableHttpRequestHeaders.this.headers;

						// Determine if match on name for next
						for (int i = (this.index + 1); i < headers.length; i++) {
							HttpHeader header = headers[i];
							if (header.getName().equalsIgnoreCase(nameString)) {
								return true; // another header by name
							}
						}

						// As here, no further header by name
						return false;
					}

					@Override
					public HttpHeader next() {

						// Easy access to headers
						HttpHeader[] headers = SerialisableHttpRequestHeaders.this.headers;

						// Obtain next header by name
						this.index++;
						while (this.index < headers.length) {
							HttpHeader header = headers[this.index];
							
							// Determine if the header
							if (header.getName().equalsIgnoreCase(nameString)) {
								return header; // next matching header
							}
							
							// Try next header
							this.index++;
						}

						// As here, no next header
						throw new NoSuchElementException();
					}
				};
			}
		};
	}

	@Override
	public HttpHeader headerAt(int index) {
		return this.headers[index];
	}

	@Override
	public int length() {
		return this.headers.length;
	}

}
