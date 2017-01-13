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
package net.officefloor.plugin.work.clazz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.Work;

/**
 * {@link ManagedFunction} to invoke a method on the {@link Work} object.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassTask implements ManagedFunction<ClassWork, Indexed, Indexed> {

	/**
	 * <p>
	 * Invokes the {@link Method} as the {@link ManagedFunction} on a type.
	 * <p>
	 * As the Object could be of any child of the type, the {@link Method} may
	 * need to be derived from the input instance.
	 * <p>
	 * Before the input instance is interrogated the {@link Method} is attempted
	 * to be retrieved from the <code>cache</code>. Should the {@link Method}
	 * not be in the <code>cache</code> it is derived and loaded into the
	 * <code>cache</code>.
	 * 
	 * @param instance
	 *            Instance. May be <code>null</code> if static {@link Method}.
	 * @param method
	 *            {@link Method} template. Typically the {@link Method} from the
	 *            type.
	 * @param parameters
	 *            Parameters.
	 * @param cache
	 *            Cache of the concrete {@link Class} to its corresponding
	 *            {@link Method}.
	 * @return {@link Method} return value.
	 * @throws Throwable
	 *             Failure invoking the {@link Method}.
	 */
	public static Object invokeMethod(Object instance, Method method,
			Object[] parameters, Map<Class<?>, Method> cache) throws Throwable {

		// Obtain the appropriate method
		Method task = method;
		if (instance != null) {

			// Not static method, so lazy obtain instance method
			synchronized (cache) {
				Class<?> concreteClass = instance.getClass();
				task = cache.get(concreteClass);
				if (task == null) {
					task = concreteClass.getMethod(method.getName(), method
							.getParameterTypes());
					cache.put(concreteClass, task);
				}
			}
		}

		// Invoke the method
		return invokeMethod(instance, task, parameters);
	}

	/**
	 * Invokes the {@link Method} as the {@link ManagedFunction} directly on the
	 * {@link Object}.
	 * 
	 * @param instance
	 *            Instance. May be <code>null</code> if static {@link Method}.
	 * @param method
	 *            {@link Method}.
	 * @param parameters
	 *            Parameters.
	 * @return {@link Method} return value.
	 * @throws Throwable
	 *             Failure invoking the {@link Method}.
	 */
	public static Object invokeMethod(Object instance, Method method,
			Object[] parameters) throws Throwable {

		// Invoke the task
		try {
			return method.invoke(instance, parameters);
		} catch (InvocationTargetException ex) {
			// Propagate failure of task
			throw ex.getCause();
		} catch (IllegalArgumentException ex) {

			// Provide detail of illegal argument
			StringBuilder message = new StringBuilder();
			message.append("Task failure invoking ");
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
				message.append(parameter == null ? "null" : parameter
						.getClass().getName());
			}

			// Propagate illegal argument issue
			throw new IllegalArgumentException(message.toString());
		}
	}

	/**
	 * Method to invoke for this {@link ManagedFunction}.
	 */
	private final Method method;

	/**
	 * Indicates if the {@link Method} is <code>static</code>.
	 */
	private final boolean isStaticMethod;

	/**
	 * {@link ParameterFactory} instances.
	 */
	private final ParameterFactory[] parameterFactories;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            Method to invoke for this {@link ManagedFunction}.
	 * @param isStaticMethod
	 *            Indicates if the {@link Method} is <code>static</code>.
	 * @param parameterFactories
	 *            {@link ParameterFactory} instances.
	 */
	public ClassTask(Method method, boolean isStaticMethod,
			ParameterFactory[] parameterFactories) {
		this.method = method;
		this.isStaticMethod = isStaticMethod;
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
	 * ========================= Task ==========================================
	 */

	@Override
	public Object execute(ManagedFunctionContext<ClassWork, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the instance to invoke the method on
		Object instance = (this.isStaticMethod ? null : context.getWork()
				.getObject());

		// Create the listing of parameters
		Object[] params = new Object[this.parameterFactories.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameterFactories[i].createParameter(context);
		}

		// Invoke the method as the task
		return invokeMethod(instance, this.method, params);
	}

}