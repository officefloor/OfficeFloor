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

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletContext;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.web.http.application.ServletHttpApplicationStateManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.application.HttpApplicationState;

/**
 * Tests the {@link ServletHttpApplicationStateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpApplicationStateManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(ServletHttpApplicationStateManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpApplicationState.class);
		type.addDependency(Dependencies.SERVLET_BRIDGE, ServletBridge.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletHttpApplicationStateManagedObjectSource.class);
	}

	/**
	 * Ensure load.
	 */
	@SuppressWarnings("unchecked")
	public void testLoad() throws Throwable {

		final String NAME = "name";
		final Object ATTRIBUTE = "attribute";
		final Enumeration<String> names = this.createMock(Enumeration.class);

		// Create the mocks
		final ServletBridge servletBridge = this
				.createMock(ServletBridge.class);
		final ServletContext servletContext = this
				.createMock(ServletContext.class);

		// Record obtaining Servlet Context
		this.recordReturn(servletBridge, servletBridge.getServletContext(),
				servletContext);

		// Record set, get, names and remove
		servletContext.setAttribute(NAME, ATTRIBUTE);
		this.recordReturn(servletContext, servletContext.getAttribute(NAME),
				ATTRIBUTE);
		this.recordReturn(servletContext, servletContext.getAttributeNames(),
				names);
		this.recordReturn(names, names.hasMoreElements(), true);
		this.recordReturn(names, names.nextElement(), NAME);
		this.recordReturn(names, names.hasMoreElements(), false);
		servletContext.removeAttribute(NAME);

		// Replay
		this.replayMockObjects();

		// Load source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ServletHttpApplicationStateManagedObjectSource source = loader
				.loadManagedObjectSource(ServletHttpApplicationStateManagedObjectSource.class);

		// Obtain managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVLET_BRIDGE, servletBridge);
		ManagedObject mo = user.sourceManagedObject(source);

		// Obtain the object
		HttpApplicationState state = (HttpApplicationState) mo.getObject();

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
	 * Mock object.
	 */
	public static class MockObject {
	}

}