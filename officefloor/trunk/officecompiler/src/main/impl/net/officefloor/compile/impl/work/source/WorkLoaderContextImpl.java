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
package net.officefloor.compile.impl.work.source;

import java.util.Properties;

import net.officefloor.compile.spi.work.source.WorkLoaderContext;
import net.officefloor.compile.spi.work.source.WorkUnknownPropertyError;

/**
 * {@link WorkLoaderContext} implementation.
 * 
 * @author Daniel
 */
public class WorkLoaderContextImpl implements WorkLoaderContext {

	/**
	 * Names of the {@link Properties} in the order defined.
	 */
	private final String[] names;

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
	 * @param names
	 *            Names of the {@link Properties} in the order defined.
	 * @param properties
	 *            {@link Properties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public WorkLoaderContextImpl(String[] names, Properties properties,
			ClassLoader classLoader) {
		this.names = names;
		this.properties = properties;
		this.classLoader = classLoader;
	}

	/*
	 * ==================== WorkLoaderContext ================================
	 */

	@Override
	public String[] getPropertyNames() {
		return this.names;
	}

	@Override
	public String getProperty(String name) throws WorkUnknownPropertyError {
		String value = this.getProperty(name, null);
		if (value == null) {
			throw new WorkUnknownPropertyError("Unknown property '" + name
					+ "'", name);
		}
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		String value = this.properties.getProperty(name);
		if ((value == null) || (value.trim().length() == 0)) {
			return defaultValue;
		}
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