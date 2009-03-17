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
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Reflective {@link Work} meta-data.
 * 
 * @author Daniel
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
		TaskBuilder<Object, ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder = this.workBuilder
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	@Override
	public ReflectiveWorkBuilder createWork() {
		return this;
	}

	/**
	 * Reflective {@link Task} meta-data.
	 */
	public class ReflectiveTaskBuilder implements
			TaskFactory<Object, ReflectiveWorkBuilder, Indexed, Indexed>,
			Task<Object, ReflectiveWorkBuilder, Indexed, Indexed> {

		/**
		 * {@link Method} on work object to invoke.
		 */
		private final Method method;

		/**
		 * {@link TaskBuilder}.
		 */
		private TaskBuilder<Object, ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder;

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
			this.parameterFactories = new ParameterFactory[this.method
					.getParameterTypes().length];
		}

		/**
		 * Specifies the {@link TaskBuilder}.
		 * 
		 * @param taskBuilder
		 *            {@link TaskBuilder}.
		 */
		void setTaskBuilder(
				TaskBuilder<Object, ReflectiveWorkBuilder, Indexed, Indexed> taskBuilder) {
			this.taskBuilder = taskBuilder;
		}

		/**
		 * Obtains the {@link TaskBuilder}.
		 * 
		 * @return {@link TaskBuilder}.
		 */
		public TaskBuilder<Object, ReflectiveWorkBuilder, Indexed, Indexed> getBuilder() {
			return this.taskBuilder;
		}

		/**
		 * Builds the parameter.
		 */
		public void buildParameter() {
			// Link in the parameter
			this.parameterFactories[this.parameterIndex] = new ParameterParameterFactory();

			// Set for next parameter
			this.parameterIndex++;
		}

		/**
		 * Links the {@link ManagedObject} to the {@link Task}.
		 * 
		 * @param scopeManagedObjectName
		 *            Scope name of the {@link ManagedObject}.
		 */
		public void buildObject(String scopeManagedObjectName) {

			// Link to task and setup to return
			this.taskBuilder.linkManagedObject(this.objectIndex,
					scopeManagedObjectName);
			this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(
					this.objectIndex);

			// Set for next managed object and parameter
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

			// Link to task and setup to return
			this.taskBuilder.linkManagedObject(this.objectIndex,
					officeManagedObjectName);
			this.parameterFactories[this.parameterIndex] = new ObjectParameterFactory(
					this.objectIndex);

			// Set for next managed object and parameter
			this.objectIndex++;
			this.parameterIndex++;

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
		 */
		public void buildFlow(String taskName,
				FlowInstigationStrategyEnum strategy) {
			this.buildFlow(null, taskName, strategy);
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
		 */
		public void buildFlow(String workName, String taskName,
				FlowInstigationStrategyEnum strategy) {
			// Link in the flow and allow for invocation
			if (workName != null) {
				this.taskBuilder.linkFlow(this.flowIndex, workName, taskName,
						strategy);
			} else {
				this.taskBuilder.linkFlow(this.flowIndex, taskName, strategy);
			}
			this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(
					this.flowIndex);

			// Set for next flow and parameter
			this.flowIndex++;
			this.parameterIndex++;
		}

		/*
		 * ============== TaskFactory =======================================
		 */

		@Override
		public Task<Object, ReflectiveWorkBuilder, Indexed, Indexed> createTask(
				ReflectiveWorkBuilder work) {
			return this;
		}

		/*
		 * ===================== Task ====================================
		 */

		@Override
		public Object doTask(
				TaskContext<Object, ReflectiveWorkBuilder, Indexed, Indexed> context)
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
			try {
				return this.method.invoke(
						ReflectiveWorkBuilder.this.workObject, parameters);
			} catch (InvocationTargetException ex) {
				// Throw cause of exception
				throw ex.getCause();
			}
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link TaskContext}.
	 */
	private static interface ParameterFactory {
		Object createParamater(
				TaskContext<Object, ReflectiveWorkBuilder, Indexed, Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to create the parameter.
	 */
	private static class ParameterParameterFactory implements ParameterFactory {

		@Override
		public Object createParamater(
				TaskContext<Object, ReflectiveWorkBuilder, Indexed, Indexed> context) {
			return context.getParameter();
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the object of a managed object.
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
				TaskContext<Object, ReflectiveWorkBuilder, Indexed, Indexed> context) {
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
				final TaskContext<Object, ReflectiveWorkBuilder, Indexed, Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public void doFlow(Object parameter) {
					// Invoke the flow
					context.doFlow(ReflectiveFlowParameterFactory.this.index,
							parameter);
				}
			};
		}
	}

}
