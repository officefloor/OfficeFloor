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
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;

/**
 * {@link net.officefloor.work.WorkLoader} for a class.
 * 
 * @author Daniel
 */
public class ClassWorkLoader implements WorkLoader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.WorkLoader#loadWork(net.officefloor.work.WorkLoaderContext)
	 */
	@SuppressWarnings("unchecked")
	public WorkModel loadWork(WorkLoaderContext context) throws Exception {

		// Obtain the class
		Class clazz = context.getClassLoader().loadClass(
				context.getConfiguration());

		// Obtain the listing of tasks
		List<TaskModel> tasks = new LinkedList<TaskModel>();
		for (Method method : clazz.getMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {

				// Ignore Object methods
				if (method.getDeclaringClass() == Object.class) {
					continue;
				}

				// Obtain from the method signature
				Class<?>[] paramTypes = method.getParameterTypes();

				// Create to populate later
				ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

				// Create the listing of task objects and flows
				List<TaskObjectModel> objects = new LinkedList<TaskObjectModel>();
				List<TaskFlowModel> flows = new LinkedList<TaskFlowModel>();
				int flowIndex = 0;
				for (int i = 0; i < paramTypes.length; i++) {
					// Obtain the param type
					Class<?> paramType = paramTypes[i];

					// Determine if a flow
					if ((Flow.class.isAssignableFrom(paramType))
							|| (Flow.class.getName()
									.equals(paramType.getName()))) {
						// Add as flow
						flows.add(new TaskFlowModel<Indexed>(null, flowIndex));

						// Specify the flow parameter factory
						parameters[i] = new FlowParameterFactory(flowIndex);

						// Increment for next flow
						flowIndex++;
					} else {
						// Add as object
						objects.add(new TaskObjectModel<Indexed>(null,
								paramType.getName()));

						// Object parameter factories added via Task Factory
					}
				}

				// Create the Task
				TaskObjectModel[] objectArray = objects
						.toArray(new TaskObjectModel[0]);
				TaskFlowModel[] flowArray = flows.toArray(new TaskFlowModel[0]);
				TaskModel task = new TaskModel<Indexed, Indexed>(method
						.getName(), new ClassTaskFactoryManufacturer(method,
						parameters), null, null, objectArray, flowArray);

				// Add task to listing
				tasks.add(task);
			}
		}

		// Create the Work Model
		TaskModel[] taskArray = tasks.toArray(new TaskModel[0]);
		WorkModel work = new WorkModel(ClassWork.class, new ClassWorkFactory(
				clazz), taskArray);

		// Return the work model
		return work;
	}

}
