/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import net.officefloor.frame.test.OfficeFrameTestCase;

import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.shared.ldap.name.LdapDN;

/**
 * Tests LDAP integration.
 * 
 * @author Daniel Sagenschneider
 */
public class EmbeddedLdapTest extends OfficeFrameTestCase {

	/**
	 * {@link EmbeddedLdap}.
	 */
	private final EmbeddedLdap ldap = new EmbeddedLdap();

	@Override
	protected void setUp() throws Exception {

		// Add partition with appropriate attributes
		this.ldap.addPartition("officefloor", "dc=officefloor,dc=net",
				"objectClass", "ou", "uid");

		// Start LDAP
		this.ldap.start(63636);

		// Inject the OfficeFloor root entry
		ServerEntry entry = this.ldap.newEntry("dc=officefloor,dc=net");
		entry.add("objectClass", "top", "domain", "extensibleObject");
		this.ldap.bindEntry(entry);
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop LDAP
		this.ldap.stop();
	}

	/**
	 * Ensure able to access directly.
	 */
	public void testEmbeddedAccess() throws Exception {
		// Lookup the OfficeFloor entry
		ServerEntry entry = this.ldap.getDirectoryService().getAdminSession()
				.lookup(new LdapDN("dc=officefloor,dc=net"));
		assertNotNull("Expected entry", entry);
		System.out.println("Looked up entry: " + entry);
	}

	/**
	 * Ensure able to access via JNDI.
	 */
	public void testJndiAccess() throws Exception {

		// Create the context
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.setProperty(Context.PROVIDER_URL, "ldap://localhost:63636");
		DirContext context = new InitialDirContext(env);

		// Obtain the entry
		boolean hasValue = false;
		NamingEnumeration<? extends Attribute> attributes = context
				.getAttributes("dc=officefloor,dc=net").getAll();
		while (attributes.hasMore()) {
			Attribute attribute = attributes.next();

			// Output the attribute
			System.out.println("Attribute: " + attribute.getID());
			NamingEnumeration<?> values = attribute.getAll();
			while (values.hasMore()) {
				Object value = values.next();

				// Output value
				System.out.println("   value: " + value);
				hasValue = true;
			}
		}
		assertTrue("Must have a value", hasValue);
	}

}