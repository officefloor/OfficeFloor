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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;

/**
 * Tests the {@link MaterialisingHttpRequestHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequestHeadersTest extends OfficeFrameTestCase implements NonMaterialisedHttpHeaders {

	/**
	 * First {@link HttpHeader} for each name.
	 */
	private Map<String, HttpHeader> firstHttpHeader = new HashMap<>();

	/**
	 * All {@link HttpHeader} instances for each name.
	 */
	private Map<String, List<HttpHeader>> namedHttpHeaders = new HashMap<>();

	/**
	 * {@link MaterialisingHttpRequestHeaders}.
	 */
	private MaterialisingHttpRequestHeaders headers;

	@Override
	protected void setUp() throws Exception {

		// Load the HTTTP headers
		this.addHttpHeader("one", "1");
		this.addHttpHeader("same", "S1");
		this.addHttpHeader("two", "2");
		this.addHttpHeader("three", "3");
		this.addHttpHeader("same", "S2");
		this.addHttpHeader("four", "4");
		this.addHttpHeader("same", "S3");
		this.headers = new MaterialisingHttpRequestHeaders(this);

		// Load the first HTTP headers
		for (HttpHeader header : this.list) {
			String name = header.getName();
			if (!this.firstHttpHeader.containsKey(name)) {
				this.firstHttpHeader.put(name, header);
			}
		}

		// Load the named HTTP headers
		for (HttpHeader header : this.list) {
			String name = header.getName();
			List<HttpHeader> namedList = this.namedHttpHeaders.get(name);
			if (namedList == null) {
				namedList = new ArrayList<>();
				this.namedHttpHeaders.put(name, namedList);
			}
			namedList.add(header);
		}
	}

	/**
	 * Ensure correct number of {@link HttpHeader} instances.
	 */
	public void testLength() {
		assertEquals("Incorrect number of headers", this.list.size(), this.headers.length());
	}

	/**
	 * Ensure correct header at index.
	 */
	public void testHeaderAt() {
		for (int i = 0; i < this.list.size(); i++) {
			HttpHeader header = this.headers.headerAt(i);
			assertHttpHeader(this.list.get(i), header);

			// Ensure cache the materialised header
			assertSame("Should cache further access to header", header, this.headers.headerAt(i));
		}
	}

	/**
	 * Ensure can iterate over all the {@link HttpHeader} instances.
	 */
	public void testAllHeaders() {
		int i = 0;
		List<HttpHeader> cached = new ArrayList<>();
		for (HttpHeader header : this.headers) {
			assertHttpHeader(this.list.get(i++), header);
			cached.add(header);
		}
		assertEquals("Incorrect number of headers", this.list.size(), i);

		// Ensure cache headers
		i = 0;
		for (HttpHeader header : this.headers) {
			assertSame("Incorrect cached header " + i, cached.get(i++), header);
		}
		assertEquals("Incorrect number cached", this.list.size(), i);
	}

	/**
	 * Ensure correct {@link HttpHeader}.
	 */
	public void testGetHeader() {
		for (String name : this.firstHttpHeader.keySet()) {
			HttpHeader expected = this.firstHttpHeader.get(name);
			HttpHeader actual = this.headers.getHeader(name);
			assertHttpHeader(expected, actual);

			// Ensure use cached value
			assertSame("Incorrect cached", actual, this.headers.getHeader(name));
		}
	}

	/**
	 * Ensure can iterate over named {@link HttpHeader} instances.
	 */
	public void testNamedHeaders() {
		for (String name : this.namedHttpHeaders.keySet()) {
			List<HttpHeader> namedList = this.namedHttpHeaders.get(name);
			int i = 0;
			List<HttpHeader> cached = new ArrayList<>();
			for (HttpHeader header : this.headers.getHeaders(name)) {
				assertHttpHeader(namedList.get(i++), header);
				cached.add(header);
			}
			assertEquals("Incorrect number of named headers " + name, namedList.size(), i);

			// Ensure cache headers
			i = 0;
			for (HttpHeader header : this.headers.getHeaders(name)) {
				assertSame("Incorrect cached", cached.get(i++), header);
			}
			assertEquals("Incorrect number cached " + name, namedList.size(), i);
		}
	}

	/**
	 * Asserts the {@link HttpHeader}.
	 * 
	 * @param expected
	 *            Expected {@link HttpHeader}.
	 * @param actual
	 *            Actual {@link HttpHeader}.
	 */
	private static void assertHttpHeader(HttpHeader expected, HttpHeader actual) {
		assertEquals("Incorrect name", expected.getName(), actual.getName());
		assertEquals("Incorrect value", expected.getValue(), actual.getValue());
	}

	/**
	 * Adds a {@link NonMaterialisedHttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 */
	private void addHttpHeader(String name, String value) {
		this.list.add(new SerialisableHttpHeader(name, value));
		this.nonMaterialisedHeaders.add(new MockNonMaterialisedHttpHeader(name, value));
	}

	/*
	 * ==================== NonMaterialisedHttpHeaders ====================
	 */

	private List<HttpHeader> list = new ArrayList<>();

	private List<NonMaterialisedHttpHeader> nonMaterialisedHeaders = new ArrayList<>();

	@Override
	public Iterator<NonMaterialisedHttpHeader> iterator() {
		return this.nonMaterialisedHeaders.iterator();
	}

	@Override
	public int length() {
		return this.nonMaterialisedHeaders.size();
	}

	/**
	 * Mock {@link NonMaterialisedHttpHeader}.
	 */
	private static class MockNonMaterialisedHttpHeader implements NonMaterialisedHttpHeader {

		private final String name;

		private final String value;

		public MockNonMaterialisedHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public HttpHeader materialiseHttpHeader() {
			return new SerialisableHttpHeader(this.name, this.value);
		}
	}

}
