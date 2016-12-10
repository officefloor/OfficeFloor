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

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract LDAP test.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractLdapTest extends OfficeFrameTestCase {

	/**
	 * Port for the {@link EmbeddedLdap}.
	 */
	protected static final int LDAP_PORT = 63636;

	/**
	 * URL to the {@link EmbeddedLdap}.
	 */
	protected static final String LDAP_URL = "ldap://localhost:" + LDAP_PORT;

	/**
	 * Domain within {@link EmbeddedLdap} to use.
	 */
	protected static final String LDAP_DOMAIN = "dc=officefloor,dc=net";

	/**
	 * {@link EmbeddedLdap}.
	 */
	protected EmbeddedLdap ldap = null;

	/**
	 * Sets up the {@link EmbeddedLdap} instance.
	 */
	protected void setupLdap() throws Exception {
		// Start the LDAP server
		this.ldap = new EmbeddedLdap();
		this.ldap.addPartition("OfficeFloor", LDAP_DOMAIN, "objectClass");
		this.ldap.start(LDAP_PORT);

		// Populate the LDAP server
		this.ldap.addCredentialStoreEntries();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop LDAP (if used)
		if (this.ldap != null) {
			this.ldap.stop();
		}
	}

}