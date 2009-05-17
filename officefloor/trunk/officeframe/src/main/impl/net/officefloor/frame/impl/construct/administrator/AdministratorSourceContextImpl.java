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
package net.officefloor.frame.impl.construct.administrator;

import java.util.Properties;

import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceUnknownPropertyError;

/**
 * Implementation of the {@link AdministratorSourceContext}.
 * 
 * @author Daniel
 */
public class AdministratorSourceContextImpl implements
		AdministratorSourceContext {

	/**
	 * {@link Properties}.
	 */
	private final Properties properties;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link Properties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public AdministratorSourceContextImpl(Properties properties,
			ClassLoader classLoader) {
		this.properties = properties;
		this.classLoader = classLoader;
	}

	/*
	 * ================== AdministratorSourceContext ===========================
	 */

	@Override
	public String getProperty(String name)
			throws AdministratorSourceUnknownPropertyError {
		// Obtain the value
		String value = this.getProperty(name, null);

		// Ensure have a value
		if (value == null) {
			throw new AdministratorSourceUnknownPropertyError(
					"Unknown property '" + name + "'", name);
		}

		// Return the value
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		// Obtain the value
		String value = this.properties.getProperty(name);

		// Default value if not specified
		if (value == null) {
			value = defaultValue;
		}

		// Return the value
		return value;
	}

	@Override
	public Properties getProperties() {
		return this.properties;
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}