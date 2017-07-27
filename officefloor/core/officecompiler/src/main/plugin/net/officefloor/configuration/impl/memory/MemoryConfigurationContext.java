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
package net.officefloor.configuration.impl.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.AbstractWritableConfigurationContext;

/**
 * {@link ConfigurationContext} that stores content in memory only (not
 * persisting it).
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryConfigurationContext extends AbstractWritableConfigurationContext {

	/**
	 * Convenience method to create a {@link WritableConfigurationItem}.
	 * 
	 * @param location
	 *            Location.
	 * @return {@link WritableConfigurationItem}.
	 * @throws IOException
	 *             If fails to create {@link WritableConfigurationItem}.
	 */
	public static WritableConfigurationItem createWritableConfigurationItem(String location) throws IOException {
		return new MemoryConfigurationContext().createConfigurationItem("location",
				new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * In memory configuration by location.
	 */
	private final Map<String, byte[]> items = new HashMap<String, byte[]>();

	/**
	 * Instantiate.
	 */
	public MemoryConfigurationContext() {
		this.init((location) -> {

			// Obtain the configuration
			byte[] content = this.items.get(location);
			return (content == null ? null : new ByteArrayInputStream(content));

		}, (location, isCreate, configuration) -> {

			// Obtain the configuration data
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int byteValue = configuration.read(); byteValue >= 0; byteValue = configuration.read()) {
				buffer.write(byteValue);
			}
			buffer.flush();

			// Load the configuration into memory
			this.items.put(location, buffer.toByteArray());

		}, (location) -> {

			// Remove configuration from memory
			this.items.remove(location);
		});
	}

}