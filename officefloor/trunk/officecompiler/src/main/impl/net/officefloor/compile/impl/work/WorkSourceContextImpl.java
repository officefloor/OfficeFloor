/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.compile.impl.work;

import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkUnknownPropertyError;

/**
 * {@link WorkSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceContextImpl implements WorkSourceContext {

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
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public WorkSourceContextImpl(PropertyList propertyList,
			ClassLoader classLoader) {
		this.propertyList = propertyList;
		this.classLoader = classLoader;
	}

	/*
	 * ==================== WorkLoaderContext ================================
	 */

	@Override
	public String[] getPropertyNames() {
		return this.propertyList.getPropertyNames();
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