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

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link ClassWorkSource}.
 * 
 * @author Daniel
 */
public class ClassWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext taskContext = this.createMock(TaskContext.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		MockClass.reset(this.taskContext);
	}

	/**
	 * Ensures specification correct.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to load {@link WorkType} for the {@link ClassWorkSource}.
	 */
	public void testWorkType() throws Exception {

		// Create the work type builder
		WorkTypeBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(MockClass.class));

		// taskInstanceMethod
		TaskTypeBuilder<?, ?> instanceMethod = work.addTaskType(
				"taskInstanceMethod", new ClassTaskFactory(null, false, null),
				null, null);
		instanceMethod.setReturnType(String.class);
		instanceMethod.addObject(String.class).setLabel(
				String.class.getSimpleName());
		instanceMethod.addFlow().setLabel("sequential");
		TaskFlowTypeBuilder<?> parallel = instanceMethod.addFlow();
		parallel.setLabel("parallel");
		parallel.setArgumentType(Integer.class);
		TaskFlowTypeBuilder<?> asynchronous = instanceMethod.addFlow();
		asynchronous.setLabel("asynchronous");
		asynchronous.setArgumentType(String.class);
		instanceMethod.addEscalation(IOException.class);

		// taskFailMethod
		TaskTypeBuilder<?, ?> failMethod = work.addTaskType(
				"taskFailMethod", new ClassTaskFactory(null, false, null),
				null, null);
		failMethod.addEscalation(SQLException.class);

		// taskStaticMethod
		TaskTypeBuilder<?, ?> staticMethod = work.addTaskType(
				"taskStaticMethod", new ClassTaskFactory(null, false, null),
				null, null);
		staticMethod.setReturnType(Object.class);

		// Validate the work type
		WorkLoaderUtil.validateWorkType(work, ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, MockClass.class
						.getName());
	}

	/**
	 * Ensure can invoke the the instance {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void testInvokeInstanceMethod() throws Throwable {

		final FlowFuture ignoreFlowFuture = this.createMock(FlowFuture.class);
		final FlowFuture flowFuture = this.createMock(FlowFuture.class);
		final String PARAMETER_VALUE = "PARAMETER";
		final String RETURN_VALUE = "INSTANCE RETURN VALUE";

		// Create the task
		WorkType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, MockClass.class
						.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		TaskType<ClassWork, ?, ?> taskType = workType.getTaskTypes()[1];
		Task<ClassWork, ?, ?> task = taskType.getTaskFactory().createTask(work);
		assertEquals("Incorrect task", "taskInstanceMethod", taskType
				.getTaskName());

		// Record invoking method
		MockClass.expectedParameter = PARAMETER_VALUE;
		MockClass.returnValue = RETURN_VALUE;
		this.recordReturn(this.taskContext, this.taskContext.getWork(), work);
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				PARAMETER_VALUE);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(0, null),
				ignoreFlowFuture);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(1,
				new Integer(1)), ignoreFlowFuture);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(2,
				PARAMETER_VALUE), flowFuture);
		this.taskContext.join(flowFuture, 1000, "TOKEN");

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the task ensuring the correct return value
		Object returnValue = task.doTask(this.taskContext);
		assertEquals("Incorrect return value", RETURN_VALUE, returnValue);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure can throw an {@link Exception} from the {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void testThrowException() throws Throwable {

		final SQLException exception = new SQLException("Method failure");

		// Create the task
		WorkType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, MockClass.class
						.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		TaskType<ClassWork, ?, ?> taskType = workType.getTaskTypes()[0];
		Task<ClassWork, ?, ?> task = taskType.getTaskFactory().createTask(work);
		assertEquals("Incorrect task", "taskFailMethod", taskType.getTaskName());

		// Record invoking method
		MockClass.sqlException = exception;
		this.recordReturn(this.taskContext, this.taskContext.getWork(), work);

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the task ensuring it throws exception
		try {
			task.doTask(this.taskContext);
			fail("Should not return succesfully");
		} catch (SQLException ex) {
			assertEquals("Incorrect failure", exception, ex);
		}

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Ensure can invoke static {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void testStaticException() throws Throwable {

		final String RETURN_VALUE = "STATIC RETURN VALUE";

		// Create the task
		WorkType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME, MockClass.class
						.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		TaskType<ClassWork, ?, ?> taskType = workType.getTaskTypes()[2];
		Task<ClassWork, ?, ?> task = taskType.getTaskFactory().createTask(work);
		assertEquals("Incorrect task", "taskStaticMethod", taskType
				.getTaskName());

		// Record invoking method
		MockClass.returnValue = RETURN_VALUE;

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the task ensuring the correct return value
		Object returnValue = task.doTask(this.taskContext);
		assertEquals("Incorrect return value", RETURN_VALUE, returnValue);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Class} to load as {@link ClassWork}.
	 */
	public static class MockClass {

		/**
		 * Expected parameter.
		 */
		public static String expectedParameter;

		/**
		 * Expected {@link TaskContext}.
		 */
		public static TaskContext<?, ?, ?> expectedContext;

		/**
		 * Value to be returned from the {@link Method}.
		 */
		public static String returnValue;

		/**
		 * {@link SQLException}.
		 */
		public static SQLException sqlException;

		/**
		 * Resets for the next test.
		 * 
		 * @param expectedContext
		 *            Expected {@link TaskContext}.
		 */
		static void reset(TaskContext<?, ?, ?> expectedContext) {
			expectedParameter = null;
			MockClass.expectedContext = expectedContext;
			returnValue = null;
			sqlException = null;
		}

		/**
		 * {@link Task} taskMethod.
		 */
		public String taskInstanceMethod(String parameter, MockFlows flows,
				TaskContext<?, ?, ?> context) throws IOException {

			// Ensure correct inputs
			assertEquals("Incorrect parameter", expectedParameter, parameter);
			assertNotNull("Must have flows", flows);
			assertEquals("Incorrect task context", expectedContext, context);

			// Invoke the flows
			flows.sequential();
			flows.parallel(new Integer(1));
			FlowFuture flowFuture = flows.asynchronous(parameter);

			// Ensure can join on the flow future
			assertNotNull("Must obtain flow future", flowFuture);
			context.join(flowFuture, 1000, "TOKEN");

			// Return the value
			return returnValue;
		}

		/**
		 * {@link Task} anotherMethod.
		 */
		public void taskFailMethod() throws SQLException {
			throw sqlException;
		}

		/**
		 * {@link Task} staticMethod.
		 */
		public static Object taskStaticMethod() {
			return returnValue;
		}

		/**
		 * Private method to the class.
		 */
		Object nonTaskMethod(Object parameter) {
			return null;
		}
	}

	/**
	 * Mock interface for flows from the {@link MockClass}.
	 */
	@FlowInterface
	public static interface MockFlows {

		/**
		 * Sequential invocation. Does not have a parameter.
		 */
		void sequential();

		/**
		 * Parallel invocation.
		 * 
		 * @param parameter
		 *            Parameter.
		 */
		void parallel(Integer parameter);

		/**
		 * Asynchronous invocation.
		 * 
		 * @param parameter
		 *            Parameter.
		 * @return {@link FlowFuture}.
		 */
		FlowFuture asynchronous(String parameter);
	}

}