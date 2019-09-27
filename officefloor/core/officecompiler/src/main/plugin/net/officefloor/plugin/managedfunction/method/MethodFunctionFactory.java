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
package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link ManagedFunctionFactory} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodFunctionFactory implements ManagedFunctionFactory<Indexed, Indexed> {

	/**
	 * {@link MethodObjectInstanceFactory}. Will be <code>null</code> if static
	 * {@link Method}.
	 */
	private final MethodObjectInstanceFactory methodObjectInstanceFactory;

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final MethodParameterFactory[] parameters;

	/**
	 * {@link MethodReturnTranslator} or <code>null</code>.
	 */
	private final MethodReturnTranslator<Object, Object> returnTranslator;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      {@link Method} to invoke for the
	 *                                    {@link ManagedFunction}.
	 * @param parameters                  {@link MethodParameterFactory} instances.
	 * @param returnTranslator            {@link MethodReturnTranslator}.
	 */
	public MethodFunctionFactory(MethodObjectInstanceFactory methodObjectInstanceFactory, Method method,
			MethodParameterFactory[] parameters, MethodReturnTranslator<Object, Object> returnTranslator) {
		this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		this.method = method;
		this.parameters = parameters;
		this.returnTranslator = returnTranslator;
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
	public MethodFunction createManagedFunction() {
		return new MethodFunction(this.methodObjectInstanceFactory, this.method, this.parameters,
				this.returnTranslator);
	}

}