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
package net.officefloor.web.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

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
	 * Validate specification.
	 */
	public void testSpecification() {
		// Should require no properties
		ManagedObjectLoaderUtil.validateSpecification(HttpRequestStateManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpRequestState.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpRequestStateManagedObjectSource.class);
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
		HttpRequestState another = (HttpRequestState) user.sourceManagedObject(this.source).getObject();
		assertNull("Should be new empty state", another.getAttribute(NAME));

		// Ensure can remove attribute
		state.removeAttribute(NAME);
		assertNull("Attribute should be removed", state.getAttribute(NAME));
	}

	/**
	 * Ensure only valid momento.
	 */
	public void testInvalidMomento() throws Throwable {

		final Serializable momento = this.createMock(Serializable.class);
		HttpRequestState requestState = this.createHttpRequestState();

		try {
			requestState.importState(momento);
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
		clonedState.importState(requestState.exportState());

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
		Serializable momento = requestState.exportState();

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
		clonedState.importState(unserialisedMomento);

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
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		this.source = loader.loadManagedObjectSource(HttpRequestStateManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(this.source);

		// Ensure correct object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpRequestState);
		HttpRequestState state = (HttpRequestState) object;

		// Return the request state
		return state;
	}

}