/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.store;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.web.security.store.JndiLdapManagedObjectSource.DependencyKeys;

/**
 * Tests the {@link JndiLdapManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiLdapManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JndiLdapManagedObjectSource.class,
				JndiLdapManagedObjectSource.PROPERTY_ALGORITHM, "Algorithm",
				JndiLdapManagedObjectSource.PROPERTY_ENTRY_SEARCH_BASE_DN, "Entry Search Base Dn",
				JndiLdapManagedObjectSource.PROPERTY_ROLES_SEARCH_BASE_DN, "Roles Search Base Dn");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(CredentialStore.class);
		type.addDependency(DependencyKeys.DIR_CONTEXT, DirContext.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JndiLdapManagedObjectSource.class,
				JndiLdapManagedObjectSource.PROPERTY_ALGORITHM, "MD5",
				JndiLdapManagedObjectSource.PROPERTY_ENTRY_SEARCH_BASE_DN, "ou=People,dc=officefloor,dc=net",
				JndiLdapManagedObjectSource.PROPERTY_ROLES_SEARCH_BASE_DN, "ou=Groups,dc=officefloor,dc=net");
	}

	/**
	 * Ensure able to source {@link CredentialStore}.
	 */
	@SuppressWarnings("unchecked")
	public void testSource() throws Throwable {

		// Mocks
		DirContext context = this.createMock(DirContext.class);
		final NamingEnumeration<SearchResult> searchResults = this.createMock(NamingEnumeration.class);
		final Attributes attributes = this.createMock(Attributes.class);

		// Objects
		final SearchResult searchResult = new SearchResult("uid=daniel", null, attributes);
		searchResult.setNameInNamespace("uid=daniel,ou=People,dc=officefloor,dc=net");

		// Record
		this.recordReturn(context,
				context.search("ou=People,dc=officefloor,dc=net", "(&(objectClass=inetOrgPerson)(uid=daniel))", null),
				searchResults);
		this.recordReturn(searchResults, searchResults.hasMore(), true);
		this.recordReturn(searchResults, searchResults.next(), searchResult);

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(JndiLdapManagedObjectSource.PROPERTY_ALGORITHM, "MD5");
		loader.addProperty(JndiLdapManagedObjectSource.PROPERTY_ENTRY_SEARCH_BASE_DN,
				"ou=People,dc=officefloor,dc=net");
		loader.addProperty(JndiLdapManagedObjectSource.PROPERTY_ROLES_SEARCH_BASE_DN,
				"ou=Groups,dc=officefloor,dc=net");
		JndiLdapManagedObjectSource source = loader.loadManagedObjectSource(JndiLdapManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.DIR_CONTEXT, context);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the object
		Object object = managedObject.getObject();
		assertTrue("Object must be of type " + CredentialStore.class, object instanceof CredentialStore);
		CredentialStore store = (CredentialStore) object;

		// Ensure working by obtaining entry
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", "realm");
		assertNotNull("Expect to obtain entry", entry);

		// Verify functionality
		this.verifyMockObjects();
	}

}
