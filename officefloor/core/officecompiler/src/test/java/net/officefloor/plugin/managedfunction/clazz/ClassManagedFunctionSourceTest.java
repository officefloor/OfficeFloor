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
package net.officefloor.plugin.managedfunction.clazz;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link ClassManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final ManagedFunctionContext functionContext = this.createMock(ManagedFunctionContext.class);

	@Override
	protected void setUp() throws Exception {
		MockClass.reset(this.functionContext);
	}

	/**
	 * Ensures specification correct.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(ClassManagedFunctionSource.class,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to provider {@link Qualifier} to dependency name.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testQualifiedDependency() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(String.class);
		objectOne.setTypeQualifier(MockQualification.class.getName());
		objectOne.setLabel(MockQualification.class.getName() + "-" + String.class.getName());
		ManagedFunctionObjectTypeBuilder<?> objectTwo = function.addObject(String.class);
		objectTwo.setLabel(String.class.getName());

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockQualifiedClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals("Incorrect number of function objects", 2, functionObjects.length);
		ManagedFunctionObjectType<?> functionObjectOne = functionObjects[0];
		assertEquals("Incorrect first object", MockQualification.class.getName() + "-" + String.class.getName(),
				functionObjectOne.getObjectName());
		Object[] functionObjectOneAnnotations = functionObjectOne.getAnnotations();
		assertEquals("Incorrect number of annotations", 1, functionObjectOneAnnotations.length);
		assertTrue("Incorrect annotation", functionObjectOneAnnotations[0] instanceof MockQualification);
		assertEquals("Incorrect second object", String.class.getName(), functionObjects[1].getObjectName());
	}

	/**
	 * Mock {@link Qualifier}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public static @interface MockQualification {
	}

	/**
	 * Mock qualified {@link Class}.
	 */
	public static class MockQualifiedClass {
		public void function(@MockQualification String objectOne, String objectTwo) {
		}
	}

	/**
	 * Ensure issue if provide multiple {@link Qualifier} to dependency name.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testMultipleQualifiedDependency() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue(
				"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
						+ ClassManagedFunctionSource.class.getName(),
				new IllegalArgumentException("Method function parameter 0 has more than one Qualifier"));

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function",
				new ClassFunctionFactory(null, null, null), null, null);
		function.addObject(String.class).setLabel(MockQualification.class.getName());

		// Test
		this.replayMockObjects();

		// Validate the namespace type
		FunctionNamespaceType type = ManagedFunctionLoaderUtil.loadManagedFunctionType(ClassManagedFunctionSource.class,
				compiler, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockMultipleQualifiedClass.class.getName());
		assertNull("Should not load namespace with multiple qualifers", type);

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
	 * Mock multiple qualified {@link Class}.
	 */
	public static class MockMultipleQualifiedClass {
		public void function(@MockQualification @MockAnotherQualification String dependency) {
		}
	}

	/**
	 * Ensure able to dynamically determine qualifier.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testDynamicQualifiedDependency() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(String.class);
		objectOne.setTypeQualifier("MOCK_ONE");
		objectOne.setLabel("MOCK_ONE-" + String.class.getName());
		ManagedFunctionObjectTypeBuilder<?> objectTwo = function.addObject(String.class);
		objectTwo.setTypeQualifier("MOCK_TWO");
		objectTwo.setLabel("MOCK_TWO-" + String.class.getName());

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockDynamicQualifiedClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals("Incorrect number of function objects", 2, functionObjects.length);
		ManagedFunctionObjectType<?> functionObjectOne = functionObjects[0];
		assertEquals("Incorrect first object", "MOCK_ONE-" + String.class.getName(), functionObjectOne.getObjectName());
		MockDynamicQualification objectOneAnnotation = (MockDynamicQualification) functionObjectOne.getAnnotations()[0];
		assertEquals("Incorrect function object one annotation", "ONE", objectOneAnnotation.value());
		ManagedFunctionObjectType<?> functionObjectTwo = functionObjects[1];
		assertEquals("Incorrect second object", "MOCK_TWO-" + String.class.getName(),
				functionObjectTwo.getObjectName());
		MockDynamicQualification objectTwoAnnotation = (MockDynamicQualification) functionObjectTwo.getAnnotations()[0];
		assertEquals("Incorrect function object two annotation", "TWO", objectTwoAnnotation.value());
	}

	/**
	 * Dynamic mock {@link Qualifier}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier(nameFactory = MockQualifierNameFactory.class)
	public @interface MockDynamicQualification {
		String value();
	}

	/**
	 * Mock {@link QualifierNameFactory}.
	 */
	public static class MockQualifierNameFactory implements QualifierNameFactory<MockDynamicQualification> {
		@Override
		public String getQualifierName(MockDynamicQualification annotation) {
			return "MOCK_" + annotation.value();
		}
	}

	/**
	 * Mock dynamic qualified {@link Class}.
	 */
	public static class MockDynamicQualifiedClass {
		public void function(@MockDynamicQualification("ONE") String one, @MockDynamicQualification("TWO") String two) {
		}
	}

	/**
	 * Ensure {@link Annotation} instances on the parameter type are included in
	 * the object annotations.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testParameterTypeAnnotations() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(MockParameter.class);
		objectOne.setTypeQualifier("MOCK_value");
		objectOne.setLabel("MOCK_value-" + MockParameter.class.getName());

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockAnnotateParameterClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals("Incorrect number of function objects", 1, functionObjects.length);
		ManagedFunctionObjectType<?> functionObject = functionObjects[0];
		Object[] annotations = functionObject.getAnnotations();
		assertEquals("Incorrect number of annotations", 2, annotations.length);
		assertTrue("Incorrect first annotation", annotations[0] instanceof MockTypeAnnotation);
		assertTrue("Incorrect second annotation", annotations[1] instanceof MockDynamicQualification);
	}

	/**
	 * Type mock {@link Annotation}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	private @interface MockTypeAnnotation {
	}

	/**
	 * Mock parameter.
	 */
	@MockTypeAnnotation
	private static class MockParameter {
	}

	/**
	 * Mock type qualified parameter.
	 */
	public static class MockAnnotateParameterClass {
		public void function(@MockDynamicQualification("value") MockParameter parameter) {
		}
	}

	/**
	 * Ensure able to load {@link FunctionNamespaceType} for the
	 * {@link ClassManagedFunctionSource}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testFunctionNamespaceType() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// functionInstanceMethod
		ManagedFunctionTypeBuilder instanceMethod = namespace.addManagedFunctionType("functionInstanceMethod",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
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

		// functionFailMethod
		ManagedFunctionTypeBuilder failMethod = namespace.addManagedFunctionType("functionFailMethod",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		failMethod.addEscalation(SQLException.class);

		// functionStaticMethod
		ManagedFunctionTypeBuilder staticMethod = namespace.addManagedFunctionType("functionStaticMethod",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		staticMethod.setReturnType(Object.class);

		// Validate the namespace type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, ClassManagedFunctionSource.class,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure can invoke the the instance {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void testInvokeInstanceMethod() throws Throwable {

		final String PARAMETER_VALUE = "PARAMETER";
		final String RETURN_VALUE = "INSTANCE RETURN VALUE";

		// Index order of flows due to sorting by method name
		final int SEQUENTIAL_FLOW_INDEX = 2;
		final int PARALLEL_FLOW_INDEX = 1;
		final int ASYNCHRONOUS_FLOW_INDEX = 0;

		// Create the function
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[1];
		ManagedFunction<?, ?> function = functionType.getManagedFunctionFactory().createManagedFunction();
		assertEquals("Incorrect function", "functionInstanceMethod", functionType.getFunctionName());

		// Record invoking method
		MockClass.expectedParameter = PARAMETER_VALUE;
		MockClass.returnValue = RETURN_VALUE;
		this.recordReturn(this.functionContext, this.functionContext.getObject(0), PARAMETER_VALUE);
		this.functionContext.doFlow(SEQUENTIAL_FLOW_INDEX, null, null);
		this.functionContext.doFlow(PARALLEL_FLOW_INDEX, new Integer(1), null);
		this.functionContext.doFlow(ASYNCHRONOUS_FLOW_INDEX, PARAMETER_VALUE, null);

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the function ensuring the correct return value
		Object returnValue = function.execute(this.functionContext);
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

		// Create the function
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		ManagedFunction<?, ?> function = functionType.getManagedFunctionFactory().createManagedFunction();
		assertEquals("Incorrect function", "functionFailMethod", functionType.getFunctionName());

		// Record invoking method
		MockClass.sqlException = exception;

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the function ensuring it throws exception
		try {
			function.execute(this.functionContext);
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

		// Create the function
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[2];
		ManagedFunction<?, ?> function = functionType.getManagedFunctionFactory().createManagedFunction();
		assertEquals("Incorrect function", "functionStaticMethod", functionType.getFunctionName());

		// Record invoking method
		MockClass.returnValue = RETURN_VALUE;

		// Replay the mock objects
		this.replayMockObjects();

		// Invoke the function ensuring the correct return value
		Object returnValue = function.execute(this.functionContext);
		assertEquals("Incorrect return value", RETURN_VALUE, returnValue);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link Class}.
	 */
	public static class MockClass {

		/**
		 * Expected parameter.
		 */
		public static String expectedParameter;

		/**
		 * Expected {@link ManagedFunctionContext}.
		 */
		public static ManagedFunctionContext<?, ?> expectedContext;

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
		static void reset(ManagedFunctionContext<?, ?> expectedContext) {
			expectedParameter = null;
			MockClass.expectedContext = expectedContext;
			returnValue = null;
			sqlException = null;
		}

		/**
		 * {@link ManagedFunction} functionMethod.
		 */
		public String functionInstanceMethod(String parameter, MockFlows flows, ManagedFunctionContext<?, ?> context)
				throws IOException {

			// Ensure correct inputs
			assertEquals("Incorrect parameter", expectedParameter, parameter);
			assertNotNull("Must have flows", flows);
			assertEquals("Incorrect function context", expectedContext, context);

			// Invoke the flows
			flows.sequential();
			flows.parallel(new Integer(1));
			flows.asynchronous(parameter, null);

			// Return the value
			return returnValue;
		}

		/**
		 * {@link ManagedFunction} anotherMethod.
		 */
		public void functionFailMethod() throws SQLException {
			throw sqlException;
		}

		/**
		 * {@link ManagedFunction} staticMethod.
		 */
		public static Object functionStaticMethod() {
			return returnValue;
		}

		/**
		 * Private method to the class.
		 */
		Object nonFunctionMethod(Object parameter) {
			return null;
		}

		/**
		 * Annotated method to NOT be a {@link ManagedFunction}.
		 */
		@NonFunctionMethod
		public void nonFunctionMethod() {
		}

		/**
		 * Annotated static method to NOT be a {@link ManagedFunction}.
		 */
		@NonFunctionMethod
		public static void nonStaticFunctionMethod() {
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
		 * @param callback
		 *            {@link FlowCallback}.
		 */
		void asynchronous(String parameter, FlowCallback callback);
	}

	/**
	 * Ensure able to inherit by method name for the
	 * {@link ClassManagedFunctionSource}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testFunctionNamespaceInheritance() throws Exception {

		// Invalid test if not inheriting
		assertTrue("Invalid test if not extending", (new ChildClass()) instanceof ParentClass);

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// function
		ManagedFunctionTypeBuilder functionMethod = namespace.addManagedFunctionType("function",
				new ClassFunctionFactory(null, null, null), Indexed.class, Indexed.class);
		functionMethod.setReturnType(Integer.class);
		functionMethod.addObject(Integer.class).setLabel(Integer.class.getName());

		// Validate the namespace type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, ClassManagedFunctionSource.class,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, ChildClass.class.getName());
	}

	/**
	 * Parent class.
	 */
	public static class ParentClass {

		public String function(String parameter) {
			return parameter;
		}
	}

	/**
	 * Child class.
	 */
	public static class ChildClass extends ParentClass {

		// Overrides by method name
		public Integer function(Integer parameter) {
			return parameter;
		}

		// Non function method so not included in method name inheritance
		@NonFunctionMethod
		public Character function(Character parameter) {
			return parameter;
		}
	}

	/**
	 * Ensure issue if class specifies the method twice by same name.
	 */
	public void testDuplicateMethodName() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue(
				"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
						+ ClassManagedFunctionSource.class.getName(),
				new IllegalStateException(
						"Two methods by the same name 'function' in class " + GrandChildClass.class.getName()
								+ ".  Either rename one of the methods or annotate one with @NonFunctionMethod"));

		// Validate the namespace type
		this.replayMockObjects();
		ManagedFunctionLoaderUtil.loadManagedFunctionType(ClassManagedFunctionSource.class, compiler,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, GrandChildClass.class.getName());
		this.verifyMockObjects();
	}

	/**
	 * Grand child class with duplicate method names.
	 */
	public static class GrandChildClass extends ChildClass {

		public Integer function(Integer parameter) {
			return parameter;
		}

		public Double function(Double parameter) {
			return parameter;
		}
	}

}