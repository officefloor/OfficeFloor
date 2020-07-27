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

package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;

/**
 * {@link ClassDependencyFactory} for {@link FlowInterface},
 * 
 * @author Daniel Sagenschneider
 */
public class FlowClassDependencyFactory implements ClassDependencyFactory {

	/**
	 * {@link ClassFlowInterfaceFactory}.
	 */
	private final ClassFlowInterfaceFactory factory;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Instantiate.
	 * 
	 * @param factory {@link ClassFlowInterfaceFactory}.
	 */
	public FlowClassDependencyFactory(ClassFlowInterfaceFactory factory) {
		this.factory = factory;
	}

	/*
	 * ================== ClassDependencyFactory =======================
	 */

	@Override
	public void loadManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Exception {
		return this.factory.createFlows((flowIndex, parameter, callback) -> this.executeContext.invokeProcess(flowIndex,
				parameter, managedObject, 0, callback));
	}

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return this.factory
				.createFlows((flowIndex, parameter, callback) -> context.doFlow(flowIndex, parameter, callback));
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		return this.factory
				.createFlows((flowIndex, parameter, callback) -> context.doFlow(flowIndex, parameter, callback));
	}

}
