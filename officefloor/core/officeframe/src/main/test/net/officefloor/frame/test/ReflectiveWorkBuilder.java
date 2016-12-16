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
package net.officefloor.frame.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Reflective {@link Work} meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class ReflectiveWorkBuilder implements Work,
		WorkFactory<ReflectiveWorkBuilder> {

	/**
	 * {@link AbstractOfficeConstructTestCase}.
	 */
	private final AbstractOfficeConstructTestCase testCase;

	/**
	 * {@link Work} object to invoke reflectively.
	 */
	private final Object workObject;

	/**
	 * {@link OfficeBuilder} building this {@link Work}.
	 */
	private final OfficeBuilder officeBuilder;

	/**
	 * {@link WorkBuilder}.
	 */
	private final WorkBuilder<ReflectiveWorkBuilder> workBuilder;

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workObject
	 *            {@link Work} object to invoke reflectively.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} to load {@link WorkBuilder}.
	 * @param initialTaskName
	 *            Initial task name.
	 */
	public ReflectiveWorkBuilder(AbstractOfficeConstructTestCase testCase,
			String workName, Object workObject, OfficeBuilder officeBuilder,
			String initialTaskName) {
		this.testCase = testCase;
		this.workObject = workObject;
		this.officeBuilder = officeBuilder;

		// Create and initiate the work builder
		this.workBuilder = officeBuilder.addWork(workName, this);

		// Specify initial task only if provided
		if (initialTaskName != null) {
			this.workBuilder.setInitialTask(initialTaskName);
		}
	}

	/**
	 * Obtains the {@link WorkBuilder}.
	 * 
	 * @return {@link WorkBuilder}.
	 */
	public WorkBuilder<ReflectiveWorkBuilder> getBuilder() {
		return this.workBuilder;
	}

	/**
	 * Builds a reflective {@link Task} on the work object.
	 * 
	 * @param methodName
	 *            Name of the method to invoke.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link ReflectiveTaskBuilder} for the method.
	 */
	public ReflectiveTaskBuilder buildTask(String methodName, String teamName) {

		// Obtain the method name for the task
		Method taskMethod = null;
		for (Method method : this.workObject.getClass().getMethods()) {
			if (method.getName().equals(methodName)) {
				taskMethod = method;
			}
		}
		if (taskMethod == null) {
			TestCase.fail("No method '" + methodName + "' on work "
					+ this.workObject.getClass().getName());
		}

		// Create the reflective task meta-data
		ReflectiveTaskBuilder taskMetaData = new ReflectiveTaskBuilder(
				taskMethod);

		// Create the task builder (parameter type Object)
		TaskBuilder<ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder = this.workBuilder
				.addTask(methodName, taskMetaData);
		taskMetaData.setTaskBuilder(taskBuilder);

		// Initiate the task builder
		taskBuilder.setTeam(teamName);

		// Return the task meta-data
		return taskMetaData;
	}

	/*
	 * ============== WorkFactory =====================================
	 */

	@Override
	public ReflectiveWorkBuilder createWork() {
		return this;
	}

	/**
	 * Reflective {@link Task} meta-data.
	 */
	public class ReflectiveTaskBuilder implements
			TaskFactory<ReflectiveWorkBuilder, Indexed, Indexed>,
			Task<ReflectiveWorkBuilder, Indexed, Indexed> {

		/**
		 * {@link Method} on work object to invoke.
		 */
		private final Method method;

		/**
		 * Types for the parameters of the {@link Method}.
		 */
		private final Class<?>[] parameterTypes;

		/**
		 * {@link TaskBuilder}.
		 */
		private TaskBuilder<ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder;

		/**
		 * {@link ParameterFactory} instances for the method.
		 */
		private final ParameterFactory[] parameterFactories;

		/**
		 * Next index to specify the {@link ParameterFactory}.
		 */
		private int parameterIndex = 0;

		/**
		 * Next index to specify object.
		 */
		private int objectIndex = 0;

		/**
		 * Next index to specify flow.
		 */
		private int flowIndex = 0;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            {@link Method} on work object to invoke.
		 */
		public ReflectiveTaskBuilder(Method method) {
			this.method = method;

			// Create the parameter factories for the method
			this.parameterTypes = this.method.getParameterTypes();
			this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
		}

		/**
		 * Specifies the {@link TaskBuilder}.
		 * 
		 * @param taskBuilder
		 *            {@link TaskBuilder}.
		 */
		void setTaskBuilder(
				TaskBuilder<ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder) {
			this.taskBuilder = taskBuilder;
		}

		/**
		 * Obtains the {@link TaskBuilder}.
		 * 
		 * @return {@link TaskBuilder}.
		 */
		public TaskBuilder<ReflectiveWorkBuilder, Indexed, Indexed> getBuilder() {
			return this.taskBuilder;
		}

		/**
		 * Builds the {@link TaskContext}.
		 */
		public void buildTaskContext() {

			// Ensure parameter is TaskContext
			Class<?> parameterType = this.parameterTypes[this.parameterIndex];
			TestCase.assertTrue("Parameter " + this.parameterIndex
					+ " must be " + TaskContext.class.getSimpleName(),
					TaskContext.class.isAssignableFrom(parameterType));

			// Link TaskContext
			this.parameterFactories[this.parameterIndex] = new TaskContextParameterFactory();

			// Set for next parameter
			this.parameterIndex++;
		}

		/**
		 * Builds the parameter.
		 */
		public void buildParameter() {

			// Obtain the type of the parameter
			Class<?> parameterType = this.parameterTypes[this.parameterIndex];

			// Link parameter and setup to return
			this.taskBuilder.linkParameter(this.objectIndex, parameterType);
			this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(
					this.objectIndex);

			// Set for next object and parameter
			this.objectIndex++;
			this.parameterIndex++;
		}

		/**
		 * Links the {@link ManagedObject} to the {@link Task}.
		 * 
		 * @param scopeManagedObjectName
		 *            Scope name of the {@link ManagedObject}.
		 */
		public void buildObject(String scopeManagedObjectName) {

			// Obtain the type of the object
			Class<?> objectType = this.parameterTypes[this.parameterIndex];

			// Link to task and setup to return
			this.taskBuilder.linkManagedObject(this.objectIndex,
					scopeManagedObjectName, objectType);
			this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(
					this.objectIndex);

			// Set for next object and parameter
			this.objectIndex++;
			this.parameterIndex++;
		}

		/**
		 * Builds the {@link ManagedObjectScope} bound {@link ManagedObject}.
		 * 
		 * @param officeManagedObjectName
		 *            {@link Office} name of the {@link ManagedObject}.
		 * @param managedObjectScope
		 *            {@link ManagedObjectScope} for the {@link ManagedObject}.
		 * @return {@link DependencyMappingBuilder}.
		 */
		public DependencyMappingBuilder buildObject(
				String officeManagedObjectName,
				ManagedObjectScope managedObjectScope) {

			// Build the managed object based on scope
			DependencyMappingBuilder mappingBuilder;
			switch (managedObjectScope) {
			case WORK:
				mappingBuilder = ReflectiveWorkBuilder.this.workBuilder
						.addWorkManagedObject(officeManagedObjectName,
								officeManagedObjectName);
				break;

			case THREAD:
				mappingBuilder = ReflectiveWorkBuilder.this.officeBuilder
						.addThreadManagedObject(officeManagedObjectName,
								officeManagedObjectName);
				break;

			case PROCESS:
				mappingBuilder = ReflectiveWorkBuilder.this.officeBuilder
						.addProcessManagedObject(officeManagedObjectName,
								officeManagedObjectName);
				break;

			default:
				TestCase.fail("Unknown managed object scope "
						+ managedObjectScope);
				return null;
			}

			// Link to object to task
			this.buildObject(officeManagedObjectName);

			// Return the dependency mapping builder
			return mappingBuilder;
		}

		/**
		 * Builds the flow.
		 * 
		 * @param taskName
		 *            Task name.
		 * @param strategy
		 *            {@link FlowInstigationStrategyEnum}.
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 */
		public void buildFlow(String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			this.buildFlow(null, taskName, strategy, argumentType);
		}

		/**
		 * Builds the flow.
		 * 
		 * @param workName
		 *            Work name.
		 * @param taskName
		 *            Task name.
		 * @param strategy
		 *            {@link FlowInstigationStrategyEnum}.
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 */
		public void buildFlow(String workName, String taskName,
				FlowInstigationStrategyEnum strategy, Class<?> argumentType) {
			// Link in the flow and allow for invocation
			if (workName != null) {
				this.taskBuilder.linkFlow(this.flowIndex, workName, taskName,
						strategy, argumentType);
			} else {
				this.taskBuilder.linkFlow(this.flowIndex, taskName, strategy,
						argumentType);
			}
			this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(
					this.flowIndex);

			// Set for next flow and parameter
			this.flowIndex++;
			this.parameterIndex++;
		}

		/**
		 * Specifies the next {@link Task} using the return type of the
		 * {@link Method} as the argument type.
		 * 
		 * @param taskName
		 *            Task name.
		 */
		public void setNextTaskInFlow(String taskName) {
			this.setNextTaskInFlow(null, taskName);
		}

		/**
		 * Specifies the next {@link Task} using the return type of the
		 * {@link Method} as the argument type.
		 * 
		 * @param workName
		 *            Work name.
		 * @param taskName
		 *            Task name.
		 */
		public void setNextTaskInFlow(String workName, String taskName) {

			// Obtain the method return type
			Class<?> returnType = this.method.getReturnType();
			if (returnType == Void.class) {
				returnType = null; // null if no argument type
			}

			// Specify the next task
			if (workName != null) {
				this.taskBuilder.setNextTaskInFlow(workName, taskName,
						returnType);
			} else {
				this.taskBuilder.setNextTaskInFlow(taskName, returnType);
			}
		}

		/*
		 * ============== TaskFactory =======================================
		 */

		@Override
		public Task<ReflectiveWorkBuilder, Indexed, Indexed> createTask(
				ReflectiveWorkBuilder work) {
			return this;
		}

		/*
		 * ===================== Task ====================================
		 */

		@Override
		public Object doTask(
				TaskContext<ReflectiveWorkBuilder, Indexed, Indexed> context)
				throws Throwable {

			// Create the parameters
			Object[] parameters = new Object[this.method.getParameterTypes().length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = this.parameterFactories[i]
						.createParamater(context);
			}

			// Record invoking method
			ReflectiveWorkBuilder.this.testCase
					.recordReflectiveTaskMethodInvoked(this.method.getName());

			// Invoke the method on work object to get return
			Object returnValue;
			try {
				returnValue = this.method.invoke(
						ReflectiveWorkBuilder.this.workObject, parameters);
			} catch (InvocationTargetException ex) {
				// Throw cause of exception
				throw ex.getCause();
			}

			// Return the value from method
			return returnValue;
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link TaskContext}.
	 */
	private static interface ParameterFactory {
		Object createParamater(
				TaskContext<ReflectiveWorkBuilder, Indexed, Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to obtain the {@link TaskContext}.
	 */
	private static class TaskContextParameterFactory implements
			ParameterFactory {

		@Override
		public Object createParamater(
				TaskContext<ReflectiveWorkBuilder, Indexed, Indexed> context) {
			return context;
		}
	}

	/**
	 * {@link ParameterFactory} to obtain a dependency.
	 */
	private static class ObjectParameterFactory implements ParameterFactory {

		/**
		 * Index of the object.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index
		 *            Index of the object.
		 */
		public ObjectParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(
				TaskContext<ReflectiveWorkBuilder, Indexed, Indexed> context) {
			// Return the managed object
			return context.getObject(index);
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the flow.
	 */
	private class ReflectiveFlowParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index
		 * 
		 */
		public ReflectiveFlowParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(
				final TaskContext<ReflectiveWorkBuilder, Indexed, Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public FlowFuture doFlow(Object parameter) {
					// Invoke the flow and return the flow future
					return context.doFlow(
							ReflectiveFlowParameterFactory.this.index,
							parameter);
				}
			};
		}
	}

}