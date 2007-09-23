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

import java.lang.reflect.Method;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link net.officefloor.frame.api.execute.Task} to invoke a method on the
 * {@link net.officefloor.frame.api.execute.Work} object.
 * 
 * @author Daniel
 */
class ClassTask<P extends Object, M extends Enum<M>, F extends Enum<F>>
		implements Task<P, ClassWork, M, F> {

	/**
	 * {@link Work}.
	 */
	protected final ClassWork work;

	/**
	 * Method to invoke for this task.
	 */
	protected final Method method;

	/**
	 * Parameters.
	 */
	protected final ParameterFactory<P, ClassWork, M, F>[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            Method to invoke for this {@link Task}.
	 */
	public ClassTask(ClassWork work, Method method,
			ParameterFactory<P, ClassWork, M, F>[] parameters) {
		this.work = work;
		this.method = method;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public Object doTask(TaskContext<P, ClassWork, M, F> context)
			throws Exception {

		// Create the listing of parameters
		Object[] params = new Object[this.parameters.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = this.parameters[i].createParameter(context);
		}

		// Invoke the task
		return this.method.invoke(this.work.getObject(), params);
	}

}