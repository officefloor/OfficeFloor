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

package net.officefloor.compile.impl.work;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link WorkSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceContextImpl extends SourcePropertiesImpl implements
		WorkSourceContext {

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
		super(new PropertyListSourceProperties(propertyList));
		this.classLoader = classLoader;
	}

	/*
	 * ==================== WorkLoaderContext ================================
	 */

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}