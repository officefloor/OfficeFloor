/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;

/**
 * {@link MethodParameterManufacturer} for an {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/*
	 * =========== MethodParameterManufacturerServiceFactory ===============
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================ MethodParameterManufacturer =================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Determine if asynchronous flow
		if (AsynchronousFlow.class.equals(context.getParameterClass())) {
			// Parameter is an asynchronous flow
			return new AsynchronousFlowParameterFactory();
		}

		// Not asynchronous flow
		return null;
	}
}