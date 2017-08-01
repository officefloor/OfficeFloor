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
package net.officefloor.plugin.web.http.tokenise;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenHandler;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniser;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpTestUtil;
import net.officefloor.server.http.UsAsciiUtil;

/**
 * Tests the {@link HttpRequestTokeniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTokeniserTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpRequestTokenHandler}.
	 */
	private final HttpRequestTokenHandler handler = this
			.createMock(HttpRequestTokenHandler.class);

	/**
	 * Ensure can load domain.
	 */
	public void testDomain() throws Exception {
		this.handler.handlePath("http://wwww.officefloor.net");
		this.doTest("GET", "http://wwww.officefloor.net", null);
	}

	/**
	 * Ensure can load domain directory.
	 */
	public void testDomainDirectory() throws Exception {
		this.handler.handlePath("http://wwww.officefloor.net/");
		this.doTest("GET", "http://wwww.officefloor.net/", null);
	}

	/**
	 * Ensure can load GET request with no parameters.
	 */
	public void testGetWithNoParameters() throws Exception {
		this.handler.handlePath("/path");
		this.doTest("GET", "/path", null);
	}

	/**
	 * Ensure can load a single parameter.
	 */
	public void testGetWithOneParameter() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleQueryString("FirstName=Daniel");
		this.doTest("GET", "/path?FirstName=Daniel", null);
	}

	/**
	 * Ensure can load multiple parameters.
	 */
	public void testGetWithMultipleParameters() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleHttpParameter("LastName", "Sagenschneider");
		this.handler
				.handleQueryString("FirstName=Daniel;LastName=Sagenschneider");
		this.doTest("GET", "/path?FirstName=Daniel;LastName=Sagenschneider",
				null);
	}

	/**
	 * Ensures able to parse GET with only a segment.
	 */
	public void testGetWithFragment() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleFragment("fragment");
		this.doTest("GET", "/path#fragment", null);
	}

	/**
	 * Ensures able to parse GET with empty tokens.
	 */
	public void testEmptyTokens() throws Exception {
		this.handler.handlePath("");
		this.handler.handleHttpParameter("", "");
		this.handler.handleQueryString("=");
		this.handler.handleFragment("");
		this.doTest("GET", "?=#", null);
	}

	/**
	 * Ensures able to parse GET with parameters and fragments.
	 */
	public void testGetWithParametersAndFragments() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleHttpParameter("LastName", "Sagenschneider");
		this.handler
				.handleQueryString("FirstName=Daniel&LastName=Sagenschneider");
		this.handler.handleFragment("fragment");
		this.doTest("GET",
				"/path?FirstName=Daniel&LastName=Sagenschneider#fragment", null);
	}

	/**
	 * Ensure able to load parameter with <code>+</code> for space.
	 */
	public void testGetParameterWithSpace() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel Aaron");
		this.handler.handleQueryString("FirstName=Daniel+Aaron");
		this.doTest("GET", "/path?FirstName=Daniel+Aaron", null);
	}

	/**
	 * Ensure able to load parameter with <code>%HH</code> escaping.
	 */
	public void testGetParameterWithEscape() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel Aaron");
		this.handler.handleQueryString("FirstName=Daniel%20Aaron");
		this.doTest("GET", "/path?FirstName=Daniel%20Aaron", null);
	}

	/**
	 * Ensure if invalid <code>%HH</code> escaping.
	 */
	public void testGetParameterWithInvalidEscape() throws Exception {

		// Only gets to path
		this.handler.handlePath("/path");

		try {
			this.doTest("GET", "/path?Invalid=%WRONG", null);
			fail("Should not be successful");
		} catch (HttpRequestTokeniseException ex) {
			assertEquals("Incorrect cause",
					"Invalid character for escaping: W", ex.getMessage());
		}

		// Verify (as exception did not trigger)
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load POST request with no parameters.
	 */
	public void testPostNoParameter() throws Exception {
		this.handler.handlePath("/path");
		this.doTest("POST", "/path", "");
	}

	/**
	 * Ensure can load POST request with a parameter.
	 */
	public void testPostWithOneParameter() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.doTest("POST", "/path", "FirstName=Daniel");
	}

	/**
	 * Ensure can load POST request with multiple parameters.
	 */
	public void testPostWithMultipleParameters() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleHttpParameter("LastName", "Sagenschneider");
		this.doTest("POST", "/path", "FirstName=Daniel&LastName=Sagenschneider");
	}

	/**
	 * Ensure can load POST request with parameters in the URI and the body.
	 */
	public void testPostWithUriAndBodyParameters() throws Exception {
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleQueryString("FirstName=Daniel");
		this.handler.handleHttpParameter("LastName", "Sagenschneider");
		this.doTest("POST", "/path?FirstName=Daniel", "LastName=Sagenschneider");
	}

	/**
	 * Validate possible values for <code>%HH</code> values.
	 */
	public void testPostWithAllEscapedValues() throws Exception {

		// Validate transforms
		assertEquals("Ensure transform to HTTP", " ",
				getCharacterValue((byte) 2, (byte) 0));
		assertEquals("Ensure 1 transforms", "1", getCharacterValue(1));
		assertEquals("Ensure B transforms", "B", getCharacterValue(0xB));

		// Record the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Do not test control characters
				if (isControlCharacter(highBits, lowBits)) {
					continue;
				}

				// Obtain the characters
				String high = getCharacterValue(highBits);
				String low = getCharacterValue(lowBits);
				String character = getCharacterValue((byte) highBits,
						(byte) lowBits);

				// Record handling
				this.handler.handlePath("/path" + character);
				this.handler.handleHttpParameter(character, character);
				this.handler.handleHttpParameter("other", "value");
				this.handler.handleQueryString("%" + high + low + "=%" + high
						+ low + "&other=value");
				this.handler.handleFragment("fragment" + character);
				this.handler.handleHttpParameter(character, character);
				this.handler.handleHttpParameter("another", "value");
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
				String queryString = escapedCharacter + "=" + escapedCharacter
						+ "&other=value";
				String fragment = "fragment" + escapedCharacter;
				String entity = escapedCharacter + "=" + escapedCharacter
						+ ";another=value";
				HttpRequest request = HttpTestUtil.createHttpRequest("POST",
						path + "?" + queryString + "#" + fragment, entity);
				HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();
				tokeniser.tokeniseHttpRequest(request, this.handler);
			}
		}

		// Validate
		this.verifyMockObjects();
	}

	/**
	 * Indicates if the high and low bits result in a control character.
	 * 
	 * @param highBits
	 *            High bits.
	 * @param lowBits
	 *            Low bits.
	 * @return <code>true</code> if control character.
	 */
	public static boolean isControlCharacter(int highBits, int lowBits) {
		byte value = (byte) ((highBits << 4) | lowBits);
		return ((value <= 31) || (value == 127));
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
	public static String getCharacterValue(int highBits, int lowBits) {
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
	public static String getCharacterValue(int hexidecimal) {
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
	 * Ensure can tokenise on just request URI.
	 */
	public void testRequestURI() throws Exception {
		// Record tokenising of Request URI
		this.handler.handlePath("/path");
		this.handler.handleHttpParameter("FirstName", "Daniel");
		this.handler.handleHttpParameter("LastName", "Sagenschneider");
		this.handler
				.handleQueryString("FirstName=Daniel&LastName=Sagenschneider");
		this.handler.handleFragment("fragment");

		// Test with request URI
		this.replayMockObjects();
		HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();
		tokeniser.tokeniseRequestURI(
				"/path?FirstName=Daniel&LastName=Sagenschneider#fragment",
				this.handler);
		this.verifyMockObjects();
	}

	/**
	 * Does the test, expecting mocks to have recorded actions.
	 * 
	 * @param method
	 *            {@link HttpRequest} Method.
	 * @param requestUri
	 *            {@link HttpRequest} Request URI.
	 * @param body
	 *            Body of the {@link HttpRequest}.
	 */
	private void doTest(String method, String requestUri, String body)
			throws Exception {
		this.replayMockObjects();
		HttpRequest request = HttpTestUtil.createHttpRequest(method,
				requestUri, body);
		HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();
		tokeniser.tokeniseHttpRequest(request, this.handler);
		this.verifyMockObjects();
	}

}