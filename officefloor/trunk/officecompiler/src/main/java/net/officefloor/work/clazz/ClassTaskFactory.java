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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link net.officefloor.frame.api.build.TaskFactory} to invoke the method on
 * the {@link net.officefloor.frame.api.execute.Work} class.
 * 
 * @author Daniel
 */
public class ClassTaskFactory implements
		TaskFactory<Object, ClassWork, Indexed, Indexed> {

	/**
	 * Method to invoke for this task.
	 */
	private final Method method;

	/**
	 * Parameters.
	 */
	private final ParameterFactory[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            {@link Method} to invoke on the {@link Work} class.
	 * @param parameters
	 *            Parameters.
	 */
	public ClassTaskFactory(Method method, ParameterFactory[] parameters) {
		this.method = method;
		this.parameters = parameters;

		// TODO remove
		if (this.parameters.length > 0) {
			this.parameters[0] = new ManagedObjectParameterFactory(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public ClassTask createTask(ClassWork work) {
		return new ClassTask(work, this.method, this.parameters);
	}

}
