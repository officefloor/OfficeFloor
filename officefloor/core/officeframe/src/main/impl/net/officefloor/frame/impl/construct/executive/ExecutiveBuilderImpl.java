/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.executive;

import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Implements the {@link ExecutiveBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveBuilderImpl<XS extends ExecutiveSource>
		implements ExecutiveBuilder<XS>, ExecutiveConfiguration<XS> {

	/**
	 * {@link ExecutiveSource}.
	 */
	private final XS executiveSource;

	/**
	 * {@link Class} of the {@link ExecutiveSource}.
	 */
	private final Class<XS> executiveSourceClass;

	/**
	 * {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Initiate.
	 * 
	 * @param executiveSource {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(XS executiveSource) {
		this.executiveSource = executiveSource;
		this.executiveSourceClass = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param executiveSourceClass {@link Class} of the {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(Class<XS> executiveSourceClass) {
		this.executiveSource = null;
		this.executiveSourceClass = executiveSourceClass;
	}

	/*
	 * ================ ExecutiveBuilder =====================
	 */

	@Override
	public void addProperty(String name, String value) {
		// TODO Auto-generated method stub

	}

	/*
	 * ============== ExecutiveConfiguration ===================
	 */

	@Override
	public XS getExecutiveSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<XS> getExecutiveSourceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}