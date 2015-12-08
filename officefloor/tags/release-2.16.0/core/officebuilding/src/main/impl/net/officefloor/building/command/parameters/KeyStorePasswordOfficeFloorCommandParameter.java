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

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the password to the key store
 * {@link File}.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyStorePasswordOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {
	
	/**
	 * Default key store password.
	 */
	public static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

	/**
	 * Initiate.
	 */
	public KeyStorePasswordOfficeFloorCommandParameter() {
		super("key_store_password", "kp", "Password to the key store file");
	}

	/**
	 * Obtains password to the key store {@link File}.
	 * 
	 * @return Password to the key store {@link File}.
	 */
	public String getKeyStorePassword() {
		return this.getValue();
	}

}