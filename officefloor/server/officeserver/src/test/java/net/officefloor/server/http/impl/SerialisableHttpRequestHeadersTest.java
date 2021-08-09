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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;

/**
 * Tests the {@link SerialisableHttpRequestHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequestHeadersTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpHeader} {@link List}.
	 */
	private List<HttpHeader> list = new ArrayList<>();

	/**
	 * First {@link HttpHeader} for each name.
	 */
	private Map<String, HttpHeader> firstHttpHeader = new HashMap<>();

	/**
	 * All {@link HttpHeader} instances for each name.
	 */
	private Map<String, List<HttpHeader>> namedHttpHeaders = new HashMap<>();

	/**
	 * {@link SerialisableHttpRequestHeaders}.
	 */
	private SerialisableHttpRequestHeaders headers;

	@Override
	protected void setUp() throws Exception {

		// Load the HTTP headers
		this.list.add(new MockHttpHeader("one", "1"));
		this.list.add(new MockHttpHeader("same", "S1"));
		this.list.add(new MockHttpHeader("two", "2"));
		this.list.add(new MockHttpHeader("three", "3"));
		this.list.add(new MockHttpHeader("same", "S2"));
		this.list.add(new MockHttpHeader("four", "4"));
		this.list.add(new MockHttpHeader("same", "S3"));
		this.headers = new SerialisableHttpRequestHeaders(this.list);

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
			assertHttpHeader(this.list.get(i), this.headers.headerAt(i));
		}
	}

	/**
	 * Ensure can iterate over all the {@link HttpHeader} instances.
	 */
	public void testAllHeaders() {
		int i = 0;
		for (HttpHeader header : this.headers) {
			assertHttpHeader(this.list.get(i++), header);
		}
		assertEquals("Incorrect number of headers", this.list.size(), i);
	}

	/**
	 * Ensure correct {@link HttpHeader}.
	 */
	public void testGetHeader() {
		for (String name : this.firstHttpHeader.keySet()) {
			HttpHeader expected = this.firstHttpHeader.get(name);
			assertHttpHeader(expected, this.headers.getHeader(name));
		}
	}

	/**
	 * Ensure can iterate over named {@link HttpHeader} instances.
	 */
	public void testNamedHeaders() {
		for (String name : this.namedHttpHeaders.keySet()) {
			List<HttpHeader> namedList = this.namedHttpHeaders.get(name);
			int i = 0;
			for (HttpHeader header : this.headers.getHeaders(name)) {
				assertHttpHeader(namedList.get(i++), header);
			}
			assertEquals("Incorrect number of named headers " + name, namedList.size(), i);
		}
	}

	/**
	 * Ensure can take a copy for {@link Serializable}.
	 */
	public void testCopy() {
		SerialisableHttpRequestHeaders copy = new SerialisableHttpRequestHeaders(this.headers);
		int i = 0;
		for (HttpHeader header : copy) {
			assertHttpHeader(this.list.get(i), header);
			assertNotSame("Should be copy of HTTP header", this.headers.headerAt(i), header);
			i++;
		}
		assertEquals("Incorrect number of headers", this.list.size(), i);
	}

	/**
	 * Ensure can serialise.
	 */
	public void testSerialize() throws Exception {

		// Serialise the HTTP headers
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
		outputStream.writeObject(this.headers);
		outputStream.flush();

		// Materialise the HTTP headers
		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		Object object = inputStream.readObject();
		assertTrue("Incorrect object", object instanceof SerialisableHttpRequestHeaders);
		SerialisableHttpRequestHeaders materialise = (SerialisableHttpRequestHeaders) object;

		// Ensure have all the headers
		int i = 0;
		for (HttpHeader header : materialise) {
			assertHttpHeader(this.list.get(i++), header);
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
	 * Mock {@link HttpHeader}.
	 */
	private static class MockHttpHeader implements HttpHeader {

		private final String name;

		private final String value;

		private MockHttpHeader(String name, String value) {
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
