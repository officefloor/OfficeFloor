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
