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
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoaderContext;

/**
 * {@link net.officefloor.work.WorkLoader} for a class.
 * 
 * @author Daniel
 */
public class ClassWorkLoader extends AbstractWorkLoader {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor
	 * .work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.work.WorkLoader#loadWork(net.officefloor.work.
	 * WorkLoaderContext)
	 */
	@SuppressWarnings("unchecked")
	public WorkModel<ClassWork> loadWork(WorkLoaderContext context)
			throws Exception {

		// Obtain the class name
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);

		// Obtain the class
		Class<?> clazz = context.getClassLoader().loadClass(className);

		// Obtain the listing of tasks
		List<TaskModel<?, ?>> tasks = new LinkedList<TaskModel<?, ?>>();
		for (Method method : clazz.getMethods()) {

			// Ignore non-public methods
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			// Ignore Object methods
			if (method.getDeclaringClass() == Object.class) {
				continue;
			}

			// Obtain from the method signature
			Class<?>[] paramTypes = method.getParameterTypes();

			// Create to populate later
			ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

			// Create the listing of task objects and flows
			List<TaskObjectModel<?>> objects = new LinkedList<TaskObjectModel<?>>();
			List<TaskFlowModel<?>> flows = new LinkedList<TaskFlowModel<?>>();
			int flowIndex = 0;
			for (int i = 0; i < paramTypes.length; i++) {
				// Obtain the parameter type
				Class<?> paramType = paramTypes[i];

				// Determine if a flow
				if ((Flow.class.isAssignableFrom(paramType))
						|| (Flow.class.getName().equals(paramType.getName()))) {
					// Add as flow
					flows.add(new TaskFlowModel<Indexed>(null, flowIndex,
							String.valueOf(flowIndex)));

					// Specify the flow parameter factory
					parameters[i] = new FlowParameterFactory(flowIndex);

					// Increment for next flow
					flowIndex++;
				} else {
					// Add as object
					objects.add(new TaskObjectModel<Indexed>(null, paramType
							.getName()));

					// Object parameter factories added via Task Factory
				}
			}

			// Create the listing of escalations
			List<TaskEscalationModel> escalations = new LinkedList<TaskEscalationModel>();
			for (Class<?> escalationType : method.getExceptionTypes()) {
				// Create the escalation
				TaskEscalationModel escalation = new TaskEscalationModel(
						escalationType.getName());

				// Add the escalation
				escalations.add(escalation);
			}

			// Create the Task
			TaskObjectModel<Indexed>[] objectArray = objects
					.toArray(new TaskObjectModel[0]);
			TaskFlowModel<Indexed>[] flowArray = flows
					.toArray(new TaskFlowModel[0]);
			TaskEscalationModel[] escalationArray = escalations
					.toArray(new TaskEscalationModel[0]);
			TaskModel<?, ?> task = new TaskModel<Indexed, Indexed>(method
					.getName(), new ClassTaskFactoryManufacturer(method,
					parameters), null, null, objectArray, flowArray,
					escalationArray);

			// Add task to listing
			tasks.add(task);

		}

		// Create the Work Model
		TaskModel<?, ?>[] taskArray = tasks.toArray(new TaskModel[0]);
		WorkModel<ClassWork> work = new WorkModel<ClassWork>(ClassWork.class,
				new ClassWorkFactory(clazz), taskArray);

		// Return the work model
		return work;
	}

}
