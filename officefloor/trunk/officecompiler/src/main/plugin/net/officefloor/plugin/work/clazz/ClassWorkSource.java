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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.WorkSourceService;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkSource} for a {@link Class} having the {@link Object} as the
 * {@link Work} and {@link Method} instances as the {@link Task} instances.
 * 
 * @author Daniel
 */
public class ClassWorkSource extends AbstractWorkSource<ClassWork> implements
		WorkSourceService<ClassWork, ClassWorkSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * =================== WorkSourceService ================================
	 */

	@Override
	public String getWorkSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassWorkSource> getWorkSourceClass() {
		return ClassWorkSource.class;
	}

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

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Obtain the class
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> clazz = classLoader.loadClass(className);

		// Define the work factory
		workTypeBuilder.setWorkFactory(new ClassWorkFactory(clazz));

		// Obtain the listing of tasks from the methods of the class
		for (Method method : clazz.getMethods()) {

			// Ignore non-public methods
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}

			// Ignore Object methods
			if (Object.class.equals(method.getDeclaringClass())) {
				continue;
			}

			// Obtain details of the method
			String methodName = method.getName();
			Class<?>[] paramTypes = method.getParameterTypes();

			// Create to parameters to method to be populated
			ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

			// Determine if the method is static
			boolean isStatic = Modifier.isStatic(method.getModifiers());

			// Include method as task in type definition
			TaskTypeBuilder<Indexed, Indexed> taskTypeBuilder = workTypeBuilder
					.addTaskType(methodName, new ClassTaskFactory(method,
							isStatic, parameters), null, null);

			// Define the return type (it not void)
			Class<?> returnType = method.getReturnType();
			if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
				taskTypeBuilder.setReturnType(returnType);
			}

			// Define the listing of task objects and flows
			int objectIndex = 0;
			int flowIndex = 0;
			for (int i = 0; i < paramTypes.length; i++) {
				// Obtain the parameter type
				Class<?> paramType = paramTypes[i];

				// Determine if task context
				if (TaskContext.class.equals(paramType)) {
					// Parameter is a task context
					parameters[i] = new TaskContextParameterFactory();
					continue;
				}

				// Determine if flows
				if (paramType.getAnnotation(FlowInterface.class) != null) {
					// Ensure is an interface
					if (!paramType.isInterface()) {
						throw new Exception(
								"Parameter "
										+ paramType.getSimpleName()
										+ " on method "
										+ methodName
										+ " must be an interface as parameter type is annotated with "
										+ FlowInterface.class.getName());
					}

					// Create a flow for each method of the interface
					Method[] flowMethods = paramType.getMethods();
					Map<String, FlowMethodMetaData> flowMethodMetaDatas = new HashMap<String, FlowMethodMetaData>(
							flowMethods.length);
					for (int m = 0; m < flowMethods.length; m++) {
						Method flowMethod = flowMethods[m];
						String flowMethodName = flowMethod.getName();

						// Not include object methods
						if (Object.class.equals(flowMethod.getDeclaringClass())) {
							continue;
						}

						// Ensure not duplicate flow names
						if (flowMethodMetaDatas.containsKey(flowMethodName)) {
							throw new Exception(
									"May not have duplicate flow method names (task="
											+ methodName + ", flow="
											+ paramType.getSimpleName() + "."
											+ flowMethodName + ")");
						}

						// Ensure at most one parameter
						Class<?> flowParameterType;
						Class<?>[] flowMethodParams = flowMethod
								.getParameterTypes();
						if (flowMethodParams.length == 0) {
							flowParameterType = null;
						} else if (flowMethodParams.length == 1) {
							flowParameterType = flowMethodParams[0];
						} else {
							// Invalid to have more than one parameter
							throw new Exception(
									"Flow methods may only have at most one parameter (task "
											+ methodName + ", flow "
											+ paramType.getSimpleName() + "."
											+ flowMethodName + ")");
						}

						// Ensure correct return type
						boolean isReturnFlowFuture;
						Class<?> flowReturnType = flowMethod.getReturnType();
						if (FlowFuture.class.equals(flowReturnType)) {
							// Returns a flow future
							isReturnFlowFuture = true;
						} else if (Void.TYPE.equals(flowReturnType)
								|| (flowReturnType == null)) {
							// Does not return value
							isReturnFlowFuture = false;
						} else {
							// Invalid return type
							throw new Exception("Flow method "
									+ paramType.getSimpleName() + "."
									+ methodName
									+ " return type is invalid (return type="
									+ flowReturnType.getName() + ", task="
									+ methodName + ")");
						}

						// Create and register the flow method meta-data
						FlowMethodMetaData flowMethodMetaData = new FlowMethodMetaData(
								flowIndex++, (flowParameterType != null),
								isReturnFlowFuture);
						flowMethodMetaDatas.put(flowMethodName,
								flowMethodMetaData);

						// Register the flow
						TaskFlowTypeBuilder<Indexed> flowTypeBuilder = taskTypeBuilder
								.addFlow();
						flowTypeBuilder.setLabel(flowMethodName);
						if (flowParameterType != null) {
							flowTypeBuilder.setArgumentType(flowParameterType);
						}
					}

					// Parameter is a flow
					parameters[i] = new FlowParameterFactory(classLoader,
							paramType, flowMethodMetaDatas);
					continue;
				}

				// Otherwise must be an object
				parameters[i] = new ObjectParameterFactory(objectIndex++);
				TaskObjectTypeBuilder<Indexed> objectTypeBuilder = taskTypeBuilder
						.addObject(paramType);
				objectTypeBuilder.setLabel(paramType.getSimpleName());
			}

			// Define the escalation listing
			for (Class<?> escalationType : method.getExceptionTypes()) {
				taskTypeBuilder
						.addEscalation((Class<Throwable>) escalationType);
			}
		}
	}

}