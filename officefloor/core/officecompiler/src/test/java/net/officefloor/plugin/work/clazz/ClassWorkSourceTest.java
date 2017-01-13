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
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.function.FlowFuture;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link ClassWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final ManagedFunctionContext taskContext = this.createMock(ManagedFunctionContext.class);

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
		FunctionNamespaceBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(
						MockQualifiedClass.class));

		// Task
		ManagedFunctionTypeBuilder task = work.addManagedFunctionType("task", new ClassTaskFactory(
				null, false, null), null, null);
		ManagedFunctionObjectTypeBuilder<?> objectOne = task.addObject(String.class);
		objectOne.setTypeQualifier(MockQualification.class.getName());
		objectOne.setLabel(MockQualification.class.getName() + "-"
				+ String.class.getName());
		ManagedFunctionObjectTypeBuilder<?> objectTwo = task.addObject(String.class);
		objectTwo.setLabel(String.class.getName());

		// Validate the work type
		FunctionNamespaceType<?> workType = WorkLoaderUtil.validateWorkType(work,
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockQualifiedClass.class.getName());

		// Ensure appropriate objects for task
		ManagedFunctionType<?, ?, ?> taskType = workType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] taskObjects = taskType.getObjectTypes();
		assertEquals("Incorrect number of task objects", 2, taskObjects.length);
		assertEquals(
				"Incorrect first object",
				MockQualification.class.getName() + "-"
						+ String.class.getName(),
				taskObjects[0].getObjectName());
		assertEquals("Incorrect second object", String.class.getName(),
				taskObjects[1].getObjectName());
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

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue(
				"Failed to source WorkType definition from WorkSource "
						+ ClassWorkSource.class.getName(),
				new IllegalArgumentException(
						"Method task parameter 0 has more than one Qualifier"));

		// Create the work type builder
		FunctionNamespaceBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(
						MockMultipleQualifiedClass.class));

		// task
		ManagedFunctionTypeBuilder task = work.addManagedFunctionType("task", new ClassTaskFactory(
				null, false, null), null, null);
		task.addObject(String.class)
				.setLabel(MockQualification.class.getName());

		// Test
		this.replayMockObjects();

		// Validate the work type
		FunctionNamespaceType<?> type = WorkLoaderUtil.loadWorkType(ClassWorkSource.class,
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
	 * Ensure able to load {@link FunctionNamespaceType} for the {@link ClassWorkSource}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testWorkType() throws Exception {

		// Create the work type builder
		FunctionNamespaceBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(MockClass.class));

		// taskInstanceMethod
		ManagedFunctionTypeBuilder instanceMethod = work.addManagedFunctionType("taskInstanceMethod",
				new ClassTaskFactory(null, false, null), null, null);
		instanceMethod.setReturnType(String.class);
		instanceMethod.addObject(String.class).setLabel(String.class.getName());
		ManagedFunctionFlowTypeBuilder<?> asynchronous = instanceMethod.addFlow();
		asynchronous.setLabel("asynchronous");
		asynchronous.setArgumentType(String.class);
		ManagedFunctionFlowTypeBuilder<?> parallel = instanceMethod.addFlow();
		parallel.setLabel("parallel");
		parallel.setArgumentType(Integer.class);
		instanceMethod.addFlow().setLabel("sequential");
		instanceMethod.addEscalation(IOException.class);

		// taskFailMethod
		ManagedFunctionTypeBuilder failMethod = work.addManagedFunctionType("taskFailMethod",
				new ClassTaskFactory(null, false, null), null, null);
		failMethod.addEscalation(SQLException.class);

		// taskStaticMethod
		ManagedFunctionTypeBuilder staticMethod = work.addManagedFunctionType("taskStaticMethod",
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
		FunctionNamespaceType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		ManagedFunctionType<ClassWork, ?, ?> taskType = workType.getManagedFunctionTypes()[1];
		ManagedFunction<ClassWork, ?, ?> task = taskType.getManagedFunctionFactory().createManagedFunction(work);
		assertEquals("Incorrect task", "taskInstanceMethod",
				taskType.getFunctionName());

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
		Object returnValue = task.execute(this.taskContext);
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
		FunctionNamespaceType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		ManagedFunctionType<ClassWork, ?, ?> taskType = workType.getManagedFunctionTypes()[0];
		ManagedFunction<ClassWork, ?, ?> task = taskType.getManagedFunctionFactory().createManagedFunction(work);
		assertEquals("Incorrect task", "taskFailMethod", taskType.getFunctionName());

		// Record invoking method
		MockClass.sqlException = exception;
		this.recordReturn(this.taskContext, this.taskContext.getWork(), work);

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the task ensuring it throws exception
		try {
			task.execute(this.taskContext);
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
		FunctionNamespaceType<ClassWork> workType = WorkLoaderUtil.loadWorkType(
				ClassWorkSource.class,
				ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ClassWork work = workType.getWorkFactory().createWork();
		ManagedFunctionType<ClassWork, ?, ?> taskType = workType.getManagedFunctionTypes()[2];
		ManagedFunction<ClassWork, ?, ?> task = taskType.getManagedFunctionFactory().createManagedFunction(work);
		assertEquals("Incorrect task", "taskStaticMethod",
				taskType.getFunctionName());

		// Record invoking method
		MockClass.returnValue = RETURN_VALUE;

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the task ensuring the correct return value
		Object returnValue = task.execute(this.taskContext);
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
		 * Expected {@link ManagedFunctionContext}.
		 */
		public static ManagedFunctionContext<?, ?, ?> expectedContext;

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
		 *            Expected {@link ManagedFunctionContext}.
		 */
		static void reset(ManagedFunctionContext<?, ?, ?> expectedContext) {
			expectedParameter = null;
			MockClass.expectedContext = expectedContext;
			returnValue = null;
			sqlException = null;
		}

		/**
		 * {@link ManagedFunction} taskMethod.
		 */
		public String taskInstanceMethod(String parameter, MockFlows flows,
				ManagedFunctionContext<?, ?, ?> context) throws IOException {

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
		 * {@link ManagedFunction} anotherMethod.
		 */
		public void taskFailMethod() throws SQLException {
			throw sqlException;
		}

		/**
		 * {@link ManagedFunction} staticMethod.
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
		 * Annotated method to NOT be a {@link ManagedFunction}.
		 */
		@NonTaskMethod
		public void nonTaskMethod() {
		}

		/**
		 * Annotated static method to NOT be a {@link ManagedFunction}.
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
		FunctionNamespaceBuilder<ClassWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new ClassWorkFactory(ChildClass.class));

		// task
		ManagedFunctionTypeBuilder taskMethod = work.addManagedFunctionType("task",
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

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue(
				"Failed to source WorkType definition from WorkSource "
						+ ClassWorkSource.class.getName(),
				new IllegalStateException(
						"Two methods by the same name 'task' in class "
								+ GrandChildClass.class.getName()
								+ ".  Either rename one of the methods or annotate one with @NonTaskMethod"));

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