/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.plugin.managedfunction.clazz;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
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
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.FlowSuccessful;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.NonFunctionMethod;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.plugin.clazz.method.MethodFunctionFactory;
import net.officefloor.plugin.section.clazz.MethodAnnotation;
import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.test.OfficeFloorExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the {@link ClassManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(OfficeFloorExtension.class)
public class ClassManagedFunctionSourceTest {

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("rawtypes")
	private final ManagedFunctionContext functionContext = this.mocks.createMock(ManagedFunctionContext.class);

	@BeforeEach
	protected void setUp() throws Exception {
		MockClass.reset(this.functionContext);
	}

	/**
	 * Ensures specification correct.
	 */
	@Test
	public void specification() {
		ManagedFunctionLoaderUtil.validateSpecification(ClassManagedFunctionSource.class,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to provider {@link Qualifier} to dependency name.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void qualifiedDependency() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(String.class);
		objectOne.setTypeQualifier(MockQualification.class.getName());
		objectOne.setLabel(MockQualification.class.getName() + "-" + String.class.getName());
		objectOne.addAnnotation((MockQualification) MockQualifiedClass.class
				.getMethod("function", String.class, String.class).getParameterAnnotations()[0][0]);
		ManagedFunctionObjectTypeBuilder<?> objectTwo = function.addObject(String.class);
		objectTwo.setLabel(String.class.getName());

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockQualifiedClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		assertMethodAnnotation(MockQualifiedClass.class, "function", functionType);
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals(2, functionObjects.length, "Incorrect number of function objects");
		ManagedFunctionObjectType<?> functionObjectOne = functionObjects[0];
		assertEquals(MockQualification.class.getName() + "-" + String.class.getName(),
				functionObjectOne.getObjectName(), "Incorrect first object");
		assertMethodParameterAnnotation(MockQualifiedClass.class, "function", 0, functionObjectOne);
		assertObjectAnnotation(MockQualification.class, functionObjectOne);
		ManagedFunctionObjectType<?> functionObjectTwo = functionObjects[1];
		assertEquals(String.class.getName(), functionObjectTwo.getObjectName(), "Incorrect second object");
		assertMethodParameterAnnotation(MockQualifiedClass.class, "function", 1, functionObjectTwo);
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
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void multipleQualifiedDependency() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this.mocks);

		// Use compiler to record issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue(
				"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
						+ ClassManagedFunctionSource.class.getName(),
				new InvalidConfigurationError("Method function parameter 0 has more than one Qualifier"));

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function", null, null);
		function.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		function.addObject(String.class).setLabel(MockQualification.class.getName());

		// Test
		this.mocks.replayMockObjects();

		// Validate the namespace type
		FunctionNamespaceType type = ManagedFunctionLoaderUtil.loadManagedFunctionType(ClassManagedFunctionSource.class,
				compiler, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockMultipleQualifiedClass.class.getName());
		assertNull(type, "Should not load namespace with multiple qualifers");

		// Verify
		this.mocks.verifyMockObjects();
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
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void dynamicQualifiedDependency() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Obtain the method for function
		Method method = MockDynamicQualifiedClass.class.getMethod("function", String.class, String.class);

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(String.class);
		objectOne.setTypeQualifier("MOCK_ONE");
		objectOne.setLabel("MOCK_ONE-" + String.class.getName());
		objectOne.addAnnotation((MockDynamicQualification) method.getParameterAnnotations()[0][0]);
		ManagedFunctionObjectTypeBuilder<?> objectTwo = function.addObject(String.class);
		objectTwo.setTypeQualifier("MOCK_TWO");
		objectTwo.setLabel("MOCK_TWO-" + String.class.getName());
		objectTwo.addAnnotation((MockDynamicQualification) method.getParameterAnnotations()[1][0]);

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockDynamicQualifiedClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		assertMethodAnnotation(MockDynamicQualifiedClass.class, "function", functionType);
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals(2, functionObjects.length, "Incorrect number of function objects");
		ManagedFunctionObjectType<?> functionObjectOne = functionObjects[0];
		assertEquals( "MOCK_ONE-" + String.class.getName(), functionObjectOne.getObjectName(), "Incorrect first object");
		assertMethodParameterAnnotation(MockDynamicQualifiedClass.class, "function", 0, functionObjectOne);
		MockDynamicQualification objectOneAnnotation = assertObjectAnnotation(MockDynamicQualification.class, functionObjectOne);
		assertEquals("ONE", objectOneAnnotation.value(), "Incorrect function object one annotation");
		ManagedFunctionObjectType<?> functionObjectTwo = functionObjects[1];
		assertEquals("MOCK_TWO-" + String.class.getName(), functionObjectTwo.getObjectName(), "Incorrect second object");
		assertMethodParameterAnnotation(MockDynamicQualifiedClass.class, "function", 1, functionObjectTwo);
		MockDynamicQualification objectTwoAnnotation = assertObjectAnnotation(MockDynamicQualification.class, functionObjectTwo);
		assertEquals("TWO", objectTwoAnnotation.value(), "Incorrect function object two annotation");
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
	 * Ensure {@link Annotation} instances on the {@link Method} are included in
	 * {@link ManagedFunctionType}.
	 */
	@Test
	public void methodAnnotations() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		namespace.addManagedFunctionType("function", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null))
				.addAnnotation(MockFunctionAnnotation.class);

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockAnnotateMethodClass.class.getName());

		// Ensure appropriate annotation on method
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		assertMethodAnnotation(MockAnnotateMethodClass.class, "function", functionType);
		assertFunctionAnnotation(MockFunctionAnnotation.class, functionType);
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface MockFunctionAnnotation {
	}

	public static class MockAnnotateMethodClass {
		@MockFunctionAnnotation
		public void function() {
		}
	}

	/**
	 * Ensure {@link Annotation} instances on the parameter type are included in the
	 * object annotations.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parameterTypeAnnotations() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Function
		ManagedFunctionTypeBuilder function = namespace.addManagedFunctionType("function", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		ManagedFunctionObjectTypeBuilder<?> objectOne = function.addObject(MockParameter.class);
		objectOne.setTypeQualifier("MOCK_value");
		objectOne.setLabel("MOCK_value-" + MockParameter.class.getName());
		objectOne.addAnnotation(MockTypeAnnotation.class);
		objectOne.addAnnotation(MockDynamicQualification.class);

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockAnnotateParameterClass.class.getName());

		// Ensure appropriate objects for function
		ManagedFunctionType<?, ?> functionType = namespaceType.getManagedFunctionTypes()[0];
		assertMethodAnnotation(MockAnnotateParameterClass.class, "function", functionType);
		ManagedFunctionObjectType<?>[] functionObjects = functionType.getObjectTypes();
		assertEquals(1, functionObjects.length, "Incorrect number of function objects");
		ManagedFunctionObjectType<?> functionObject = functionObjects[0];
		assertObjectAnnotation(MockDynamicQualification.class, functionObject);
		assertObjectAnnotation(MockTypeAnnotation.class, functionObject);
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
	 * Ensure appropriate {@link Var} types.
	 */
	@Test
	public void variableTypes() throws Exception {
		this.doVariableTypesTest(MockVariables.class, "");
	}

	/**
	 * Ensure appropriate named/qualified {@link Var} types.
	 */
	@Test
	public void namedVariableTypes() throws Exception {
		MockDynamicQualification annotation = (MockDynamicQualification) MockNamedVariables.class
				.getMethod("val", String.class).getParameterAnnotations()[0][0];
		this.doVariableTypesTest(MockNamedVariables.class, "MOCK_NAME-", annotation);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doVariableTypesTest(Class clazz, String qualifier, Annotation... annotations) throws Exception {

		// Obtain the variable name
		String variableName = qualifier + String.class.getName();

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Load the functions (all depend on variable)
		Val valAnnotation = (Val) MockVariables.class.getMethod("val", String.class).getParameterAnnotations()[0][0];
		for (String methodName : new String[] { "var", "out", "in", "val" }) {
			ManagedFunctionTypeBuilder method = namespace
					.addManagedFunctionType(methodName, Indexed.class, Indexed.class)
					.setFunctionFactory(new MethodFunctionFactory(null, null, null));
			ManagedFunctionObjectTypeBuilder var = method.addObject(Var.class);
			var.setLabel(variableName + "-" + Var.class.getName());
			var.setTypeQualifier(variableName);
			for (Annotation annotation : annotations) {
				var.addAnnotation(annotation);
			}
			if ("val".equals(methodName)) {
				var.addAnnotation(valAnnotation);
			}
			var.addAnnotation(new VariableAnnotation(variableName, String.class.getName()));
		}

		// Validate the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace,
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());

		// Validate the annotations
		for (ManagedFunctionType<?, ?> function : namespaceType.getManagedFunctionTypes()) {
			ManagedFunctionObjectType<?> parameter = function.getObjectTypes()[0];
			boolean isVariableAnnotation = false;
			for (Object annotation : parameter.getAnnotations()) {
				if (annotation instanceof VariableAnnotation) {
					isVariableAnnotation = true;
					VariableAnnotation var = (VariableAnnotation) annotation;
					assertEquals(variableName, var.getVariableName(), "Incorrect name");
				}
			}
			assertTrue(isVariableAnnotation, "Should be variable annotation for " + function.getFunctionName());
		}
	}

	public static class MockVariables {
		public void var(Var<String> variable) {
			// Testing
		}

		public void out(Out<String> out) {
			// Testing
		}

		public void in(In<String> in) {
			// Testing
		}

		public void val(@Val String val) {
			// Testing
		}
	}

	public static class MockNamedVariables {
		public void var(@MockDynamicQualification("NAME") Var<String> variable) {
			// Testing
		}

		public void out(@MockDynamicQualification("NAME") Out<String> out) {
			// Testing
		}

		public void in(@MockDynamicQualification("NAME") In<String> in) {
			// Testing
		}

		public void val(@MockDynamicQualification("NAME") @Val String val) {
			// Testing
		}
	}

	/**
	 * Ensure able to load {@link FunctionNamespaceType} for the
	 * {@link ClassManagedFunctionSource}.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void functionNamespaceType() throws Exception {

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// functionInstanceMethod
		ManagedFunctionTypeBuilder instanceMethod = namespace
				.addManagedFunctionType("functionInstanceMethod", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		instanceMethod.setReturnType(String.class);
		instanceMethod.addObject(String.class).setLabel(String.class.getName());
		ManagedFunctionFlowTypeBuilder<?> asynchronous = instanceMethod.addFlow();
		asynchronous.setLabel("asynchronous");
		asynchronous.setArgumentType(String.class);
		ManagedFunctionFlowTypeBuilder<?> parallel = instanceMethod.addFlow();
		parallel.setLabel("parallel");
		parallel.setArgumentType(Integer.class);
		instanceMethod.addFlow().setLabel("sequential");
		instanceMethod.addFlow().setLabel("successfulFlow");
		instanceMethod.addEscalation(IOException.class);

		// functionFailMethod
		ManagedFunctionTypeBuilder failMethod = namespace
				.addManagedFunctionType("functionFailMethod", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		failMethod.addEscalation(SQLException.class);

		// functionStaticMethod
		ManagedFunctionTypeBuilder staticMethod = namespace
				.addManagedFunctionType("functionStaticMethod", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		staticMethod.setReturnType(Object.class);

		// managedFunctionContext
		ManagedFunctionTypeBuilder managedFunctionContext = namespace
				.addManagedFunctionType("managedFunctionContext", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		managedFunctionContext.setReturnType(ManagedFunctionContext.class);

		// asynchronousFlow
		ManagedFunctionTypeBuilder asynchronousFlow = namespace
				.addManagedFunctionType("asynchronousFlow", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		asynchronousFlow.setReturnType(AsynchronousFlow.class);

		// asynchronousFlows
		ManagedFunctionTypeBuilder asynchronousFlows = namespace
				.addManagedFunctionType("asynchronousFlows", Indexed.class, Indexed.class)
				.setFunctionFactory(new MethodFunctionFactory(null, null, null));
		asynchronousFlows.setReturnType(AsynchronousFlow[].class);

		// Validate the namespace type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, ClassManagedFunctionSource.class,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure can invoke the instance {@link Method} with Java compiled
	 * {@link FlowInterface} implementations.
	 */
	@Test
	public void invokeInstanceMethodWithCompiling() throws Throwable {
		SourceContext sourceContext = OfficeFloorCompiler.newOfficeFloorCompiler(this.getClass().getClassLoader())
				.createRootSourceContext();
		assertNotNull(OfficeFloorJavaCompiler.newInstance(sourceContext), "Ensure Java compiler available");
		this.doInvokeInstanceMethodTest();
	}

	/**
	 * Ensure able to fall back to {@link Proxy} implementation if Java compiler not
	 * available.
	 */
	@Test
	public void invokeInstanceMethodWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInvokeInstanceMethodTest());
	}

	/**
	 * Ensure can invoke the the instance {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public void doInvokeInstanceMethodTest() throws Throwable {

		final String PARAMETER_VALUE = "PARAMETER";
		final String RETURN_VALUE = "INSTANCE RETURN VALUE";

		// Index order of flows due to sorting by method name
		final int SUCCESSFUL_FLOW_INDEX = 3;
		final int SEQUENTIAL_FLOW_INDEX = 2;
		final int PARALLEL_FLOW_INDEX = 1;
		final int ASYNCHRONOUS_FLOW_INDEX = 0;

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("functionInstanceMethod");

		// Record invoking method
		MockClass.expectedParameter = PARAMETER_VALUE;
		MockClass.returnValue = RETURN_VALUE;
		this.mocks.recordReturn(this.functionContext, this.functionContext.getObject(0), PARAMETER_VALUE);
		this.functionContext.doFlow(SEQUENTIAL_FLOW_INDEX, null, null);
		this.functionContext.doFlow(PARALLEL_FLOW_INDEX, Integer.valueOf(1), null);
		this.functionContext.doFlow(ASYNCHRONOUS_FLOW_INDEX, PARAMETER_VALUE, null);
		this.functionContext.doFlow(SUCCESSFUL_FLOW_INDEX, null, null);
		this.functionContext.setNextFunctionArgument(RETURN_VALUE);

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring the correct return value
		function.execute(this.functionContext);

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can throw an {@link Exception} from the {@link Method}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void throwException() throws Throwable {

		final SQLException exception = new SQLException("Method failure");

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("functionFailMethod");

		// Record invoking method
		MockClass.sqlException = exception;

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring it throws exception
		try {
			function.execute(this.functionContext);
			fail("Should not return succesfully");
		} catch (SQLException ex) {
			assertEquals(exception, ex, "Incorrect failure");
		}

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can invoke static {@link Method}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void staticMethod() throws Throwable {

		final String RETURN_VALUE = "STATIC RETURN VALUE";

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("functionStaticMethod");

		// Record invoking method
		MockClass.returnValue = RETURN_VALUE;
		this.functionContext.setNextFunctionArgument(RETURN_VALUE);

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring the correct return value
		function.execute(this.functionContext);

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to access {@link ManagedFunctionContext}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void managedFunctionContext() throws Throwable {

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("managedFunctionContext");

		// Ensure correct next argument
		this.functionContext.setNextFunctionArgument(this.functionContext);

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring the correct return value
		function.execute(this.functionContext);

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to access {@link AsynchronousFlow}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void asynchronousFlow() throws Throwable {

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("asynchronousFlow");

		// Record obtain asynchronous flow
		AsynchronousFlow flow = this.mocks.createMock(AsynchronousFlow.class);
		this.mocks.recordReturn(this.functionContext, this.functionContext.createAsynchronousFlow(), flow);
		this.functionContext.setNextFunctionArgument(flow);

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring the correct return value
		function.execute(this.functionContext);

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure creates new {@link AsynchronousFlow} for each parameter.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void multipleAsynchronousFlows() throws Throwable {

		// Create the function
		ManagedFunction<?, ?> function = createMockClassManagedFunction("asynchronousFlows");

		// Record obtain asynchronous flow
		AsynchronousFlow flowOne = this.mocks.createMock(AsynchronousFlow.class);
		AsynchronousFlow flowTwo = this.mocks.createMock(AsynchronousFlow.class);
		this.mocks.recordReturn(this.functionContext, this.functionContext.createAsynchronousFlow(), flowOne);
		this.mocks.recordReturn(this.functionContext, this.functionContext.createAsynchronousFlow(), flowTwo);
		this.functionContext.setNextFunctionArgument(this.mocks.paramType(AsynchronousFlow[].class));
		this.mocks.recordVoid(this.functionContext, (arguments) -> {
			AsynchronousFlow[] actualFlows = (AsynchronousFlow[]) arguments[0];
			return (flowOne == actualFlows[0]) && (flowTwo == actualFlows[1]);
		});

		// Replay the mock objects
		this.mocks.replayMockObjects();

		// Invoke the function ensuring the correct return value
		function.execute(this.functionContext);

		// Verify mock objects
		this.mocks.verifyMockObjects();
	}

	/**
	 * Creates the {@link ManagedFunction} from the {@link MockClass}.
	 * 
	 * @param methodName Name of {@link Method} on {@link MockClass}.
	 * @return {@link ManagedFunction} for the {@link Method}.
	 */
	private static ManagedFunction<?, ?> createMockClassManagedFunction(String methodName) throws Throwable {

		// Create the function
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				ClassManagedFunctionSource.class, ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				MockClass.class.getName());
		for (ManagedFunctionType<?, ?> functionType : namespaceType.getManagedFunctionTypes()) {
			if (functionType.getFunctionName().equals(methodName)) {
				return functionType.getManagedFunctionFactory().createManagedFunction();
			}
		}

		// As here, method not exist (invalid test)
		fail("INVALID TEST: method " + methodName + " not on " + MockClass.class.getName());
		return null;
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
		 * @param expectedContext Expected {@link ManagedFunctionContext}.
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
			assertEquals(expectedParameter, parameter, "Incorrect parameter");
			assertNotNull(flows, "Must have flows");
			assertEquals(expectedContext, context, "Incorrect function context");

			// Invoke the flows
			flows.sequential();
			flows.parallel(Integer.valueOf(1));
			flows.asynchronous(parameter, null);
			flows.successfulFlow(null);

			// Return the value
			return returnValue;
		}

		/**
		 * {@link ManagedFunction} anotherMethod.
		 */
		public void functionFailMethod() throws SQLException {
			throw sqlException;
			// Testing
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
			// Testing
		}

		/**
		 * Annotated static method to NOT be a {@link ManagedFunction}.
		 */
		@NonFunctionMethod
		public static void nonStaticFunctionMethod() {
			// Testing
		}

		/**
		 * Ensure inject {@link ManagedFunctionContext}.
		 */
		public ManagedFunctionContext<?, ?> managedFunctionContext(ManagedFunctionContext<?, ?> context) {
			return context;
		}

		/**
		 * Ensure inject {@link AsynchronousFlow}.
		 */
		public AsynchronousFlow asynchronousFlow(AsynchronousFlow flow) {
			return flow;
		}

		/**
		 * Ensure inject multiple {@link AsynchronousFlow} instances.
		 */
		public AsynchronousFlow[] asynchronousFlows(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
			return new AsynchronousFlow[] { flowOne, flowTwo };
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
		 * @param parameter Parameter.
		 */
		void parallel(Integer parameter);

		/**
		 * Asynchronous invocation.
		 * 
		 * @param parameter Parameter.
		 * @param callback  {@link FlowCallback}.
		 */
		void asynchronous(String parameter, FlowCallback callback);

		/**
		 * Handles only successful {@link Flow}.
		 * 
		 * @param successful {@link FlowSuccessful}.
		 */
		void successfulFlow(FlowSuccessful successful);
	}

	/**
	 * Ensure able to inherit by method name for the
	 * {@link ClassManagedFunctionSource}.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void functionNamespaceInheritance() throws Exception {

		// Invalid test if not inheriting
		assertTrue((new ChildClass()) instanceof ParentClass, "Invalid test if not extending");

		// Create the namespace type builder
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// function
		ManagedFunctionTypeBuilder functionMethod = namespace.addManagedFunctionType("function", Indexed.class,
				Indexed.class);
		functionMethod.setFunctionFactory(new MethodFunctionFactory(null, null, null));
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
	@Test
	public void duplicateMethodName() throws Exception {

		final MockCompilerIssues issues = new MockCompilerIssues(this.mocks);

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
		this.mocks.replayMockObjects();
		ManagedFunctionLoaderUtil.loadManagedFunctionType(ClassManagedFunctionSource.class, compiler,
				ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, GrandChildClass.class.getName());
		this.mocks.verifyMockObjects();
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

	/**
	 * Obtains the {@link Method}.
	 */
	private static Method obtainMethod(Class<?> declaringClass, String methodName) {
		Method method = null;
		for (Method check : declaringClass.getDeclaredMethods()) {
			if (check.getName().equals(methodName)) {
				method = check;
			}
		}
		assertNotNull(method, "Can not obtain method " + declaringClass.getName() + "#" + methodName);
		return method;
	}

	/**
	 * Asserts an annotation on the {@link ManagedFunctionType}.
	 */
	private static <A> A assertFunctionAnnotation(Class<A> annotationType, ManagedFunctionType<?, ?> functionType) {
		A annotation = functionType.getAnnotation(annotationType);
		assertNotNull(annotation, "No " + annotationType.getName() + " annotation");
		assertTrue(annotationType.isInstance(annotation), "Incorrect annotation type");
		return annotation;
	}

	/**
	 * Asserts {@link MethodAnnotation}.
	 */
	private static void assertMethodAnnotation(Class<?> declaringClass, String methodName, ManagedFunctionType<?, ?> functionType) {

		// Obtain the method
		Method method = obtainMethod(declaringClass, methodName);

		// Ensure annotation
		MethodAnnotation annotation = assertFunctionAnnotation(MethodAnnotation.class, functionType);
		assertEquals(method, annotation.getMethod(), "Incorrect method");
	}

	/**
	 * Asserts annotations on the {@link ManagedFunctionObjectType}.
	 */
	private static <A> A assertObjectAnnotation(Class<A> annotationType, ManagedFunctionObjectType<?> objectType) {
		A annotation = objectType.getAnnotation(annotationType);
		assertNotNull(annotation, "No " + annotationType.getName() + " annotation");
		assertTrue(annotationType.isInstance(annotation), "Incorrect annotation type");
		return annotation;
	}

	private static void assertMethodParameterAnnotation(Class<?> declaringClass, String methodName, int parameterIndex, ManagedFunctionObjectType<?> objectType) {

		// Obtain the method
		Method method = obtainMethod(declaringClass, methodName);

		// Ensure annotation
		MethodParameterAnnotation annotation = assertObjectAnnotation(MethodParameterAnnotation.class, objectType);
		assertEquals(method, annotation.getMethod(), "Incorrect method");
		assertEquals(parameterIndex, annotation.getParameterIndex(), "Incorrect parameter index");
	}

}
