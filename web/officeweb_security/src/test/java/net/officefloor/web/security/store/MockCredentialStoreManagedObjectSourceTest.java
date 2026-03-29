/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.store;

import java.security.MessageDigest;
import java.util.Set;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.security.scheme.DigestHttpSecuritySource;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Tests the {@link MockCredentialStoreManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockCredentialStoreManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(MockCredentialStoreManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Exception {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(CredentialStore.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, MockCredentialStoreManagedObjectSource.class);
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
		assertTrue("Object should be " + CredentialStore.class.getSimpleName(), object instanceof CredentialStore);
		CredentialStore store = (CredentialStore) object;
		assertNull("Should be no algorithm", store.getAlgorithm());

		// Ensure handle single role
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		assertEquals("Incorrect credentials", "daniel",
				new String(entry.retrieveCredentials(), AbstractHttpSecuritySource.UTF_8));
		Set<String> roles = entry.retrieveRoles();
		assertEquals("Incorrect number of roles", 1, roles.size());
		assertTrue("Incorrect role", roles.contains("daniel"));

		// Ensure handle multiple roles
		entry = store.retrieveCredentialEntry("daniel, founder", null);
		assertEquals("Incorrect credentials", "daniel, founder",
				new String(entry.retrieveCredentials(), AbstractHttpSecuritySource.UTF_8));
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

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(MockCredentialStoreManagedObjectSource.PROPERTY_ALGORITHM, "MD5");
		MockCredentialStoreManagedObjectSource source = loader
				.loadManagedObjectSource(MockCredentialStoreManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Ensure appropriate credential store
		Object object = managedObject.getObject();
		assertTrue("Object should be " + CredentialStore.class.getSimpleName(), object instanceof CredentialStore);
		CredentialStore store = (CredentialStore) object;
		assertEquals("Incorrect algorithm", "MD5", store.getAlgorithm());

		// Determine the encrypted password
		MessageDigest digest = CredentialStoreUtil.createDigest("MD5");
		byte[] expectedPassword = digest.digest("daniel".getBytes(AbstractHttpSecuritySource.UTF_8));

		// Ensure provide password encrypted with algorithm
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		byte[] actualPassword = entry.retrieveCredentials();
		assertEquals("Incorrect number of bytes for password", expectedPassword.length, actualPassword.length);
		for (int i = 0; i < expectedPassword.length; i++) {
			assertEquals("Incorrect password", expectedPassword[i], actualPassword[i]);
		}
		Set<String> roles = entry.retrieveRoles();
		assertEquals("Incorrect number of roles", 1, roles.size());
		assertTrue("Incorrect role", roles.contains("daniel"));
	}

}
