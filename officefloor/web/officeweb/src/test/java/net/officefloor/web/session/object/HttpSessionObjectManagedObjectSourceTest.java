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

package net.officefloor.web.session.object;

import java.io.Serializable;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ParameterCapture;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.object.HttpSessionObjectManagedObject.Dependencies;

/**
 * Tests the {@link HttpSessionObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionObjectManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, "Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency(Dependencies.HTTP_SESSION.name(), HttpSession.class, null,
				Dependencies.HTTP_SESSION.ordinal(), Dependencies.HTTP_SESSION);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpSessionObjectManagedObjectSource.class,
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName());
	}

	/**
	 * Ensures the object is {@link Serializable}.
	 */
	public void testInvalidObjectAsNotSerializable() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record issue as not serializable object
		issues.recordIssue("Failed to init",
				new Exception("HttpSession object " + MockInvalidObject.class.getName() + " must be Serializable"));

		// Test
		this.replayMockObjects();
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME)
				.setValue(MockInvalidObject.class.getName());
		compiler.getManagedObjectLoader().loadManagedObjectType(HttpSessionObjectManagedObjectSource.class, properties);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can use the {@link ManagedObject} name.
	 */
	public void testUseManagedObjectName() throws Throwable {
		this.doTest((String) null);
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
	 * @param boundName Name to bind object within {@link HttpSession}.
	 *                  <code>null</code> to use {@link ManagedObject} name.
	 */
	public void doTest(String boundName) throws Throwable {

		final HttpSession httpSession = this.createMock(HttpSession.class);

		// Determine the managed object name
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Record instantiate and cache in session
		ParameterCapture<MockObject> instantiatedObject = new ParameterCapture<>();
		this.recordReturn(httpSession, httpSession.getAttribute(RETRIEVE_NAME), null);
		httpSession.setAttribute(this.param(RETRIEVE_NAME), instantiatedObject.capture());

		// Record cached within session
		final MockObject CACHED_OBJECT = new MockObject();
		this.recordReturn(httpSession, httpSession.getAttribute(RETRIEVE_NAME), CACHED_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_BIND_NAME, boundName);
		}
		HttpSessionObjectManagedObjectSource source = loader
				.loadManagedObjectSource(HttpSessionObjectManagedObjectSource.class);

		// Instantiate and cache object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_NAME);
		user.mapDependency(Dependencies.HTTP_SESSION, httpSession);
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertEquals("Incorrect instantiated object", instantiatedObject.getValue(), managedObject.getObject());

		// Obtain the cached object
		managedObject = user.sourceManagedObject(source);
		assertEquals("Incorrect cached object", CACHED_OBJECT, managedObject.getObject());

		this.verifyMockObjects();
	}

	/**
	 * Mock object.
	 */
	public static class MockObject implements Serializable {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Invalid mock object.
	 */
	public static class MockInvalidObject {
	}

}
