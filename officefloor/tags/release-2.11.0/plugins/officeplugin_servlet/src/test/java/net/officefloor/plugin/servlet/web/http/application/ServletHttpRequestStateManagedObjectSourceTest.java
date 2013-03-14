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
package net.officefloor.plugin.servlet.web.http.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.web.http.application.ServletHttpRequestStateManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.application.HttpRequestState;

/**
 * Tests the {@link ServletHttpRequestStateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpRequestStateManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * {@link ServletBridge}.
	 */
	private final ServletBridge servletBridge = this
			.createMock(ServletBridge.class);

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(ServletHttpRequestStateManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpRequestState.class);
		type.addDependency(Dependencies.SERVLET_BRIDGE, ServletBridge.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletHttpRequestStateManagedObjectSource.class);
	}

	/**
	 * Ensure load.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final String NAME = "name";
		final Serializable ATTRIBUTE = "attribute";
		final Enumeration<String> names = this.createMock(Enumeration.class);

		// Create the mocks
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);

		// Record obtaining Servlet Context
		this.recordReturn(this.servletBridge, this.servletBridge.getRequest(),
				request);

		// Record set, get, names and remove
		request.setAttribute(NAME, ATTRIBUTE);
		this.recordReturn(request, request.getAttribute(NAME), ATTRIBUTE);
		this.recordReturn(request, request.getAttributeNames(), names);
		this.recordReturn(names, names.hasMoreElements(), true);
		this.recordReturn(names, names.nextElement(), NAME);
		this.recordReturn(names, names.hasMoreElements(), false);
		request.removeAttribute(NAME);

		// Replay
		this.replayMockObjects();

		// Obtain the object
		HttpRequestState state = this.createHttpRequestState();

		// Attempt to get, set, names and remove
		state.setAttribute(NAME, ATTRIBUTE);
		assertEquals("Incorrect attribute", ATTRIBUTE, state.getAttribute(NAME));
		Iterator<String> nameList = state.getAttributeNames();
		assertTrue("Expect a name", nameList.hasNext());
		assertEquals("Incorrect name", NAME, nameList.next());
		assertFalse("Should only be one name", nameList.hasNext());
		state.removeAttribute(NAME);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to export and import the state.
	 */
	@SuppressWarnings("unchecked")
	public void testExportImportState() throws Throwable {

		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final Enumeration<String> attributeNames = this
				.createMock(Enumeration.class);

		// Record exporting the state
		this.recordReturn(this.servletBridge, this.servletBridge.getRequest(),
				request);
		this.recordReturn(request, request.getAttributeNames(), attributeNames);
		this.recordReturn(attributeNames, attributeNames.hasMoreElements(),
				true);
		this.recordReturn(attributeNames, attributeNames.nextElement(), "ONE");
		this.recordReturn(request, request.getAttribute("ONE"), "1");
		this.recordReturn(attributeNames, attributeNames.hasMoreElements(),
				true);
		this.recordReturn(attributeNames, attributeNames.nextElement(), "TWO");
		this.recordReturn(request, request.getAttribute("TWO"), "2");
		this.recordReturn(attributeNames, attributeNames.hasMoreElements(),
				false);

		// Record creating cloned request state
		this.recordReturn(this.servletBridge, this.servletBridge.getRequest(),
				request);

		// Record importing the state
		request.setAttribute("ONE", "1");
		request.setAttribute("TWO", "2");

		// Test
		this.replayMockObjects();
		HttpRequestState state = this.createHttpRequestState();

		// Export the state
		Serializable momento = state.exportState();

		// Serialise the momento
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
		output.writeObject(momento);
		output.flush();

		// Unserialise the momento
		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(
				outputBuffer.toByteArray());
		ObjectInputStream input = new ObjectInputStream(inputBuffer);
		Serializable unserialisedMomento = (Serializable) input.readObject();

		// Clone the request state
		HttpRequestState clone = this.createHttpRequestState();
		clone.importState(unserialisedMomento);

		// Verify loaded momento state
		this.verifyMockObjects();
	}

	/**
	 * Creates the {@link HttpRequestState}.
	 * 
	 * @return {@link HttpRequestState}.
	 */
	private HttpRequestState createHttpRequestState() throws Throwable {

		// Load source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ServletHttpRequestStateManagedObjectSource source = loader
				.loadManagedObjectSource(ServletHttpRequestStateManagedObjectSource.class);

		// Obtain managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVLET_BRIDGE, this.servletBridge);
		ManagedObject mo = user.sourceManagedObject(source);

		// Obtain the object
		HttpRequestState state = (HttpRequestState) mo.getObject();

		// Return the request state
		return state;
	}

}