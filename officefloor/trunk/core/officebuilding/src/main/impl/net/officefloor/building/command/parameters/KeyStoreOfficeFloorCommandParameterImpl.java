/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the key store {@link File}.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyStoreOfficeFloorCommandParameterImpl extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Location of the default key store on the classpath.
	 */
	public static final String DEFAULT_KEY_STORE_CLASSPATH_LOCATION = "config/keystore.jks";

	/**
	 * Initiate.
	 */
	public KeyStoreOfficeFloorCommandParameterImpl() {
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