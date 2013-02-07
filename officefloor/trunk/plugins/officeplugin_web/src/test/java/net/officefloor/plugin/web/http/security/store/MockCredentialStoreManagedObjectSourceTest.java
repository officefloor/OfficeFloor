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
package net.officefloor.plugin.web.http.security.store;

import java.util.Set;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource;

/**
 * Tests the {@link MockCredentialStoreManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockCredentialStoreManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(MockCredentialStoreManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Exception {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(CredentialStore.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				MockCredentialStoreManagedObjectSource.class);
	}

	/**
	 * Ensure can obtain the {@link CredentialStore}.
	 */
	public void testCredentialStore() throws Throwable {

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		MockCredentialStoreManagedObjectSource source = loader
				.loadManagedObjectSource(MockCredentialStoreManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Ensure appropriate credential store
		Object object = managedObject.getObject();
		assertTrue("Object should be " + CredentialStore.class.getSimpleName(),
				object instanceof CredentialStore);
		CredentialStore store = (CredentialStore) object;

		// Ensure handle single role
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		Set<String> roles = entry.retrieveRoles();
		assertEquals("Incorrect number of roles", 1, roles.size());
		assertTrue("Incorrect role", roles.contains("daniel"));

		// Ensure handle multiple roles
		entry = store.retrieveCredentialEntry("daniel, founder", null);
		roles = entry.retrieveRoles();
		assertEquals("Incorrect number of roles", 2, roles.size());
		assertTrue("Must have role daniel", roles.contains("daniel"));
		assertTrue("Must have role founder", roles.contains("founder"));
	}

	/**
	 * Ensure can user algorithm. Necessary for mocking with
	 * {@link DigestHttpSecuritySource}.
	 */
	public void testCredentialStoreWithAlgorithm() throws Throwable {
		fail("TODO implement");
	}

}