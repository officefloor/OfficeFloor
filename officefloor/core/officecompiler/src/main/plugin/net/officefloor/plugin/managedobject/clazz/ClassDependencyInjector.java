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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * Injector of dependencies into an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyInjector {

	/**
	 * Injects dependencies into the object.
	 * 
	 * @param object        Object to receive the dependencies.
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link ManagedFunctionContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link AdministrationContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, AdministrationContext<Object, Indexed, Indexed> context) throws Throwable;
}
