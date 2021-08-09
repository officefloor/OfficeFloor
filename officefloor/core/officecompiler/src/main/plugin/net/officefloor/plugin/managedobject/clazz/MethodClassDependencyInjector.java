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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * {@link Field} {@link ClassDependencyInjector}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodClassDependencyInjector implements ClassDependencyInjector {

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * {@link ClassDependencyFactory} for the {@link Parameter} instances.
	 */
	private final ClassDependencyFactory[] parameterFactories;

	/**
	 * Instantiate.
	 * 
	 * @param method             {@link Method}.
	 * @param parameterFactories {@link ClassDependencyFactory} for the
	 *                           {@link Parameter} instances.
	 */
	public MethodClassDependencyInjector(Method method, ClassDependencyFactory[] parameterFactories) {
		this.method = method;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * =================== ClassDependencyInjector =======================
	 */

	@Override
	public void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {

		// Obtain the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createDependency(managedObject, context, registry);
		}

		// Load the dependencies
		try {
			this.method.invoke(object, parameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	@Override
	public void injectDependencies(Object object, ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createDependency(context);
		}

		// Load the dependencies
		try {
			this.method.invoke(object, parameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	@Override
	public void injectDependencies(Object object, AdministrationContext<Object, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createDependency(context);
		}

		// Load the dependencies
		try {
			this.method.invoke(object, parameters);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

}
