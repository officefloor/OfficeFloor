/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.work.clazz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link Task} to invoke a method on the {@link Work} object.
 * 
 * @author Daniel
 */
class ClassTask implements Task<Object, ClassWork, Indexed, Indexed> {

	/**
	 * {@link Work}.
	 */
	private final ClassWork work;

	/**
	 * Method to invoke for this {@link Task}.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final ParameterFactory[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param work
	 *            {@link ClassWork}.
	 * @param method
	 *            Method to invoke for this {@link Task}.
	 */
	public ClassTask(ClassWork work, Method method,
			ParameterFactory[] parameters) {
		this.work = work;
		this.method = method;
		this.parameters = parameters;
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
	public Object doTask(
			TaskContext<Object, ClassWork, Indexed, Indexed> context)
			throws Throwable {

		// Create the listing of parameters
		Object[] params = new Object[this.parameters.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameters[i].createParameter(context);
		}

		// Invoke the task
		try {
			return this.method.invoke(this.work.getObject(), params);
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