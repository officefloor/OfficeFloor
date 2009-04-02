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
package net.officefloor.compile.impl.desk;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.desk.DeskOperations;
import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.compile.spi.work.TaskEscalationType;
import net.officefloor.compile.spi.work.TaskFlowType;
import net.officefloor.compile.spi.work.TaskObjectType;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Provides abstract functionality for the {@link DeskOperations}.
 * 
 * @author Daniel
 */
public abstract class AbstractDeskOperationsTestCase extends
		OfficeFrameTestCase {

	/**
	 * {@link DeskModel} loaded for testing.
	 */
	protected DeskModel desk;

	/**
	 * {@link DeskOperations} to be tested.
	 */
	protected DeskOperations operations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Retrieve the setup desk
		this.desk = this.retrieveDesk("setup", null);

		// Create the desk operations
		this.operations = new DeskOperationsImpl(this.desk);
	}

	/**
	 * Validates the {@link DeskModel} against the default {@link DeskModel}
	 * file for the test.
	 */
	protected void validateDesk() {
		this.validateDesk(null);
	}

	/**
	 * Validates the {@link DeskModel} against the specific {@link DeskModel}
	 * file for the test.
	 * 
	 * @param specific
	 *            Indicates the specific {@link DeskModel} file for the test.
	 */
	protected void validateDesk(String specific) {
		this.validateDesk(this.getName(), specific);
	}

	/**
	 * <p>
	 * Validates the {@link DeskModel} against the {@link DeskModel} setup for
	 * testing.
	 * <p>
	 * This is useful to test the revert functionality of a {@link Change}.
	 */
	protected void validateAsSetupDesk() {
		this.validateDesk("setup", null);
	}

	/**
	 * Validates the {@link DeskModel}.
	 * 
	 * @param testName
	 *            Name of the test.
	 * @param specific
	 *            Specific name for the test. May be <code>null</code> for the
	 *            default {@link DeskModel} for the test.
	 */
	private void validateDesk(String testName, String specific) {

		// Obtain the desk
		DeskModel compareDesk = this.retrieveDesk(testName, specific);

		try {
			// Ensure the desks are the same
			assertGraph(compareDesk, this.desk,
					RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			StringWriter msg = new StringWriter();
			ex.printStackTrace(new PrintWriter(msg));
			fail("Failed to validate graph");
		}
	}

	/**
	 * Retrieves the {@link DeskModel} for the test.
	 * 
	 * @param testName
	 *            Name of the test.
	 * @param specific
	 *            Specific name for the test. May be <code>null</code> for the
	 *            default {@link DeskModel} for the test.
	 * @return {@link DeskModel}.
	 */
	private DeskModel retrieveDesk(String testName, String specific) {

		// Move to 'Test' to start of test case name
		String testCasePath = this.getClass().getSimpleName();
		testCasePath = this.getClass().getPackage().getName().replace('.', '/')
				+ "/Test"
				+ testCasePath.substring(0, (testCasePath.length() - "Test"
						.length()));

		// Construct the path to the desk
		String testPath = testCasePath.replace('.', '/') + "/" + testName;
		String deskPath = testPath + (specific == null ? "" : "/" + specific)
				+ ".desk.xml";

		try {
			// Obtain the configuration item to the desk
			ConfigurationItem item = new ClassLoaderConfigurationContext(this
					.getClass().getClassLoader())
					.getConfigurationItem(deskPath);
			assertNotNull("Can not find desk model configuration: " + deskPath,
					item);

			// Return the retrieved desk
			return new DeskRepositoryImpl(new ModelRepositoryImpl())
					.retrieveDesk(item);

		} catch (Exception ex) {
			// Fail on failure (stops have to throw exception in tests)
			StringWriter msg = new StringWriter();
			ex.printStackTrace(new PrintWriter(msg));
			fail("Failed to retrieveDesk: " + deskPath + "\n" + msg.toString());
			return null; // fail will throw
		}
	}

	/**
	 * Creates a {@link WorkType}.
	 * 
	 * @param constructor
	 *            {@link WorkTypeConstructor} to construct the {@link WorkType}.
	 * @return {@link WorkType}.
	 */
	protected WorkType<?> constructWorkType(WorkTypeConstructor constructor) {

		// Create the work type builder
		WorkTypeImpl<?> workTypeBuilder = new WorkTypeImpl<Work>();

		// Build the work type via the constructor
		WorkTypeContext context = new WorkTypeContextImpl(workTypeBuilder);
		constructor.construct(context);

		// Return the work type
		return workTypeBuilder;
	}

	/**
	 * {@link WorkTypeConstructor} to construct the {@link WorkType}.
	 */
	protected interface WorkTypeConstructor {

		/**
		 * Constructs the {@link WorkType}.
		 * 
		 * @param context
		 *            {@link WorkTypeContext}.
		 */
		void construct(WorkTypeContext context);
	}

	/**
	 * Context to construct the {@link WorkType}.
	 */
	protected interface WorkTypeContext {

		/**
		 * Adds a {@link TaskType}.
		 * 
		 * @param taskName
		 *            Name of the {@link Task}.
		 * @return {@link TaskTypeConstructor} to provide simplified
		 *         {@link TaskType} construction.
		 */
		TaskTypeConstructor addTask(String taskName);

		/**
		 * Adds a {@link TaskTypeBuilder}.
		 * 
		 * @param taskName
		 *            Name of the {@link Task}.
		 * @param dependencyKeys
		 *            Dependency keys {@link Enum}.
		 * @param flowKeys
		 *            Flow keys {@link Enum}.
		 * @return {@link TaskTypeBuilder}.
		 */
		<D extends Enum<D>, F extends Enum<F>> TaskTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys);
	}

	/**
	 * Provides simplified construction of a {@link TaskType}.
	 */
	protected interface TaskTypeConstructor {

		/**
		 * Adds a {@link TaskObjectType}.
		 * 
		 * @param objectType
		 *            {@link Object} type.
		 * @param key
		 *            Key identifying the {@link TaskObjectType}.
		 * @return {@link TaskObjectTypeBuilder} for the added
		 *         {@link TaskObjectType}.
		 */
		TaskObjectTypeBuilder<?> addObject(Class<?> objectType, Enum<?> key);

		/**
		 * Adds a {@link TaskFlowType}.
		 * 
		 * @param argumentType
		 *            Argument type.
		 * @param key
		 *            Key identifying the {@link TaskFlowType}.
		 * @return {@link TaskFlowTypeBuilder} for the added
		 *         {@link TaskObjectType}.
		 */
		TaskFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key);

		/**
		 * Adds a {@link TaskEscalationType}.
		 * 
		 * @param escalationType
		 *            Escalation type.
		 * @return {@link TaskEscalationTypeBuilder} for the added
		 *         {@link TaskEscalationType}.
		 */
		TaskEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType);

		/**
		 * Obtains the underlying {@link TaskTypeBuilder}.
		 * 
		 * @return Underlying {@link TaskTypeBuilder}.
		 */
		TaskTypeBuilder<?, ?> getBuilder();
	}

	/**
	 * {@link WorkTypeContext} implementation.
	 */
	private class WorkTypeContextImpl implements WorkTypeContext {

		/**
		 * {@link WorkTypeBuilder}.
		 */
		private final WorkTypeBuilder<?> workTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param workTypeBuilder
		 *            {@link WorkTypeBuilder}.
		 */
		public WorkTypeContextImpl(WorkTypeBuilder<?> workTypeBuilder) {
			this.workTypeBuilder = workTypeBuilder;
		}

		/*
		 * ================== WorkTypeContext ============================
		 */

		@Override
		public TaskTypeConstructor addTask(String taskName) {
			// Add the task
			TaskTypeBuilder<?, ?> taskTypeBuilder = this.workTypeBuilder
					.addTaskType(taskName, null, (Class<Indexed>) null,
							(Class<Indexed>) null);

			// Return the task type constructor for the task type builder
			return new TaskTypeConstructorImpl(taskTypeBuilder);
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> TaskTypeBuilder<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys) {
			return this.workTypeBuilder.addTaskType(taskName, null,
					dependencyKeys, flowKeys);
		}
	}

	/**
	 * {@link TaskTypeConstructor} implementation.
	 */
	private class TaskTypeConstructorImpl implements TaskTypeConstructor {

		/**
		 * {@link TaskTypeBuilder}.
		 */
		private final TaskTypeBuilder<?, ?> taskTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param taskTypeBuilder
		 *            {@link TaskTypeBuilder}.
		 */
		public TaskTypeConstructorImpl(TaskTypeBuilder<?, ?> taskTypeBuilder) {
			this.taskTypeBuilder = taskTypeBuilder;
		}

		/*
		 * ================= TaskTypeConstructor ===========================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public TaskObjectTypeBuilder<?> addObject(Class<?> objectType,
				Enum<?> key) {
			TaskObjectTypeBuilder object = this.taskTypeBuilder
					.addObject(objectType);
			if (key != null) {
				object.setKey(key);
			}
			return object;
		}

		@Override
		@SuppressWarnings("unchecked")
		public TaskFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key) {
			TaskFlowTypeBuilder flow = this.taskTypeBuilder.addFlow();
			flow.setArgumentType(argumentType);
			if (key != null) {
				flow.setKey(key);
			}
			return flow;
		}

		@Override
		public TaskEscalationTypeBuilder addEscalation(
				Class<? extends Throwable> escalationType) {
			TaskEscalationTypeBuilder escalation = this.taskTypeBuilder
					.addEscalation(escalationType);
			return escalation;
		}

		@Override
		public TaskTypeBuilder<?, ?> getBuilder() {
			return this.taskTypeBuilder;
		}
	}

}