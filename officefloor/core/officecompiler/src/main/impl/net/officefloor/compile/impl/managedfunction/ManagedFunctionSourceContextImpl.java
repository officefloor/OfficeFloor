/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link ManagedFunctionSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourceContextImpl extends SourceContextImpl implements ManagedFunctionSourceContext {

	/**
	 * Initiate.
	 * 
	 * @param managedFunctionSourceName Name of {@link ManagedFunctionSource}.
	 * @param isLoadingType             Indicates if loading type.
	 * @param additionalProfiles        Additional profiles.
	 * @param propertyList              {@link PropertyList}.
	 * @param context                   {@link NodeContext}.
	 */
	public ManagedFunctionSourceContextImpl(String managedFunctionSourceName, boolean isLoadingType,
			String[] additionalProfiles, PropertyList propertyList, NodeContext context) {
		super(managedFunctionSourceName, isLoadingType, additionalProfiles, context.getRootSourceContext(),
				new PropertyListSourceProperties(propertyList));
	}

}
