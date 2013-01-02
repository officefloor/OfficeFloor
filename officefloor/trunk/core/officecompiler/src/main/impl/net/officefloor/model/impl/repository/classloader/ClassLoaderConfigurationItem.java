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
package net.officefloor.model.impl.repository.classloader;

import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ReadOnlyConfigurationException;

/**
 * {@link ConfigurationItem} for the {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderConfigurationItem implements ConfigurationItem {

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * {@link InputStream}.
	 */
	private InputStream inputStream;

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Location.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ClassLoaderConfigurationItem(String location, ClassLoader classLoader) {
		this(location, classLoader, new ClassLoaderConfigurationContext(
				classLoader), null);
	}

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            location.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @param inputStream
	 *            {@link InputStream} of the resource.
	 */
	public ClassLoaderConfigurationItem(String location,
			ClassLoader classLoader, ConfigurationContext context,
			InputStream inputStream) {
		this.location = location;
		this.classLoader = classLoader;
		this.context = context;
		this.inputStream = inputStream;
	}

	/*
	 * ====================== ConfigurationItem ================================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public InputStream getConfiguration() throws Exception {

		// Determine if first time asking for configuration
		if (this.inputStream != null) {
			// Return input stream and clear as used
			InputStream stream = this.inputStream;
			this.inputStream = null;
			return stream;
		}

		// Obtain the input stream again and return
		return this.classLoader.getResourceAsStream(this.location);
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		throw new ReadOnlyConfigurationException(
				"Can not change item on class path for a "
						+ this.getClass().getSimpleName());
	}

	@Override
	public ConfigurationContext getContext() {
		return this.context;
	}

}