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

package net.officefloor.plugin.administration.clazz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;

/**
 * {@link Administration} that delegates to {@link Method} instances of an
 * {@link Object} to do administration.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministration
		implements AdministrationFactory<Object, Indexed, Indexed>, Administration<Object, Indexed, Indexed> {

	/**
	 * {@link ClassObjectFactory}.
	 */
	private final ClassObjectFactory objectFactory;

	/**
	 * {@link Method} to invoke on the {@link Object} for this
	 * {@link Administration}.
	 */
	private final Method administrationMethod;

	/**
	 * {@link ClassDependencyFactory} instances.
	 */
	private final ClassDependencyFactory[] parameterFactories;

	/**
	 * Initiate.
	 * 
	 * @param objectFactory        {@link ClassObjectFactory}.
	 * @param administrationMethod {@link Method} to invoke on the {@link Object}
	 *                             for this {@link Administration}.
	 * @param parameterFactories   {@link ClassDependencyFactory} instances.
	 */
	public ClassAdministration(ClassObjectFactory objectFactory, Method administrationMethod,
			ClassDependencyFactory[] parameterFactories) {
		this.objectFactory = objectFactory;
		this.administrationMethod = administrationMethod;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ============== AdministrationFactory ===============================
	 */

	@Override
	public Administration<Object, Indexed, Indexed> createAdministration() throws Throwable {
		return this;
	}

	/*
	 * ================== Administration ==================================
	 */

	@Override
	public void administer(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {

		// Create the object
		Object object = this.objectFactory.createObject(context);

		// Create the parameters
		Object[] parameters = new Object[this.parameterFactories.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = this.parameterFactories[i].createDependency(context);
		}

		try {
			// Invoke the method to administer
			this.administrationMethod.invoke(object, parameters);

		} catch (InvocationTargetException ex) {
			// Propagate cause
			throw ex.getCause();
		}
	}

}
