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

import java.util.Iterator;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;

/**
 * Tests the {@link ProcessAwareHttpResponseHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseHeadersTest extends OfficeFrameTestCase {

	/**
	 * {@link ProcessAwareHttpResponseHeaders} to be tested.
	 */
	private final ProcessAwareHttpResponseHeaders headers = new ProcessAwareHttpResponseHeaders(
			new ProcessAwareContext() {
				@Override
				public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
					return operation.run();
				}
			});

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Add the headers (using combinations of string and object)
		this.headers.addHeader("one", "1");
		this.headers.addHeader(new HttpHeaderName("two"), "2");
		this.headers.addHeader("same", new HttpHeaderValue("First"));
		this.headers.addHeader(new HttpHeaderName("three"), new HttpHeaderValue("3"));
		this.headers.addHeader("same", "Second");

		// Should have all headers added
		assertEquals("Incorrect number of headers", 5, this.headers.length());
	}

	/**
	 * Ensure can add headers and iterate over them.
	 */
	public void testGetHeaders() {

		// Ensure correct index access
		assertEquals("Incorrect header 0", "1", this.headers.headerAt(0).getValue());
		assertEquals("Incorrect header 1", "2", this.headers.headerAt(1).getValue());
		assertEquals("Incorrect header 2", "First", this.headers.headerAt(2).getValue());
		assertEquals("Incorrect header 3", "3", this.headers.headerAt(3).getValue());
		assertEquals("Incorrect header 4", "Second", this.headers.headerAt(4).getValue());

		// Ensure can iterate over all headers
		assertHeaderNames(this.headers, "one", "two", "same", "three", "same");

		// Ensure can get first header
		assertEquals("Incorrect first unique header", "1", this.headers.getHeader("one").getValue());
		assertEquals("Incorrect first non-unique header", "First", this.headers.getHeader("same").getValue());
		assertNull("Should not find header", this.headers.getHeader("not exist"));

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
	public void testRemoveHeader() {
		HttpHeader firstSame = this.headers.getHeader("same");

		// Remove the header
		assertTrue("Header should be removed", this.headers.removeHeader(firstSame));
		assertHeaderValues(this.headers, "1", "2", "3", "Second");

		// Removing the same header should have not effect
		assertFalse("Header already removed", this.headers.removeHeader(firstSame));
		assertHeaderValues(this.headers, "1", "2", "3", "Second");
	}

	/**
	 * Ensure can remove {@link HttpHeader} instances by name.
	 */
	public void testRemoveHeadersByName() {

		// Remove unique header
		assertTrue("Should remove one", this.headers.removeHeaders("one"));
		assertHeaderNames(this.headers, "two", "same", "three", "same");
		assertFalse("Should be no headers by name 'one'", this.headers.removeHeaders("one"));

		// Remove all non-unique header
		assertTrue("Should remove same", this.headers.removeHeaders("same"));
		assertHeaderNames(this.headers, "two", "three");
		assertFalse("Should be no headers by name 'same'", this.headers.removeHeaders("same"));
	}

	/**
	 * Ensure can remove {@link HttpHeader} instances by {@link Iterator}.
	 */
	public void testRemoveHeadersByIterator() {

		// Remove via all headers iterator
		Iterator<HttpHeader> iterator = this.headers.iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertHeaderNames(this.headers, "two", "same", "three", "same");
		HttpHeader header = iterator.next();
		assertEquals("Incorrect next header after removing", "two", header.getName());
	}

	/**
	 * Ensure can remove {@link HttpHeader} by named {@link Iterator}.
	 */
	public void testRemoveNamedHeaderByIterator() {

		// Remove unique header via name iterator
		Iterator<HttpHeader> iterator = this.headers.getHeaders("three").iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertFalse("Should be no further headers", iterator.hasNext());

		// Remove non-unique header via iterator
		iterator = this.headers.getHeaders("same").iterator();
		iterator.next(); // move to first
		iterator.remove();
		assertTrue("Should have further same header", iterator.hasNext());
		assertEquals("Incorrect http header", "Second", iterator.next().getValue());
	}

	/**
	 * Ensure correct writing of {@link WritableHttpHeader}.
	 */
	public void testWrittenHeaderBytes() {
		fail("TODO implement");
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param headers
	 *            {@link Iterator} over the {@link HttpHeader} instances.
	 * @param expectedHeaderNames
	 *            Expected {@link HttpHeader} names in order as per
	 *            {@link Iterator}.
	 */
	private static void assertHeaderNames(Iterable<? extends HttpHeader> headers, String... expectedHeaderNames) {
		Iterator<? extends HttpHeader> iterator = headers.iterator();
		for (int i = 0; i < expectedHeaderNames.length; i++) {
			assertTrue("Should have HTTP header " + i, iterator.hasNext());
			HttpHeader header = iterator.next();
			assertEquals("Incorrect HTTP header " + i, expectedHeaderNames[i], header.getName());
		}
		assertFalse("Should be no further headers", iterator.hasNext());
	}

	/**
	 * Asserts the {@link HttpHeader} instances.
	 * 
	 * @param headers
	 *            {@link Iterator} over the {@link HttpHeader} instances.
	 * @param expectedHeaderValues
	 *            Expected {@link HttpHeader} values in order as per
	 *            {@link Iterator}.
	 */
	private static void assertHeaderValues(Iterable<? extends HttpHeader> headers, String... expectedHeaderValues) {
		Iterator<? extends HttpHeader> iterator = headers.iterator();
		for (int i = 0; i < expectedHeaderValues.length; i++) {
			assertTrue("Should have HTTP header value " + i, iterator.hasNext());
			HttpHeader header = iterator.next();
			assertEquals("Incorrect HTTP header value " + i, expectedHeaderValues[i], header.getValue());
		}
		assertFalse("Should be no further headers", iterator.hasNext());
	}

}