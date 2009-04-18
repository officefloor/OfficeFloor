/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.officefloor;

import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.ConfigurationContextPropagateError;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorUnknownPropertyError;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeFloorSourceContext} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorSourceContextImpl implements OfficeFloorSourceContext {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public OfficeFloorSourceContextImpl(String officeFloorLocation,
			ConfigurationContext configurationContext,
			PropertyList propertyList, ClassLoader classLoader) {
		this.officeFloorLocation = officeFloorLocation;
		this.configurationContext = configurationContext;
		this.propertyList = propertyList;
		this.classLoader = classLoader;
	}

	/*
	 * ================= OfficeFloorLoaderContext ================================
	 */

	@Override
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public ConfigurationItem getConfiguration(String location) {
		try {
			return this.configurationContext.getConfigurationItem(location);
		} catch (Throwable ex) {
			// Propagate failure to office floor loader
			throw new ConfigurationContextPropagateError(location, ex);
		}
	}

	@Override
	public String[] getPropertyNames() {
		return this.propertyList.getPropertyNames();
	}

	@Override
	public String getProperty(String name) throws OfficeFloorUnknownPropertyError {
		String value = this.getProperty(name, null);
		if (value == null) {
			throw new OfficeFloorUnknownPropertyError("Unknown property '" + name
					+ "'", name);
		}
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		Property property = this.propertyList.getProperty(name);
		String value = (property != null ? property.getValue() : null);
		if (CompileUtil.isBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	@Override
	public Properties getProperties() {
		return this.propertyList.getProperties();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}