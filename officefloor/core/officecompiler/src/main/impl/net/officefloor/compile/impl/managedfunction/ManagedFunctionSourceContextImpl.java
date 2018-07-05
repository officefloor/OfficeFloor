/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ManagedFunctionSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceContextImpl extends SourceContextImpl implements
		ManagedFunctionSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedFunctionSourceContextImpl(boolean isLoadingType,
			PropertyList propertyList, NodeContext context) {
		super(isLoadingType, context.getRootSourceContext(),
				new PropertyListSourceProperties(propertyList));
	}

}