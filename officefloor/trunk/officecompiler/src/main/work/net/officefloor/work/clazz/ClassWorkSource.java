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

import net.officefloor.compile.impl.work.source.AbstractWorkSource;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkSource} for a {@link Class} having the {@link Object} as the
 * {@link Work} and {@link Method} instances as the {@link Task} instances.
 * 
 * @author Daniel
 */
public class ClassWorkSource extends AbstractWorkSource<ClassWork> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * =================== AbstractWorkLoader ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@SuppressWarnings("unchecked")
	public void sourceWork(WorkTypeBuilder<ClassWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the class
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> clazz = context.getClassLoader().loadClass(className);

		// Define the work factory
		workTypeBuilder.setWorkFactory(new ClassWorkFactory(clazz));

		// Obtain the listing of tasks from the methods of the class
		for (Method method : clazz.getMethods()) {

			// Ignore non-public methods
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			// Ignore Object methods
			if (method.getDeclaringClass() == Object.class) {
				continue;
			}

			// Obtain details of the method
			String methodName = method.getName();
			Class<?>[] paramTypes = method.getParameterTypes();

			// Create to parameters to method to be populated
			ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

			// Include method as task in type definition
			TaskTypeBuilder<ClassWork, Indexed, Indexed> taskTypeBuilder = workTypeBuilder
					.addTaskType(methodName, new ClassTaskFactoryManufacturer(
							method, parameters), null, null);

			// Define the listing of task objects and flows
			int flowIndex = 0;
			for (int i = 0; i < paramTypes.length; i++) {
				// Obtain the parameter type
				Class<?> paramType = paramTypes[i];

				// Determine if a flow
				if ((Flow.class.isAssignableFrom(paramType))
						|| (Flow.class.getName().equals(paramType.getName()))) {
					// Add as a flow
					taskTypeBuilder.addFlow();

					// Specify the parameter factory for the flow
					parameters[i] = new FlowParameterFactory(flowIndex++);

				} else {
					// Add as an object
					taskTypeBuilder.addObject(paramType);

					// Object parameter factories added via Task Factory
					// TODO remove P from Task so this is not necessary
				}
			}

			// Define the escalation listing
			for (Class<?> escalationType : method.getExceptionTypes()) {
				taskTypeBuilder
						.addEscalation((Class<Throwable>) escalationType);
			}
		}
	}

}