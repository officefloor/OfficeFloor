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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowMethodMetaData;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link MethodParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceParameterFactory implements MethodParameterFactory {

	/**
	 * {@link ClassFlowInterfaceFactory}.
	 */
	private final ClassFlowInterfaceFactory flowParameterFactory;

	/**
	 * Initiate.
	 * 
	 * @param flowParameterFactory {@link ClassFlowInterfaceFactory}.
	 */
	public FlowInterfaceParameterFactory(ClassFlowInterfaceFactory flowParameterFactory) {
		this.flowParameterFactory = flowParameterFactory;
	}

	/**
	 * Obtains the {@link ClassFlowMethodMetaData}.
	 * 
	 * @return {@link ClassFlowMethodMetaData} instances.
	 */
	public ClassFlowMethodMetaData[] getFlowMethodMetaData() {
		return this.flowParameterFactory.getFlowMethodMetaData();
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) throws Exception {
		return this.flowParameterFactory
				.createFlows((flowIndex, parameter, callback) -> context.doFlow(flowIndex, parameter, callback));
	}

}
