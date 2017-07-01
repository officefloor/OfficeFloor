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
package net.officefloor.plugin.jndi.function;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.managedfunction.clazz.ClassFunction;

/**
 * {@link ManagedFunctionFactory} for the {@link ManagedFunction} invoked as a
 * {@link Method} of the JNDI Object.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiObjectManagedFunctionFactory implements ManagedFunctionFactory<Indexed, None> {

	/**
	 * {@link JndiReference}.
	 */
	private final JndiReference reference;

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * {@link ParameterFactory} instances.
	 */
	private final ParameterFactory[] parameterFactories;

	/**
	 * Cache of {@link Class} to its {@link Method}.
	 */
	private final Map<Class<?>, Method> classToMethodCache = new ConcurrentHashMap<>();

	/**
	 * Initiate.
	 * 
	 * @param reference
	 *            {@link JndiReference}.
	 * @param method
	 *            {@link Method}.
	 * @param parameterFactories
	 *            {@link ParameterFactory} instances.
	 */
	public JndiObjectManagedFunctionFactory(JndiReference reference, Method method,
			ParameterFactory[] parameterFactories) {
		this.reference = reference;
		this.method = method;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ================== ManagedFunctionFactory ==================
	 */

	@Override
	public ManagedFunction<Indexed, None> createManagedFunction() {
		return new JndiObjectManagedFunction();
	}

	/**
	 * {@link ManagedFunction} to execute JNDI Object {@link Method}.
	 */
	private class JndiObjectManagedFunction implements ManagedFunction<Indexed, None> {

		/*
		 * ====================== ManagedFunction ======================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the JNDI object
			Context jndiContext = (Context) context.getObject(0);
			Object object = JndiObjectManagedFunctionFactory.this.reference.getJndiObject(jndiContext);

			// Create the listing of parameters
			Object[] params = new Object[JndiObjectManagedFunctionFactory.this.parameterFactories.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = JndiObjectManagedFunctionFactory.this.parameterFactories[i].createParameter(object,
						context);
			}

			// Invoke the function
			Object returnValue = ClassFunction.invokeMethod(object, JndiObjectManagedFunctionFactory.this.method,
					params);

			// Return the value
			return returnValue;
		}
	}

}