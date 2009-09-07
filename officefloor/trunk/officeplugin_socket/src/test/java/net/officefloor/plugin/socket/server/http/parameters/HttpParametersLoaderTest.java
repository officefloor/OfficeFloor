/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.parameters;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.impl.BufferStreamImpl;

/**
 * Tests the {@link HttpParametersLoader}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link MockInterface}.
	 */
	private final MockInterface object = this.createMock(MockInterface.class);

	/**
	 * Ensures all {@link Method} instances of the type are available.
	 */
	public void testAllMethodsAvaiable() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.object.setDescription("description");
		// ignore should be ignored
		this
				.doTest(
						"GET",
						"/path?FirstName=Daniel&LastName=Sagenschneider&Description=description&Ignore=ignore",
						null, true);
	}

	/**
	 * Ensure able to load via alias mapping.
	 */
	public void testAliasMapping() throws Exception {
		this.object.setFirstName("Dan");
		this.doTest("GET", "/path?Alias=Dan", null, true, "Alias", "FirstName");
	}

	/**
	 * Ensure only the type properties are loaded.
	 */
	public void testOnlyTypeParametersLoaded() throws Exception {

		// Load the parameters
		HttpRequest request = this
				.createRequest(
						"GET",
						"/path?FirstName=Daniel&LastName=Sagenschneider&Description=description&Ignore=NotLoaded",
						null);
		HttpParametersLoader<MockInterface> loader = this.createLoader(
				MockInterface.class, true);
		MockObject mockObject = new MockObject();
		loader.loadParameters(request, mockObject);

		// Ensure only the type parameters are loaded
		assertEquals("Incorrect first name", "Daniel", mockObject.firstName);
		assertEquals("Incorrect last name", "Sagenschneider",
				mockObject.lastName);
		assertEquals("Incorrect description", "description",
				mockObject.description);
	}

	/**
	 * Ensure can ignore case in parameter names for loading.
	 */
	public void testCaseInsensitive() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.object.setDescription("description");
		this
				.doTest(
						"GET",
						"/path?firstname=Daniel&lastname=Sagenschneider&description=description&ignore=ignore",
						null, false);
	}

	/**
	 * Ensure can ignore case in parameter names for loading via an alias.
	 */
	public void testCaseInsensitiveAlias() throws Exception {
		this.object.setFirstName("Dan");
		this
				.doTest("GET", "/path?alias=Dan", null, false, "ALIAS",
						"firstname");
	}

	/**
	 * Ensure can load GET request with no parameters.
	 */
	public void testGetWithNoParameters() throws Exception {
		// Nothing loaded
		this.doTest("GET", "http://wwww.officefloor.net", null, true);
	}

	/**
	 * Ensure can not loads an unknown parameter.
	 */
	public void testGetWithUnknownParameter() throws Exception {
		// Nothing loaded
		this.doTest("GET", "/path?Unknown=NotLoad", null, true);
	}

	/**
	 * Ensure can load a single parameter.
	 */
	public void testGetWithOneParameter() throws Exception {
		this.object.setFirstName("Daniel");
		this.doTest("GET", "/path?FirstName=Daniel", null, true);
	}

	/**
	 * Ensure can load multiple parameters.
	 */
	public void testGetWithMultipleParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("GET", "/path?FirstName=Daniel;LastName=Sagenschneider",
				null, true);
	}

	/**
	 * Ensures able to parse GET with only a segment.
	 */
	public void testGetWithFragment() throws Exception {
		// Nothing loaded
		this.doTest("GET", "/path#fragment", null, true);
	}

	/**
	 * Ensures able to parse GET with parameters and fragments.
	 */
	public void testGetWithParametersAndFragments() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("GET",
				"/path?FirstName=Daniel&LastName=Sagenschneider#fragment",
				null, true);
	}

	/**
	 * Ensure able to load parameter with <code>+</code> for space.
	 */
	public void testGetParameterWithSpace() throws Exception {
		this.object.setFirstName("Daniel Aaron");
		this.doTest("GET", "/path?FirstName=Daniel+Aaron", null, true);
	}

	/**
	 * Ensure able to load parameter with <code>%HH</code> escaping.
	 */
	public void testGetParameterWithEscape() throws Exception {
		this.object.setFirstName("Daniel Aaron");
		this.doTest("GET", "/path?FirstName=Daniel%20Aaron", null, true);
	}

	/**
	 * Ensure can load POST request with no parameters.
	 */
	public void testPostNoParameter() throws Exception {
		// Nothing loaded
		this.doTest("POST", "/path", "", true);
	}

	/**
	 * Ensure can load POST request with a parameter.
	 */
	public void testPostWithOneParameter() throws Exception {
		this.object.setFirstName("Daniel");
		this.doTest("POST", "/path", "FirstName=Daniel", true);
	}

	/**
	 * Ensure can load POST request with multiple parameters.
	 */
	public void testPostWithMultipleParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("POST", "/path",
				"FirstName=Daniel&LastName=Sagenschneider", true);
	}

	/**
	 * Ensure can load POST request with parameters in the URI and the body.
	 */
	public void testPostWithUriAndBodyParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("POST", "/path?FirstName=Daniel",
				"LastName=Sagenschneider", true);
	}

	/**
	 * Does the test (mainly for parsing content)
	 *
	 * @param method
	 *            {@link HttpRequest} Method.
	 * @param requestUri
	 *            {@link HttpRequest} Request URI.
	 * @param body
	 *            Body of the {@link HttpRequest}.
	 * @param isCaseSensitive
	 *            Indicates if case sensitive.
	 * @param aliasProperties
	 *            Alias property name pairs.
	 */
	private void doTest(String method, String requestUri, String body,
			boolean isCaseSensitive, String... aliasProperties)
			throws Exception {
		this.replayMockObjects();
		HttpRequest request = this.createRequest(method, requestUri, body);
		HttpParametersLoader<MockInterface> loader = this.createLoader(
				MockInterface.class, isCaseSensitive, aliasProperties);
		loader.loadParameters(request, this.object);
		this.verifyMockObjects();
	}

	/**
	 * Creates the initialisd {@link HttpParametersLoader} for testing.
	 *
	 * @param type
	 *            Object type to be loaded.
	 * @param isCaseSensitive
	 *            Indicates if should be case sensitive.
	 * @param aliasProperties
	 *            Alias mappings of alias to property name pairs.
	 * @return Initialised {@link HttpParametersLoader}.
	 */
	private <T> HttpParametersLoader<T> createLoader(Class<T> type,
			boolean isCaseSensitive, String... aliasProperties)
			throws Exception {

		// Create the loader
		HttpParametersLoader<T> loader = new HttpParametersLoaderImpl<T>();

		// Create the alias mappings
		Map<String, String> aliasMappings = new HashMap<String, String>();
		for (int i = 0; i < aliasProperties.length; i += 2) {
			String aliasName = aliasProperties[i];
			String propertyName = aliasProperties[i + 1];
			aliasMappings.put(aliasName, propertyName);
		}

		// Initialise the loader
		loader.init(type, aliasMappings, isCaseSensitive);

		// Return the loader
		return loader;
	}

	/**
	 * Creates a {@link HttpRequest} for testing.
	 *
	 * @param method
	 *            HTTP method (GET, POST).
	 * @param requestUri
	 *            Request URI.
	 * @param body
	 *            Contents of the {@link HttpRequest} body.
	 * @param headerNameValues
	 *            {@link HttpHeader} name values.
	 * @return {@link HttpRequest}.
	 */
	private HttpRequest createRequest(String method, String requestUri,
			String body, String... headerNameValues) throws Exception {

		// Transform the body into input buffer stream
		byte[] bodyData = (body == null ? new byte[0] : body.getBytes(Charset
				.forName("UTF-8")));
		BufferStream bufferStream = new BufferStreamImpl(ByteBuffer
				.wrap(bodyData));
		bufferStream.getOutputBufferStream().close(); // all data available
		InputBufferStream inputBufferStream = bufferStream
				.getInputBufferStream();

		// Create the headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		if (body != null) {
			// Include content length if body
			headers.add(new HttpHeaderImpl("content-length", String
					.valueOf(bodyData.length)));
		}
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Return the HTTP request
		return new HttpRequestImpl(method, requestUri, "HTTP/1.1", headers,
				inputBufferStream);
	}

	/**
	 * Interface of Mock object. Allows to test type rather than class.
	 */
	public static interface MockInterface {

		void setFirstName(String firstName);

		void setLastName(String lastName);

		void setDescription(String description);

		void setIgnore(int value);
	}

	/**
	 * Mock object for testing loading.
	 */
	public static class MockObject implements MockInterface {

		/**
		 * First name.
		 */
		public String firstName = null;

		/**
		 * Last name.
		 */
		public String lastName = null;

		/**
		 * Description.
		 */
		public String description = null;

		/**
		 * Ensures only the interface (type) methods are loaded.
		 *
		 * @param value
		 *            String value as required.
		 */
		public void setIgnore(String value) {
			fail("Should not be invoked as not on interface");
		}

		/*
		 * ================ MockInterface ==========================
		 */

		@Override
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		@Override
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@Override
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public void setIgnore(int value) {
			fail("Should not be invoked as ignored (not String parameter)");
		}
	}

}