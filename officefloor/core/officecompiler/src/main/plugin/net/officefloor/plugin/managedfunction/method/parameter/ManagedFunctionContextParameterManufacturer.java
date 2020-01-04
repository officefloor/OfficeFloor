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

package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;

/**
 * {@link MethodParameterManufacturer} for the {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContextParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/*
	 * =========== MethodParameterManufacturerServiceFactory ===============
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== MethodParameterManufacturer =====================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Determine if managed function context
		if (ManagedFunctionContext.class.equals(context.getParameterClass())) {
			// Parameter is a managed function context
			return new ManagedFunctionContextParameterFactory();
		}

		// Not function context
		return null;
	}

}
