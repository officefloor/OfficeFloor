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

package net.officefloor.plugin.web.http.application;

import java.io.Serializable;
import java.util.Iterator;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link HttpRequestStateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestStateManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		// Should require no properties
		ManagedObjectLoaderUtil
				.validateSpecification(HttpRequestStateManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpRequestState.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				HttpRequestStateManagedObjectSource.class);
	}

	/**
	 * Validates use.
	 */
	public void testLoadAndUse() throws Throwable {

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		HttpRequestStateManagedObjectSource source = loader
				.loadManagedObjectSource(HttpRequestStateManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(source);

		// Ensure correct object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof HttpRequestState);
		HttpRequestState state = (HttpRequestState) object;

		// Set, get, name attributes
		final String NAME = "name";
		final Serializable ATTRIBUTE = "ATTRIBUTE";
		state.setAttribute(NAME, ATTRIBUTE);
		assertEquals("Must obtain attribute", ATTRIBUTE,
				state.getAttribute(NAME));
		Iterator<String> names = state.getAttributeNames();
		assertTrue("Expect name", names.hasNext());
		assertEquals("Incorrect name", NAME, names.next());
		assertFalse("Expect only one name", names.hasNext());

		// Source another managed object as should be new empty state
		HttpRequestState another = (HttpRequestState) user.sourceManagedObject(
				source).getObject();
		assertNull("Should be new empty state", another.getAttribute(NAME));

		// Ensure can remove attribute
		state.removeAttribute(NAME);
		assertNull("Attribute should be removed", state.getAttribute(NAME));
	}

}