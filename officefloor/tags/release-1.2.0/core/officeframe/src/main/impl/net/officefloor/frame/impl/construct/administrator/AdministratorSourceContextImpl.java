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

package net.officefloor.frame.impl.construct.administrator;

import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.source.SourceProperties;

/**
 * Implementation of the {@link AdministratorSourceContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorSourceContextImpl extends SourcePropertiesImpl
		implements AdministratorSourceContext {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public AdministratorSourceContextImpl(SourceProperties properties,
			ClassLoader classLoader) {
		super(properties);
		this.classLoader = classLoader;
	}

	/*
	 * ================== AdministratorSourceContext ===========================
	 */

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}