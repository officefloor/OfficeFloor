/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.http.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.officefloor.server.buffer.StreamBufferByteSequence;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;

/**
 * {@link NonMaterialisedHttpHeaders} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class NonMaterialisedHeadersImpl implements NonMaterialisedHttpHeaders {

	/**
	 * Listing of {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers;

	/**
	 * Instantiate.
	 * 
	 * @param initialCapacity
	 *            Initial capacity of {@link NonMaterialisedHttpHeader}
	 *            instances.
	 */
	public NonMaterialisedHeadersImpl(int initialCapacity) {
		this.headers = new ArrayList<>(initialCapacity);
	}

	/**
	 * Adds a {@link NonMaterialisedHttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 */
	public void addHttpHeader(StreamBufferByteSequence name, StreamBufferByteSequence value) {
		this.headers.add(new NonMaterialisedHeaderImpl(name, value));
	}

	/*
	 * =================== NonMaterialisedHttpHeaders ====================
	 */

	@Override
	public Iterator<NonMaterialisedHttpHeader> iterator() {
		return this.headers.iterator();
	}

	@Override
	public int length() {
		return this.headers.size();
	}

	/**
	 * {@link NonMaterialisedHttpHeader} implementation.
	 */
	private static class NonMaterialisedHeaderImpl implements NonMaterialisedHttpHeader {

		/**
		 * Name.
		 */
		private final StreamBufferByteSequence name;

		/**
		 * Value.
		 */
		private final StreamBufferByteSequence value;

		/**
		 * Flags whether the header name/value have been trimmed.
		 */
		private boolean isHeaderTrimmed = false;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param value
		 *            {@link HttpHeader} value.
		 */
		public NonMaterialisedHeaderImpl(StreamBufferByteSequence name, StreamBufferByteSequence value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ================== NonMaterialisedHttpHeader ==================
		 */

		@Override
		public CharSequence getName() {
			return this.name.getHttpCharSequence();
		}

		@Override
		public HttpHeader materialiseHttpHeader() {

			// Ensure trim the header
			if (!this.isHeaderTrimmed) {
				this.value.trim();
			}

			// Return the HTTP header
			String name = this.name.toHttpString();
			String value = this.value.toHttpString();
			return new HttpHeaderImpl(name, value);
		}
	}

	/**
	 * {@link HttpHeader} implementation.
	 */
	private static class HttpHeaderImpl implements HttpHeader {

		/**
		 * {@link HttpHeader} name.
		 */
		private final String name;

		/**
		 * {@link HttpHeader} value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param value
		 *            {@link HttpHeader} value.
		 */
		public HttpHeaderImpl(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ======================= HttpHeader ==============================
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
