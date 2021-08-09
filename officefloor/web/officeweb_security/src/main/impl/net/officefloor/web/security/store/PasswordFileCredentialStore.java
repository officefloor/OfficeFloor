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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

/**
 * Password file implementation of {@link CredentialStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordFileCredentialStore implements CredentialStore {

	/**
	 * <p>
	 * Loads the {@link PasswordFile} from the raw file.
	 * <p>
	 * The password file must be of the form:
	 * 
	 * <pre>
	 * # comment line
	 * algorithm=[algorithm]
	 * [UserId]:[Credentials]:[Role],[Role]
	 * </pre>
	 * 
	 * <p>
	 * where items in brackets, [x], should be replaced with appropriate
	 * information.
	 * <p>
	 * The algorithm must be specified as first data line. Typically the
	 * [algorithm] value would be <code>MD5</code>.
	 * <p>
	 * The may be many <code>UserId</code> entry lines with the user having many
	 * <code>Role</code>'s defined separated by commas (,).
	 * <p>
	 * The binary credentials must be <code>Base64</code> encoded.
	 * 
	 * @param rawFile
	 *            Raw file containing the password details.
	 * @return Populated {@link PasswordFile} from the raw file.
	 * @throws IOException
	 *             If failure reading the password {@link File}.
	 */
	public static PasswordFile loadPasswordFile(File rawFile)
			throws IOException {

		// Access the file data
		BufferedReader reader = new BufferedReader(new FileReader(rawFile));

		// Password file
		PasswordFile file = null;

		try {
			// Populate the password file
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {

				// Trim the line for parsing
				line = line.trim();

				// Ignore blank line
				if (line.length() == 0) {
					continue;
				}

				// Ignore comments
				if (line.startsWith("#")) {
					continue;
				}

				// First data line is expected to be algorithm line
				if (file == null) {
					// Obtain algorithm from first data line
					String[] details = line.split("=");
					if (details.length != 2) {
						throw new IOException(
								"Must provide algorithm definition first");
					}
					String propertyName = details[0].trim();
					if (!"algorithm".equalsIgnoreCase(propertyName)) {
						throw new IOException(
								"Must provide algorithm definition first");
					}
					String algorithm = details[1].trim();

					// Algorithm obtained, instantiate password file to be
					// populated
					file = new PasswordFile(algorithm);

					// Algorithm specified
					continue;
				}

				// Parse out the entry details
				String[] details = line.split(":");
				if (details.length < 2) {
					continue; // ignore line with not enough details
				}
				String userId = details[0].trim();
				String credentials = details[1].trim();
				Set<String> roles = new HashSet<String>();
				if (details.length > 2) {
					for (String role : details[2].split(",")) {
						role = role.trim();
						roles.add(role);
					}
				}

				// Decode the credentials
				byte[] credentialData = Base64.decodeBase64(credentials);
				if ((credentialData == null) || (credentialData.length == 0)) {
					continue; // ignore invalid entry
				}

				// Add the password entry
				file.addEntry(userId, credentialData, roles);
			}
		} finally {
			// Ensure close reader
			reader.close();
		}

		// Must have at least specified the algorithm
		if (file == null) {
			throw new IOException("Must provide algorithm definition first");
		}

		// Return the password file
		return file;
	}

	/**
	 * {@link PasswordFile}.
	 */
	private PasswordFile file;

	/**
	 * Initiate.
	 * 
	 * @param file
	 *            {@link PasswordFile}.
	 */
	public PasswordFileCredentialStore(PasswordFile file) {
		this.file = file;
	}

	/*
	 * ================= CredentialStore =========================
	 */

	@Override
	public String getAlgorithm() {
		return this.file.getAlgorithm();
	}

	@Override
	public CredentialEntry retrieveCredentialEntry(String userId, String realm) {

		// Obtain the entry
		PasswordEntry entry = this.file.getEntry(userId);

		// Return the entry
		return entry;
	}

}
