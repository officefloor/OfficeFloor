/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;
import net.officefloor.plugin.stream.squirtfactory.HeapByteBufferSquirtFactory;

/**
 * Tests the {@link HttpRequestParser}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParserTest extends OfficeFrameTestCase {

	/**
	 * Maximum number of {@link HttpHeader} instances for testing.
	 */
	private static final int MAX_HEADER_COUNT = 255;

	/**
	 * Maximum length of the body in bytes for testing.
	 */
	private static final long MAX_BODY_LENGTH = 1024;

	/**
	 * {@link HttpRequestParser} to test.
	 */
	private HttpRequestParser httpRequestParser = new HttpRequestParserImpl(
			MAX_HEADER_COUNT, MAX_BODY_LENGTH);

	/**
	 * Temporary buffer for parsing.
	 */
	private char[] tempBuffer = new char[255];

	/**
	 * Ensure correct initial state.
	 */
	public void testInitialState() {
		assertEquals("Incorrect initial method", "",
				this.httpRequestParser.getMethod());
		assertEquals("Incorrect initial request URI", "",
				this.httpRequestParser.getRequestURI());
		assertEquals("Incorrect initial HTTP version", "",
				this.httpRequestParser.getHttpVersion());
		assertEquals("Incorrect initial HTTP headers", 0,
				this.httpRequestParser.getHeaders().size());
		assertNull("Initially should be no body",
				this.httpRequestParser.getBody());
	}

	/**
	 * Ensure able to handle empty request).
	 */
	public void testEmpty() {
		this.doMethodTest("", false, null, null, null, null);
	}

	/**
	 * Ensure able to handle a blank request (only spaces received for it).
	 */
	public void testBlank() {
		this.doMethodTest(" ", false, null, null, null, null);
	}

	/**
	 * Ensure able to handle leading spaces.
	 */
	public void testLeadingSpaces() {
		this.doMethodTest(" \n GET ", false, "GET", null, null, null);
	}

	/**
	 * Ensure able to parse up to just the method.
	 */
	public void testToMethod() {
		this.doMethodTest("GET ", false, "GET", null, null, null);
	}

	/**
	 * Ensure able to parse up to just the path.
	 */
	public void testToPath() {
		this.doMethodTest("GET /path ", false, "GET", "/path", null, null);
	}

	/**
	 * Ensure able to parse up to just the version.
	 */
	public void testToVersion() {
		this.doMethodTest("GET /path HTTP/1.1\n", false, "GET", "/path",
				"HTTP/1.1", null);
	}

	/**
	 * Ensure tolerance if request line contains LF without preceding CR.
	 */
	public void testToVersionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n", false, "GET", "/path",
				"HTTP/1.1", null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderName() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length:", false, "GET",
				"/path", "HTTP/1.1", null);
	}

	/**
	 * Ensure issue if missing header name.
	 */
	public void testMissingHeaderName() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n: value\n",
				HttpStatus.SC_BAD_REQUEST, "Missing header name");
	}

	/**
	 * Ensure able to parse header name with escaped length.
	 */
	public void testToHeaderNameContainsEscapedColon() {
		this.doMethodTest(
				"GET /path HTTP/1.1\nbefore%20%3A%20after : colon \n", false,
				"GET", "/path", "HTTP/1.1", null, "before : after", "colon");
	}

	/**
	 * Ensure able to delimit header name via CR.
	 */
	public void testToHeaderNameNoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader\n", false, "GET",
				"/path", "HTTP/1.1", null, "Header", "");
	}

	/**
	 * Ensure able to delimit header name via LF.
	 */
	public void testToHeaderNameNoValueMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\nHeader\n", false, "GET",
				"/path", "HTTP/1.1", null, "Header", "");
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderValue() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\nHost",
				false, "POST", "/path", "HTTP/1.1", null, "Content-Length",
				"10");
	}

	/**
	 * Ensure tolerance if header contains LF without preceding CR.
	 */
	public void testHeaderMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 10\nHost", false, "POST",
				"/path", "HTTP/1.1", null, "Content-Length", "10");
	}

	/**
	 * Ensure able to parse up to the body.
	 */
	public void testToBody() {
		this.doMethodTest(
				"POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT",
				false, "POST", "/path", "HTTP/1.1", null, "Content-Length",
				"1000");
	}

	/**
	 * Ensure tolerance if header separation from body contains LF without
	 * preceding CR.
	 */
	public void testToBodyMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT",
				false, "POST", "/path", "HTTP/1.1", null, "Content-Length",
				"1000");
	}

	/**
	 * Ensure able to handle first header with leading space gracefully.
	 */
	public void testLeadingSpaceToHeader() {
		this.doInvalidMethodTest(
				"GET /path HTTP/1.1\n \t WhiteSpaceBeforeHeader: value\n",
				HttpStatus.SC_BAD_REQUEST,
				"White spacing before first HTTP header");
	}

	/**
	 * Ensure able to have header value on multiple lines.
	 */
	public void testMultiplelineHeaderValue() {
		this.doMethodTest(
				"GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n",
				true, "GET", "/path", "HTTP/1.1", "", "Multiline",
				"Value One Value Two");
	}

	/**
	 * Ensure able to have header value on multiple lines with missing CR.
	 */
	public void testMultiplelineHeaderValueMissingCR() {
		this.doMethodTest(true,
				"GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n",
				true, "GET", "/path", "HTTP/1.1", "", "Multiline",
				"Value One Value Two");
	}

	/**
	 * Ensure able to parse header and body separation containing white spacing.
	 * This is to be more tolerant of the client.
	 */
	public void testHeaderToBodyHavingWhitespacing() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST",
				true, "POST", "/path", "HTTP/1.1", "TEST", "Content-Length",
				"4");
	}

	/**
	 * Ensure able to parse header and body separation containing white spacing.
	 * This is to be more tolerant of the client. Also tolerant of missing CR.
	 */
	public void testHeaderToBodyHavingWhitespacingMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST", true,
				"POST", "/path", "HTTP/1.1", "TEST", "Content-Length", "4");
	}

	/**
	 * Ensure able to parse header separation containing white spacing to no
	 * body. This is to be more tolerant of the client.
	 */
	public void testWhitespacingInGetCompletion() {
		this.doMethodTest("GET /path HTTP/1.1\n \t\n", true, "GET", "/path",
				"HTTP/1.1", "");
	}

	/**
	 * Ensure able to parse header and body separation containing white spacing.
	 * This is to be more tolerant of the client. Also tolerant of missing CR.
	 */
	public void testWhitespacingInGetCompletionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n \t\n", true, "GET",
				"/path", "HTTP/1.1", "");
	}

	/**
	 * Validate GET.
	 */
	public void testGet() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", true, "GET", "/path",
				"HTTP/1.1", "");
	}

	/**
	 * Ensure tolerance if GET request is missing CR before the LF.
	 */
	public void testGetMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n\n", true, "GET", "/path",
				"HTTP/1.1", "");
	}

	/**
	 * Validate GET with one parameter.
	 */
	public void testGetWithOneParamter() {
		this.doMethodTest("GET /path?param=value HTTP/1.1\n\n", true, "GET",
				"/path?param=value", "HTTP/1.1", "");
	}

	/**
	 * Validate GET with two parameters.
	 */
	public void testGetWithTwoParamters() {
		this.doMethodTest(
				"GET /path?paramOne=valueOne&paramOne=valueTwo HTTP/1.1\n\n",
				true, "GET", "/path?paramOne=valueOne&paramOne=valueTwo",
				"HTTP/1.1", "");
	}

	/**
	 * Validate headers.
	 */
	public void testHeaders_WithValue() {
		this.doMethodTest(
				"GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n",
				true, "GET", "/path", "HTTP/1.1", "", "Header1", "Value1",
				"Header2", "Value2");
	}

	/**
	 * Ensure able to have blank value for header.
	 */
	public void testHeaders_BlankValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1:\nHeader2: \n\n", true,
				"GET", "/path", "HTTP/1.1", "", "Header1", "", "Header2", "");
	}

	/**
	 * Ensure able to have no value for header.
	 */
	public void testHeaders_NoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1\nHeader2 \n\n", true,
				"GET", "/path", "HTTP/1.1", "", "Header1", "", "Header2", "");
	}

	/**
	 * Validate GET with entity.
	 */
	public void testGetWithEntity() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST",
				true, "GET", "/path", "HTTP/1.1", "TEST", "Content-Length", "4");
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
	 * Ensure tolerance if POST request is missing CR before the LF.
	 */
	public void testPostMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", true, "POST",
				"/path", "HTTP/1.1", "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST with no body (entity).
	 */
	public void testPostWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", true,
				"POST", "/path", "HTTP/1.1", "", "Content-Length", "0");
	}

	/**
	 * Validated POST with not all of the body (entity) received.
	 */
	public void testPostWithNotAllOfEntityReceived() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\n\n12345",
				false, "POST", "/path", "HTTP/1.1", null, "Content-Length",
				"10");
	}

	/**
	 * Validates partial method being too long.
	 */
	public void testTooLong_PartialMethod() {
		this.tempBuffer = new char[1];
		this.doInvalidMethodTest("TooLarge", HttpStatus.SC_BAD_REQUEST,
				"Method too long");
	}

	/**
	 * Validates complete method being too long.
	 */
	public void testTooLong_CompleteMethod() {
		this.tempBuffer = new char[1];
		this.doInvalidMethodTest("TooLarge ", HttpStatus.SC_BAD_REQUEST,
				"Method too long");
	}

	/**
	 * Validates partial request URI being too long.
	 */
	public void testTooLong_PartialRequestURI() {
		this.tempBuffer = new char[3];
		this.doInvalidMethodTest("GET /TooLong",
				HttpStatus.SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates complete request URI being too long.
	 */
	public void testTooLong_CompleteRequestURI() {
		this.tempBuffer = new char[3];
		this.doInvalidMethodTest("GET /TooLong ",
				HttpStatus.SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates partial version too long.
	 */
	public void testTooLong_PartialVersion() {
		this.tempBuffer = new char[5];
		this.doInvalidMethodTest("GET /path TooLong",
				HttpStatus.SC_BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates complete version too long.
	 */
	public void testTooLong_CompleteVersion() {
		this.tempBuffer = new char[5];
		this.doInvalidMethodTest("GET /path TooLong\n",
				HttpStatus.SC_BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates partial header name too long.
	 */
	public void testTooLong_PartialHeaderName() {
		this.tempBuffer = new char[8];
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName",
				HttpStatus.SC_BAD_REQUEST, "Header name too long");
	}

	/**
	 * Validates complete header name too long.
	 */
	public void testTooLong_CompleteHeaderName() {
		this.tempBuffer = new char[8];
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName:",
				HttpStatus.SC_BAD_REQUEST, "Header name too long");
	}

	/**
	 * Validates partial header value too long.
	 */
	public void testTooLong_PartialHeaderValue() {
		this.tempBuffer = new char[8];
		this.doInvalidMethodTest(
				"GET /path HTTP/1.1\nName: HeaderValueTooLong",
				HttpStatus.SC_BAD_REQUEST, "Header value too long");
	}

	/**
	 * Validates complete header value too long.
	 */
	public void testTooLong_CompleteHeaderValue() {
		this.tempBuffer = new char[8];
		this.doInvalidMethodTest(
				"GET /path HTTP/1.1\nName: HeaderValueTooLong\n",
				HttpStatus.SC_BAD_REQUEST, "Header value too long");
	}

	/**
	 * Ensure fails if too many {@link HttpHeader} instances.
	 */
	public void testTooMany_Headers() {
		// Create request with too many headers
		StringBuilder request = new StringBuilder();
		request.append("GET /path HTTP/1.1\n");
		for (int i = 0; i < (MAX_HEADER_COUNT + 1); i++) {
			request.append("Header" + i + ": Value" + i + "\n");
		}
		request.append("\n");
		this.doInvalidMethodTest(request.toString(), HttpStatus.SC_BAD_REQUEST,
				"Too Many Headers");
	}

	/**
	 * Ensures fails if body is bigger than maximum size.
	 */
	public void testTooLong_Body() {
		// Create body that is too large
		long tooLargeBodySize = MAX_BODY_LENGTH + 1;
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: "
				+ tooLargeBodySize + "\n\n",
				HttpStatus.SC_REQUEST_ENTITY_TOO_LARGE,
				"Request entity must be less than maximum of "
						+ MAX_BODY_LENGTH + " bytes");
	}

	/**
	 * Validates that Content-Length is required for POST.
	 */
	public void testNoContentLengthForPost() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\n\nTEST",
				HttpStatus.SC_LENGTH_REQUIRED,
				"Must provide Content-Length header for POST");
	}

	/**
	 * Validates that Content-Length is required for PUT.
	 */
	public void testNoContentLengthForPut() {
		this.doInvalidMethodTest("PUT /path HTTP/1.1\n\nTEST",
				HttpStatus.SC_LENGTH_REQUIRED,
				"Must provide Content-Length header for PUT");
	}

	/**
	 * Ensures the Content-Length contains a value.
	 */
	public void testBlankContentLengthValue() {
		this.doInvalidMethodTest(
				"POST /path HTTP/1.1\nContent-Length:\n\nTEST",
				HttpStatus.SC_LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensures the Content-Length is an integer.
	 */
	public void testNonIntegerContentLengthValue() {
		this.doInvalidMethodTest(
				"POST /path HTTP/1.1\nContent-Length: INVALID\n\nTEST",
				HttpStatus.SC_LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensure able to reset {@link HttpRequestParser} to parse another request.
	 */
	public void testReset() {
		// Parse first request
		this.doMethodTest(
				"POST /one HTTP/1.1\nContent-Length: 4\nHeaderOne: ValueOne\n\nTEST",
				true, "POST", "/one", "HTTP/1.1", "TEST", "Content-Length",
				"4", "HeaderOne", "ValueOne");

		// Reset and parse second request
		this.httpRequestParser.reset();
		this.doMethodTest(
				"PUT /two HTTP/1.0\nContent-Length: 7\nHeaderTwo: ValueTwo\n\nANOTHER",
				true, "PUT", "/two", "HTTP/1.0", "ANOTHER", "Content-Length",
				"7", "HeaderTwo", "ValueTwo");
	}

	/**
	 * Ensure <code>%HH</code> results in respective byte value.
	 */
	public void testPercentageEscape() {
		this.doMethodTest("GET /space%20byte HTTP/1.1\n\n", true, "GET",
				"/space byte", "HTTP/1.1", "");
	}

	/**
	 * Ensure bad parse with invalid value for <code>%HH</code>.
	 */
	public void testPercentageInvalidValue() {
		this.doInvalidMethodTest("GET /invalid%WRONG",
				HttpStatus.SC_BAD_REQUEST,
				"Invalid escaped hexidecimal character 'W'");
	}

	/**
	 * Validate possible values for <code>%HH</code> values.
	 */
	public void testAllPercentageValues() {

		// Validate transforms
		assertEquals("Ensure transform to HTTP", " ",
				this.getCharacterValue((byte) 2, (byte) 0));
		assertEquals("Ensure 1 transforms", "1", this.getCharacterValue(1));
		assertEquals("Ensure B transforms", "B", this.getCharacterValue(0xB));

		// Validate the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Obtain the characters
				String high = this.getCharacterValue(highBits);
				String low = this.getCharacterValue(lowBits);
				String character = this.getCharacterValue((byte) highBits,
						(byte) lowBits);

				// Do not run for control characters
				byte value = (byte) ((highBits << 4) | lowBits);
				if ((value <= 31) || (value == 127)) {
					continue; // control character
				}

				// Validate the percentage value
				this.doMethodTest("GET /%" + high + low + " HTTP/1.1\n\n",
						true, "GET", "/" + character, "HTTP/1.1", "");
				this.httpRequestParser.reset();
			}
		}
	}

	/**
	 * Transforms the high and low bits to the corresponding character value.
	 * 
	 * @param highBits
	 *            High bits.
	 * @param lowBits
	 *            Low bits
	 * @return Character value.
	 */
	private String getCharacterValue(byte highBits, byte lowBits) {
		byte byteValue = (byte) ((highBits << 4) + lowBits);
		return UsAsciiUtil.convertToString(new byte[] { byteValue });
	}

	/**
	 * Obtains the character value for the hexidecimal value.
	 * 
	 * @param hexidecimal
	 *            Hexidecimal value.
	 * @return Character value.
	 */
	private String getCharacterValue(int hexidecimal) {
		int charValue;
		if ((0 <= hexidecimal) && (hexidecimal <= 9)) {
			charValue = '0' + hexidecimal;
		} else if ((0xA <= hexidecimal) && (hexidecimal <= 0xF)) {
			charValue = 'A' + (hexidecimal - 0xA);
		} else {
			throw new IllegalArgumentException("Invalid hexidecimal value "
					+ hexidecimal);
		}
		return String.valueOf((char) charValue);
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
		this.doMethodTest(false, httpRequest, expectedIsComplete,
				expectedMethod, expectedPath, expectedVersion, expectedBody,
				expectedHeaderNameValues);
	}

	/**
	 * Does a valid HTTP request test.
	 * 
	 * @param isRemoveCR
	 *            Flag indicating to remove the CR (typically before the LF).
	 *            This is to allow more tolerant handling of requests.
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
	private void doMethodTest(boolean isRemoveCR, String httpRequest,
			boolean expectedIsComplete, String expectedMethod,
			String expectedPath, String expectedVersion, String expectedBody,
			String... expectedHeaderNameValues) {
		try {

			// Create input buffer stream with content
			byte[] content = UsAsciiUtil.convertToHttp(httpRequest);
			if (isRemoveCR) {
				// Remove the CR characters (testing tolerance)
				byte CR = UsAsciiUtil.convertToUsAscii('\r');
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				for (byte value : content) {
					if (value == CR) {
						continue; // do not include CR
					}
					buffer.write(value);
				}
				content = buffer.toByteArray();
			}
			BufferStream bufferStream = new BufferStreamImpl(
					new HeapByteBufferSquirtFactory(1024));
			bufferStream.getOutputBufferStream().write(content);
			InputBufferStream inputBufferStream = bufferStream
					.getInputBufferStream();

			// Parse the content
			boolean isComplete = this.httpRequestParser.parse(
					inputBufferStream, this.tempBuffer);

			// Validate request line
			assertEquals((expectedMethod == null ? "" : expectedMethod),
					this.httpRequestParser.getMethod());
			assertEquals((expectedPath == null ? "" : expectedPath),
					this.httpRequestParser.getRequestURI());
			assertEquals((expectedVersion == null ? "" : expectedVersion),
					this.httpRequestParser.getHttpVersion());

			// Validate correct number of headers
			assertEquals("Incorrect number of headers",
					(expectedHeaderNameValues.length / 2),
					this.httpRequestParser.getHeaders().size());

			// Validate correct headers
			for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
				String expectedHeaderName = expectedHeaderNameValues[i];
				String expectedHeaderValue = expectedHeaderNameValues[i + 1];
				int headerIndex = i / 2;
				HttpHeader header = this.httpRequestParser.getHeaders().get(
						headerIndex);
				assertEquals("Incorrect name for header (" + headerIndex + ")",
						expectedHeaderName, header.getName());
				assertEquals("Incorrect value for header '"
						+ expectedHeaderName + "' (" + headerIndex + ")",
						expectedHeaderValue, header.getValue());
			}

			// Validate the body
			InputBufferStream bodyStream = this.httpRequestParser.getBody();
			if (expectedBody == null) {
				// Should not have a body
				if (bodyStream != null) {
					// Body being parsed so ensure no content yet
					assertEquals("Should not have a body", 0,
							bodyStream.available());
					assertEquals("Should not be end of stream", 0,
							bodyStream.read(new byte[10]));
				}

			} else {
				// Obtain the body content
				long available = this.httpRequestParser.getBody().available();
				byte[] body = new byte[(int) (available < 0 ? 0 : available)];
				int bodySize = this.httpRequestParser.getBody().read(body);
				body = Arrays.copyOfRange(body, 0,
						(bodySize < 0 ? 0 : bodySize));
				UsAsciiUtil.assertEquals("Incorrect body",
						(expectedBody == null ? "" : expectedBody), body);
			}

			// Validate if complete
			assertEquals("Incorrect completion", expectedIsComplete, isComplete);

		} catch (HttpRequestParseException ex) {
			// Parse exception not expected
			fail("Request should be parsed successfully [failure "
					+ ex.getHttpStatus() + ": " + ex.getMessage() + "]");

		} catch (IOException ex) {
			fail("Should not have I/O failure: " + ex.getMessage());
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

		// Create input buffer stream with content
		byte[] content = UsAsciiUtil.convertToHttp(invalidHttpRequest);
		BufferStream bufferStream = new BufferStreamImpl(
				new HeapByteBufferSquirtFactory(1024));
		try {
			bufferStream.getOutputBufferStream().write(content);
		} catch (IOException ex) {
			fail("Should not fail to write content: " + ex.getMessage());
		}
		InputBufferStream inputBufferStream = bufferStream
				.getInputBufferStream();

		// Should not be able parse invalid method
		try {

			// Parse the content
			this.httpRequestParser.parse(inputBufferStream, this.tempBuffer);

			// Should not be parsed
			fail("Should not parse invalid HTTP request:\n"
					+ invalidHttpRequest);

		} catch (HttpRequestParseException ex) {
			// Validate details of parse failure
			assertEquals("Incorrect http status", expectedHttpStatus,
					ex.getHttpStatus());
			assertEquals("Incorrect parse failure reason",
					expectedParseFailureReason, ex.getMessage());

		} catch (IOException ex) {
			fail("Should not have I/O failure: " + ex.getMessage());
		}
	}

}