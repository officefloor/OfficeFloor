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

package net.officefloor.web.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource.HttpRequestStateDependencies;
import net.officefloor.web.tokenise.FormHttpArgumentParser;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Tests the {@link HttpRequestStateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestStateManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpRequestObjectManagedObjectSource}.
	 */
	private HttpRequestStateManagedObjectSource source;

	/**
	 * {@link MockServerHttpConnection}.
	 */
	private MockServerHttpConnection connection = MockHttpServer.mockConnection();

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		// Should require no properties
		ManagedObjectLoaderUtil.validateSpecification(new HttpRequestStateManagedObjectSource(null));
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpRequestState.class);
		type.addDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, ServerHttpConnection.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new HttpRequestStateManagedObjectSource(null));
	}

	/**
	 * Validates use.
	 */
	public void testLoadAndUse() throws Throwable {

		// Load the request state
		HttpRequestState state = this.createHttpRequestState();

		// Set, get, name attributes
		final String NAME = "name";
		final Serializable ATTRIBUTE = "ATTRIBUTE";
		state.setAttribute(NAME, ATTRIBUTE);
		assertEquals("Must obtain attribute", ATTRIBUTE, state.getAttribute(NAME));
		Iterator<String> names = state.getAttributeNames();
		assertTrue("Expect name", names.hasNext());
		assertEquals("Incorrect name", NAME, names.next());
		assertFalse("Expect only one name", names.hasNext());

		// Source another managed object as should be new empty state
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, this.connection);
		HttpRequestState another = (HttpRequestState) user.sourceManagedObject(this.source).getObject();
		assertNull("Should be new empty state", another.getAttribute(NAME));

		// Ensure can remove attribute
		state.removeAttribute(NAME);
		assertNull("Attribute should be removed", state.getAttribute(NAME));
	}

	/**
	 * Ensure can load values to {@link ValueLoader}.
	 */
	public void testLoadValues() throws Throwable {

		// Create the request with all values
		HttpCookie cookie = new HttpCookie("c", "C");
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		request.uri("/P?q=Q");
		request.header("header", "H");
		request.header("cookie", cookie.toString());
		this.connection = MockHttpServer.mockConnection(request);

		// Create the request state (with path parameters)
		HttpRequestState state = this.createHttpRequestState();
		HttpRequestStateManagedObjectSource
				.initialiseHttpRequestState(new HttpArgument("p", "P", HttpValueLocation.PATH), state);

		// Load the values
		List<HttpArgument> arguments = new LinkedList<>();
		state.loadValues((name, value, location) -> {
			arguments.add(new HttpArgument(name, value, location));
		});
		HttpArgument[] expectedArguments = new HttpArgument[] {
				new HttpArgument("cookie", cookie.toString(), HttpValueLocation.HEADER),
				new HttpArgument("header", "H", HttpValueLocation.HEADER),
				new HttpArgument("q", "Q", HttpValueLocation.QUERY), new HttpArgument("p", "P", HttpValueLocation.PATH),
				new HttpArgument("c", "C", HttpValueLocation.COOKIE) };
		assertEquals("Incorrect number of arguments", expectedArguments.length, arguments.size());
		int index = 0;
		for (HttpArgument expected : expectedArguments) {
			HttpArgument actual = arguments.get(index++);
			assertEquals("Incorrect name", expected.name, actual.name);
			assertEquals("Incorrect value", expected.value, actual.value);
			assertEquals("Incorrect location", expected.location, actual.location);
		}
	}

	/**
	 * Ensure only valid momento.
	 */
	public void testInvalidMomento() throws Throwable {

		final Serializable momento = this.createMock(Serializable.class);
		HttpRequestState requestState = this.createHttpRequestState();

		try {
			HttpRequestStateManagedObjectSource.importHttpRequestState(momento, requestState);
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause", "Invalid momento for HttpRequestState", ex.getMessage());
		}
	}

	/**
	 * Ensure can have empty state.
	 */
	public void testEmptyStateMomento() throws Throwable {

		HttpRequestState requestState = this.createHttpRequestState();

		// Create and validate the cloned request state
		HttpRequestState clonedState = this.createClonedState(requestState);
		assertHttpRequestState(clonedState);
	}

	/**
	 * Ensure can have non-empty state.
	 */
	public void testNonEmptyStateMomento() throws Throwable {

		HttpRequestState requestState = this.createHttpRequestState();
		requestState.setAttribute("ATTRIBUTE_ONE", "VALUE_A");
		requestState.setAttribute("ATTRIBUTE_TWO", "VALUE_B");

		// Create and validate the cloned request state
		HttpRequestState clonedState = this.createClonedState(requestState);
		assertHttpRequestState(clonedState, "ATTRIBUTE_ONE", "VALUE_A", "ATTRIBUTE_TWO", "VALUE_B");
	}

	/**
	 * Ensures the momento overrides the state.
	 */
	public void testEnsureMomentoOverrideState() throws Throwable {

		HttpRequestState requestState = this.createHttpRequestState();
		requestState.setAttribute("ATTRIBUTE", "OVERRIDE");

		// Create and validate the cloned request state
		HttpRequestState clonedState = this.createHttpRequestState();
		clonedState.setAttribute("ATTRIBUTE", "CLONE");

		// Import the state
		Serializable momento = HttpRequestStateManagedObjectSource.exportHttpRequestState(requestState);
		HttpRequestStateManagedObjectSource.importHttpRequestState(momento, clonedState);

		// Ensure override state
		assertHttpRequestState(clonedState, "ATTRIBUTE", "OVERRIDE");
	}

	/**
	 * Ensure the cloned state is separate.
	 */
	public void testEnsureMomentoStatesSeparate() throws Throwable {

		HttpRequestState requestState = this.createHttpRequestState();
		requestState.setAttribute("ONE", "A");

		// Create and validate the cloned request state
		HttpRequestState clonedState = this.createClonedState(requestState);
		assertHttpRequestState(clonedState, "ONE", "A");

		// Change the original cloned state
		requestState.setAttribute("TWO", "B");
		requestState.removeAttribute("ONE");

		// Cloned state should stay unchanged
		assertHttpRequestState(clonedState, "ONE", "A");
	}

	/**
	 * Ensure the {@link HttpArgument} values from previous are re-instated.
	 * However, no {@link HttpRequestCookie} instances are re-instated (as must
	 * come from client).
	 */
	public void testEnsureHttpArgumentsReinstated() throws Throwable {

		// Provide request with header and cookie
		this.connection = MockHttpServer.mockConnection(MockHttpServer.mockRequest("/path?query=two")
				.header("header", "three").header("Content-Type", "application/x-www-form-urlencoded")
				.cookie("cookie", "four").entity("entity=five"));

		// Initiate request state with path arguments and attribute
		HttpRequestState requestState = this.createHttpRequestState();
		HttpRequestStateManagedObjectSource
				.initialiseHttpRequestState(new HttpArgument("path", "one", HttpValueLocation.PATH), requestState);
		requestState.setAttribute("ONE", "A");

		// Now request with parameters to override (except cookie)
		this.connection = MockHttpServer.mockConnection(MockHttpServer.mockRequest("/path?query=override")
				.header("header", "override").header("Content-Type", "application/x-www-form-urlencoded")
				.cookie("cookie", "client").entity("entity=override"));

		HttpRequestState clonedState = this.createClonedState(requestState);
		assertHttpRequestState(clonedState, "ONE", "A");

		// Ensure provided previous arguments
		Map<String, String> values = new HashMap<>();
		Map<String, HttpValueLocation> locations = new HashMap<>();
		clonedState.loadValues((name, value, location) -> {
			values.put(name, value);
			locations.put(name, location);
		});
		assertEquals("Incorrect number of parameters", 6, values.size());
		assertEquals("Must have path", "one", values.get("path"));
		assertEquals("Incorrect path location", HttpValueLocation.PATH, locations.get("path"));
		assertEquals("Must have query", "two", values.get("query"));
		assertEquals("Incorrect query location", HttpValueLocation.QUERY, locations.get("query"));
		assertEquals("Must have header", "three", values.get("header"));
		assertEquals("Incorrect Content-Type", "application/x-www-form-urlencoded", values.get("Content-Type"));
		assertEquals("Incorrect header location", HttpValueLocation.HEADER, locations.get("header"));
		assertEquals("Should have client cookie", "client", values.get("cookie"));
		assertEquals("Incorrect cookie location", HttpValueLocation.COOKIE, locations.get("cookie"));
		assertEquals("Must have entity", "five", values.get("entity"));
		assertEquals("Incorrect entity location", HttpValueLocation.ENTITY, locations.get("entity"));
	}

	/**
	 * Asserts the {@link HttpRequestState}.
	 * 
	 * @param state
	 *            {@link HttpRequestState} to validate.
	 * @param attributeNameValuePairs
	 *            Expected attribute name/value pairs in the
	 *            {@link HttpRequestState} (listed in alphabetical order by
	 *            name).
	 */
	private void assertHttpRequestState(HttpRequestState state, String... attributeNameValuePairs) {

		// Obtain the list of all names in the state
		int expectedNumberOfAttributes = (attributeNameValuePairs.length / 2);
		List<String> names = new ArrayList<String>(expectedNumberOfAttributes);
		for (Iterator<String> iterator = state.getAttributeNames(); iterator.hasNext();) {
			String name = iterator.next();
			names.add(name);
		}
		assertEquals("Incorrect number of attributes", expectedNumberOfAttributes, names.size());

		// Validate correct name/value pairs
		for (int i = 0; i < expectedNumberOfAttributes; i++) {
			String name = attributeNameValuePairs[i * 2];
			String expectedValue = attributeNameValuePairs[(i * 2) + 1];
			String actualValue = (String) state.getAttribute(name);
			assertEquals("Incorrect value for attribute '" + name + "'", expectedValue, actualValue);
		}
	}

	/**
	 * Creates a cloned {@link HttpRequestState}.
	 * 
	 * @param requestState
	 *            {@link HttpRequestState} to clone.
	 * @return Cloned {@link HttpRequestState}.
	 */
	private HttpRequestState createClonedState(HttpRequestState requestState) throws Throwable {

		// Export the momento
		Serializable momento = HttpRequestStateManagedObjectSource.exportHttpRequestState(requestState);

		// Serialise the momento
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
		output.writeObject(momento);
		output.flush();

		// Unserialise the momento
		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
		ObjectInputStream input = new ObjectInputStream(inputBuffer);
		Serializable unserialisedMomento = (Serializable) input.readObject();

		// Create a new request state from momento
		HttpRequestState clonedState = this.createHttpRequestState();
		HttpRequestStateManagedObjectSource.importHttpRequestState(unserialisedMomento, clonedState);

		// Return the cloned state
		return clonedState;
	}

	/**
	 * Creates the {@link HttpRequestState}.
	 * 
	 * @return {@link HttpRequestState}.
	 */
	private HttpRequestState createHttpRequestState() throws Throwable {

		// Load the source
		this.source = new HttpRequestStateManagedObjectSource(
				new HttpArgumentParser[] { new FormHttpArgumentParser() });

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(HttpRequestStateDependencies.SERVER_HTTP_CONNECTION, this.connection);
		ManagedObject mo = user.sourceManagedObject(this.source);

		// Ensure correct object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpRequestState);
		HttpRequestState state = (HttpRequestState) object;

		// Return the request state
		return state;
	}

}
