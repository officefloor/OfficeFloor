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
package net.officefloor.model.impl.repository.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link ConfigurationItem} that stores content in memory only (not persisting
 * it).
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryConfigurationItem implements ConfigurationItem {

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * {@link ConfigurationContext}.
	 */
	private ConfigurationContext context = null;

	/**
	 * Contents of this {@link ConfigurationItem}.
	 */
	private byte[] contents = null;

	/**
	 * Default constructor for simple use.
	 */
	public MemoryConfigurationItem() {
		this.location = this.getClass().getSimpleName();
	}

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Location.
	 */
	public MemoryConfigurationItem(String location) {
		this.location = location;
	}

	/**
	 * Initiate.
	 * 
	 * @param location
	 *            Location.
	 * @param context
	 *            {@link ConfigurationContext}.
	 */
	public MemoryConfigurationItem(String location, ConfigurationContext context) {
		this.location = location;
		this.context = context;
	}

	/*
	 * ================= ConfigurationItem ================================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public ConfigurationContext getContext() {

		// Lazy create a context if not provided one
		if (this.context == null) {
			this.context = new MemoryConfigurationContext();
		}

		// Return the context
		return this.context;
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		try {
			// Copy the configuration into memory
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int value = configuration.read(); value != -1; value = configuration
					.read()) {
				buffer.write(value);
			}
			this.contents = buffer.toByteArray();
		} finally {
			configuration.close();
		}
	}

	@Override
	public InputStream getConfiguration() throws Exception {
		return new ByteArrayInputStream(this.contents != null ? this.contents
				: new byte[0]);
	}

}