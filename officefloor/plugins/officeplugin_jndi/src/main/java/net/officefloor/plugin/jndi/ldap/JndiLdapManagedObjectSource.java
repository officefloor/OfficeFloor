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
package net.officefloor.plugin.jndi.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.directory.DirContext;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.jndi.dircontext.JndiDirContextManagedObjectSource;

/**
 * {@link ManagedObjectSource} for a JNDI LDAP {@link DirContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiLdapManagedObjectSource extends
		JndiDirContextManagedObjectSource {

	/*
	 * ======================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(Context.PROVIDER_URL, "LDAP URL");
	}

	/*
	 * ================= JndiDirContextManagedObjectSource ==================
	 */

	@Override
	protected Properties getProperties(ManagedObjectSourceContext<None> context)
			throws Exception {

		// Obtain the properties
		Properties properties = context.getProperties();

		// Add LDAP details
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		// Return the properties
		return properties;
	}

}