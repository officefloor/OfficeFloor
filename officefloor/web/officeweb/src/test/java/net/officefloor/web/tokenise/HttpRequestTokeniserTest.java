/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.tokenise;

import java.net.HttpCookie;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.escalation.BadRequestHttpException;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Tests the {@link HttpRequestTokeniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTokeniserTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ValueLoader}.
	 */
	private final ValueLoader loader = this.createMock(ValueLoader.class);

	/**
	 * Ensure can load GET request with no parameters.
	 */
	public void testGetWithNoParameters() throws Exception {
		this.doTest(HttpMethod.GET, "/path", null);
	}

	/**
	 * Ensure can load a single parameter.
	 */
	public void testGetWithOneParameter() throws Exception {
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "/path?FirstName=Daniel", null);
	}

	/**
	 * Ensure can load multiple parameters.
	 */
	public void testGetWithMultipleParameters() throws Exception {
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.QUERY);
		this.loader.loadValue("LastName", "Sagenschneider", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "/path?FirstName=Daniel;LastName=Sagenschneider", null);
	}

	/**
	 * Ensures able to parse GET with only a segment.
	 */
	public void testGetWithFragment() throws Exception {
		this.doTest(HttpMethod.GET, "/path#fragment", null);
	}

	/**
	 * Ensures able to parse GET with empty tokens.
	 */
	public void testEmptyTokens() throws Exception {
		this.loader.loadValue("", "", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "?=#", null);
	}

	/**
	 * Ensures able to parse GET with parameters and fragments.
	 */
	public void testGetWithParametersAndFragments() throws Exception {
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.QUERY);
		this.loader.loadValue("LastName", "Sagenschneider", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "/path?FirstName=Daniel&LastName=Sagenschneider#fragment", null);
	}

	/**
	 * Ensure able to load parameter with <code>+</code> for space.
	 */
	public void testGetParameterWithSpace() throws Exception {
		this.loader.loadValue("FirstName", "Daniel Aaron", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "/path?FirstName=Daniel+Aaron", null);
	}

	/**
	 * Ensure able to load parameter with <code>%HH</code> escaping.
	 */
	public void testGetParameterWithEscape() throws Exception {
		this.loader.loadValue("FirstName", "Daniel Aaron", HttpValueLocation.QUERY);
		this.doTest(HttpMethod.GET, "/path?FirstName=Daniel%20Aaron", null);
	}

	/**
	 * Ensure if invalid <code>%HH</code> escaping.
	 */
	public void testGetParameterWithInvalidEscape() throws Exception {
		try {
			this.doTest(HttpMethod.GET, "/path?Invalid=%WRONG", null);
			fail("Should not be successful");
		} catch (BadRequestHttpException ex) {
			assertEquals("Incorrect cause", "Invalid character for escaping: W", ex.getEntity());
		}

		// Verify (as exception did not trigger)
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load POST request with no parameters.
	 */
	public void testPostNoParameter() throws Exception {
		this.doTest(HttpMethod.POST, "/path", "");
	}

	/**
	 * Ensure can load POST request with a parameter.
	 */
	public void testPostWithOneParameter() throws Exception {
		final String entity = "FirstName=Daniel";
		this.loader.loadValue("content-type", "application/x-www-form-urlencoded", HttpValueLocation.HEADER);
		this.loader.loadValue("content-length", String.valueOf(entity.length()), HttpValueLocation.HEADER);
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.ENTITY);
		this.doTest(HttpMethod.POST, "/path", entity, "content-type", "application/x-www-form-urlencoded");
	}

	/**
	 * Ensure can load POST request with multiple parameters.
	 */
	public void testPostWithMultipleParameters() throws Exception {
		final String entity = "FirstName=Daniel&LastName=Sagenschneider";
		this.loader.loadValue("content-type", "application/x-www-form-urlencoded", HttpValueLocation.HEADER);
		this.loader.loadValue("content-length", String.valueOf(entity.length()), HttpValueLocation.HEADER);
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.ENTITY);
		this.loader.loadValue("LastName", "Sagenschneider", HttpValueLocation.ENTITY);
		this.doTest(HttpMethod.POST, "/path", entity, "content-type", "application/x-www-form-urlencoded");
	}

	/**
	 * Ensure can tokenise {@link HttpCookie} values.
	 */
	public void testCookieParameter() {
		this.loader.loadValue("cookie", "name=value", HttpValueLocation.HEADER);
		// Cookie not tokenised
		this.doTest(HttpMethod.GET, "/path", null, "cookie", "name=value");
	}

	/**
	 * Ensure can load POST request with parameters in the URI and the body.
	 */
	public void testPostWithUriAndBodyParameters() throws Exception {
		final String entity = "LastName=Sagenschneider";
		this.loader.loadValue("FirstName", "Daniel", HttpValueLocation.QUERY);
		this.loader.loadValue("content-type", "application/x-www-form-urlencoded", HttpValueLocation.HEADER);
		this.loader.loadValue("content-length", String.valueOf(entity.length()), HttpValueLocation.HEADER);
		this.loader.loadValue("LastName", "Sagenschneider", HttpValueLocation.ENTITY);
		this.doTest(HttpMethod.POST, "/path?FirstName=Daniel", entity, "content-type",
				"application/x-www-form-urlencoded");
	}

	/**
	 * Validate possible values for <code>%HH</code> values.
	 */
	public void testPostWithAllEscapedValues() throws Exception {

		// Validate transforms
		assertEquals("Ensure transform to HTTP", " ", getCharacterValue((byte) 2, (byte) 0));
		assertEquals("Ensure 1 transforms", "1", getCharacterValue(1));
		assertEquals("Ensure B transforms", "B", getCharacterValue(0xB));

		// Function to create entity
		Function<String, String> entityFactory = (escapedCharacter) -> escapedCharacter + "=" + escapedCharacter
				+ ";another=value";
		String exampleEntity = entityFactory.apply("%AB");

		// Record the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Do not test control characters
				if (isControlCharacter(highBits, lowBits)) {
					continue;
				}

				// Obtain the character
				String character = getCharacterValue((byte) highBits, (byte) lowBits);

				// Record handling
				this.loader.loadValue(character, character, HttpValueLocation.QUERY);
				this.loader.loadValue("other", "value", HttpValueLocation.QUERY);
				this.loader.loadValue("content-type", "application/x-www-form-urlencoded", HttpValueLocation.HEADER);
				this.loader.loadValue("content-length", String.valueOf(exampleEntity.length()),
						HttpValueLocation.HEADER);
				this.loader.loadValue(character, character, HttpValueLocation.ENTITY);
				this.loader.loadValue("another", "value", HttpValueLocation.ENTITY);
			}
		}

		// Test
		this.replayMockObjects();

		// Validate the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Do not test control characters
				if (isControlCharacter(highBits, lowBits)) {
					continue;
				}

				// Obtain the characters
				String high = getCharacterValue(highBits);
				String low = getCharacterValue(lowBits);
				String escapedCharacter = "%" + high + low;

				// Validate the percentage value
				String path = "/path" + escapedCharacter;
				String queryString = escapedCharacter + "=" + escapedCharacter + "&other=value";
				String fragment = "fragment" + escapedCharacter;
				String entity = entityFactory.apply(escapedCharacter);
				HttpRequest request = MockHttpServer.mockRequest().method(HttpMethod.POST)
						.uri(path + "?" + queryString + "#" + fragment)
						.header("content-type", "application/x-www-form-urlencoded").entity(entity).build();
				HttpRequestTokeniser.tokeniseHttpRequest(request,
						new HttpArgumentParser[] { new FormHttpArgumentParser() }, this.loader);
			}
		}

		// Validate
		this.verifyMockObjects();
	}

	/**
	 * Indicates if the high and low bits result in a control character.
	 * 
	 * @param highBits High bits.
	 * @param lowBits  Low bits.
	 * @return <code>true</code> if control character.
	 */
	public static boolean isControlCharacter(int highBits, int lowBits) {
		byte value = (byte) ((highBits << 4) | lowBits);
		return ((value <= 31) || (value == 127));
	}

	/**
	 * Transforms the high and low bits to the corresponding character value.
	 * 
	 * @param highBits High bits.
	 * @param lowBits  Low bits
	 * @return Character value.
	 */
	public static String getCharacterValue(int highBits, int lowBits) {
		byte byteValue = (byte) ((highBits << 4) + lowBits);
		return UsAsciiUtil.convertToString(new byte[] { byteValue });
	}

	/**
	 * Obtains the character value for the hexidecimal value.
	 * 
	 * @param hexidecimal Hexidecimal value.
	 * @return Character value.
	 */
	public static String getCharacterValue(int hexidecimal) {
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
	 * Does the test, expecting mocks to have recorded actions.
	 * 
	 * @param method           {@link HttpMethod}.
	 * @param requestUri       {@link HttpRequest} Request URI.
	 * @param entity           Entity of the {@link HttpRequest}.
	 * @param headerNameValues {@link HttpHeader} name value pairs.
	 */
	private void doTest(HttpMethod method, String requestUri, String entity, String... headerNameValues)
			throws HttpException {
		this.replayMockObjects();
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(requestUri).method(method).entity(entity);
		for (int i = 0; i < headerNameValues.length; i += 2) {
			request.header(headerNameValues[i], headerNameValues[i + 1]);
		}
		HttpRequestTokeniser.tokeniseHttpRequest(request.build(),
				new HttpArgumentParser[] { new FormHttpArgumentParser() }, this.loader);
		this.verifyMockObjects();
	}

}
