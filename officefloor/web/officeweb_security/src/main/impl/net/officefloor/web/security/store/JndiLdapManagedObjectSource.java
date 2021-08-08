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

import javax.naming.directory.DirContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link JndiLdapCredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiLdapManagedObjectSource
		extends AbstractManagedObjectSource<JndiLdapManagedObjectSource.DependencyKeys, None> {

	/**
	 * Name of property for the algorithm.
	 */
	public static final String PROPERTY_ALGORITHM = "jndi.ldap.store.algorithm";

	/**
	 * Name of property for the search base Dn for entries.
	 */
	public static final String PROPERTY_ENTRY_SEARCH_BASE_DN = "jndi.ldap.store.entry.search.base.dn";

	/**
	 * Name of property for the search base Dn for roles.
	 */
	public static final String PROPERTY_ROLES_SEARCH_BASE_DN = "jndi.ldap.store.roles.search.base.dn";

	/**
	 * Keys for dependencies.
	 */
	public static enum DependencyKeys {
		DIR_CONTEXT
	}

	/**
	 * Algorithm.
	 */
	private String algorithm;

	/**
	 * Entry search base Dn.
	 */
	private String entrySearchBaseDn;

	/**
	 * Roles search base Dn.
	 */
	private String rolesSearchBaseDn;

	/*
	 * ==================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_ALGORITHM, "Algorithm");
		context.addProperty(PROPERTY_ENTRY_SEARCH_BASE_DN, "Entry Search Base Dn");
		context.addProperty(PROPERTY_ROLES_SEARCH_BASE_DN, "Roles Search Base Dn");
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the properties
		this.algorithm = mosContext.getProperty(PROPERTY_ALGORITHM);
		this.entrySearchBaseDn = mosContext.getProperty(PROPERTY_ENTRY_SEARCH_BASE_DN);
		this.rolesSearchBaseDn = mosContext.getProperty(PROPERTY_ROLES_SEARCH_BASE_DN);

		// Add dependency on DirContext
		context.addDependency(DependencyKeys.DIR_CONTEXT, DirContext.class);

		// Specify details of object
		context.setObjectClass(CredentialStore.class);
		context.setManagedObjectClass(JndiLdapManagedObject.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JndiLdapManagedObject();
	}

	/**
	 * {@link ManagedObject} for JNDI LDAP {@link CredentialStore}.
	 */
	private class JndiLdapManagedObject implements CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link CredentialStore}.
		 */
		private CredentialStore store;

		/*
		 * ============== CoordinatingManagedObject ========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {

			// Obtain the DirContext for access to LDAP via JNDI
			DirContext context = (DirContext) registry.getObject(DependencyKeys.DIR_CONTEXT);

			// Create the JNDI LDAP Credential Store
			this.store = new JndiLdapCredentialStore(JndiLdapManagedObjectSource.this.algorithm, context,
					JndiLdapManagedObjectSource.this.entrySearchBaseDn,
					JndiLdapManagedObjectSource.this.rolesSearchBaseDn);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.store;
		}
	}

}
