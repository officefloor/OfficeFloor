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
package net.officefloor.compile.spi.officefloor.source;

import java.util.Properties;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Context for the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSourceContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeFloor}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the
	 * {@link OfficeFloorSource}, however passing it to
	 * {@link #getConfiguration(String)} should return a
	 * {@link ConfigurationItem}.
	 * 
	 * @return Location of the {@link OfficeFloor}.
	 */
	String getOfficeFloorLocation();

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param location
	 *            Location of the {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem} or <code>null</code> if can not find
	 *         the {@link ConfigurationItem}.
	 */
	ConfigurationItem getConfiguration(String location);

	/**
	 * <p>
	 * Obtains the names of the available properties in the order they were
	 * defined. This allows for ability to provide variable number of properties
	 * identified by a naming convention and being able to maintain their order.
	 * <p>
	 * An example would be providing a listing of routing configurations, each
	 * entry named <code>route.[something]</code> and order indicating priority.
	 * 
	 * @return Names of the properties in the order defined.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws OfficeFloorUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name) throws OfficeFloorUnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the {@link OfficeFloorSource}.
	 * 
	 * @return Properties specific for the {@link OfficeFloorSource}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ClassLoader} for the {@link OfficeFloorSource}.
	 * 
	 * @return {@link ClassLoader} for the {@link OfficeFloorSource}.
	 */
	ClassLoader getClassLoader();

}