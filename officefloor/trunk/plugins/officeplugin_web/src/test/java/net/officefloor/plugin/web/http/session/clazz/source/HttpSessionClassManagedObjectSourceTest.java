/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.session.clazz.source;

import java.io.Serializable;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.clazz.source.HttpSessionClassManagedObject.Dependencies;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpSessionClassManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionClassManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpSessionClassManagedObjectSource.class,
				HttpSessionClassManagedObjectSource.PROPERTY_CLASS_NAME,
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
		type.addDependency(Dependencies.HTTP_SESSION.name(), HttpSession.class,
				Dependencies.HTTP_SESSION.ordinal(), Dependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSessionClassManagedObjectSource.class,
				HttpSessionClassManagedObjectSource.PROPERTY_CLASS_NAME,
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
	 * Undertakes the test to use the {@link HttpSession}.
	 * 
	 * @param boundName
	 *            Name to bind object within {@link HttpSession}.
	 *            <code>null</code> to use {@link ManagedObject} name.
	 */
	public void doTest(String boundName) throws Throwable {

		final HttpSession httpSession = this.createMock(HttpSession.class);

		// Determine the managed object name
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Record instantiate and cache in session
		final MockObject[] instantiatedObject = new MockObject[1];
		this.recordReturn(httpSession, httpSession.getAttribute(RETRIEVE_NAME),
				null);
		httpSession.setAttribute(RETRIEVE_NAME, null);
		this.control(httpSession).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect bound name", RETRIEVE_NAME, actual[0]);
				MockObject object = (MockObject) actual[1];
				assertNotNull("Expecting instantiated object", object);
				instantiatedObject[0] = object;
				return true;
			}
		});

		// Record cached within session
		final MockObject CACHED_OBJECT = new MockObject();
		this.recordReturn(httpSession, httpSession.getAttribute(RETRIEVE_NAME),
				CACHED_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpSessionClassManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(
					HttpSessionClassManagedObjectSource.PROPERTY_BIND_NAME,
					boundName);
		}
		HttpSessionClassManagedObjectSource source = loader
				.loadManagedObjectSource(HttpSessionClassManagedObjectSource.class);

		// Instantiate and cache object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_NAME);
		user.mapDependency(Dependencies.HTTP_SESSION, httpSession);
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
	public static class MockObject implements Serializable {
	}

}