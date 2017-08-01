/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.server.http.protocol.HttpStatus;

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
	 * Maximum bytes for a TEXT.
	 */
	private static final int MAX_TEXT_LENGTH = 1024;

	/**
	 * Maximum length of the entity in bytes for testing.
	 */
	private static final long MAX_ENTITY_LENGTH = 1024;

	/**
	 * {@link HttpRequestParser} to test.
	 */
	private HttpRequestParser httpRequestParser = new HttpRequestParserImpl(
			MAX_HEADER_COUNT, MAX_TEXT_LENGTH, MAX_ENTITY_LENGTH);

	/**
	 * Ensure correct initial state.
	 */
	public void testInitialState() throws IOException {
		assertEquals("Incorrect initial method", "",
				this.httpRequestParser.getMethod());
		assertEquals("Incorrect initial request URI", "",
				this.httpRequestParser.getRequestURI());
		assertEquals("Incorrect initial HTTP version", "",
				this.httpRequestParser.getHttpVersion());
		assertEquals("Incorrect initial HTTP headers", 0,
				this.httpRequestParser.getHeaders().size());
		assertEquals("Initially should be no entity data", 0,
				this.httpRequestParser.getEntity().getInputStream().available());
	}

	/**
	 * Ensure able to handle empty request).
	 */
	public void testEmpty() {
		this.doMethodTest("", false, -1, null, null, null, null);
	}

	/**
	 * Ensure able to handle a blank request (only spaces received for it).
	 */
	public void testBlank() {
		this.doMethodTest(" ", false, -1, null, null, null, null);
	}

	/**
	 * Ensure able to handle leading spaces.
	 */
	public void testLeadingSpaces() {
		this.doMethodTest(" \n GET ", false, -1, "GET", null, null, null);
	}

	/**
	 * Ensure able to parse up to just the method.
	 */
	public void testToMethod() {
		this.doMethodTest("GET ", false, -1, "GET", null, null, null);
	}

	/**
	 * Ensure able to parse up to just the path.
	 */
	public void testToPath() {
		this.doMethodTest("GET /path ", false, -1, "GET", "/path", null, null);
	}

	/**
	 * Ensure able to parse up to just the version.
	 */
	public void testToVersion() {
		this.doMethodTest("GET /path HTTP/1.1\n", false, -1, "GET", "/path",
				"HTTP/1.1", null);
	}

	/**
	 * Ensure tolerance if request line contains LF without preceding CR.
	 */
	public void testToVersionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n", false, -1, "GET",
				"/path", "HTTP/1.1", null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderName() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length:", false, -1,
				"GET", "/path", "HTTP/1.1", null);
	}

	/**
	 * Ensure issue if missing header name.
	 */
	public void testMissingHeaderName() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n: value\n",
				HttpStatus.SC_BAD_REQUEST, "Missing header name");
	}

	/**
	 * Ensure able to delimit header name via CR.
	 */
	public void testToHeaderNameNoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader\n", false, -1, "GET",
				"/path", "HTTP/1.1", null, "Header", "");
	}

	/**
	 * Ensure able to delimit header name via LF.
	 */
	public void testToHeaderNameNoValueMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\nHeader\n", false, -1,
				"GET", "/path", "HTTP/1.1", null, "Header", "");
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderValue() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\nHost",
				false, -1, "POST", "/path", "HTTP/1.1", null, "Content-Length",
				"10");
	}

	/**
	 * Ensure tolerance if header contains LF without preceding CR.
	 */
	public void testHeaderMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 10\nHost", false, -1,
				"POST", "/path", "HTTP/1.1", null, "Content-Length", "10");
	}

	/**
	 * Ensure able to parse up to the entity.
	 */
	public void testToEntity() {
		this.doMethodTest(
				"POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT",
				false, -1, "POST", "/path", "HTTP/1.1", "NOT ALL CONTENT",
				"Content-Length", "1000");
	}

	/**
	 * Ensure tolerance if header separation from entity contains LF without
	 * preceding CR.
	 */
	public void testToEntityMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT",
				false, -1, "POST", "/path", "HTTP/1.1", "NOT ALL CONTENT",
				"Content-Length", "1000");
	}

	/**
	 * Ensure multiple parsing for completing the entity content.
	 */
	public void testPartialEntityThenComplete() throws Exception {

		// Provide only partial entity (typically if network packet is split)
		byte[] firstPacket = UsAsciiUtil
				.convertToUsAscii("POST /path HTTP/1.1\nContent-Length: 4\n\nTE");
		boolean isCompleteOnFirstPacket = this.httpRequestParser.parse(
				firstPacket, 0);
		assertFalse("Should not complete if partial entity",
				isCompleteOnFirstPacket);
		assertEquals("Should have consumed all bytes", -1,
				this.httpRequestParser.nextByteToParseIndex());

		// Provide remaining of entity (with some data of next HTTP request)
		byte[] secondPacket = UsAsciiUtil.convertToUsAscii("ST more");
		boolean isCompleteOnSecondPacket = this.httpRequestParser.parse(
				secondPacket, 0);
		assertTrue("Should be complete with remaining of entity received",
				isCompleteOnSecondPacket);
		assertEquals(
				"Should have further bytes remaining",
				(secondPacket.length - UsAsciiUtil.convertToUsAscii(" more").length),
				this.httpRequestParser.nextByteToParseIndex());

		// Validate the parsing
		this.validateHttpRequestParserState("POST", "/path", "HTTP/1.1",
				"TEST", "Content-Length", "4");
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
				true, -1, "GET", "/path", "HTTP/1.1", "", "Multiline",
				"Value One Value Two");
	}

	/**
	 * Ensure able to have header value on multiple lines with missing CR.
	 */
	public void testMultiplelineHeaderValueMissingCR() {
		this.doMethodTest(true,
				"GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n",
				true, -1, "GET", "/path", "HTTP/1.1", "", "Multiline",
				"Value One Value Two");
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client.
	 */
	public void testHeaderToEntityHavingWhitespacing() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST",
				true, -1, "POST", "/path", "HTTP/1.1", "TEST",
				"Content-Length", "4");
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client. Also tolerant of
	 * missing CR.
	 */
	public void testHeaderToEntityHavingWhitespacingMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST", true, -1,
				"POST", "/path", "HTTP/1.1", "TEST", "Content-Length", "4");
	}

	/**
	 * Ensure able to parse header separation containing white spacing to no
	 * entity. This is to be more tolerant of the client.
	 */
	public void testWhitespacingInGetCompletion() {
		this.doMethodTest("GET /path HTTP/1.1\n \t\n", true, -1, "GET",
				"/path", "HTTP/1.1", "");
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client. Also tolerant of
	 * missing CR.
	 */
	public void testWhitespacingInGetCompletionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n \t\n", true, -1, "GET",
				"/path", "HTTP/1.1", "");
	}

	/**
	 * Validate GET.
	 */
	public void testGet() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", true, -1, "GET", "/path",
				"HTTP/1.1", "");
	}

	/**
	 * Ensure tolerance if GET request is missing CR before the LF.
	 */
	public void testGetMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n\n", true, -1, "GET",
				"/path", "HTTP/1.1", "");
	}

	/**
	 * Validate GET with one parameter.
	 */
	public void testGetWithOneParamter() {
		this.doMethodTest("GET /path?param=value HTTP/1.1\n\n", true, -1,
				"GET", "/path?param=value", "HTTP/1.1", "");
	}

	/**
	 * Validate GET with two parameters.
	 */
	public void testGetWithTwoParamters() {
		this.doMethodTest(
				"GET /path?paramOne=valueOne&paramOne=valueTwo HTTP/1.1\n\n",
				true, -1, "GET", "/path?paramOne=valueOne&paramOne=valueTwo",
				"HTTP/1.1", "");
	}

	/**
	 * Validate headers.
	 */
	public void testHeaders_WithValue() {
		this.doMethodTest(
				"GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n",
				true, -1, "GET", "/path", "HTTP/1.1", "", "Header1", "Value1",
				"Header2", "Value2");
	}

	/**
	 * Ensure able to have blank value for header.
	 */
	public void testHeaders_BlankValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1:\nHeader2: \n\n", true,
				-1, "GET", "/path", "HTTP/1.1", "", "Header1", "", "Header2",
				"");
	}

	/**
	 * Ensure able to have no value for header.
	 */
	public void testHeaders_NoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1\nHeader2 \n\n", true,
				-1, "GET", "/path", "HTTP/1.1", "", "Header1", "", "Header2",
				"");
	}

	/**
	 * Validate GET with entity.
	 */
	public void testGetWithEntity() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST",
				true, -1, "GET", "/path", "HTTP/1.1", "TEST", "Content-Length",
				"4");
	}

	/**
	 * Validate POST.
	 */
	public void testPost() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n\nTEST",
				true, -1, "POST", "/path", "HTTP/1.1", "TEST",
				"Content-Length", "4");
	}

	/**
	 * Ensure tolerance if POST request is missing CR before the LF.
	 */
	public void testPostMissingCR() {
		this.doMethodTest(true,
				"POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", true, -1,
				"POST", "/path", "HTTP/1.1", "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST with no entity.
	 */
	public void testPostWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", true,
				-1, "POST", "/path", "HTTP/1.1", "", "Content-Length", "0");
	}

	/**
	 * Validated POST with not all of the entity received.
	 */
	public void testPostWithNotAllOfEntityReceived() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\n\n12345",
				false, -1, "POST", "/path", "HTTP/1.1", "12345",
				"Content-Length", "10");
	}

	/**
	 * Validates partial method being too long.
	 */
	public void testTooLong_PartialMethod() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 1,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("TooLarge", HttpStatus.SC_BAD_REQUEST,
				"Method too long");
	}

	/**
	 * Validates complete method being too long.
	 */
	public void testTooLong_CompleteMethod() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 1,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("TooLarge ", HttpStatus.SC_BAD_REQUEST,
				"Method too long");
	}

	/**
	 * Validates partial request URI being too long.
	 */
	public void testTooLong_PartialRequestURI() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 3,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /TooLong",
				HttpStatus.SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates complete request URI being too long.
	 */
	public void testTooLong_CompleteRequestURI() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 3,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /TooLong ",
				HttpStatus.SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates partial version too long.
	 */
	public void testTooLong_PartialVersion() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 5,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path TooLong",
				HttpStatus.SC_BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates complete version too long.
	 */
	public void testTooLong_CompleteVersion() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 5,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path TooLong\n",
				HttpStatus.SC_BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates partial header name too long.
	 */
	public void testTooLong_PartialHeaderName() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName",
				HttpStatus.SC_BAD_REQUEST, "Header name too long");
	}

	/**
	 * Validates complete header name too long.
	 */
	public void testTooLong_CompleteHeaderName() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName:",
				HttpStatus.SC_BAD_REQUEST, "Header name too long");
	}

	/**
	 * Validates partial header value too long.
	 */
	public void testTooLong_PartialHeaderValue() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8,
				MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest(
				"GET /path HTTP/1.1\nName: HeaderValueTooLong",
				HttpStatus.SC_BAD_REQUEST, "Header value too long");
	}

	/**
	 * Validates complete header value too long.
	 */
	public void testTooLong_CompleteHeaderValue() {
		this.httpRequestParser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8,
				MAX_ENTITY_LENGTH);
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
	 * Ensures fails if entity is bigger than maximum size.
	 */
	public void testTooLong_Entity() {
		// Create entity that is too large
		long tooLargeEntitySize = MAX_ENTITY_LENGTH + 1;
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: "
				+ tooLargeEntitySize + "\n\n",
				HttpStatus.SC_REQUEST_ENTITY_TOO_LARGE,
				"Request entity must be less than maximum of "
						+ MAX_ENTITY_LENGTH + " bytes");
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
				true, -1, "POST", "/one", "HTTP/1.1", "TEST", "Content-Length",
				"4", "HeaderOne", "ValueOne");

		// Reset and parse second request
		this.httpRequestParser.reset();
		this.doMethodTest(
				"PUT /two HTTP/1.0\nContent-Length: 7\nHeaderTwo: ValueTwo\n\nANOTHER",
				true, -1, "PUT", "/two", "HTTP/1.0", "ANOTHER",
				"Content-Length", "7", "HeaderTwo", "ValueTwo");
	}

	/**
	 * Ensure <code>%HH</code> is not translated. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately.
	 */
	public void testPercentageEscape() {
		this.doMethodTest("GET /space%20byte HTTP/1.1\n\n", true, -1, "GET",
				"/space%20byte", "HTTP/1.1", "");
	}

	/**
	 * Allow invalid value for <code>%HH</code>. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately. Plus
	 * if not using URL then do not raise issue unnecessarily.
	 */
	public void testPercentageInvalidValue() {
		this.doMethodTest("GET /invalid%WRONG HTTP/1.1\n\n", true, -1, "GET",
				"/invalid%WRONG", "HTTP/1.1", "");
	}

	/**
	 * Validate possible values for <code>%HH</code> values are not translated.
	 * Necessary as '&amp;' should not yet be translated as causes issues in
	 * parsing out parameter values.
	 */
	public void testAllEscapedValues() {

		// Validate transforms
		assertEquals("Ensure 1 transforms", "1", this.getCharacterValue(1));
		assertEquals("Ensure B transforms", "B", this.getCharacterValue(0xB));

		// Validate the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Obtain the characters
				String high = this.getCharacterValue(highBits);
				String low = this.getCharacterValue(lowBits);
				String escapedCharacter = "%" + high + low;

				// Do not run for control characters
				byte value = (byte) ((highBits << 4) | lowBits);
				if ((value <= 31) || (value == 127)) {
					continue; // control character
				}

				// Validate not parse escaped character
				this.doMethodTest("GET /" + escapedCharacter + " HTTP/1.1\n\n",
						true, -1, "GET", "/" + escapedCharacter, "HTTP/1.1", "");
				this.httpRequestParser.reset();
			}
		}
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
	 * Creates the data to parse.
	 * 
	 * @param httpRequest
	 *            HTTP request content.
	 * @param isRemoveCR
	 *            Flag indicating to remove the CR (typically before the LF).
	 *            This is to allow more tolerant handling of requests.
	 * @return Data to parse.
	 */
	private byte[] createDataToParse(String httpRequest, boolean isRemoveCR)
			throws IOException {

		// Create buffer stream with content
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

		// Return the data
		return content;
	}

	/**
	 * Validates the {@link HttpRequestParser} state.
	 * 
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedPath
	 *            Expected path.
	 * @param expectedVersion
	 *            Expected version.
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void validateHttpRequestParserState(String expectedMethod,
			String expectedPath, String expectedVersion, String expectedEntity,
			String... expectedHeaderNameValues) throws IOException {

		// Validate request line
		assertEquals((expectedMethod == null ? "" : expectedMethod),
				this.httpRequestParser.getMethod());
		assertEquals((expectedPath == null ? "" : expectedPath),
				this.httpRequestParser.getRequestURI());
		assertEquals((expectedVersion == null ? "" : expectedVersion),
				this.httpRequestParser.getHttpVersion());

		// Validate correct number of headers
		assertEquals("Incorrect number of headers",
				(expectedHeaderNameValues.length / 2), this.httpRequestParser
						.getHeaders().size());

		// Validate correct headers
		for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
			String expectedHeaderName = expectedHeaderNameValues[i];
			String expectedHeaderValue = expectedHeaderNameValues[i + 1];
			int headerIndex = i / 2;
			HttpHeader header = this.httpRequestParser.getHeaders().get(
					headerIndex);
			assertEquals("Incorrect name for header (" + headerIndex + ")",
					expectedHeaderName, header.getName());
			assertEquals("Incorrect value for header '" + expectedHeaderName
					+ "' (" + headerIndex + ")", expectedHeaderValue,
					header.getValue());
		}

		// Validate the entity
		InputStream entityStream = this.httpRequestParser.getEntity()
				.getInputStream();
		if (expectedEntity == null) {
			// Should be no entity content available
			assertEquals("Should be no entity content available", 0,
					entityStream.available());

		} else {
			// Validate the entity content
			long available = entityStream.available();
			byte[] entity = new byte[(int) (available < 0 ? 0 : available)];
			int entitySize = entityStream.read(entity);
			entity = Arrays.copyOfRange(entity, 0, (entitySize < 0 ? 0
					: entitySize));
			UsAsciiUtil
					.assertEquals("Incorrect entity", expectedEntity, entity);
		}
	}

	/**
	 * Does a valid HTTP request test.
	 * 
	 * @param httpRequest
	 *            HTTP request content.
	 * @param expectedIsComplete
	 *            Flag indicating if expecting HTTP request to be complete.
	 * @param expectedNextByteToParseIndex
	 *            Expected index of next byte to parse.
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedPath
	 *            Expected path.
	 * @param expectedVersion
	 *            Expected version.
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(String httpRequest, boolean expectedIsComplete,
			int expectedNextByteToParseIndex, String expectedMethod,
			String expectedPath, String expectedVersion, String expectedEntity,
			String... expectedHeaderNameValues) {
		this.doMethodTest(false, httpRequest, expectedIsComplete,
				expectedNextByteToParseIndex, expectedMethod, expectedPath,
				expectedVersion, expectedEntity, expectedHeaderNameValues);
	}

	/**
	 * Does a valid HTTP request test.
	 * 
	 * @param isRemoveCR
	 *            Flag indicating to remove the CR (typically before the LF).
	 *            This is to allow more tolerant handling of requests.
	 * @param httpRequest
	 *            HTTP request content.
	 * @param expectedIsComplete
	 *            Flag indicating if expecting HTTP request to be complete.
	 * @param expectedNextByteToParseIndex
	 *            Expected index of next byte to parse.
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedPath
	 *            Expected path.
	 * @param expectedVersion
	 *            Expected version.
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(boolean isRemoveCR, String httpRequest,
			boolean expectedIsComplete, int expectedNextByteToParseIndex,
			String expectedMethod, String expectedPath, String expectedVersion,
			String expectedEntity, String... expectedHeaderNameValues) {
		try {

			// Create data to parse
			byte[] data = this.createDataToParse(httpRequest, isRemoveCR);

			// Parse the content
			boolean isComplete = this.httpRequestParser.parse(data, 0);

			// Validate the parse state
			this.validateHttpRequestParserState(expectedMethod, expectedPath,
					expectedVersion, expectedEntity, expectedHeaderNameValues);

			// Validate if complete
			assertEquals("Incorrect completion", expectedIsComplete, isComplete);

			// Ensure no further content for entity if complete
			if (isComplete) {
				// Note, validate above should have consumed the entire entity
				assertEquals("Should be no further content for entity", -1,
						this.httpRequestParser.getEntity().getInputStream()
								.read());
			}

			// Ensure correctly indicates the next byte to parse index
			assertEquals("Incorrect next byte to parse index",
					expectedNextByteToParseIndex,
					this.httpRequestParser.nextByteToParseIndex());

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

		// Should not be able parse invalid method
		try {

			// Parse the content
			this.httpRequestParser.parse(content, 0);

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