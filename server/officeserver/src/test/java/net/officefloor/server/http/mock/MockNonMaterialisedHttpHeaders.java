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

package net.officefloor.server.http.mock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;

/**
 * Mock {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers = new LinkedList<>();

	/**
	 * Adds a {@link NonMaterialisedHttpHeader}.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addHttpHeader(String name, String value) {
		this.headers.add(new MockNonMaterialisedHttpHeader(name, value));
	}

	/*
	 * =================== NonMaterialisedHttpHeaders ===============
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
	 * Mock {@link NonMaterialisedHttpHeader}.
	 */
	private static class MockNonMaterialisedHttpHeader implements NonMaterialisedHttpHeader, HttpHeader {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public MockNonMaterialisedHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ======= NonMaterialisedHttpHeader / HttpHeader ============
		 */

		@Override
		public HttpHeader materialiseHttpHeader() {
			return this;
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
