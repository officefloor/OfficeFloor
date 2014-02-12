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
package net.officefloor.plugin.web.http.parameters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserTest;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * <p>
 * Ensure the {@link HttpParameters} is loaded in servicing a request.
 * <p>
 * This also ensure appropriate handling of escaped characters.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersIntegrationTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpServerAutoWireApplication}.
	 */
	private final HttpServerAutoWireApplication source = new HttpServerAutoWireOfficeFloorSource();

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor server;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	/**
	 * Actual value.
	 */
	private static String actualValue = null;

	@Override
	protected void setUp() throws Exception {

		// Configure the application
		AutoWireSection section = this.source.addSection("SERVICE",
				ClassSectionSource.class.getName(), Servicer.class.getName());
		this.source.linkUri("service", section, "service");
		this.source.addHttpRequestObject(Parameters.class, true);

		// Start the server
		this.server = this.source.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.close();
		} finally {
			// Stop the server
			if (this.server != null) {
				this.server.closeOfficeFloor();
			}
		}
	}

	/**
	 * {@link HttpParameters}.
	 */
	public static class Parameters implements Serializable {

		private String value;

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		private String one;

		public String getOne() {
			return this.one;
		}

		public void setOne(String one) {
			this.one = one;
		}

		public String two;

		public String getTwo() {
			return this.two;
		}

		public void setTwo(String two) {
			this.two = two;
		}
	}

	/**
	 * Servicer logic.
	 */
	public static class Servicer {

		public void service(Parameters parameters) {

			// Ensure correct surrounding parameters
			assertEquals("Incorrect one value", "1", parameters.getOne());
			assertEquals("Incorrect two value", "2", parameters.getTwo());

			// Service request by tracking actual value
			synchronized (HttpParametersIntegrationTest.class) {
				actualValue = parameters.getValue();
			}
		}
	}

	/**
	 * Ensure escaped characters are provided correctly.
	 */
	public void testEscapedCharacters() throws Exception {

		// Key track of characters tested
		Set<String> characters = new HashSet<String>();

		// Record the range of percentage values
		for (int highBits = 0; highBits <= 0xF; highBits++) {
			for (int lowBits = 0; lowBits <= 0xF; lowBits++) {

				// Do not test control characters
				if (HttpRequestTokeniserTest.isControlCharacter(highBits,
						lowBits)) {
					continue;
				}

				// Obtain the characters
				String high = HttpRequestTokeniserTest
						.getCharacterValue(highBits);
				String low = HttpRequestTokeniserTest
						.getCharacterValue(lowBits);
				String character = HttpRequestTokeniserTest.getCharacterValue(
						(byte) highBits, (byte) lowBits);

				// Set up for next request
				synchronized (HttpParametersIntegrationTest.class) {
					actualValue = null;
				}

				// Undertake the request
				HttpUriRequest request = new HttpGet(
						"http://localhost:7878/service?one=1&value=%" + high
								+ low + ";two=2");
				HttpResponse response = this.client.execute(request);
				assertEquals("Should be successful (no entity)", 204, response
						.getStatusLine().getStatusCode());

				// Validate that obtained appropriate character value
				synchronized (HttpParametersIntegrationTest.class) {
					assertEquals("Incorrect character value", character,
							actualValue);
				}

				// Track the characters tested
				characters.add(character);
			}
		}

		// Ensure key characters are tested
		assertTrue("Must test '&'", characters.contains("&"));
		assertTrue("Must test ';'", characters.contains(";"));
		assertTrue("Must test '#'", characters.contains("#"));
		assertTrue("Must test '?'", characters.contains("?"));
		assertTrue("Must test '+'", characters.contains("+"));
		assertTrue("Must test ' '", characters.contains(" "));
	}

}