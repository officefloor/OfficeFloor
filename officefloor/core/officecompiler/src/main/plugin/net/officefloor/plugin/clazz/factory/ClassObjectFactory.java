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

package net.officefloor.plugin.clazz.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyInjector;

/**
 * Creates an object from {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassObjectFactory {

	/**
	 * {@link Constructor} to instantiate the object.
	 */
	private final Constructor<?> constructor;

	/**
	 * {@link Constructor} {@link ClassDependencyFactory} instances.
	 */
	private final ClassDependencyFactory[] constructorDependencyFactories;

	/**
	 * {@link ClassDependencyInjector} instances.
	 */
	private final ClassDependencyInjector[] dependencyInjectors;

	/**
	 * Instantiate.
	 * 
	 * @param constructor                    {@link Constructor} to instantiate the
	 *                                       object.
	 * @param constructorDependencyFactories {@link Constructor}
	 *                                       {@link ClassDependencyFactory}
	 *                                       instances.
	 * @param dependencyInjectors            {@link ClassDependencyInjector}
	 *                                       instances.
	 */
	public ClassObjectFactory(Constructor<?> constructor, ClassDependencyFactory[] constructorDependencyFactories,
			ClassDependencyInjector[] dependencyInjectors) {
		this.constructor = constructor;
		this.constructorDependencyFactories = constructorDependencyFactories;
		this.dependencyInjectors = dependencyInjectors;
	}

	/**
	 * Creates the object within a {@link ManagedObject} context.
	 * 
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @return Created object.
	 * @throws Throwable If fails to create object.
	 */
	public Object createObject(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {

		// Create the constructor parameters
		Object[] constructorParameters = new Object[this.constructorDependencyFactories.length];
		for (int i = 0; i < this.constructorDependencyFactories.length; i++) {
			ClassDependencyFactory factory = this.constructorDependencyFactories[i];
			constructorParameters[i] = factory.createDependency(managedObject, context, registry);
		}

		// Construct an instance of the object
		Object object;
		try {
			object = this.constructor.newInstance(constructorParameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

		// Load the dependencies
		for (int i = 0; i < this.dependencyInjectors.length; i++) {
			this.dependencyInjectors[i].injectDependencies(object, managedObject, context, registry);
		}

		// Return the created object
		return object;
	}

	/**
	 * Creates the object within a {@link ManagedFunction} context.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 * @return Created object.
	 * @throws Throwable If fails to create object.
	 */
	public Object createObject(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Create the constructor parameters
		Object[] constructorParameters = new Object[this.constructorDependencyFactories.length];
		for (int i = 0; i < this.constructorDependencyFactories.length; i++) {
			ClassDependencyFactory factory = this.constructorDependencyFactories[i];
			constructorParameters[i] = factory.createDependency(context);
		}

		// Construct an instance of the object
		Object object;
		try {
			object = this.constructor.newInstance(constructorParameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

		// Load the dependencies
		for (int i = 0; i < this.dependencyInjectors.length; i++) {
			this.dependencyInjectors[i].injectDependencies(object, context);
		}

		// Return the created object
		return object;
	}

	/**
	 * Creates the object within the {@link Administration} context.
	 * 
	 * @param context {@link AdministrationContext}.
	 * @return Created object.
	 * @throws Throwable If fails to create object.
	 */
	public Object createObject(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

		// Create the constructor parameters
		Object[] constructorParameters = new Object[this.constructorDependencyFactories.length];
		for (int i = 0; i < this.constructorDependencyFactories.length; i++) {
			ClassDependencyFactory factory = this.constructorDependencyFactories[i];
			constructorParameters[i] = factory.createDependency(context);
		}

		// Construct an instance of the object
		Object object;
		try {
			object = this.constructor.newInstance(constructorParameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

		// Load the dependencies
		for (int i = 0; i < this.dependencyInjectors.length; i++) {
			this.dependencyInjectors[i].injectDependencies(object, context);
		}

		// Return the created object
		return object;
	}

}
