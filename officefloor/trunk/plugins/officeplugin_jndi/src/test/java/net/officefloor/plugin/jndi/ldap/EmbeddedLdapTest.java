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

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.name.Dn;

/**
 * Tests LDAP integration.
 * 
 * @author Daniel Sagenschneider
 */
public class EmbeddedLdapTest extends AbstractLdapTest {

	@Override
	protected void setUp() throws Exception {
		this.setupLdap();
	}

	/**
	 * Ensure able to access directly.
	 */
	public void testEmbeddedAccess() throws Exception {
		// Lookup the OfficeFloor entry
		Entry entry = this.ldap.getDirectoryService().getAdminSession()
				.lookup(new Dn("dc=officefloor,dc=net"));
		assertNotNull("Expected entry", entry);
		System.out.println("Looked up entry: " + entry);
	}

	/**
	 * Ensure able to access via JNDI.
	 */
	public void testJndiAccess() throws Exception {

		// Create the context
		DirContext context = this.ldap.getDirContext();

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

	/**
	 * Ensure to undertake simple login via JNDI.
	 */
	public void testSimpleJndiLogin() throws Exception {

		// Create the context
		DirContext context = this.ldap.getDirContext("simple",
				"uid=daniel,ou=People,dc=officefloor,dc=net", "password");

		// Obtain the entry
		DirContext officeFloor = (DirContext) context
				.lookup("dc=officefloor,dc=net");
		assertNotNull("Expecting OfficeFloor domain context", officeFloor);
	}

	/**
	 * Ensure to undertake <code>DIGEST-MD5</code> SASL login via JNDI.
	 */
	public void testDigestMd5JndiLogin() throws Exception {

		// Create the context
		DirContext context = this.ldap.getDirContext("DIGEST-MD5", "daniel",
				"password", "java.naming.security.sasl.realm", "officefloor");

		// Obtain the entry
		DirContext officeFloor = (DirContext) context
				.lookup("dc=officefloor,dc=net");
		assertNotNull("Expecting OfficeFloor domain context", officeFloor);
	}
}