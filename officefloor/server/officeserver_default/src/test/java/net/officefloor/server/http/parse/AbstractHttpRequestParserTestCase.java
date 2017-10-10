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

import java.io.IOException;
import java.util.Iterator;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.HttpVersion.HttpVersionEnum;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Tests the {@link HttpRequestParser}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpRequestParserTestCase extends OfficeFrameTestCase {

	/**
	 * <p>
	 * Test specific undertaking of parsing the {@link HttpRequest}.
	 * <p>
	 * This allows child implementations to run the full range of tests by
	 * different feeds of the data in {@link StreamBuffer} instances.
	 * 
	 * @param parser
	 *            {@link HttpRequestParser} to use to parse the
	 *            {@link HttpRequest}.
	 * @param request
	 *            {@link HttpRequest} bytes to parse.
	 * @return Result of parsing the bytes.
	 * @throws HttpException
	 *             If fails to parse the {@link HttpRequest}.
	 */
	protected abstract boolean parse(HttpRequestParser parser, byte[] request) throws HttpException;

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
	private HttpRequestParser parser = new HttpRequestParser(
			new HttpRequestParserMetaData(MAX_HEADER_COUNT, MAX_TEXT_LENGTH, MAX_ENTITY_LENGTH));

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
	 * Ensure able to handle simple request
	 */
	public void testSimpleRequest() {
		this.doMethodTest("GET / HTTP/1.1\n\n", HttpMethod.GET, "/", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Ensure able to handle simple request
	 */
	public void testSimpleFullRequest() {
		this.doMethodTest("POST / HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/", HttpVersion.HTTP_1_1,
				"TEST", "Content-Length", "4");
	}

	/**
	 * Ensure able to handle empty request.
	 */
	public void testEmpty() {
		this.doMethodTest("", null, null, null, null);
	}

	/**
	 * Ensure invalid if leading spaces.
	 */
	public void testLeadingSpacesInvalid() {
		this.doInvalidMethodTest(" GET /test", HttpStatus.BAD_REQUEST, "Leading spaces for request invalid");
	}

	/**
	 * Ensure able to handle a blank request (only spaces received for it).
	 */
	public void testRobustness_SeparatingCRLF() {
		this.doMethodTest("\nGET /test HTTP/1.1\n\n", HttpMethod.GET, "/test", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * GET.
	 */
	public void testGetMethod() {
		this.doMethodTest("GET /test", HttpMethod.GET, null, null, null);
	}

	/**
	 * POST.
	 */
	public void testPostMethod() {
		this.doMethodTest("POST /test", HttpMethod.POST, null, null, null);
	}

	/**
	 * PUT.
	 */
	public void testPutMethod() {
		this.doMethodTest("PUT /test", HttpMethod.PUT, null, null, null);
	}

	/**
	 * DELETE.
	 */
	public void testDeleteMethod() {
		this.doMethodTest("DELETE /test", HttpMethod.DELETE, null, null, null);
	}

	/**
	 * CONNECT.
	 */
	public void testConnectMethod() {
		this.doMethodTest("CONNECT /test", HttpMethod.CONNECT, null, null, null);
	}

	/**
	 * HEAD.
	 */
	public void testHeadMethod() {
		this.doMethodTest("HEAD /test", HttpMethod.HEAD, null, null, null);
	}

	/**
	 * OPTIONS.
	 */
	public void testOptionsMethod() {
		this.doMethodTest("OPTIONS /test", HttpMethod.OPTIONS, null, null, null);
	}

	/**
	 * Custom method.
	 */
	public void testCustomMethod() {
		this.doMethodTest("custom /test", new HttpMethod("custom"), null, null, null);
	}

	/**
	 * Ensure able to parse up to just the request URI.
	 */
	public void testToRequestUri() {
		this.doMethodTest("GET /uri ", HttpMethod.GET, "/uri", null, null);
	}

	/**
	 * HTTP/1.0.
	 */
	public void testVersion_1_0() {
		this.doMethodTest("GET /path HTTP/1.0\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_0, null);
	}

	/**
	 * HTTP/1.1.
	 */
	public void testVersion_1_1() {
		this.doMethodTest("GET /path HTTP/1.1\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
	}

	/**
	 * Custom version.
	 */
	public void testCustomVersion() {
		this.doMethodTest("GET /path custom\n", HttpMethod.GET, "/path", new HttpVersion("custom"), null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderName() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length:", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
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
		this.doMethodTest("GET /path HTTP/1.1\nHeader\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	public void testToHeaderValue() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\nHost", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "10");
	}

	/**
	 * Ensure able to parse up to the entity.
	 */
	public void testToEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "1000");
	}

	/**
	 * Ensure able to handle first header with leading space gracefully.
	 */
	public void testLeadingSpaceToHeader() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n \t WhiteSpaceBeforeHeader: value\n", HttpStatus.BAD_REQUEST,
				"White spacing before HTTP header name");
	}

	/**
	 * Ensure able to have header value on multiple lines.
	 */
	public void testMultiplelineHeaderValue() {
		// As of RFC-7230, multi-line requests have been deprecated
		this.doInvalidMethodTest("GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n", HttpStatus.BAD_REQUEST,
				"White spacing before HTTP header name");
	}

	/**
	 * Validate GET.
	 */
	public void testGet() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate GET with one parameter.
	 */
	public void testGetWithOneParamter() {
		this.doMethodTest("GET /path?param=value HTTP/1.1\n\n", HttpMethod.GET, "/path?param=value",
				HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate GET with two parameters.
	 */
	public void testGetWithTwoParamters() {
		this.doMethodTest("GET /path?paramOne=valueOne&paramOne=valueTwo HTTP/1.1\n\n", HttpMethod.GET,
				"/path?paramOne=valueOne&paramOne=valueTwo", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate headers.
	 */
	public void testHeaders_WithValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", "Header1", "Value1", "Header2", "Value2");
	}

	/**
	 * Ensure able to have blank value for header.
	 */
	public void testHeaders_BlankValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1:\nHeader2: \t \n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", "Header1", "", "Header2", "");
	}

	/**
	 * Validate GET with entity.
	 */
	public void testGetWithEntity() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST.
	 */
	public void testPost() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST with no entity.
	 */
	public void testPostWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", HttpMethod.POST, "/path", HttpVersion.HTTP_1_1,
				"", "Content-Length", "0");
	}

	/**
	 * Validated POST with not all of the entity received.
	 */
	public void testPostWithNotAllOfEntityReceived() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\n\n12345", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "10");
	}

	/**
	 * Validates partial method being too long.
	 */
	public void testTooLong_PartialMethod() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 1, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("TooLarge", HttpStatus.BAD_REQUEST, "Method too long");
	}

	/**
	 * Validates partial request URI being too long.
	 */
	public void testTooLong_PartialRequestURI() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 3, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /TooLong", HttpStatus.REQUEST_URI_TOO_LARGE, "Request-URI Too Large");
	}

	/**
	 * Validates partial version too long.
	 */
	public void testTooLong_PartialVersion() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 6, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path TooLong", HttpStatus.BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates partial header name too long.
	 */
	public void testTooLong_PartialHeaderName() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 9, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName", HttpStatus.BAD_REQUEST,
				"Header name too long");
	}

	/**
	 * Validates partial header value too long.
	 */
	public void testTooLong_PartialHeaderValue() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 9, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path HTTP/1.1\nName: HeaderValueTooLong", HttpStatus.BAD_REQUEST,
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
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4", "HeaderOne", "ValueOne");

		// Parse second request
		this.doMethodTest("PUT /two HTTP/1.0\nContent-Length: 7\nHeaderTwo: ValueTwo\n\nANOTHER", HttpMethod.PUT,
				"/two", HttpVersion.HTTP_1_0, "ANOTHER", "Content-Length", "7", "HeaderTwo", "ValueTwo");
	}

	/**
	 * Ensure <code>%HH</code> is not translated. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately.
	 */
	public void testPercentageEscape() {
		this.doMethodTest("GET /space%20byte HTTP/1.1\n\n", HttpMethod.GET, "/space%20byte", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Allow invalid value for <code>%HH</code>. This is left to later
	 * translation as need to distinguish '&amp;' characters appropriately. Plus
	 * if not using URL then do not raise issue unnecessarily.
	 */
	public void testPercentageInvalidValue() throws HttpException {
		this.doMethodTest("GET /invalid%WRONG HTTP/1.1\n\n", HttpMethod.GET, "/invalid%WRONG", HttpVersion.HTTP_1_1,
				"");
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

				// Obtain the encoded characters
				String high = this.getCharacterValue(highBits);
				String low = this.getCharacterValue(lowBits);
				String encodedCharacters = "%" + high + low;

				// Do not run for control characters
				byte value = (byte) ((highBits << 4) | lowBits);
				if ((value <= 31) || (value == 127)) {
					continue; // control character
				}

				// Validate not parse escaped character
				this.doMethodTest("GET /" + encodedCharacters + " HTTP/1.1\n\n", HttpMethod.GET,
						"/" + encodedCharacters, HttpVersion.HTTP_1_1, "");
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
			assertNull("Request URI not yet parsed", this.parser.getRequestURI());
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
				assertTrue("Should have header" + msgSuffix, headers.hasNext());
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
		if ((expectedEntity == null)) {
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
	 * @param expectedHeaderNameValues
	 *            Expected listing of header name/values.
	 */
	private void doMethodTest(String httpRequest, HttpMethod expectedMethod, String expectedPath,
			HttpVersion expectedVersion, String expectedEntity, String... expectedHeaderNameValues) {
		try {

			// Create data to parse
			byte[] data = UsAsciiUtil.convertToHttp(httpRequest);

			// Parse the content
			boolean isComplete = this.parse(this.parser, data);

			// Validate the parse state
			this.validateHttpRequestParserState(expectedMethod, expectedPath, expectedVersion, expectedEntity,
					expectedHeaderNameValues);

			// If have non-null expected Entity (then should be complete)
			assertEquals("Incorrect parse result", (expectedEntity != null), isComplete);

		} catch (HttpException ex) {
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

		// Should not be able parse invalid method
		try {

			// Parse the content
			this.parse(this.parser, content);

			// Should not be parsed
			fail("Should not parse invalid HTTP request:\n" + invalidHttpRequest);

		} catch (HttpException ex) {
			// Validate details of parse failure
			assertEquals("Incorrect http status", expectedHttpStatus, ex.getHttpStatus());
			assertEquals("Incorrect parse failure reason", expectedParseFailureReason, ex.getMessage());
		}
	}

}