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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link net.officefloor.frame.api.build.TaskFactory} to invoke the method on
 * the {@link net.officefloor.frame.api.execute.Work} class.
 * 
 * @author Daniel
 */
public class ClassTaskFactory<P extends Object, M extends Enum<M>, F extends Enum<F>>
		implements TaskFactory<P, ClassWork, M, F> {

	/**
	 * Method to invoke for this task.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final ParameterFactory<P, ClassWork, M, F>[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            {@link Method} to invoke on the {@link Work} class.
	 * @param parameters
	 *            Parameters.
	 */
	public ClassTaskFactory(Method method,
			ParameterFactory<P, ClassWork, M, F>[] parameters) {
		this.method = method;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public Task<P, ClassWork, M, F> createTask(ClassWork work) {
		return new ClassTask<P, M, F>(work, this.method, this.parameters);
	}

}
