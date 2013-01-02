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
package net.officefloor.plugin.web.http.application;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.application.HttpApplicationObjectManagedObjectSource.Dependencies;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpApplicationObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationObjectManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpApplicationObjectManagedObjectSource.class,
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				"Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency(Dependencies.HTTP_APPLICATION_STATE.name(),
				HttpApplicationState.class, null,
				Dependencies.HTTP_APPLICATION_STATE.ordinal(),
				Dependencies.HTTP_APPLICATION_STATE);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpApplicationObjectManagedObjectSource.class,
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
	}

	/**
	 * Ensure can use the {@link ManagedObject} name.
	 */
	public void testUseManagedObjectName() throws Throwable {
		this.doTest(null);
	}

	/**
	 * Ensure can override binding name.
	 */
	public void testOverrideBindingName() throws Throwable {
		this.doTest("OVERRIDDEN");
	}

	/**
	 * Undertakes the test to use the {@link HttpApplicationState}.
	 * 
	 * @param boundName
	 *            Name to bind object within {@link HttpApplicationState}.
	 *            <code>null</code> to use {@link ManagedObject} name.
	 */
	public void doTest(String boundName) throws Throwable {

		final HttpApplicationState state = this
				.createMock(HttpApplicationState.class);

		// Determine the managed object name
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Record instantiate and cache in application state
		final MockObject[] instantiatedObject = new MockObject[1];
		this.recordReturn(state, state.getAttribute(RETRIEVE_NAME), null);
		state.setAttribute(RETRIEVE_NAME, null);
		this.control(state).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect bound name", RETRIEVE_NAME, actual[0]);
				MockObject object = (MockObject) actual[1];
				assertNotNull("Expecting instantiated object", object);
				instantiatedObject[0] = object;
				return true;
			}
		});

		// Record cached within application state
		final MockObject CACHED_OBJECT = new MockObject();
		this.recordReturn(state, state.getAttribute(RETRIEVE_NAME),
				CACHED_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(
					HttpApplicationObjectManagedObjectSource.PROPERTY_BIND_NAME,
					boundName);
		}
		HttpApplicationObjectManagedObjectSource source = loader
				.loadManagedObjectSource(HttpApplicationObjectManagedObjectSource.class);

		// Instantiate and cache object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_NAME);
		user.mapDependency(Dependencies.HTTP_APPLICATION_STATE, state);
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertEquals("Incorrect instantiated object", instantiatedObject[0],
				managedObject.getObject());

		// Obtain the cached object
		managedObject = user.sourceManagedObject(source);
		assertEquals("Incorrect cached object", CACHED_OBJECT,
				managedObject.getObject());

		this.verifyMockObjects();
	}

	/**
	 * Mock object.
	 */
	public static class MockObject {
	}

}