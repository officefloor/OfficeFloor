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

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ParameterCapture;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.state.HttpApplicationObjectManagedObjectSource.Dependencies;

/**
 * Tests the {@link HttpApplicationObjectManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationObjectManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(HttpApplicationObjectManagedObjectSource.class,
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME, "Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Obtain the type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(MockObject.class);
		type.addDependency(Dependencies.HTTP_APPLICATION_STATE.name(), HttpApplicationState.class, null,
				Dependencies.HTTP_APPLICATION_STATE.ordinal(), Dependencies.HTTP_APPLICATION_STATE);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, HttpApplicationObjectManagedObjectSource.class,
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName());
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
	 * Undertakes the test to use the {@link HttpApplicationState}.
	 * 
	 * @param boundName Name to bind object within {@link HttpApplicationState}.
	 *                  <code>null</code> to use {@link ManagedObject} name.
	 */
	public void doTest(String boundName) throws Throwable {

		final HttpApplicationState state = this.createMock(HttpApplicationState.class);

		// Determine the managed object name
		final String MO_NAME = "MO";
		final String RETRIEVE_NAME = (boundName == null ? MO_NAME : boundName);

		// Record instantiate and cache in application state
		ParameterCapture<MockObject> instantiatedObject = new ParameterCapture<>();
		this.recordReturn(state, state.getAttribute(RETRIEVE_NAME), null);
		state.setAttribute(this.param(RETRIEVE_NAME), instantiatedObject.capture());

		// Record cached within application state
		final MockObject CACHED_OBJECT = new MockObject();
		this.recordReturn(state, state.getAttribute(RETRIEVE_NAME), CACHED_OBJECT);

		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME, MockObject.class.getName());
		if (boundName != null) {
			loader.addProperty(HttpApplicationObjectManagedObjectSource.PROPERTY_BIND_NAME, boundName);
		}
		HttpApplicationObjectManagedObjectSource source = loader
				.loadManagedObjectSource(HttpApplicationObjectManagedObjectSource.class);

		// Instantiate and cache object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_NAME);
		user.mapDependency(Dependencies.HTTP_APPLICATION_STATE, state);
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
	public static class MockObject {
	}

}
