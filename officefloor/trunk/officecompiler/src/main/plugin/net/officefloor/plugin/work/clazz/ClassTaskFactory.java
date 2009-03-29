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
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.work.source.CompilerAwareTaskFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;

/**
 * {@link TaskFactory} for the {@link ClassTask}.
 * 
 * @author Daniel
 */
public class ClassTaskFactory implements
		CompilerAwareTaskFactory<ClassWork, Indexed, Indexed> {

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
	 * =================== CompilerAwareTaskFactory ===========================
	 */

	@Override
	public void initialiseTaskFactory(FlowItemModel task) throws Exception {

		// Create the indexes of objects
		List<Integer> objectIndexList = new LinkedList<Integer>();
		for (int i = 0; i < this.parameters.length; i++) {
			if (this.parameters[i] == null) {
				// Object as not specified.
				// Configuration indicates if parameter or managed object.
				objectIndexList.add(new Integer(i));
			}
		}
		Integer[] objectIndexes = objectIndexList.toArray(new Integer[0]);

		// Ensure matching object configuration
		List<DeskTaskObjectModel> objectList = task.getDeskTask().getTask()
				.getObjects();
		if (objectList.size() != objectIndexes.length) {
			throw new Exception("Incorrect configuration as expect "
					+ objectIndexes.length
					+ " objects but provided configuration for "
					+ objectList.size() + " objects");
		}
		DeskTaskObjectModel[] objects = objectList
				.toArray(new DeskTaskObjectModel[0]);

		// Ensure matching flow configuration (parameters after objects)
		List<FlowItemOutputModel> flowList = task.getOutputs();
		int flowOutputCount = this.parameters.length - objectIndexes.length;
		if (flowList.size() != flowOutputCount) {
			throw new Exception("Incorrect configuration as expect "
					+ flowOutputCount
					+ " flow outputs but provided configuration for "
					+ flowList.size() + " flow outputs");
		}

		// Load the parameter factories
		int moIndex = 0;
		for (int i = 0; i < objectIndexes.length; i++) {

			// Obtain the task object
			DeskTaskObjectModel object = objects[i];

			// Obtain the parameter index
			int paramIndex = objectIndexes[i].intValue();

			// Create the appropriate parameter factory
			ParameterFactory parameterFactory;
			if (object.getIsParameter()) {
				parameterFactory = new ParameterParameterFactory();
			} else {
				parameterFactory = new ManagedObjectParameterFactory(moIndex++);
			}

			// Ensure parameter is not already specified
			if (this.parameters[paramIndex] != null) {
				throw new IllegalStateException("Object parameter "
						+ paramIndex + " should not be specified");
			}

			// Specify the parameter factory
			this.parameters[paramIndex] = parameterFactory;
		}
	}

	/*
	 * =============== TaskFactory ===========================================
	 */

	@Override
	public ClassTask createTask(ClassWork work) {
		return new ClassTask(work, this.method, this.parameters);
	}

}