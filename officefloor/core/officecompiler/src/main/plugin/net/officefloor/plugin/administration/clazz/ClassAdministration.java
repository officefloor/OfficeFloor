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
