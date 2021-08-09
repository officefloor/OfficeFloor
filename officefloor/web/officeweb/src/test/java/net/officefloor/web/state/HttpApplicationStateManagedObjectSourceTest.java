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

import java.util.Iterator;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link HttpApplicationStateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpApplicationStateManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		// Should require no properties
		ManagedObjectLoaderUtil.validateSpecification(new HttpApplicationStateManagedObjectSource(null));
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpApplicationState.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, new HttpApplicationStateManagedObjectSource(null));
	}

	/**
	 * Validates use.
	 */
	public void testLoadAndUse() throws Throwable {

		// Load the source
		HttpApplicationStateManagedObjectSource source = new HttpApplicationStateManagedObjectSource(null);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(source);

		// Ensure correct object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpApplicationState);
		HttpApplicationState state = (HttpApplicationState) object;

		// Set, get, name attributes
		final String NAME = "name";
		final Object ATTRIBUTE = "ATTRIBUTE";
		state.setAttribute(NAME, ATTRIBUTE);
		assertEquals("Must obtain attribute", ATTRIBUTE, state.getAttribute(NAME));
		Iterator<String> names = state.getAttributeNames();
		assertTrue("Expect name", names.hasNext());
		assertEquals("Incorrect name", NAME, names.next());
		assertFalse("Expect only one name", names.hasNext());

		// Source another managed object as should be same state
		HttpApplicationState another = (HttpApplicationState) user.sourceManagedObject(source).getObject();
		assertEquals("Should be same state", ATTRIBUTE, another.getAttribute(NAME));

		// Ensure can remove attribute
		state.removeAttribute(NAME);
		assertNull("Attribute should be removed", state.getAttribute(NAME));

		// Ensure also removed from the another state
		assertNull("Also removed from another state", state.getAttribute(NAME));
	}

}
