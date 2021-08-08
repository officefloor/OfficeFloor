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

package net.officefloor.plugin.clazz.dependency;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObject;

/**
 * Creates the dependency for the {@link ClassManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyFactory {

	/**
	 * Loads the {@link ManagedObjectExecuteContext}.
	 * 
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 */
	default void loadManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		// do nothing
	}

	/**
	 * Creates the dependency for a {@link ManagedObject}.
	 * 
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(ManagedObject managedObject, ManagedObjectContext context, ObjectRegistry<Indexed> registry)
			throws Throwable;

	/**
	 * Creates the dependency for a {@link ManagedFunction}.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

	/**
	 * Creates the dependency for {@link Administration}.
	 * 
	 * @param context {@link AdministrationContext}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable;
}
