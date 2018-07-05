/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link ManagedFunctionFactory} for the {@link ClassFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassFunctionFactory implements ManagedFunctionFactory<Indexed, Indexed> {

	/**
	 * Default {@link Constructor} for the {@link Class} containing the
	 * {@link Method}. Will be <code>null</code> if static {@link Method}.
	 */
	private final Constructor<?> constructor;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final ManagedFunctionParameterFactory[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param constructor
	 *            Default {@link Constructor} for the {@link Class} containing
	 *            the {@link Method}. Will be <code>null</code> if static
	 *            {@link Method}.
	 * @param method
	 *            {@link Method} to invoke for the {@link ManagedFunction}.
	 * @param parameters
	 *            {@link ManagedFunctionParameterFactory} instances.
	 */
	public ClassFunctionFactory(Constructor<?> constructor, Method method, ManagedFunctionParameterFactory[] parameters) {
		this.constructor = constructor;
		this.method = method;
		this.parameters = parameters;
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * =============== ManagedFunctionFactory ===============
	 */

	@Override
	public ClassFunction createManagedFunction() {
		return new ClassFunction(this.constructor, this.method, this.parameters);
	}

}