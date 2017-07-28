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
package net.officefloor.plugin.jndi.dircontext;

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.jndi.ldap.AbstractLdapTest;

/**
 * Tests the {@link JndiDirContextManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiDirContextManagedObjectSourceTest extends AbstractLdapTest {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(JndiDirContextManagedObjectSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(DirContext.class);

		// Validate the type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, JndiDirContextManagedObjectSource.class);
	}

	/**
	 * Ensure able to obtain {@link DirContext}.
	 */
	public void testDirContext() throws Throwable {

		// Setup LDAP
		this.setupLdap();

		// Obtain the JNDI Context Managed Object Source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		loader.addProperty(Context.PROVIDER_URL, LDAP_URL);
		JndiDirContextManagedObjectSource mos = loader.loadManagedObjectSource(JndiDirContextManagedObjectSource.class);

		// Obtain the Managed Object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the DirContext
		Object object = mo.getObject();
		assertNotNull("Must have DirContext", object);
		assertTrue("Must be DirContext type", object instanceof DirContext);
		assertTrue("Must be Synchronised DirContext", object instanceof SynchronisedDirContext);

		// Ensure can obtain the entry
		DirContext context = (DirContext) object;
		Attributes attributes = context.getAttributes(LDAP_DOMAIN);
		assertNotNull("Must be able to obtain attribute", attributes);
	}

	/**
	 * Ensure able to obtain sub {@link DirContext}.
	 */
	public void testSubDirContext() throws Throwable {

		// Setup LDAP
		this.setupLdap();

		// Obtain the JNDI Context Managed Object Source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		loader.addProperty(Context.PROVIDER_URL, LDAP_URL);
		loader.addProperty(JndiDirContextManagedObjectSource.PROPERTY_SUB_CONTEXT_NAME, LDAP_DOMAIN);
		JndiDirContextManagedObjectSource mos = loader.loadManagedObjectSource(JndiDirContextManagedObjectSource.class);

		// Obtain the Managed Object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the DirContext
		Object object = mo.getObject();
		assertNotNull("Must have DirContext", object);
		assertTrue("Must be DirContext type", object instanceof DirContext);
		assertTrue("Must be Synchronised DirContext", object instanceof SynchronisedDirContext);

		// Ensure can obtain the entry
		DirContext context = (DirContext) object;
		Attributes attributes = context.getAttributes("");
		assertNotNull("Must be able to obtain attribute", attributes);
	}

}