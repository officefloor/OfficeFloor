/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.session.object;

import java.io.Serializable;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.plugin.web.http.session.object.HttpSessionObjectManagedObject.Dependencies;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpSessionObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
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
				null, Dependencies.HTTP_SESSION.ordinal(),
				Dependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
	}

	/**
	 * Ensures the object is {@link Serializable}.
	 */
	public void testInvalidObjectAsNotSerializable() {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record issue as not serializable object
		issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				"Failed to init", null);
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				for (int i = 0; i < (expected.length - 1); i++) {
					assertEquals("Invalid parameter " + i, expected[i],
							actual[i]);
				}
				Exception cause = (Exception) actual[expected.length - 1];
				assertEquals("Incorrect cause", "HttpSession object "
						+ MockInvalidObject.class.getName()
						+ " must be Serializable", cause.getMessage());
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME)
				.setValue(MockInvalidObject.class.getName());
		compiler.getManagedObjectLoader().loadManagedObjectType(
				HttpSessionObjectManagedObjectSource.class, properties);
		this.verifyMockObjects();
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
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(
					HttpSessionObjectManagedObjectSource.PROPERTY_BIND_NAME,
					boundName);
		}
		HttpSessionObjectManagedObjectSource source = loader
				.loadManagedObjectSource(HttpSessionObjectManagedObjectSource.class);

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

	/**
	 * Invalid mock object.
	 */
	public static class MockInvalidObject {
	}

}