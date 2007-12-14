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
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.work.CompilerAwareTaskFactory;

/**
 * {@link net.officefloor.frame.api.build.TaskFactory} to invoke the method on
 * the {@link net.officefloor.frame.api.execute.Work} class.
 * 
 * @author Daniel
 */
public class ClassTaskFactory implements
		CompilerAwareTaskFactory<Object, ClassWork, Indexed, Indexed> {

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.CompilerAwareTaskFactory#initialiseTaskFactory(net.officefloor.model.desk.DeskTaskModel)
	 */
	@Override
	public void initialiseTaskFactory(DeskTaskModel task) throws Exception {

		// Ensure matching configuration
		List<DeskTaskObjectModel> objects = task.getObjects();
		if (objects.size() != this.parameters.length) {
			throw new IllegalArgumentException(
					"Incorrect configuration as have " + this.parameters.length
							+ " parameters but configration for only "
							+ objects.size());
		}

		// Load the parameter factories
		int moIndex = 0;
		for (int i = 0; i < this.parameters.length; i++) {

			// Obtain the corresponding object
			DeskTaskObjectModel object = objects.get(i);

			// Create the appropriate parameter factory
			ParameterFactory parameterFactory;
			if (object.getIsParameter()) {
				parameterFactory = new ParameterParameterFactory();
			} else {
				parameterFactory = new ManagedObjectParameterFactory(moIndex++);
			}

			// Specify the parameter factory
			this.parameters[i] = parameterFactory;
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
