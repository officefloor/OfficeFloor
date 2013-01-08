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
package net.officefloor.building.command.parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the key store {@link File}.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyStoreOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Location of the default key store on the classpath.
	 */
	public static final String DEFAULT_KEY_STORE_CLASSPATH_LOCATION = "config/keystore.jks";

	/**
	 * <p>
	 * Makes the default key store file available.
	 * <p>
	 * This is only for development and testing. Within production the keys
	 * should be changed and therefore the default key store file will not be
	 * useful.
	 * 
	 * @return Key store {@link File}.
	 * @throws IOException
	 *             If fails to obtain the key store {@link File}.
	 */
	public static File getDefaultKeyStoreFile() throws IOException {

		// Obtain location for the key store file
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File keyStore = new File(tempDir, "officefloorkeystore.jks");

		// Ensure the key store file exists
		if (!(keyStore.exists())) {
			// Create the file
			InputStream contents = Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							KeyStoreOfficeFloorCommandParameter.DEFAULT_KEY_STORE_CLASSPATH_LOCATION);
			FileOutputStream output = new FileOutputStream(keyStore, false);
			for (int value = contents.read(); value != -1; value = contents
					.read()) {
				output.write(value);
			}
			output.close();
			contents.close();
		}

		// Return the key store file
		return keyStore;
	}

	/**
	 * Initiate.
	 */
	public KeyStoreOfficeFloorCommandParameter() {
		super("key_store", "ks", "Location of the key store file");
	}

	/**
	 * Obtains the key store {@link File}.
	 * 
	 * @return Key store {@link File}.
	 * @throws FileNotFoundException
	 *             If not specified or key store {@link File} not found.
	 */
	public File getKeyStore() throws FileNotFoundException {

		// Ensure have key store
		String keyStoreLocation = this.getValue();
		if ((keyStoreLocation == null)
				|| (keyStoreLocation.trim().length() == 0)) {
			throw new FileNotFoundException("Key store file must be specified");
		}

		// Ensure the key store file exists
		File keyStore = new File(keyStoreLocation);
		if (!(keyStore.exists())) {
			throw new FileNotFoundException("Can not find key store file at "
					+ keyStore.getAbsolutePath());
		}

		// Return the key store file
		return keyStore;
	}

}