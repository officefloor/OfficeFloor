/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.model.impl.repository.classloader;

import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link ConfigurationContext} for a {@link ClassLoader} class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderConfigurationContext implements ConfigurationContext {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ClassLoaderConfigurationContext(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/*
	 * ================== ConfigurationContext ===============================
	 */

	@Override
	public String getLocation() {
		return "CLASS LOADER";
	}

	@Override
	public String[] getClasspath() {
		throw new UnsupportedOperationException(
				"Can not obtain class path from a "
						+ this.getClass().getSimpleName());
	}

	@Override
	public ConfigurationItem getConfigurationItem(String location)
			throws Exception {

		// Obtain the resource on the class path
		InputStream resource = this.classLoader.getResourceAsStream(location);
		if (resource == null) {
			// Not found
			return null;
		}

		// Create the configuration item
		ConfigurationItem item = new ClassLoaderConfigurationItem(location,
				this.classLoader, this, resource);

		// Return the configuration item
		return item;
	}

	@Override
	public ConfigurationItem createConfigurationItem(String id,
			InputStream configuration) throws Exception {
		throw new UnsupportedOperationException(
				"Can not create items on the class path from a "
						+ this.getClass().getSimpleName());
	}

}