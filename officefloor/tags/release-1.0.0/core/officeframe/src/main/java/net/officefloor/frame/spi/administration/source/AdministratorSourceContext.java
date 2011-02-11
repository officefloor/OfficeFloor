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

package net.officefloor.frame.spi.administration.source;

import java.util.Properties;

/**
 * Context for initialising a {@link AdministratorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorSourceContext {

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws AdministratorSourceUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name)
			throws AdministratorSourceUnknownPropertyError;

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
	 * Properties to configure the {@link AdministratorSource}.
	 * 
	 * @return Properties specific to the {@link AdministratorSource}.
	 */
	Properties getProperties();

	/**
	 * <p>
	 * Should this {@link AdministratorSource} require to obtain various
	 * resources to initialise it should use the returned {@link ClassLoader} to
	 * find them on the class path.
	 * <p>
	 * A possible example of a resource would be an XML configuration file
	 * specific to the {@link AdministratorSource}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

}