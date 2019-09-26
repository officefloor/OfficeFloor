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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;

/**
 * {@link ManagedFunction} to invoke a {@link Method}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodFunction implements ManagedFunction<Indexed, Indexed> {

	/**
	 * Invokes the {@link Method} as the {@link ManagedFunction} directly on the
	 * {@link Object}.
	 * 
	 * @param instance   Instance. May be <code>null</code> if static
	 *                   {@link Method}.
	 * @param method     {@link Method}.
	 * @param parameters Parameters.
	 * @return {@link Method} return value.
	 * @throws Throwable Failure invoking the {@link Method}.
	 */
	public static Object invokeMethod(Object instance, Method method, Object[] parameters) throws Throwable {

		// Invoke the function
		try {
			return method.invoke(instance, parameters);
		} catch (InvocationTargetException ex) {
			// Propagate failure of function
			throw ex.getCause();
		} catch (IllegalArgumentException ex) {

			// Provide detail of illegal argument
			StringBuilder message = new StringBuilder();
			message.append("Function failure invoking ");
			message.append(method.getName());
			message.append("(");
			boolean isFirst = true;
			for (Class<?> parameterType : method.getParameterTypes()) {
				if (isFirst) {
					isFirst = false;
				} else {
					message.append(", ");
				}
				message.append(parameterType.getName());
			}
			message.append(") with arguments ");
			isFirst = true;
			for (Object parameter : parameters) {
				if (isFirst) {
					isFirst = false;
				} else {
					message.append(", ");
				}
				message.append(parameter == null ? "null" : parameter.getClass().getName());
			}

			// Propagate illegal argument issue
			throw new IllegalArgumentException(message.toString());
		}
	}

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
	 * {@link MethodParameterFactory} instances.
	 */
	private final MethodParameterFactory[] parameterFactories;

	/**
	 * Initiate.
	 * 
	 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
	 *                                    be <code>null</code> if static
	 *                                    {@link Method}.
	 * @param method                      Method to invoke for this
	 *                                    {@link ManagedFunction}.
	 * @param parameterFactories          {@link MethodParameterFactory}
	 *                                    instances.
	 */
	public MethodFunction(MethodObjectInstanceFactory methodObjectInstanceFactory, Method method,
			MethodParameterFactory[] parameterFactories) {
		this.method = method;
		this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		this.parameterFactories = parameterFactories;
	}

	/**
	 * Returns the {@link Method} for the {@link ManagedFunction}.
	 * 
	 * @return {@link Method} for the {@link ManagedFunction}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * ========================= ManagedFunction =========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the instance to invoke the method on (null if static method)
		Object instance = (this.methodObjectInstanceFactory == null) ? null
				: this.methodObjectInstanceFactory.createInstance();

		// Create the listing of parameters
		Object[] params = new Object[this.parameterFactories.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameterFactories[i].createParameter(context);
		}

		// Invoke the method as the function
		return invokeMethod(instance, this.method, params);
	}

}