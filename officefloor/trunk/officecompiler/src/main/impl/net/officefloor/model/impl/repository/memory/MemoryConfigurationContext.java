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
package net.officefloor.model.impl.repository.memory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link ConfigurationContext} that stores content in memory only (not
 * persisting it).
 * 
 * @author Daniel Sagenschneider
 */
public class MemoryConfigurationContext implements ConfigurationContext {

	/**
	 * {@link MemoryConfigurationItem} instances by their location.
	 */
	private final Map<String, MemoryConfigurationItem> items = new HashMap<String, MemoryConfigurationItem>();

	/*
	 * ================= ConfigurationContext ================================
	 */

	@Override
	public String getLocation() {
		return "MEMORY";
	}

	@Override
	public ConfigurationItem createConfigurationItem(String relativeLocation,
			InputStream configuration) throws Exception {
		// Create, load, and register the configuration item
		MemoryConfigurationItem item = new MemoryConfigurationItem(
				relativeLocation, this);
		item.setConfiguration(configuration);
		this.items.put(relativeLocation, item);

		// Return the item
		return item;
	}

	@Override
	public String[] getClasspath() {
		// No class path for memory configuration
		return new String[0];
	}

	@Override
	public ConfigurationItem getConfigurationItem(String relativeLocation)
			throws Exception {
		return this.items.get(relativeLocation);
	}

}