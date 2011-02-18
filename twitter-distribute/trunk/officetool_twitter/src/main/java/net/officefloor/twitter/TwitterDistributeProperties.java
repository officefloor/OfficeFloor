/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Twitter distribute properties.
 * 
 * @author Daniel Sagenschneider
 */
public class TwitterDistributeProperties extends Properties {

	/**
	 * Initiate.
	 * 
	 * @throws IOException
	 *             If fails to load properties.
	 */
	public TwitterDistributeProperties() throws IOException {
		// Load properties from twitter-distribute.properties in user directory
		File propertiesFile = new File(System.getProperty("user.home"),
				"twitter-distribute.properties");
		if (!propertiesFile.isFile()) {
			throw new FileNotFoundException("Can not find properties file: "
					+ propertiesFile.getPath());
		}
		this.load(new FileReader(propertiesFile));
	}

	/**
	 * Obtains the ensured property.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value for the property.
	 * @throws IOException
	 *             If property not configured.
	 */
	public String getEnsuredProperty(String name) throws IOException {
		String value = this.getProperty(name);
		if (value == null) {
			throw new IOException("Property '" + name + "' not configured");
		}
		return value;
	}

}