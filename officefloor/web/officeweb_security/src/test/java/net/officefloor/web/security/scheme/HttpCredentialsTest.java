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

package net.officefloor.web.security.scheme;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Tests the {@link HttpCredentialsImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCredentialsTest extends OfficeFrameTestCase {

	/**
	 * Ensure provides values.
	 */
	public void testWithValues() {
		this.doTest("daniel", "password");
	}

	/**
	 * Ensure can handle <code>null</code> values.
	 */
	public void testWithNull() {
		this.doTest(null, null);
	}

	/**
	 * Ensure can have blank values.
	 */
	public void testWithBlanks() {
		this.doTest("", "");
	}

	/**
	 * Ensure can construct with <code>null</code> values.
	 */
	public void doTest(String username, String password) {

		// Create the HTTP credentials
		HttpCredentials credentials = new HttpCredentialsImpl(username, password);

		// Validate the credentials
		assertEquals("Incorrect username", username, credentials.getUsername());
		if (password == null) {
			assertNull("Should not have password", credentials.getPassword());
		} else {
			String actualPassword = new String(credentials.getPassword(), AbstractHttpSecuritySource.UTF_8);
			assertEquals("Incorrect password", password, actualPassword);
		}
	}

}
