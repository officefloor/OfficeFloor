/*-
 * #%L
 * Default OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.parse;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

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
import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Tests the {@link HttpRequestParser}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpRequestParserTestCase {

	/**
	 * {@link ServerMemoryOverloadHandler}.
	 */
	protected static final ServerMemoryOverloadHandler OVERLOAD_HANDLER = () -> fail("Server should not be overloaded");

	/**
	 * <p>
	 * Test specific undertaking of parsing the {@link HttpRequest}.
	 * <p>
	 * This allows child implementations to run the full range of tests by different
	 * feeds of the data in {@link StreamBuffer} instances.
	 * 
	 * @param parser  {@link HttpRequestParser} to use to parse the
	 *                {@link HttpRequest}.
	 * @param request {@link HttpRequest} bytes to parse.
	 * @return Result of parsing the bytes.
	 * @throws HttpException If fails to parse the {@link HttpRequest}.
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
	@Test
	public void initialState() throws IOException {
		assertNull(this.parser.getMethod(), "Incorrect initial method");
		assertNull(this.parser.getRequestURI(), "Incorrect initial request URI");
		assertNull(this.parser.getVersion(), "Incorrect initial HTTP version");
		assertNull(this.parser.getHeaders(), "Incorrect initial HTTP headers");
		assertNull(this.parser.getEntity(), "Initially should be no entity data");
	}

	/**
	 * Ensure able to handle simple request
	 */
	@Test
	public void simpleRequest() {
		this.doMethodTest("GET / HTTP/1.1\n\n", HttpMethod.GET, "/", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Ensure able to handle simple request
	 */
	@Test
	public void simpleFullRequest() {
		this.doMethodTest("POST / HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/", HttpVersion.HTTP_1_1,
				"TEST", "Content-Length", "4");
	}

	/**
	 * Ensure able to handle empty request.
	 */
	@Test
	public void empty() {
		this.doMethodTest("", null, null, null, null);
	}

	/**
	 * Ensure invalid if leading spaces.
	 */
	@Test
	public void leadingSpacesInvalid() {
		this.doInvalidMethodTest(" GET /test", HttpStatus.BAD_REQUEST, "Leading spaces for request invalid");
	}

	/**
	 * Ensure able to handle a blank request (only spaces received for it).
	 */
	@Test
	public void robustness_SeparatingCRLF() {
		this.doMethodTest("\nGET /test HTTP/1.1\n\n", HttpMethod.GET, "/test", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * GET.
	 */
	@Test
	public void getMethod() {
		this.doMethodTest("GET /test", HttpMethod.GET, null, null, null);
	}

	/**
	 * POST.
	 */
	@Test
	public void postMethod() {
		this.doMethodTest("POST /test", HttpMethod.POST, null, null, null);
	}

	/**
	 * PUT.
	 */
	@Test
	public void putMethod() {
		this.doMethodTest("PUT /test", HttpMethod.PUT, null, null, null);
	}

	/**
	 * DELETE.
	 */
	@Test
	public void deleteMethod() {
		this.doMethodTest("DELETE /test", HttpMethod.DELETE, null, null, null);
	}

	/**
	 * CONNECT.
	 */
	@Test
	public void connectMethod() {
		this.doMethodTest("CONNECT /test", HttpMethod.CONNECT, null, null, null);
	}

	/**
	 * HEAD.
	 */
	@Test
	public void headMethod() {
		this.doMethodTest("HEAD /test", HttpMethod.HEAD, null, null, null);
	}

	/**
	 * OPTIONS.
	 */
	@Test
	public void optionsMethod() {
		this.doMethodTest("OPTIONS /test", HttpMethod.OPTIONS, null, null, null);
	}

	/**
	 * Custom method.
	 */
	@Test
	public void customMethod() {
		this.doMethodTest("custom /test", new HttpMethod("custom"), null, null, null);
	}

	/**
	 * Ensure able to parse up to just the request URI.
	 */
	@Test
	public void toRequestUri() {
		this.doMethodTest("GET /uri ", HttpMethod.GET, "/uri", null, null);
	}

	/**
	 * HTTP/1.0.
	 */
	@Test
	public void version_1_0() {
		this.doMethodTest("GET /path HTTP/1.0\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_0, null);
	}

	/**
	 * HTTP/1.1.
	 */
	@Test
	public void version_1_1() {
		this.doMethodTest("GET /path HTTP/1.1\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
	}

	/**
	 * Custom version.
	 */
	@Test
	public void customVersion() {
		this.doMethodTest("GET /path custom\n", HttpMethod.GET, "/path", new HttpVersion("custom"), null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	@Test
	public void toHeaderName() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length:", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
	}

	/**
	 * Ensure issue if missing header name.
	 */
	@Test
	public void missingHeaderName() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n: value\n", HttpStatus.BAD_REQUEST, "Missing header name");
	}

	/**
	 * Ensure able to delimit header name via CR.
	 */
	@Test
	public void toHeaderNameNoValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, null);
	}

	/**
	 * Ensure able to parse up to just the header name.
	 */
	@Test
	public void toHeaderValue() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\nHost", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "10");
	}

	/**
	 * Ensure able to parse up to the entity.
	 */
	@Test
	public void toEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 1000\n\nNOT ALL CONTENT", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "1000");
	}

	/**
	 * Ensure able to handle first header with leading space gracefully.
	 */
	@Test
	public void leadingSpaceToHeader() {
		this.doInvalidMethodTest("GET /path HTTP/1.1\n \t WhiteSpaceBeforeHeader: value\n", HttpStatus.BAD_REQUEST,
				"White spacing before HTTP header name");
	}

	/**
	 * Ensure able to have header value on multiple lines.
	 */
	@Test
	public void multiplelineHeaderValue() {
		// As of RFC-7230, multi-line requests have been deprecated
		this.doInvalidMethodTest("GET /path HTTP/1.1\nMultiline: Value One\n Value Two\n\n", HttpStatus.BAD_REQUEST,
				"White spacing before HTTP header name");
	}

	/**
	 * Validate GET.
	 */
	@Test
	public void get() {
		this.doMethodTest("GET /path HTTP/1.1\n\n", HttpMethod.GET, "/path", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate GET with one parameter.
	 */
	@Test
	public void getWithOneParamter() {
		this.doMethodTest("GET /path?param=value HTTP/1.1\n\n", HttpMethod.GET, "/path?param=value",
				HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate GET with two parameters.
	 */
	@Test
	public void getWithTwoParamters() {
		this.doMethodTest("GET /path?paramOne=valueOne&paramOne=valueTwo HTTP/1.1\n\n", HttpMethod.GET,
				"/path?paramOne=valueOne&paramOne=valueTwo", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Validate headers.
	 */
	@Test
	public void headers_WithValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1: Value1\nHeader2: Value2\n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", "Header1", "Value1", "Header2", "Value2");
	}

	/**
	 * Ensure able to have blank value for header.
	 */
	@Test
	public void headers_BlankValue() {
		this.doMethodTest("GET /path HTTP/1.1\nHeader1:\nHeader2: \t \n\n", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "", "Header1", "", "Header2", "");
	}

	/**
	 * Validate GET with entity.
	 */
	@Test
	public void getWithEntity() {
		this.doMethodTest("GET /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.GET, "/path",
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST.
	 */
	@Test
	public void post() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 4\n\nTEST", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4");
	}

	/**
	 * Validate POST with no entity.
	 */
	@Test
	public void postWithNoEntity() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 0\n\n", HttpMethod.POST, "/path", HttpVersion.HTTP_1_1,
				"", "Content-Length", "0");
	}

	/**
	 * Validated POST with not all of the entity received.
	 */
	@Test
	public void postWithNotAllOfEntityReceived() {
		this.doMethodTest("POST /path HTTP/1.1\nContent-Length: 10\n\n12345", HttpMethod.POST, "/path",
				HttpVersion.HTTP_1_1, null, "Content-Length", "10");
	}

	/**
	 * Validates partial method being too long.
	 */
	@Test
	public void tooLong_PartialMethod() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 1, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("TooLarge", HttpStatus.BAD_REQUEST, "Method too long");
	}

	/**
	 * Validates partial request URI being too long.
	 */
	@Test
	public void tooLong_PartialRequestURI() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 3, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /TooLong", HttpStatus.REQUEST_URI_TOO_LARGE, "Request-URI Too Large");
	}

	/**
	 * Validates partial version too long.
	 */
	@Test
	public void tooLong_PartialVersion() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 6, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path TooLong", HttpStatus.BAD_REQUEST, "Version too long");
	}

	/**
	 * Validates partial header name too long.
	 */
	@Test
	public void tooLong_PartialHeaderName() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 9, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path HTTP/1.1\nTooLongHeaderName", HttpStatus.BAD_REQUEST,
				"Header name too long");
	}

	/**
	 * Validates partial header value too long.
	 */
	@Test
	public void tooLong_PartialHeaderValue() {
		this.parser = new HttpRequestParser(new HttpRequestParserMetaData(MAX_HEADER_COUNT, 9, MAX_ENTITY_LENGTH));
		this.doInvalidMethodTest("GET /path HTTP/1.1\nName: HeaderValueTooLong", HttpStatus.BAD_REQUEST,
				"Header value too long");
	}

	/**
	 * Ensure fails if too many {@link HttpHeader} instances.
	 */
	@Test
	public void tooMany_Headers() {
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
	@Test
	public void tooLong_Entity() {
		// Create entity that is too large
		long tooLargeEntitySize = MAX_ENTITY_LENGTH + 1;
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: " + tooLargeEntitySize + "\n\n",
				HttpStatus.REQUEST_ENTITY_TOO_LARGE,
				"Request entity must be less than maximum of " + MAX_ENTITY_LENGTH + " bytes");
	}

	/**
	 * Ensures the Content-Length contains a value.
	 */
	@Test
	public void blankContentLengthValue() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length:\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensures the Content-Length is an integer.
	 */
	@Test
	public void nonIntegerContentLengthValue() {
		this.doInvalidMethodTest("POST /path HTTP/1.1\nContent-Length: INVALID\n\nTEST", HttpStatus.LENGTH_REQUIRED,
				"Content-Length header value must be an integer");
	}

	/**
	 * Ensure able to reset {@link HttpRequestParser} to parse another request.
	 */
	@Test
	public void reset() {
		// Parse first request
		this.doMethodTest("POST /one HTTP/1.1\nContent-Length: 4\nHeaderOne: ValueOne\n\nTEST", HttpMethod.POST, "/one",
				HttpVersion.HTTP_1_1, "TEST", "Content-Length", "4", "HeaderOne", "ValueOne");

		// Parse second request
		this.doMethodTest("PUT /two HTTP/1.0\nContent-Length: 7\nHeaderTwo: ValueTwo\n\nANOTHER", HttpMethod.PUT,
				"/two", HttpVersion.HTTP_1_0, "ANOTHER", "Content-Length", "7", "HeaderTwo", "ValueTwo");
	}

	/**
	 * Ensure <code>%HH</code> is not translated. This is left to later translation
	 * as need to distinguish '&amp;' characters appropriately.
	 */
	@Test
	public void percentageEscape() {
		this.doMethodTest("GET /space%20byte HTTP/1.1\n\n", HttpMethod.GET, "/space%20byte", HttpVersion.HTTP_1_1, "");
	}

	/**
	 * Allow invalid value for <code>%HH</code>. This is left to later translation
	 * as need to distinguish '&amp;' characters appropriately. Plus if not using
	 * URL then do not raise issue unnecessarily.
	 */
	@Test
	public void percentageInvalidValue() throws HttpException {
		this.doMethodTest("GET /invalid%WRONG HTTP/1.1\n\n", HttpMethod.GET, "/invalid%WRONG", HttpVersion.HTTP_1_1,
				"");
	}

	/**
	 * Validate possible values for <code>%HH</code> values are not translated.
	 * Necessary as '&amp;' should not yet be translated as causes issues in parsing
	 * out parameter values.
	 */
	@Test
	public void allEscapedValues() {

		// Validate transforms
		assertEquals("1", this.getCharacterValue(1), "Ensure 1 transforms");
		assertEquals("B", this.getCharacterValue(0xB), "Ensure B transforms");

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
	 * @param hexidecimal Hexidecimal value.
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
	 * @param expectedMethod           Expected {@link HttpMethod}.
	 * @param expectedPath             Expected path.
	 * @param expectedVersion          Expected {@link HttpVersion}.
	 * @param expectedEntity           Expected entity.
	 * @param expectedHeaderNameValues Expected listing of header name/values.
	 */
	private void validateHttpRequestParserState(HttpMethod expectedMethod, String expectedPath,
			HttpVersion expectedVersion, String expectedEntity, String... expectedHeaderNameValues) throws IOException {

		// Validate method
		if (expectedMethod == null) {
			// Should not have supplier
			assertNull(this.parser.getMethod(), "HTTP method not yet parsed");
		} else if (expectedMethod.getEnum() != HttpMethodEnum.OTHER) {
			// Known method, so should re-use object
			assertSame(expectedMethod, this.parser.getMethod().get(), "Should be same HTTP method");
		} else {
			// Other type of method, so ensure equals
			assertEquals(expectedMethod, this.parser.getMethod().get(), "Should be equal HTTP method");
		}

		// Validate the request URI
		if (expectedPath == null) {
			// Should not have request URI
			assertNull(this.parser.getRequestURI(), "Request URI not yet parsed");
		} else {
			assertEquals(expectedPath, this.parser.getRequestURI().get(), "Incorrect request URI");
		}

		// Validate version
		if (expectedVersion == null) {
			// Should not have supplier
			assertNull(this.parser.getVersion(), "HTTP version not yet parsed");
		} else if (expectedVersion.getEnum() != HttpVersionEnum.OTHER) {
			// Known version, so should re-use object
			assertSame(expectedVersion, this.parser.getVersion(), "Should be same HTTP version");
		} else {
			// Other version, so ensure equals
			assertEquals(expectedVersion, this.parser.getVersion(), "Should be equal HTTP version");
		}

		// Validate correct number of headers
		if (expectedHeaderNameValues.length == 0) {
			// Should be no headers
			NonMaterialisedHttpHeaders headers = this.parser.getHeaders();
			assertTrue((headers == null) || (headers.length() == 0), "Should not have headers");

		} else {
			// Validate the headers
			assertEquals((expectedHeaderNameValues.length / 2), this.parser.getHeaders().length(),
					"Incorrect number of headers");

			// Validate correct headers
			Iterator<NonMaterialisedHttpHeader> headers = this.parser.getHeaders().iterator();
			for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
				String expectedHeaderName = expectedHeaderNameValues[i];
				String expectedHeaderValue = expectedHeaderNameValues[i + 1];
				int headerIndex = i / 2;

				// Useful suffix to identify incorrect header
				String msgSuffix = " (" + expectedHeaderName + ": " + expectedHeaderValue + " [" + headerIndex + "])";

				// Obtain the non materialised header
				assertTrue(headers.hasNext(), "Should have header" + msgSuffix);
				NonMaterialisedHttpHeader header = headers.next();

				// Ensure correct name
				CharSequence nameSequence = header.getName();
				assertEquals(expectedHeaderName.length(), nameSequence.length(),
						"Incorrect header name length" + msgSuffix);
				for (int charIndex = 0; charIndex < expectedHeaderName.length(); charIndex++) {
					assertEquals(expectedHeaderName.charAt(charIndex), nameSequence.charAt(charIndex),
							"Incorrect character " + charIndex + msgSuffix);
				}

				// Materialise the header (and ensure correct name/value)
				HttpHeader materialisedHeader = header.materialiseHttpHeader();
				assertEquals(expectedHeaderName, materialisedHeader.getName(),
						"Incorrect materialised name" + msgSuffix);
				assertEquals(expectedHeaderValue, materialisedHeader.getValue(),
						"Incorrect materialised value" + msgSuffix);
			}
		}

		// Validate the entity
		ByteSequence entityData = this.parser.getEntity();
		if ((expectedEntity == null)) {
			// Should be no entity content available
			assertNull(entityData, "Should be no entity, as not yet parsed");

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
	 * @param httpRequest              HTTP request content.
	 * @param expectedMethod           Expected method.
	 * @param expectedPath             Expected path.
	 * @param expectedVersion          Expected version.
	 * @param expectedEntity           Expected entity.
	 * @param expectedHeaderNameValues Expected listing of header name/values.
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
			assertEquals((expectedEntity != null), isComplete, "Incorrect parse result");

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
	 * @param invalidHttpRequest         Invalid HTTP request content.
	 * @param expectedHttpStatus         Expected HTTP status.
	 * @param expectedParseFailureReason Expected reason for the parse failure.
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
			assertEquals(expectedHttpStatus, ex.getHttpStatus(), "Incorrect http status");
			assertEquals(expectedParseFailureReason, ex.getMessage(), "Incorrect parse failure reason");
		}
	}

}
