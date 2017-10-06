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
package net.officefloor.web.parameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.tokenise.HttpRequestTokeniserTest;

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
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Actual value.
	 */
	private static String actualValue = null;

	@Override
	protected void setUp() throws Exception {

		this.compiler.officeFloor((context) -> {
			this.server = MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("SERVICE", "service"));
		});
		this.compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Configure the application
			OfficeSection section = context.addSection("SERVICE", Servicer.class);
			web.link(false, HttpMethod.GET, "service", section.getOfficeSectionInput("service"));
			web.addHttpRequestObject(Parameters.class, true);
		});

		// Start the server
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the server
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
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
				if (HttpRequestTokeniserTest.isControlCharacter(highBits, lowBits)) {
					continue;
				}

				// Obtain the characters
				String high = HttpRequestTokeniserTest.getCharacterValue(highBits);
				String low = HttpRequestTokeniserTest.getCharacterValue(lowBits);
				String character = HttpRequestTokeniserTest.getCharacterValue((byte) highBits, (byte) lowBits);

				// Set up for next request
				synchronized (HttpParametersIntegrationTest.class) {
					actualValue = null;
				}

				// Undertake the request
				MockHttpResponse response = this.server.send(MockHttpServer
						.mockRequest("http://localhost:7878/service?one=1&value=%" + high + low + ";two=2"));
				assertEquals("Should be successful (no entity)", 204, response.getHttpStatus().getStatusCode());

				// Validate that obtained appropriate character value
				synchronized (HttpParametersIntegrationTest.class) {
					assertEquals("Incorrect character value", character, actualValue);
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