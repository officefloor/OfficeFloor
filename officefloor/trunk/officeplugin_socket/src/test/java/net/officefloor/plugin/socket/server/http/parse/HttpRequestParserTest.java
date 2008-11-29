/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http.parse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpStatus;

/**
 * Tests the {@link HttpRequestParser}.
 * 
 * @author Daniel
 */
public class HttpRequestParserTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpRequestParser} to test.
	 */
	private HttpRequestParser httpRequestParser = new HttpRequestParser(1024,
			1024 * 1024);

	/**
	 * Validate GET.
	 */
	public void testGet() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", true, "GET", "/path",
				"HTTP/1.1", null);
	}

	/**
	 * Validate GET with headers.
	 */
	public void testGetWithHeaders() {
		this.doMethodTest(
				"GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n",
				true, "GET", "/path", "HTTP/1.1", null, "Header1", "Value1",
				"Header2", "Value2");
	}

	/**
	 * Validate GET with entity.
	 */
	public void testGetWithEntity() {
		this
				.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST",
						true, "GET", "/path", "HTTP/1.1", "TEST",
						"Content-Length", "4");
	}

	/**
	 * Validate POST.
	 */
	public void testPost() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n\nTEST",
				true, "POST", "/path", "HTTP/1.1", "TEST", "Content-Length",
				"4");
	}

	/**
	 * Validate POST with no body (entity).
	 */
	public void testPostWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", true,
				"POST", "/path", "HTTP/1.1", null, "Content-Length", "0");
	}

	/**
	 * Validates invalid partial method on request.
	 */
	public void testInvalidPartialMethod() {
		this.doInvalidMethodTest("INVALID", HttpStatus._400,
				"Unknown method: INVALID...");
	}

	/**
	 * Validates invalid method on request.
	 */
	public void testInvalidMethod() {
		this.doInvalidMethodTest("INVALID /path HTTP/1.0\n\n", HttpStatus._400,
				"Unknown method: INVALID");
	}

	/**
	 * Validates invalid partial version on request.
	 */
	public void testInvalidPartialVersion() {
		this.doInvalidMethodTest("GET /path INVALID", HttpStatus._400,
				"Unknown version: INVALID...");
	}

	/**
	 * Validates invalid version on request.
	 */
	public void testInvalidVersion() {
		this.doInvalidMethodTest("GET /path INVALID\n\n", HttpStatus._400,
				"Unknown version: INVALID");
	}

	/**
	 * Validates that Content-Length is required for POST.
	 */
	public void testNoContentLengthForPost() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\n\nTEST",
				HttpStatus._411, "Must provide Content-Length header for POST");
	}

	/**
	 * Validates that Content-Length is required for PUT.
	 */
	public void testNoContentLengthForPut() {
		this.doInvalidMethodTest("PUT /path HTTP/1.1\n\nTEST", HttpStatus._411,
				"Must provide Content-Length header for PUT");
	}

	/**
	 * Ensures the Content-Length contains a value.
	 */
	public void testBlankContentLengthValue() {
		this.doInvalidMethodTest(
				"POST /path HTTP/1.1\nContent-Length:\n\nTEST",
				HttpStatus._411,
				"Content-Length header value  must be an integer");
	}

	/**
	 * Ensures the Content-Length is an integer.
	 */
	public void testNonIntegerContentLengthValue() {
		this.doInvalidMethodTest(
				"POST /path HTTP/1.1\nContent-Length: INVALID\n\nTEST",
				HttpStatus._411,
				"Content-Length header value INVALID must be an integer");
	}

	/**
	 * Ensures that on a method that does not require a Content-Length that the
	 * Content-Length is provided if there is an entity on the request.
	 */
	public void testNoContentLengthButEntityProvided() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n\nTEST", HttpStatus._411,
				"Must provide Content-Length header if sending entity");
	}

	/**
	 * Ensures fails if entity is larger than the Content-Length.
	 */
	public void testEntityLargerThanContentLength() {
		this.doInvalidMethodTest(
				"POST /path HTTP/1.1\nContent-Length: 4\n\n12345",
				HttpStatus._400,
				"Request entity exceeded Content-Length size of 4");
	}

	/**
	 * Does a valid HTTP request test.
	 * 
	 * @param httpRequest
	 *            HTTP request content.
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedPath
	 *            Expected path.
	 * @param expectedVersion
	 *            Expected version.
	 * @param expectedBody
	 *            Expected body.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(String httpRequest, boolean expectedIsComplete,
			String expectedMethod, String expectedPath, String expectedVersion,
			String expectedBody, String... expectedHeaderNameValues) {
		try {
			// Parse the content
			byte[] content = UsAsciiUtil.convertToHttp(httpRequest);
			boolean isComplete = this.httpRequestParser.parseMoreContent(
					content, 0, content.length);

			// Validate request line
			assertEquals(expectedMethod, this.httpRequestParser.getMethod());
			assertEquals(expectedPath, this.httpRequestParser.getPath());
			assertEquals(expectedVersion, this.httpRequestParser.getVersion());

			// Validate correct number of headers
			assertEquals("Incorrect number of headers",
					(expectedHeaderNameValues.length / 2),
					this.httpRequestParser.getHeaders().size());

			// Validate correct headers
			for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
				String expectedHeaderName = expectedHeaderNameValues[i];
				String expectedHeaderValue = expectedHeaderNameValues[i + 1];
				assertEquals("Incorrect value for header '"
						+ expectedHeaderName + "'", expectedHeaderValue,
						this.httpRequestParser.getHeader(expectedHeaderName));
			}

			// Validate the body
			UsAsciiUtil.assertEquals("Incorrect body",
					(expectedBody == null ? "" : expectedBody),
					this.httpRequestParser.getBody());

			// Validate if complete
			assertEquals("Incorrect completion", expectedIsComplete, isComplete);

		} catch (ParseException ex) {
			// Parse exception not expected
			fail("Request should be parsed succesfully [failure "
					+ ex.getHttpStatus() + ": " + ex.getMessage() + "]");
		}
	}

	/**
	 * Does an invalid HTTP request test.
	 * 
	 * @param invalidHttpRequest
	 *            Invalid HTTP request content.
	 * @param expectedHttpStatus
	 *            Expected HTTP status.
	 * @param expectedParseFailureReason
	 *            Expected reason for the parse failure.
	 */
	private void doInvalidMethodTest(String invalidHttpRequest,
			int expectedHttpStatus, String expectedParseFailureReason) {

		// Parse the invalid content for use
		byte[] content = UsAsciiUtil.convertToHttp(invalidHttpRequest);

		// Should not be able parse invalid method
		try {
			this.httpRequestParser.parseMoreContent(content, 0, content.length);

			// Should not be parsed
			fail("Should not parse invalid HTTP request:\n"
					+ invalidHttpRequest);

		} catch (ParseException ex) {
			// Validate details of parse failure
			assertEquals("Incorrect http status", expectedHttpStatus, ex
					.getHttpStatus());
			assertEquals("Incorrect parse failure reason",
					expectedParseFailureReason, ex.getMessage());
		}
	}
}
