/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;
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
	 * {@link SafeManagedObjectService}.
	 */
	private SafeManagedObjectService<Indexed> servicer;

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
		this.servicer = new SafeManagedObjectService<>(executeContext);
	}

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Exception {
		return this.factory.createFlows((flowIndex, parameter, callback) -> this.servicer.invokeProcess(flowIndex,
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
