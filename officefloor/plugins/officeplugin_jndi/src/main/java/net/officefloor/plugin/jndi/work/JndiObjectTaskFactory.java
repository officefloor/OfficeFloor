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
package net.officefloor.plugin.jndi.work;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.work.clazz.ClassTask;

/**
 * {@link TaskFactory} for the {@link JndiObjectTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiObjectTaskFactory implements
		TaskFactory<JndiWork, Indexed, None> {

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * Flag indicating if the {@link Method} is static.
	 */
	private final boolean isStatic;

	/**
	 * {@link ParameterFactory} instances.
	 */
	private final ParameterFactory[] parameterFactories;

	/**
	 * Cache of {@link Class} to its {@link Method}.
	 */
	private final Map<Class<?>, Method> classToMethodCache = new HashMap<Class<?>, Method>();

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            {@link Method}.
	 * @param isStatic
	 *            Flag indicating if the {@link Method} is static.
	 * @param parameterFactories
	 *            {@link ParameterFactory} instances.
	 */
	public JndiObjectTaskFactory(Method method, boolean isStatic,
			ParameterFactory[] parameterFactories) {
		this.method = method;
		this.isStatic = isStatic;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ======================= TaskFactory ============================
	 */

	@Override
	public Task<JndiWork, Indexed, None> createTask(JndiWork work) {
		return new JndiObjectTask();
	}

	/**
	 * {@link Task} to execute JNDI {@link Work} Object {@link Method}.
	 */
	private class JndiObjectTask implements Task<JndiWork, Indexed, None> {

		/*
		 * ======================== Task ===============================
		 */

		@Override
		public Object doTask(TaskContext<JndiWork, Indexed, None> context)
				throws Throwable {

			// Obtain the JNDI object
			Context jndiContext = (Context) context.getObject(0);
			JndiWork work = context.getWork();
			Object jndiObject;
			synchronized (work) {
				jndiObject = work.getJndiObject(jndiContext);
			}

			// Obtain the instance to invoke the method on
			Object instance = (JndiObjectTaskFactory.this.isStatic ? null
					: jndiObject);

			// Create the listing of parameters
			Object[] params = new Object[JndiObjectTaskFactory.this.parameterFactories.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = JndiObjectTaskFactory.this.parameterFactories[i]
						.createParameter(jndiObject, context);
			}

			// Invoke the task
			Object returnValue = ClassTask.invokeMethod(instance,
					JndiObjectTaskFactory.this.method, params,
					JndiObjectTaskFactory.this.classToMethodCache);

			// Return the value
			return returnValue;
		}
	}

}