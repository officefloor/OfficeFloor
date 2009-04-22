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
package net.officefloor.plugin.work.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link TaskFactory} for the {@link ClassTask}.
 * 
 * @author Daniel
 */
public class ClassTaskFactory implements
		TaskFactory<ClassWork, Indexed, Indexed> {

	/**
	 * Method to invoke for this task.
	 */
	private final Method method;

	/**
	 * Indicates if the {@link Method} is <code>static</code>.
	 */
	private final boolean isStaticMethod;

	/**
	 * Parameters.
	 */
	private final ParameterFactory[] parameters;

	/**
	 * Initiate.
	 * 
	 * @param method
	 *            {@link Method} to invoke on the {@link Work} class.
	 * @param isStaticMethod
	 *            Indicates if the {@link Method} is <code>static</code>.
	 * @param parameterFactories
	 *            {@link ParameterFactory} instances.
	 */
	public ClassTaskFactory(Method method, boolean isStaticMethod,
			ParameterFactory[] parameters) {
		this.method = method;
		this.isStaticMethod = isStaticMethod;
		this.parameters = parameters;
	}

	/*
	 * =============== TaskFactory ===========================================
	 */

	@Override
	public ClassTask createTask(ClassWork work) {
		return new ClassTask(this.method, this.isStaticMethod, this.parameters);
	}

}