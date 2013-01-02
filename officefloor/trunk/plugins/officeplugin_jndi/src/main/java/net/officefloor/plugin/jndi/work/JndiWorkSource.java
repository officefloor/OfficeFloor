/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.jndi.work;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link WorkSource} to execute {@link Method} instances on a JNDI Object.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiWorkSource extends AbstractWorkSource<JndiWork> {

	/**
	 * Name of property containing the JNDI name of the Object that is to be the
	 * functionality of the {@link Work}.
	 */
	public static final String PROPERTY_JNDI_NAME = "jndi.name";

	/**
	 * Name of property containing the fully qualified type of the expected JNDI
	 * Object.
	 */
	public static final String PROPERTY_WORK_TYPE = "work.type";

	/**
	 * <p>
	 * Name of property containing the fully qualified class of the facade.
	 * <p>
	 * To simplify using JNDI objects as {@link Work}, a facade can be
	 * optionally used to simplify the JNDI object methods for configuring into
	 * {@link OfficeFloor}.
	 * <p>
	 * Only {@link Method} instances of the facade that have a parameter as per
	 * {@link #PROPERTY_WORK_TYPE} are included. The JNDI Object will however
	 * not appear as a {@link TaskObjectType}, as it will be provided from the
	 * {@link Work}.
	 * <p>
	 * Should the facade have a {@link Method} of the same name as a
	 * {@link Method} of the JNDI Object, the facade method will overwrite the
	 * JNDI Object {@link Method}.
	 * <p>
	 * The facade allows for example to:
	 * <ol>
	 * <li>Manipulating the parameter of the {@link Task} to the JNDI Object
	 * {@link Method} parameters</li>
	 * <li>Changing the type/order of parameters to the JNDI Object
	 * {@link Method}</li>
	 * <li>Invoking the JNDI Object {@link Method} instances multiple times to
	 * process lists</li>
	 * </ol>
	 */
	public static final String PROPERTY_FACADE_CLASS = "facade.class";

	/*
	 * ====================== WorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_JNDI_NAME, "JNDI Name");
		context.addProperty(PROPERTY_WORK_TYPE, "Work Type");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<JndiWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the JNDI name
		String jndiName = context.getProperty(PROPERTY_JNDI_NAME);

		// Obtain the work type
		String workTypeName = context.getProperty(PROPERTY_WORK_TYPE);
		final Class<?> workType = context.loadClass(workTypeName);

		// Keep track of the registered tasks
		final Set<String> registeredTasks = new HashSet<String>();

		// Determine if have facade
		String facadeClassName = context.getProperty(PROPERTY_FACADE_CLASS,
				null);

		Class<?> facadeClass = null;
		if ((facadeClassName != null) && (facadeClassName.trim().length() > 0)) {

			// Have facade, so obtain its class
			facadeClass = context.loadClass(facadeClassName);

			// Obtain the listing of tasks from the methods of the facade class
			for (Method method : facadeClass.getMethods()) {

				// Potentially register the method
				this.registerMethodAsPotentialTask(workTypeBuilder, method,
						workType, new TaskFactoryManufacturer() {
							@Override
							@SuppressWarnings("rawtypes")
							public TaskFactory createTaskFactory(
									String taskName, Method method,
									boolean isStatic,
									ParameterFactory[] parameters) {

								// Indicate registered task
								registeredTasks.add(taskName);

								// Create and return the facade task factory
								return new JndiFacadeTaskFactory(method,
										isStatic, parameters);
							}
						});
			}
		}

		// Define the work factory
		workTypeBuilder.setWorkFactory(new JndiWorkFactory(jndiName,
				facadeClass));

		// Obtain the listing of tasks from the methods of the work type
		for (Method method : workType.getMethods()) {

			// Potentially register the method
			this.registerMethodAsPotentialTask(workTypeBuilder, method, null,
					new TaskFactoryManufacturer() {
						@Override
						@SuppressWarnings("rawtypes")
						public TaskFactory createTaskFactory(String taskName,
								Method method, boolean isStatic,
								ParameterFactory[] parameters) {

							// Take facade method over JNDI Object method
							if (registeredTasks.contains(taskName)) {
								return null; // have facade task
							}

							// Create and return the JNDI object task factory
							return new JndiObjectTaskFactory(method, isStatic,
									parameters);
						}
					});
		}
	}

	/**
	 * Manufacturer of a {@link TaskFactory}.
	 */
	private interface TaskFactoryManufacturer {

		/**
		 * Creates the {@link TaskFactory}.
		 * 
		 * @param taskName
		 *            Name of the {@link Task}.
		 * @param method
		 *            {@link Method} for the {@link Task}.
		 * @param isStatic
		 *            <code>true</code> if the {@link Method} is static.
		 * @param parameters
		 *            {@link ParameterFactory} instances for the {@link Task}.
		 * @return {@link TaskFactory}.
		 */
		@SuppressWarnings("rawtypes")
		TaskFactory createTaskFactory(String taskName, Method method,
				boolean isStatic, ParameterFactory[] parameters);
	}

	/**
	 * Registers the {@link Task}.
	 * 
	 * @param workTypeBuilder
	 *            {@link WorkTypeBuilder}.
	 * @param method
	 *            {@link Method} to potentially create the {@link Task} from.
	 * @param workType
	 *            Type of JNDI {@link Work} object. May be <code>null</code> but
	 *            if provided must be one of the parameter types of the
	 *            {@link Method}.
	 * @param manufacturer
	 *            {@link TaskFactoryManufacturer}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerMethodAsPotentialTask(WorkTypeBuilder workTypeBuilder,
			Method method, Class<?> workType,
			TaskFactoryManufacturer manufacturer) {

		// Ignore non-public methods
		if (!Modifier.isPublic(method.getModifiers())) {
			return;
		}

		// Ignore Object methods
		if (Object.class.equals(method.getDeclaringClass())) {
			return;
		}

		// Obtain details of the method
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();

		// Determine if include based on work type provided
		if (workType != null) {
			boolean hasWorkTypeParameter = false;
			for (Class<?> paramType : paramTypes) {
				// (qualified name match due to class loader issues)
				if (workType.equals(paramType)
						|| (workType.getName().equals(paramType.getName()))) {
					hasWorkTypeParameter = true;
				}
			}
			if (!hasWorkTypeParameter) {
				// Does not have work type so do not include method
				return;
			}
		}

		// Create to parameters to method to be populated.
		ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

		// Determine if the method is static
		boolean isStatic = Modifier.isStatic(method.getModifiers());

		// Create the TaskFactory
		TaskFactory taskFactory = manufacturer.createTaskFactory(methodName,
				method, isStatic, parameters);
		if (taskFactory == null) {
			return; // no task factory, so do not register
		}

		// Include method as task in type definition
		TaskTypeBuilder<Indexed, None> taskTypeBuilder = workTypeBuilder
				.addTaskType(methodName, taskFactory, Indexed.class, None.class);

		// Define the return type (it not void)
		Class<?> returnType = method.getReturnType();
		if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
			taskTypeBuilder.setReturnType(returnType);
		}

		// Add the Context dependency
		taskTypeBuilder.addObject(Context.class).setLabel(
				Context.class.getName());

		// Define the listing of task objects
		int objectIndex = 1; // Index after Context dependency
		for (int i = 0; i < paramTypes.length; i++) {
			// Obtain the parameter type
			Class<?> paramType = paramTypes[i];

			// Determine if task context
			if (TaskContext.class.equals(paramType)) {
				// Parameter is a task context.
				parameters[i] = new TaskContextParameterFactory();
				continue;
			}

			// Determine if work type.
			// (qualified name match due to class loader issues)
			if (workType != null) {
				if (workType.equals(paramType)
						|| (workType.getName().equals(paramType.getName()))) {
					// Parameter is the work type.
					parameters[i] = new JndiWorkParameterFactory();
					continue;
				}
			}

			// Otherwise must be an object.
			parameters[i] = new ObjectParameterFactory(objectIndex++);
			taskTypeBuilder.addObject(paramType).setLabel(paramType.getName());
		}

		// Define the escalation listing
		for (Class<?> escalationType : method.getExceptionTypes()) {
			taskTypeBuilder.addEscalation((Class<Throwable>) escalationType);
		}
	}

}