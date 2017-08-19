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
import java.nio.ByteBuffer;
import java.util.Iterator;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.HttpVersion.HttpVersionEnum;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.server.stream.impl.ByteSequence;

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
	private HttpRequestParser parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, MAX_TEXT_LENGTH, MAX_ENTITY_LENGTH);

	/**
	 * Ensure correct initial state.
	 */
	public void testInitialState() throws IOException {
		assertNull("Incorrect initial method", this.parser.getMethod());
		assertNull("Incorrect initial request URI", this.parser.getRequestURI());
		assertNull("Incorrect initial HTTP version", this.parser.getVersion());
		assertNull("Incorrect initial HTTP headers", this.parser.getHeaders());
		assertNull("Initially should be no entity data", this.parser.getEntity());
	}

	/**
	 * Ensure able to handle empty request.
	 */
	public void testEmpty() {
		this.doMethodTest("", null, null, null, null, false);
	}

	/**
	 * Ensure able to handle a blank request (only spaces received for it).
	 */
	public void testBlank() {
		this.doMethodTest(" ", null, null, null, null, false);
	}

	/**
	 * Ensure able to handle leading spaces.
	 */
	public void testLeadingSpaces() {
		this.doMethodTest(" \n GET ", HttpMethod.GET, null, null, null, false);
	}

	/**
	 * GET.
	 */
	public void testGetMethod() {
		this.doMethodTest("GET /test", HttpMethod.GET, null, null, null, false);
	}

	/**
	 * POST.
	 */
	public void testPostMethod() {
		this.doMethodTest("POST ", HttpMethod.POST, null, null, null, false);
	}

	/**
	 * PUT.
	 */
	public void testPutMethod() {
		this.doMethodTest("PUT ", HttpMethod.PUT, null, null, null, false);
	}

	/**
	 * DELETE.
	 */
	public void testDeleteMethod() {
		this.doMethodTest("DELETE ", HttpMethod.DELETE, null, null, null, false);
	}

	/**
	 * CONNECT.
	 */
	public void testConnectMethod() {
		this.doMethodTest("CONNECT ", HttpMethod.CONNECT, null, null, null, false);
	}

	/**
	 * HEAD.
	 */
	public void testHeadMethod() {
		this.doMethodTest("HEAD ", HttpMethod.HEAD, null, null, null, false);
	}

	/**
	 * OPTIONS.
	 */
	public void testOptionsMethod() {
		this.doMethodTest("OPTIONS ", HttpMethod.OPTIONS, null, null, null, false);
	}

	/**
	 * Custom method.
	 */
	public void testCustomMethod() {
		this.doMethodTest("custom ", new HttpMethod("custom"), null, null, null, false);
	}

	/**
	 * Ensure able to parse up to just the path.
	 */
	public void testToPath() {
		this.doMethodTest("GET /path ", HttpMethod.GET, "/path", null, null, false);
	}

	/**
	 * HTTP/1.0.
	 */
	public void testVersion_1_0() {
		this.doMethodTest("GET /path HTTP/1.0\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_0, null, false);
	}

	/**
	 * HTTP/1.1.
	 */
	public void testVersion_1_1() {
		this.doMethodTest("GET /path HTTP/1.1\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null, false);
	}

	/**
	 * Custom version.
	 */
	public void testCustomVersion() {
		this.doMethodTest("GET /path custom\n", HttpMethod.GET, "/path", new HttpVersion("custom"), null, false);
	}

	/**
	 * Ensure tolerance if request line contains LF without preceding CR.
	 */
	public void testToVersionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null, false);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderName() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length:", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null,
				false);
	}

	/**
	 * Ensure issue if missing header name.
	 */
	public void testMissingHeaderName() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n: value\n", HttpStatus.BAD_REQUEST, "Missing header name");
	}

	/**
	 * Ensure able to delimit header name via CR.
	 */
	public void testToHeaderNameNoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null, false,
				"Header", "");
	}

	/**
	 * Ensure able to delimit header name via LF.
	 */
	public void testToHeaderNameNoValueMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\nHeader\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null,
				false, "Header", "");
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderValue() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\nHost", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, false, "Content-Length", "10");
	}

	/**
	 * Ensure tolerance if header contains LF without preceding CR.
	 */
	public void testHeaderMissingCR() {
		this.doMethodTest(true, "POST /path HTTP/1.1\nContent-Length: 10\nHost", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, false, "Content-Length", "10");
	}

	/**
	 * Ensure able to parse up to the entity.
	 */
	public void testToEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "NOT ALL CONTENT", false, "Content-Length", "1000");
	}

	/**
	 * Ensure tolerance if header separation from entity contains LF without
	 * preceding CR.
	 */
	public void testToEntityMissingCR() {
		this.doMethodTest(true, "POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT", HttpMethod.POST,
				"/path", HttpVersion.HTTP_1_1, "NOT ALL CONTENT", false, "Content-Length", "1000");
	}

	/**
	 * Ensure multiple parsing for completing the entity content.
	 */
	public void testPartialEntityThenComplete() throws Exception {

		// Provide only partial entity (typically if network packet is split)
		byte[] firstPacket = UsAsciiUtil.convertToUsAscii("POST /path HTTP/1.1\nContent-Length: 4\n\nTE");
		ByteBuffer firstBuffer = ByteBuffer.wrap(firstPacket);
		firstBuffer.flip(); // mimic writing
		boolean isCompleteOnFirstPacket = this.parser.parse(firstBuffer);
		assertFalse("Should not complete if partial entity", isCompleteOnFirstPacket);
		assertTrue("Should have consumed all bytes", this.parser.isFinishedParsingBuffer());

		// Provide remaining of entity (with some data of next HTTP request)
		byte[] secondPacket = UsAsciiUtil.convertToUsAscii("ST more");
		ByteBuffer secondBuffer = ByteBuffer.wrap(secondPacket);
		secondBuffer.flip(); // mimic writing
		boolean isCompleteOnSecondPacket = this.parser.parse(secondBuffer);
		assertTrue("Should be complete with remaining of entity received", isCompleteOnSecondPacket);
		assertFalse("Should have further bytes remaining", this.parser.isFinishedParsingBuffer());

		// Validate the parsing
		this.validateHttpRequestParserState(HttpMethod.POST, "/path", HttpVersion.HTTP_1_1, "TEST", "Content-Length",
				"4");
	}

	/**
	 * Ensure able to handle first header with leading space gracefully.
	 */
	public void testLeadingSpaceToHeader() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n \t WhiteSpaceBeforeHeader: value\n", HttpStatus.BAD_REQUEST,
				"White spacing before first HTTP header");
	}

	/**
	 * Ensure able to have header value on multiple lines.
	 */
	public void testMultiplelineHeaderValue() {
		this.doMethodTest("GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", true, "Multiline", "Value One Value Two");
	}

	/**
	 * Ensure able to have header value on multiple lines with missing CR.
	 */
	public void testMultiplelineHeaderValueMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", true, "Multiline", "Value One Value Two");
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client.
	 */
	public void testHeaderToEntityHavingWhitespacing() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4");
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client. Also tolerant of
	 * missing CR.
	 */
	public void testHeaderToEntityHavingWhitespacingMissingCR() {
		this.doMethodTest(true, "POST /path HTTP/1.1\nContent-Length: 4\n \t\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4");
	}

	/**
	 * Ensure able to parse header separation containing white spacing to no
	 * entity. This is to be more tolerant of the client.
	 */
	public void testWhitespacingInGetCompletion() {
		this.doMethodTest("GET /path HTTP/1.1\n \t\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Ensure able to parse header and entity separation containing white
	 * spacing. This is to be more tolerant of the client. Also tolerant of
	 * missing CR.
	 */
	public void testWhitespacingInGetCompletionMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n \t\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Validate GET.
	 */
	public void testGet() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Ensure tolerance if GET request is missing CR before the LF.
	 */
	public void testGetMissingCR() {
		this.doMethodTest(true, "GET /path HTTP/1.1\n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Validate GET with one parameter.
	 */
	public void testGetWithOneParamter() {
		this.doMethodTest("GET /path?param=value HTTP/1.1\n\n", HttpMethod.GET, "/path?param=value",
				HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Validate GET with two parameters.
	 */
	public void testGetWithTwoParamters() {
		this.doMethodTest("GET /path?paramOne=valueOne&paramOne=valueTwo HTTP/1.1\n\n", HttpMethod.GET,
				"/path?paramOne=valueOne&paramOne=valueTwo", HttpVersion.HTTP_1_1, "", true);
	}

	/**
	 * Validate headers.
	 */
	public void testHeaders_WithValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", true, "Header1", "Value1", "Header2", "Value2");
	}

	/**
	 * Ensure able to have blank value for header.
	 */
	public void testHeaders_BlankValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1:\nHeader2: \n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1,
				"", true, "Header1", "", "Header2", "");
	}

	/**
	 * Ensure able to have no value for header.
	 */
	public void testHeaders_NoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1\nHeader2 \n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1,
				"", true, "Header1", "", "Header2", "");
	}

	/**
	 * Validate GET with entity.
	 */
	public void testGetWithEntity() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4");
	}

	/**
	 * Validate POST.
	 */
	public void testPost() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4");
	}

	/**
	 * Ensure tolerance if POST request is missing CR before the LF.
	 */
	public void testPostMissingCR() {
		this.doMethodTest(true, "POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4");
	}

	/**
	 * Validate POST with no entity.
	 */
	public void testPostWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", HttpMethod.POST, "/path", HttpVersion.HTTP_1_1,
				"", true, "Content-Length", "0");
	}

	/**
	 * Validated POST with not all of the entity received.
	 */
	public void testPostWithNotAllOfEntityReceived() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\n\n12345", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "12345", false, "Content-Length", "10");
	}

	/**
	 * Validates partial method being too long.
	 */
	public void testTooLong_PartialMethod() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 1, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("TooLarge", HttpStatus.BAD_REQUEST, "Method too long");
	}

	/**
	 * Validates complete method being too long.
	 */
	public void testTooLong_CompleteMethod() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 1, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("TooLarge ", HttpStatus.BAD_REQUEST, "Method too long");
	}

	/**
	 * Validates partial request URI being too long.
	 */
	public void testTooLong_PartialRequestURI() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 3, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /TooLong", HttpStatus.REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates complete request URI being too long.
	 */
	public void testTooLong_CompleteRequestURI() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 3, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /TooLong ", HttpStatus.REQUEST_URI_TOO_LARGE, "Request-URI Too Long");
	}

	/**
	 * Validates partial version too long.
	 */
	public void testTooLong_PartialVersion() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 5, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path TooLong", HttpStatus.BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates complete version too long.
	 */
	public void testTooLong_CompleteVersion() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 5, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path TooLong\n", HttpStatus.BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates partial header name too long.
	 */
	public void testTooLong_PartialHeaderName() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName", HttpStatus.BAD_REQUEST,
				"Header name too long");
	}

	/**
	 * Validates complete header name too long.
	 */
	public void testTooLong_CompleteHeaderName() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName:", HttpStatus.BAD_REQUEST,
				"Header name too long");
	}

	/**
	 * Validates partial header value too long.
	 */
	public void testTooLong_PartialHeaderValue() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nName: HeaderValueTooLong", HttpStatus.BAD_REQUEST,
				"Header value too long");
	}

	/**
	 * Validates complete header value too long.
	 */
	public void testTooLong_CompleteHeaderValue() {
		this.parser = new HttpRequestParserImpl(MAX_HEADER_COUNT, 8, MAX_ENTITY_LENGTH);
		this.doInvalidMethodTest("GET /path HTTP/1.1\nName: HeaderValueTooLong\n", HttpStatus.BAD_REQUEST,
				"Header value too long");
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
		this.doInvalidMethodTest(request.toString(), HttpStatus.BAD_REQUEST, "Too Many Headers");
	}

	/**
	 * Ensures fails if entity is bigger than maximum size.
	 */
	public void testTooLong_Entity() {
		// Create entity that is too large
		long tooLargeEntitySize = MAX_ENTITY_LENGTH + 1;
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: " + tooLargeEntitySize + "\n\n",
				HttpStatus.REQUEST_ENTITY_TOO_LARGE,
				"Request entity must be less than maximum of " + MAX_ENTITY_LENGTH + " bytes");
	}

	/**
	 * Validates that Content-Length is required for POST.
	 */
	public void testNoContentLengthForPost() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Must provide Content-Length header for POST");
	}

	/**
	 * Validates that Content-Length is required for PUT.
	 */
	public void testNoContentLengthForPut() {
		this.doInvalidMethodTest("PUT /path HTTP/1.1\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Must provide Content-Length header for PUT");
	}

	/**
	 * Ensures the Content-Length contains a value.
	 */
	public void testBlankContentLengthValue() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length:\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensures the Content-Length is an integer.
	 */
	public void testNonIntegerContentLengthValue() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: INVALID\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensure able to reset {@link HttpRequestParser} to parse another request.
	 */
	public void testReset() {
		// Parse first request
		this.doMethodTest("POST /one HTTP/1.1\nContent-Length: 4\nHeaderOne: ValueOne\n\nTEST", HttpMethod.POST, "/one",
				HttpVersion.HTTP_1_1, "TEST", true, "Content-Length", "4", "HeaderOne", "ValueOne");

		// Reset and parse second request
		this.parser.reset();
		this.doMethodTest("PUT /two HTTP/1.0\nContent-Length: 7\nHeaderTwo: ValueTwo\n\nANOTHER", HttpMethod.PUT,
				"/two", HttpVersion.HTTP_1_1, "ANOTHER", true, "Content-Length", "7", "HeaderTwo", "ValueTwo");
	}

	/**
	 * Ensure <code>%HH</code> is not translated. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately.
	 */
	public void testPercentageEscape() {
		this.doMethodTest("GET /space%20byte HTTP/1.1\n\n", HttpMethod.GET, "/space%20byte", HttpVersion.HTTP_1_1, "",
				true);
	}

	/**
	 * Allow invalid value for <code>%HH</code>. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately. Plus
	 * if not using URL then do not raise issue unnecessarily.
	 */
	public void testPercentageInvalidValue() {
		this.doMethodTest("GET /invalid%WRONG HTTP/1.1\n\n", HttpMethod.GET, "/invalid%WRONG", HttpVersion.HTTP_1_1, "",
				true);
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
				this.doMethodTest("GET /" + escapedCharacter + " HTTP/1.1\n\n", HttpMethod.GET, "/" + escapedCharacter,
						HttpVersion.HTTP_1_1, "", true);
				this.parser.reset();
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
			throw new IllegalArgumentException("Invalid hexidecimal value " + hexidecimal);
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
	private byte[] createDataToParse(String httpRequest, boolean isRemoveCR) throws IOException {

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
	 *            Expected {@link HttpMethod}.
	 * @param expectedPath
	 *            Expected path.
	 * @param expectedVersion
	 *            Expected {@link HttpVersion}.
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void validateHttpRequestParserState(HttpMethod expectedMethod, String expectedPath,
			HttpVersion expectedVersion, String expectedEntity, String... expectedHeaderNameValues) throws IOException {

		// Validate method
		if (expectedMethod == null) {
			// Should not have supplier
			assertNull("HTTP method not yet parsed", this.parser.getMethod());
		} else if (expectedMethod.getEnum() != HttpMethodEnum.OTHER) {
			// Known method, so should re-use object
			assertSame("Should be same HTTP method", expectedMethod, this.parser.getMethod().get());
		} else {
			// Other type of method, so ensure equals
			assertEquals("Should be equal HTTP method", expectedMethod, this.parser.getMethod().get());
		}

		// Validate the request URI
		if (expectedPath == null) {
			// Should not have request URI
			assertNull("Reqeust URI not yet parsed", this.parser.getRequestURI());
		} else {
			assertEquals("Incorrect request URI", expectedPath, this.parser.getRequestURI().get());
		}

		// Validate version
		if (expectedVersion == null) {
			// Should not have supplier
			assertNull("HTTP version not yet parsed", this.parser.getVersion());
		} else if (expectedVersion.getEnum() != HttpVersionEnum.OTHER) {
			// Known version, so should re-use object
			assertSame("Should be same HTTP version", expectedVersion, this.parser.getVersion());
		} else {
			// Other version, so ensure equals
			assertEquals("Should be equal HTTP version", expectedVersion, this.parser.getVersion());
		}

		// Validate correct number of headers
		if (expectedHeaderNameValues.length == 0) {
			// Should be no headers
			NonMaterialisedHttpHeaders headers = this.parser.getHeaders();
			assertTrue("Should not have headers", (headers == null) || (headers.length() == 0));

		} else {
			// Validate the headers
			assertEquals("Incorrect number of headers", (expectedHeaderNameValues.length / 2),
					this.parser.getHeaders().length());

			// Validate correct headers
			Iterator<NonMaterialisedHttpHeader> headers = this.parser.getHeaders().iterator();
			for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
				String expectedHeaderName = expectedHeaderNameValues[i];
				String expectedHeaderValue = expectedHeaderNameValues[i + 1];
				int headerIndex = i / 2;

				// Useful suffix to identify incorrect header
				String msgSuffix = " (" + expectedHeaderName + ": " + expectedHeaderValue + " [" + headerIndex + "])";

				// Obtain the non materialised header
				assertEquals("Should have header" + msgSuffix, headers.hasNext());
				NonMaterialisedHttpHeader header = headers.next();

				// Ensure correct name
				CharSequence nameSequence = header.getName();
				assertEquals("Incorrect header name length" + msgSuffix, expectedHeaderName.length(),
						nameSequence.length());
				for (int charIndex = 0; charIndex < expectedHeaderName.length(); charIndex++) {
					assertEquals("Incorrect character " + charIndex + msgSuffix, expectedHeaderName.charAt(charIndex),
							nameSequence.charAt(charIndex));
				}

				// Materialise the header (and ensure correct name/value)
				HttpHeader materialisedHeader = header.materialiseHttpHeader();
				assertEquals("Incorrect materialised name" + msgSuffix, expectedHeaderName,
						materialisedHeader.getName());
				assertEquals("Incorrect materialised value" + msgSuffix, expectedHeaderValue,
						materialisedHeader.getValue());
			}
		}

		// Validate the entity
		ByteSequence entityData = this.parser.getEntity();
		if (expectedEntity == null) {
			// Should be no entity content available
			assertNull("Should be no entity, as not yet parsed", entityData);

		} else {
			// Validate the entity content
			byte[] entity = new byte[entityData.length()];
			for (int i = 0; i < entityData.length(); i++) {
				entity[i] = entityData.byteAt(i);
			}
			UsAsciiUtil.assertEquals("Incorrect entity", expectedEntity, entity);
		}
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
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedCompleteReadingBuffer
	 *            Indicates if expected completion of reading the buffer.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(String httpRequest, HttpMethod expectedMethod, String expectedPath,
			HttpVersion expectedVersion, String expectedEntity, boolean expectedCompleteReadingBuffer,
			String... expectedHeaderNameValues) {
		this.doMethodTest(false, httpRequest, expectedMethod, expectedPath, expectedVersion, expectedEntity,
				expectedCompleteReadingBuffer, expectedHeaderNameValues);
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
	 * @param expectedEntity
	 *            Expected entity.
	 * @param expectedCompleteReadingBuffer
	 *            Indicates if expected completion of reading the buffer.
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(boolean isRemoveCR, String httpRequest, HttpMethod expectedMethod, String expectedPath,
			HttpVersion expectedVersion, String expectedEntity, boolean expectedCompleteReadingBuffer,
			String... expectedHeaderNameValues) {
		try {

			// Create data to parse
			byte[] data = this.createDataToParse(httpRequest, isRemoveCR);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.position(buffer.capacity()); // to mimic just read in

			// Parse the content
			boolean isComplete = this.parser.parse(buffer);

			// Validate the parse state
			this.validateHttpRequestParserState(expectedMethod, expectedPath, expectedVersion, expectedEntity,
					expectedHeaderNameValues);

			// If have non-null expected Entity (then should be complete)
			assertEquals("Incorrect parse result", (expectedEntity != null), isComplete);

			// Ensure correctly indicates if completed with buffer
			assertEquals("Incorrect completion with buffer", expectedCompleteReadingBuffer,
					this.parser.isFinishedParsingBuffer());

		} catch (HttpRequestParseException ex) {
			// Parse exception not expected
			fail("Request should be parsed successfully [failure " + ex.getHttpStatus() + ": " + ex.getMessage() + "]");

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
	private void doInvalidMethodTest(String invalidHttpRequest, HttpStatus expectedHttpStatus,
			String expectedParseFailureReason) {

		// Create input buffer stream with content
		byte[] content = UsAsciiUtil.convertToHttp(invalidHttpRequest);
		ByteBuffer buffer = ByteBuffer.wrap(content);
		buffer.position(buffer.capacity()); // to mimic just read in data

		// Should not be able parse invalid method
		try {

			// Parse the content
			this.parser.parse(buffer);

			// Should not be parsed
			fail("Should not parse invalid HTTP request:\n" + invalidHttpRequest);

		} catch (HttpRequestParseException ex) {
			// Validate details of parse failure
			assertEquals("Incorrect http status", expectedHttpStatus, ex.getHttpStatus());
			assertEquals("Incorrect parse failure reason", expectedParseFailureReason, ex.getMessage());

		} catch (IOException ex) {
			fail("Should not have I/O failure: " + ex.getMessage());
		}
	}

}