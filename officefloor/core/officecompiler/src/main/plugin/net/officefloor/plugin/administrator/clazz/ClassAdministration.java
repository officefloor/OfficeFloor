/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.administrator.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;

/**
 * {@link Administration} that delegates to {@link Method} instances of an
 * {@link Object} to do administration.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministration
		implements AdministrationFactory<Object, Indexed, Indexed>, Administration<Object, Indexed, Indexed> {

	/**
	 * Default constructor arguments.
	 */
	private static final Object[] DEFAULT_CONSTRUCTOR_ARGS = new Object[0];

	/**
	 * {@link Constructor} for the {@link Object} providing the administration
	 * {@link Method}.
	 */
	private final Constructor<?> constructor;

	/**
	 * {@link Method} to invoke on the {@link Object} for this
	 * {@link Administration}.
	 */
	private final Method administrationMethod;

	/**
	 * Initiate.
	 * 
	 * @param constructor
	 *            {@link Constructor} for the {@link Object} providing the
	 *            administration {@link Method}.
	 * @param administrationMethod
	 *            {@link Method} to invoke on the {@link Object} for this
	 *            {@link Administration}.
	 */
	public ClassAdministration(Constructor<?> constructor, Method administrationMethod) {
		this.constructor = constructor;
		this.administrationMethod = administrationMethod;
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

		// Obtain the listing of extensions
		Object[] extensions = context.getExtensions();

		try {
			// Obtain the object
			Object object = (constructor != null ? constructor.newInstance(DEFAULT_CONSTRUCTOR_ARGS) : null);

			// Invoke the method to administer extensions
			this.administrationMethod.invoke(object, (Object) extensions);

		} catch (InvocationTargetException ex) {
			// Propagate cause
			throw ex.getCause();
		}
	}

}