/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link Task} to invoke a method on the {@link Work} object.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassTask implements Task<ClassWork, Indexed, Indexed> {

	/**
	 * Method to invoke for this {@link Task}.
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
	 *            Method to invoke for this {@link Task}.
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
	 * Returns the {@link Method} for the {@link Task}.
	 * 
	 * @return {@link Method} for the {@link Task}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * ========================= Task ==========================================
	 */

	@Override
	public Object doTask(TaskContext<ClassWork, Indexed, Indexed> context)
			throws Throwable {

		// Obtain the instance to invoke the method on
		Object instance = (this.isStaticMethod ? null : context.getWork()
				.getObject());

		// Create the listing of parameters
		Object[] params = new Object[this.parameterFactories.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameterFactories[i].createParameter(context);
		}

		// Invoke the task
		try {
			return this.method.invoke(instance, params);
		} catch (InvocationTargetException ex) {
			// Propagate failure of task
			throw ex.getCause();
		} catch (IllegalArgumentException ex) {
			// Provide detail of illegal argument
			StringBuilder message = new StringBuilder();
			message.append("Task failure invoking ");
			message.append(this.method.getName());
			message.append("(");
			boolean isFirst = true;
			for (Class<?> parameterType : this.method.getParameterTypes()) {
				if (isFirst) {
					isFirst = false;
				} else {
					message.append(", ");
				}
				message.append(parameterType.getName());
			}
			message.append(") with arguments ");
			isFirst = true;
			for (Object parameter : params) {
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

}