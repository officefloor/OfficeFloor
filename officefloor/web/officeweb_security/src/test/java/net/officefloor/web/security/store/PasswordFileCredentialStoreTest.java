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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Tests the {@link PasswordFileCredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordFileCredentialStoreTest extends OfficeFrameTestCase {

	/**
	 * Algorithm.
	 */
	public static final String ALGORITHM = "MD5";

	/**
	 * Name of the password file for testing content.
	 */
	public static final String PASSWORD_FILE_NAME = "password-file.txt";

	/**
	 * Logs the credentials to put into the password file.
	 */
	public void testShowCredentials() {
		assertEquals("Incorrect credentials", "Y3JlZGVudGlhbHM=",
				Base64.encodeBase64String("credentials".getBytes(AbstractHttpSecuritySource.UTF_8)).trim());
	}

	/**
	 * Ensure can load the password file data.
	 */
	public void testLoadPasswordFile() throws Exception {

		// Load the password file
		File rawFile = this.findFile(this.getClass(), PASSWORD_FILE_NAME);
		PasswordFile file = PasswordFileCredentialStore.loadPasswordFile(rawFile);

		// Validate password file algorithm
		assertEquals("Incorrect credential algorithm", ALGORITHM, file.getAlgorithm());

		// Validate the password file entries
		assertPasswordEntry(file.getEntry("daniel"), "daniel", "credentials", "founder", "administrator", "developer");
		assertPasswordEntry(file.getEntry("melanie"), "melanie", "credentials", "wife");
		assertPasswordEntry(file.getEntry("zeddy"), "zeddy", "credentials", "cat");
		assertNull("Expected no invalid entry", file.getEntry("invalid"));
		assertNull("Expected missing entry", file.getEntry("MissingEntry"));
	}

	/**
	 * Ensure can handle empty password file.
	 */
	public void testEmptyPasswordFile() {
		try {
			this.createCredentialStore("empty-file.txt");
			fail("Should not be successful");
		} catch (IOException ex) {
			assertEquals("Incorrect cause", "Must provide algorithm definition first", ex.getMessage());
		}
	}

	/**
	 * Ensure fails to load {@link PasswordFile} if missing
	 * <code>algorithm</code> definition.
	 */
	public void testMissingAlgorithmDefinition() {
		try {
			this.createCredentialStore("no-algorithm-definition.txt");
			fail("Should not be successful");
		} catch (IOException ex) {
			assertEquals("Incorrect cause", "Must provide algorithm definition first", ex.getMessage());
		}
	}

	/**
	 * Ensure can handle password file with no entries.
	 */
	public void testNoEntries() throws IOException {
		PasswordFileCredentialStore store = this.createCredentialStore("no-entries.txt");
		assertNotNull("Expecting store (even if no entries)", store);
	}

	/**
	 * Ensure correct algorithm.
	 */
	public void testAlgorithm() throws Exception {
		CredentialStore store = this.createCredentialStore(PASSWORD_FILE_NAME);
		assertEquals("Incorrect algorithm", ALGORITHM, store.getAlgorithm());
	}

	/**
	 * Ensure appropriately retrieves {@link CredentialEntry} instances.
	 */
	public void testRetrieveEntry() throws Exception {
		CredentialStore store = this.createCredentialStore(PASSWORD_FILE_NAME);
		assertNotNull("Expect to find daniel", store.retrieveCredentialEntry("daniel", null));
		assertNotNull("Expect to find daniel in a realm", store.retrieveCredentialEntry("daniel", "realm"));
		assertNull("Should not find unknown entry", store.retrieveCredentialEntry("unknown", null));
	}

	/**
	 * Ensure correct credentials.
	 */
	public void testCredentials() throws Exception {
		// Validate credentials
		CredentialStore store = this.createCredentialStore(PASSWORD_FILE_NAME);
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		byte[] credentials = entry.retrieveCredentials();
		assertCredentials("credentials", credentials);
	}

	/**
	 * Ensure correct roles.
	 */
	public void testRoles() throws Exception {
		// Validate the roles
		CredentialStore store = this.createCredentialStore(PASSWORD_FILE_NAME);
		CredentialEntry entry = store.retrieveCredentialEntry("daniel", null);
		Set<String> roles = entry.retrieveRoles();
		assertRoles(roles, "founder", "administrator", "developer");
	}

	/**
	 * Creates the {@link PasswordFileCredentialStore} for the file.
	 * 
	 * @param passwordFileName
	 *            Name of the file containing the password details.
	 * @return {@link PasswordFileCredentialStore}.
	 */
	private PasswordFileCredentialStore createCredentialStore(String passwordFileName) throws IOException {
		// Load the password file
		File rawFile = this.findFile(this.getClass(), passwordFileName);
		PasswordFile file = PasswordFileCredentialStore.loadPasswordFile(rawFile);

		// Create the credential store
		PasswordFileCredentialStore store = new PasswordFileCredentialStore(file);

		// Return the credential store
		return store;
	}

	/**
	 * Asserts the {@link PasswordEntry}.
	 * 
	 * @param entry
	 *            {@link PasswordEntry}.
	 * @param userId
	 *            Expected user Id.
	 * @param credentials
	 *            Expected credentials.
	 * @param roles
	 *            Expected roles.
	 */
	private static void assertPasswordEntry(PasswordEntry entry, String userId, String credentials, String... roles) {
		try {
			// Ensure correct user Id
			assertEquals("Incorrect user Id", userId, entry.getUserId());

			// Ensure correct credentials
			byte[] entryCredentialsData = entry.retrieveCredentials();
			assertCredentials(credentials, entryCredentialsData);

			// Ensure correct roles
			Set<String> entryRoles = entry.retrieveRoles();
			assertRoles(entryRoles, roles);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Asserts the credentials.
	 * 
	 * @param expectedCredentials
	 *            Expected credentials.
	 * @param actualCredentials
	 *            Actual credentials.
	 */
	private static void assertCredentials(String expectedCredentials, byte[] actualCredentials) {
		String actualCredentialsText = new String(actualCredentials, AbstractHttpSecuritySource.UTF_8);
		assertEquals("Incorrect credentials", expectedCredentials, actualCredentialsText);
	}

	/**
	 * Asserts the set of roles.
	 * 
	 * @param actualRoles
	 *            Actual roles.
	 * @param expectedRoles
	 *            Expected roles.
	 */
	private static void assertRoles(Set<String> actualRoles, String... expectedRoles) {
		assertEquals("Incorrect number of roles", expectedRoles.length, actualRoles.size());
		for (String role : expectedRoles) {
			assertTrue("Expected to have role: '" + role + "'", actualRoles.contains(role));
		}
	}

}
