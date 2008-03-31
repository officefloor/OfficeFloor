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
package net.officefloor.work;

import java.util.Properties;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.work.WorkModel;
import net.officefloor.repository.ConfigurationContext;

/**
 * Context for loading a {@link WorkModel}.
 * 
 * @author Daniel
 */
public interface WorkLoaderContext {

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws WorkUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name) throws WorkUnknownPropertyError;

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
	 * Properties to configure the {@link Work}.
	 * 
	 * @return Properties specific for the {@link Work}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ConfigurationContext}.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	@Deprecated
	ConfigurationContext getConfigurationContext();

	/**
	 * Obtains the {@link ClassLoader} for loading the {@link WorkModel}.
	 * 
	 * @return {@link ClassLoader} for loading the {@link WorkModel}.
	 */
	ClassLoader getClassLoader();

}
