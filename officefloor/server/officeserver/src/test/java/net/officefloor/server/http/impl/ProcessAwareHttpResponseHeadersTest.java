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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockManagedObjectContext;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link ProcessAwareHttpResponseHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseHeadersTest {

	/**
	 * {@link ProcessAwareHttpResponseHeaders} to be tested.
	 */
	private final ProcessAwareHttpResponseHeaders headers = new ProcessAwareHttpResponseHeaders(
			new MockManagedObjectContext());

	@BeforeEach
	public void setUp() throws Exception {

		// Add the headers (using combinations of string and object)
		this.headers.addHeader("one", "1");
		this.headers.addHeader(new HttpHeaderName("two"), "2");
		this.headers.addHeader("same", new HttpHeaderValue("First"));
		this.headers.addHeader(new HttpHeaderName("three"), new HttpHeaderValue("3"));
		this.headers.addHeader("same", "Second");
	}

	/**
	 * Ensure can add headers and iterate over them.
	 */
	@Test
	public void getHeaders() {

		// Ensure can iterate over all headers
		assertHeaderNames(this.headers, "one", "two", "same", "three", "same");

		// Ensure can get first header
		assertEquals("1", this.headers.getHeader("one").getValue(), "Incorrect first unique header");
		assertEquals("First", this.headers.getHeader("same").getValue(), "Incorrect first non-unique header");
		assertNull(this.headers.getHeader("not exist"), "Should not find header");

		// Ensure can iterate over headers by name
		assertHeaderNames(this.headers.getHeaders("one"), "one");
		assertHeaderNames(this.headers.getHeaders("two"), "two");
		assertHeaderNames(this.headers.getHeaders("three"), "three");
		assertHeaderNames(this.headers.getHeaders("same"), "same", "same");
		assertHeaderValues(this.headers.getHeaders("same"), "First", "Second");

		// Ensure correct writable headers
		assertHeaderNames(this.headers.getWritableHttpHeaders(), "one", "two", "same", "three", "same");
	}

	/**
	 * Ensure can remove {@link HttpHeader}.
	 */
	@Test
	public void removeHeader() {
		HttpHeader firstSame = this.headers.getHeader("same");

		// Remove the header
		assertTrue(this.headers.removeHeader(firstSame), "Header should be removed");
		assertHeaderValues(this.headers, "1", "2", "3", "Second");

		// Removing the same header should have not effect
		assertFalse(this.headers.removeHeader(firstSame), "Header already removed");
		assertHeaderValues(this.headers, "1", "2", "3", "Second");
	}

	/**
	 * Ensure can remove {@link HttpHeader} instances by name.
	 */
	@Test
	public void removeHeadersByName() {

		// Remove unique header
		assertTrue(this.headers.removeHeaders("one"), "Should remove one");
		assertHeaderNames(this.headers, "two", "same", "three", "same");
		assertFalse(this.headers.removeHeaders("one"), "Should be no headers by name 'one'");

		// Remove all non-unique header
		assertTrue(this.headers.removeHeaders("same"), "Should remove same");
		assertHeaderNames(this.headers, "two", "three");
		assertFalse(this.headers.removeHeaders("same"), "Should be no headers by name 'same'");
	}

	/**
	 * Ensure can remove {@link HttpHeader} instances by {@link Iterator}.
	 */
	@Test
	public void removeHeadersByIterator() {

		// Remove via all headers iterator
		Iterator<HttpHeader> iterator = this.headers.iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertHeaderNames(this.headers, "two", "same", "three", "same");
		HttpHeader header = iterator.next();
		assertEquals("two", header.getName(), "Incorrect next header after removing");
	}

	/**
	 * Ensure can remove {@link HttpHeader} by named {@link Iterator}.
	 */
	@Test
	public void removeNamedHeaderByIterator() {

		// Remove unique header via name iterator
		Iterator<HttpHeader> iterator = this.headers.getHeaders("three").iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertFalse(iterator.hasNext(), "Should be no further headers");

		// Remove non-unique header via iterator
		iterator = this.headers.getHeaders("same").iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertTrue(iterator.hasNext(), "Should have further same header");
		assertEquals("Second", iterator.next().getValue(), "Incorrect http header");
	}

	/**
	 * Ensure correct writing of {@link WritableHttpHeader}.
	 */
	@Test
	public void writtenHeaderBytes() throws IOException {

		// Obtain writer
		MockStreamBufferPool bufferPool = new MockStreamBufferPool();
		StreamBuffer<ByteBuffer> buffer = bufferPool.getPooledStreamBuffer();

		// Write the headers
		WritableHttpHeader header = this.headers.getWritableHttpHeaders();
		while (header != null) {
			header.write(buffer, bufferPool);
			header = header.next;
		}

		// Obtain the content
		MockStreamBufferPool.releaseStreamBuffers(buffer);
		String content = MockStreamBufferPool.getContent(buffer, ServerHttpConnection.HTTP_CHARSET);

		// Ensure correct content
		String expectedContent = "one: 1\r\ntwo: 2\r\nsame: First\r\nthree: 3\r\nsame: Second\r\n";
		assertEquals(expectedContent, content, "Incorrect HTTP headers content");
	}

	/**
	 * Asserts the {@link WritableHttpHeader} instances.
	 * 
	 * @param head                Head {@link WritableHttpHeader} to the linked list
	 *                            of {@link WritableHttpHeader} instances.
	 * @param expectedHeaderNames Expected {@link HttpHeader} names in order.
	 */
	private static void assertHeaderNames(WritableHttpHeader head, String... expectedHeaderNames) {
		for (int i = 0; i < expectedHeaderNames.length; i++) {
			assertEquals(expectedHeaderNames[i], head.getName(), "Incorrect header " + i);
			head = head.next;
		}
		assertNull(head, "Incorrect number of headers");
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param headers             {@link Iterator} over the {@link HttpHeader}
	 *                            instances.
	 * @param expectedHeaderNames Expected {@link HttpHeader} names in order as per
	 *                            {@link Iterator}.
	 */
	private static void assertHeaderNames(Iterable<? extends HttpHeader> headers, String... expectedHeaderNames) {
		assertHeaderNames(headers.iterator(), expectedHeaderNames);
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param headers             {@link Iterator} over the {@link HttpHeader}
	 *                            instances.
	 * @param expectedHeaderNames Expected {@link HttpHeader} names in order as per
	 *                            {@link Iterator}.
	 */
	private static void assertHeaderNames(Iterator<? extends HttpHeader> headers, String... expectedHeaderNames) {
		for (int i = 0; i < expectedHeaderNames.length; i++) {
			assertTrue(headers.hasNext(), "Should have HTTP header " + i);
			HttpHeader header = headers.next();
			assertEquals(expectedHeaderNames[i], header.getName(), "Incorrect HTTP header " + i);
		}
		assertFalse(headers.hasNext(), "Should be no further headers");
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param headers              {@link Iterator} over the {@link HttpHeader}
	 *                             instances.
	 * @param expectedHeaderValues Expected {@link HttpHeader} values in order as
	 *                             per {@link Iterator}.
	 */
	private static void assertHeaderValues(Iterable<? extends HttpHeader> headers, String... expectedHeaderValues) {
		Iterator<? extends HttpHeader> iterator = headers.iterator();
		for (int i = 0; i < expectedHeaderValues.length; i++) {
			assertTrue(iterator.hasNext(), "Should have HTTP header value " + i);
			HttpHeader header = iterator.next();
			assertEquals(expectedHeaderValues[i], header.getValue(), "Incorrect HTTP header value " + i);
		}
		assertFalse(iterator.hasNext(), "Should be no further headers");
	}

}
