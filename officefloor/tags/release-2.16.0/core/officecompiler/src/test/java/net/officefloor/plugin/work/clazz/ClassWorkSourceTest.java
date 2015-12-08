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
package net.officefloor.plugin.work.clazz;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Test the {@link ClassWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final TaskContext taskContext = this.createMock(TaskContext.class);

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
	 * Ensure able to provider {@link Qualifier} to dependency name.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testQualifiedDependency() throws Exception {

		// Create the work type builder
		WorkTypeBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(
						MockQualifiedClass.class));

		// task
		TaskTypeBuilder task = work.addTaskType("task", new ClassTaskFactory(
				null, false, null), null, null);
		TaskObjectTypeBuilder<?> objectOne = task.addObject(String.class);
		objectOne.setTypeQualifier(MockQualification.class.getName());
		objectOne.setLabel(MockQualification.class.getName() + "-"
				+ String.class.getName());
		TaskObjectTypeBuilder<?> objectTwo = task.addObject(String.class);
		objectTwo.setLabel(String.class.getName());

		// Validate the work type
		WorkLoaderUtil.validateWorkType(work, ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockQualifiedClass.class.getName());
	}

	/**
	 * Mock {@link Qualifier}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public static @interface MockQualification {
	}

	/**
	 * Mock qualified {@link Class} to load as {@link ClassWork}.
	 */
	public static class MockQualifiedClass {
		public void task(@MockQualification String objectOne, String objectTwo) {
		}
	}

	/**
	 * Ensure issue if provide multiple {@link Qualifier} to dependency name.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMultipleQualifiedDependency() throws Exception {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.addIssue(LocationType.SECTION, null, AssetType.WORK, null,
				"Failed to source WorkType definition from WorkSource "
						+ ClassWorkSource.class.getName(),
				new IllegalArgumentException(
						"Method task parameter 0 has more than one Qualifier"));
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {

				// Match initial parameters
				for (int i = 0; i < 5; i++) {
					Object e = expected[i];
					Object a = actual[i];
					if ((e == null) && (a == null)) {
						continue; // match on null
					} else if ((e != null) && (e.equals(actual[i]))) {
						continue; // match not null
					} else {
						return false; // not match
					}
				}

				// Match exception
				IllegalArgumentException eEx = (IllegalArgumentException) expected[5];
				IllegalArgumentException aEx = (IllegalArgumentException) actual[5];
				if (!eEx.getMessage().equals(aEx.getMessage())) {
					return false; // not match
				}

				// As here, matches
				return true;
			}
		});

		// Create the work type builder
		WorkTypeBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(
						MockMultipleQualifiedClass.class));

		// task
		TaskTypeBuilder task = work.addTaskType("task", new ClassTaskFactory(
				null, false, null), null, null);
		task.addObject(String.class)
				.setLabel(MockQualification.class.getName());

		// Test
		this.replayMockObjects();

		// Validate the work type
		WorkType<?> type = WorkLoaderUtil.loadWorkType(ClassWorkSource.class,
				compiler, ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockMultipleQualifiedClass.class.getName());
		assertNull("Should not load work with multiple qualifers", type);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Another mock {@link Qualifier}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockAnotherQualification {
	}

	/**
	 * Mock multiple qualified {@link Class} to load as {@link ClassWork}.
	 */
	public static class MockMultipleQualifiedClass {
		public void task(
				@MockQualification @MockAnotherQualification String dependency) {
		}
	}

	/**
	 * Ensure able to load {@link WorkType} for the {@link ClassWorkSource}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testWorkType() throws Exception {

		// Create the work type builder
		WorkTypeBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(MockClass.class));

		// taskInstanceMethod
		TaskTypeBuilder instanceMethod = work.addTaskType("taskInstanceMethod",
				new ClassTaskFactory(null, false, null), null, null);
		instanceMethod.setReturnType(String.class);
		instanceMethod.addObject(String.class).setLabel(String.class.getName());
		TaskFlowTypeBuilder<?> asynchronous = instanceMethod.addFlow();
		asynchronous.setLabel("asynchronous");
		asynchronous.setArgumentType(String.class);
		TaskFlowTypeBuilder<?> parallel = instanceMethod.addFlow();
		parallel.setLabel("parallel");
		parallel.setArgumentType(Integer.class);
		instanceMethod.addFlow().setLabel("sequential");
		instanceMethod.addEscalation(IOException.class);

		// taskFailMethod
		TaskTypeBuilder failMethod = work.addTaskType("taskFailMethod",
				new ClassTaskFactory(null, false, null), null, null);
		failMethod.addEscalation(SQLException.class);

		// taskStaticMethod
		TaskTypeBuilder staticMethod = work.addTaskType("taskStaticMethod",
				new ClassTaskFactory(null, false, null), null, null);
		staticMethod.setReturnType(Object.class);

		// Validate the work type
		WorkLoaderUtil.validateWorkType(work, ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
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

		// Index order of flows due to sorting by method name
		final int SEQUENTIAL_FLOW_INDEX = 2;
		final int PARALLEL_FLOW_INDEX = 1;
		final int ASYNCHRONOUS_FLOW_INDEX = 0;

		// Create the task
		WorkType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		TaskType<ClassWork, ?, ?> taskType = workType.getTaskTypes()[1];
		Task<ClassWork, ?, ?> task = taskType.getTaskFactory().createTask(work);
		assertEquals("Incorrect task", "taskInstanceMethod",
				taskType.getTaskName());

		// Record invoking method
		MockClass.expectedParameter = PARAMETER_VALUE;
		MockClass.returnValue = RETURN_VALUE;
		this.recordReturn(this.taskContext, this.taskContext.getWork(), work);
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				PARAMETER_VALUE);
		this.recordReturn(this.taskContext,
				this.taskContext.doFlow(SEQUENTIAL_FLOW_INDEX, null),
				ignoreFlowFuture);
		this.recordReturn(this.taskContext,
				this.taskContext.doFlow(PARALLEL_FLOW_INDEX, new Integer(1)),
				ignoreFlowFuture);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(
				ASYNCHRONOUS_FLOW_INDEX, PARAMETER_VALUE), flowFuture);
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
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
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
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		TaskType<ClassWork, ?, ?> taskType = workType.getTaskTypes()[2];
		Task<ClassWork, ?, ?> task = taskType.getTaskFactory().createTask(work);
		assertEquals("Incorrect task", "taskStaticMethod",
				taskType.getTaskName());

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

		/**
		 * Annotated method to NOT be a {@link Task}.
		 */
		@NonTaskMethod
		public void nonTaskMethod() {
		}

		/**
		 * Annotated static method to NOT be a {@link Task}.
		 */
		@NonTaskMethod
		public static void nonStaticTaskMethod() {
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

	/**
	 * Ensure able to inherit by method name for the {@link ClassWorkSource}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testWorkInheritance() throws Exception {

		// Invalid test if not inheriting
		assertTrue("Invalid test if not extending",
				(new ChildClass()) instanceof ParentClass);

		// Create the work type builder
		WorkTypeBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(ChildClass.class));

		// task
		TaskTypeBuilder taskMethod = work.addTaskType("task",
				new ClassTaskFactory(null, false, null), null, null);
		taskMethod.setReturnType(Integer.class);
		taskMethod.addObject(Integer.class).setLabel(Integer.class.getName());

		// Validate the work type
		WorkLoaderUtil.validateWorkType(work, ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				ChildClass.class.getName());
	}

	/**
	 * Parent class.
	 */
	public static class ParentClass {

		public String task(String parameter) {
			return parameter;
		}
	}

	/**
	 * Child class.
	 */
	public static class ChildClass extends ParentClass {

		// Overrides by method name
		public Integer task(Integer parameter) {
			return parameter;
		}

		// Non task method so not included in method name inheritance
		@NonTaskMethod
		public Character task(Character parameter) {
			return parameter;
		}
	}

	/**
	 * Ensure issue if class specifies the method twice by same name.
	 */
	public void testDuplicateMethodName() throws Exception {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.addIssue(
				LocationType.SECTION,
				null,
				AssetType.WORK,
				null,
				"Failed to source WorkType definition from WorkSource "
						+ ClassWorkSource.class.getName(),
				new IllegalStateException(
						"Two methods by the same name 'task' in class "
								+ GrandChildClass.class.getName()
								+ ".  Either rename one of the methods or annotate one with @NonTaskMethod"));
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {

				// Match initial parameters
				for (int i = 0; i < 5; i++) {
					Object e = expected[i];
					Object a = actual[i];
					if ((e == null) && (a == null)) {
						continue; // match on null
					} else if ((e != null) && (e.equals(actual[i]))) {
						continue; // match not null
					} else {
						return false; // not match
					}
				}

				// Match exception
				IllegalStateException eEx = (IllegalStateException) expected[5];
				IllegalStateException aEx = (IllegalStateException) actual[5];
				if (!eEx.getMessage().equals(aEx.getMessage())) {
					return false; // not match
				}

				// As here, matches
				return true;
			}
		});

		// Validate the work type
		this.replayMockObjects();
		WorkLoaderUtil.loadWorkType(ClassWorkSource.class, compiler,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				GrandChildClass.class.getName());
		this.verifyMockObjects();
	}

	/**
	 * Grand child class with duplicate method names.
	 */
	public static class GrandChildClass extends ChildClass {

		public Integer task(Integer parameter) {
			return parameter;
		}

		public Double task(Double parameter) {
			return parameter;
		}
	}

}