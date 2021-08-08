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
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Materialising {@link HttpRequestHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequestHeaders implements HttpRequestHeaders {

	/**
	 * HTTP character value for A.
	 */
	private static final char A = (char) "A".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP character value for Z.
	 */
	private static final char Z = (char) "Z".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP character value for a.
	 */
	private static final char a = (char) "a".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * Difference in character value to drop the HTTP character to lower case.
	 */
	private static final char DROP_CASE_DIFFERENCE = (char) (a - A);

	/**
	 * Drops to lower case to ignore case.
	 * 
	 * @param httpCharacter
	 *            HTTP character.
	 * @return Lower case character.
	 */
	private static char httpLowerCase(char httpCharacter) {
		// Check Z first as less range and more likely to short-cut
		if ((httpCharacter <= Z) && (httpCharacter >= A)) {
			// Capital letter, so drop to lower case
			return (char) (httpCharacter + DROP_CASE_DIFFERENCE);
		} else {
			// Use character (either already lower case or another character)
			return httpCharacter;
		}
	}

	/**
	 * Determines if the two {@link CharSequence} instances are equal ignoring
	 * case.
	 * 
	 * @param one
	 *            {@link CharSequence} one.
	 * @param two
	 *            {@link CharSequence} two.
	 * @return <code>true</code> if the {@link CharSequence} instances are equal
	 *         ignoring case.
	 */
	public static boolean httpEqualsIgnoreCase(CharSequence one, CharSequence two) {
		if (one.length() == two.length()) {
			for (int i = 0; i < one.length(); i++) {
				char charOne = httpLowerCase(one.charAt(i));
				char charTwo = httpLowerCase(two.charAt(i));
				if (charOne != charTwo) {
					return false; // not equal due to difference
				}
			}
			return true; // as here, equal (length and characters)
		}
		return false; // not equal on length
	}

	/**
	 * {@link NonMaterialisedHttpHeaders}.
	 */
	private final NonMaterialisedHttpHeaders nonMaterialised;

	/**
	 * Materialised {@link HttpHeader} instances. This is {@link Thread} safe as
	 * will just rebuild if <code>null</code> (or element is <code>null</code>).
	 */
	private HttpHeader[] materialisedHttpHeaders = null;

	/**
	 * Instantiate.
	 * 
	 * @param nonMaterialised
	 *            {@link NonMaterialisedHttpHeaders}.
	 */
	public MaterialisingHttpRequestHeaders(NonMaterialisedHttpHeaders nonMaterialised) {
		this.nonMaterialised = nonMaterialised;
	}

	/**
	 * Obtains the materialised {@link HttpHeader}.
	 * 
	 * @param nonMaterialisedHttpHeader
	 *            {@link NonMaterialisedHttpHeader}.
	 * @param index
	 *            Index of the {@link HttpHeader}.
	 * @return {@link HttpHeader}.
	 */
	private HttpHeader getMaterialisedHttpHeader(NonMaterialisedHttpHeader nonMaterialisedHttpHeader, int index) {

		// Obtain the existing HTTP header
		HttpHeader header = this.getMaterialisedHttpHeader(index);
		if (header != null) {
			return header;
		}

		// Not materialised, so materialise
		return this.materialiseHttpHeader(nonMaterialisedHttpHeader, index);
	}

	/**
	 * Obtains the {@link HttpHeader}.
	 * 
	 * @param index
	 *            Index of the {@link HttpHeader}
	 * @return {@link HttpHeader} or <code>null</code> if not yet materialised.
	 */
	private HttpHeader getMaterialisedHttpHeader(int index) {

		// Ensure have materialised headers
		if (this.materialisedHttpHeaders == null) {
			this.materialisedHttpHeaders = new HttpHeader[this.nonMaterialised.length()];
		}

		// Return the possible materialised header
		return this.materialisedHttpHeaders[index];
	}

	/**
	 * Materialises the {@link HttpHeader}.
	 * 
	 * @param nonMaterialisedHttpHeader
	 *            {@link NonMaterialisedHttpHeader}.
	 * @return Index of {@link HttpHeader} in listing of {@link HttpHeader}
	 *         instances.
	 */
	private HttpHeader materialiseHttpHeader(NonMaterialisedHttpHeader nonMaterialisedHttpHeader, int index) {

		// Materialise the HTTP header
		HttpHeader materialisedHttpHeader = nonMaterialisedHttpHeader.materialiseHttpHeader();

		// Register to re-use
		this.materialisedHttpHeaders[index] = materialisedHttpHeader;

		// Return the HTTP header
		return materialisedHttpHeader;
	}

	/*
	 * =================== HttpRequestHeaders ========================
	 */

	@Override
	public Iterator<HttpHeader> iterator() {
		return new Iterator<HttpHeader>() {

			private Iterator<NonMaterialisedHttpHeader> iterator = MaterialisingHttpRequestHeaders.this.nonMaterialised
					.iterator();

			private int index = -1; // start before first header

			@Override
			public boolean hasNext() {
				return this.iterator.hasNext();
			}

			@Override
			public HttpHeader next() {

				// Iterate to next header
				NonMaterialisedHttpHeader nonMaterialised = this.iterator.next();
				this.index++;

				// Obtain the materialised HTTP header
				return MaterialisingHttpRequestHeaders.this.getMaterialisedHttpHeader(nonMaterialised, index);
			}
		};
	}

	@Override
	public HttpHeader getHeader(CharSequence name) {
		Iterator<NonMaterialisedHttpHeader> iterator = this.nonMaterialised.iterator();
		int index = -1;
		while (iterator.hasNext()) {
			NonMaterialisedHttpHeader nonMaterialisedHttpHeader = iterator.next();
			index++;

			// Determine if header
			if (httpEqualsIgnoreCase(nonMaterialisedHttpHeader.getName(), name)) {
				// Return the materialised header
				return this.getMaterialisedHttpHeader(nonMaterialisedHttpHeader, index);
			}
		}

		// As here, no header by the name
		return null;
	}

	@Override
	public Iterable<HttpHeader> getHeaders(CharSequence name) {
		return new Iterable<HttpHeader>() {
			@Override
			public Iterator<HttpHeader> iterator() {
				return new Iterator<HttpHeader>() {

					private Iterator<NonMaterialisedHttpHeader> iterator = MaterialisingHttpRequestHeaders.this.nonMaterialised
							.iterator();

					private int index = -1; // start before first header

					@Override
					public boolean hasNext() {

						// Must search to see if have header
						Iterator<NonMaterialisedHttpHeader> search = MaterialisingHttpRequestHeaders.this.nonMaterialised
								.iterator();

						// Skip to current location
						for (int i = 0; i <= this.index; i++) {
							search.next();
						}

						// Determine if another header
						while (search.hasNext()) {
							NonMaterialisedHttpHeader nonMaterialisedHeader = search.next();
							if (httpEqualsIgnoreCase(nonMaterialisedHeader.getName(), name)) {
								return true; // another HTTP header by name
							}
						}

						// As here, no further HTTP headers by name
						return false;
					}

					@Override
					public HttpHeader next() {

						// Move to next HTTP header by the name
						while (this.iterator.hasNext()) {

							// Obtain the next non materialised header
							NonMaterialisedHttpHeader nonMaterialisedHeader = this.iterator.next();
							this.index++;

							// Determine if header of interest
							if (httpEqualsIgnoreCase(nonMaterialisedHeader.getName(), name)) {
								return MaterialisingHttpRequestHeaders.this
										.getMaterialisedHttpHeader(nonMaterialisedHeader, this.index);
							}
						}

						// As here, no header for next
						throw new NoSuchElementException();
					}
				};
			}
		};
	}

	@Override
	public HttpHeader headerAt(int index) {

		// Attempt to obtain the existing materialise header
		HttpHeader header = this.getMaterialisedHttpHeader(index);
		if (header != null) {
			return header;
		}

		// Find the non materialised HTTP header
		Iterator<NonMaterialisedHttpHeader> iterator = this.nonMaterialised.iterator();
		for (int i = 0; i < index; i++) {
			iterator.next();
		}
		NonMaterialisedHttpHeader nonMaterialisedHttpHeader = iterator.next();

		// Return the materialised HTTP header
		return this.materialiseHttpHeader(nonMaterialisedHttpHeader, index);
	}

	@Override
	public int length() {
		return this.nonMaterialised.length();
	}

}
