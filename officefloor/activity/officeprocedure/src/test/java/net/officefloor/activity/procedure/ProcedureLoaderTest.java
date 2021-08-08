/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSpecification;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;

/**
 * Tests the {@link ProcedureLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderTest extends OfficeFrameTestCase {

	/**
	 * Validate {@link Procedure}.
	 */
	public void testDefaultIsProcedure() {
		final String PROCEDURE_NAME = "procedure";
		ProcedureSource mockSource = ProcedureLoaderUtil.loadProcedureSource(MockProcedureSource.class);

		// Matches
		assertTrue("Class match", ProcedureLoaderUtil.procedure(PROCEDURE_NAME)
				.isProcedure(ClassProcedureSource.SOURCE_NAME, PROCEDURE_NAME));
		assertTrue("Source match", ProcedureLoaderUtil.procedure(PROCEDURE_NAME, MockProcedureSource.class)
				.isProcedure(mockSource.getSourceName(), PROCEDURE_NAME));
		assertTrue("Manual select match",
				ProcedureLoaderUtil.procedure(null).isProcedure(ClassProcedureSource.SOURCE_NAME, null));

		// Property should not be considered
		assertTrue("Property not considered",
				ProcedureLoaderUtil.procedure(PROCEDURE_NAME, ProcedureLoaderUtil.property("NAME"))
						.isProcedure(ClassProcedureSource.SOURCE_NAME, PROCEDURE_NAME));

		// Not match
		assertFalse("No source", ProcedureLoaderUtil.procedure(PROCEDURE_NAME).isProcedure(null, PROCEDURE_NAME));
		assertFalse("Different source",
				ProcedureLoaderUtil.procedure(PROCEDURE_NAME).isProcedure(mockSource.getSourceName(), PROCEDURE_NAME));
		assertFalse("Different procedure", ProcedureLoaderUtil.procedure("not match")
				.isProcedure(ClassProcedureSource.SOURCE_NAME, PROCEDURE_NAME));
		assertFalse("Not match to manual select",
				ProcedureLoaderUtil.procedure(null).isProcedure(ClassProcedureSource.SOURCE_NAME, PROCEDURE_NAME));
		assertFalse("Manual select not match",
				ProcedureLoaderUtil.procedure(PROCEDURE_NAME).isProcedure(ClassProcedureSource.SOURCE_NAME, null));
	}

	/**
	 * Ensure can list just one {@link Procedure}.
	 */
	public void testListSingleProcedure() {
		ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class, ProcedureLoaderUtil.procedure("single"));
	}

	public static class ListSingleProcedure {

		public void single() {
			// Test method
		}
	}

	/**
	 * Ensure can list multiple {@link Procedure} instances.
	 */
	public void testListMultipleProcedures() {
		ProcedureLoaderUtil.validateProcedures(ListMultipleProcedures.class, ProcedureLoaderUtil.procedure("one"),
				ProcedureLoaderUtil.procedure("three"), ProcedureLoaderUtil.procedure("two"));
	}

	public static class ListMultipleProcedures {

		public void one() {
			// Test method
		}

		public void two() {
			// Test method
		}

		public void three() {
			// Test method
		}
	}

	/**
	 * Ensure can list static {@link Procedure}.
	 */
	public void testListStaticProcedure() {
		ProcedureLoaderUtil.validateProcedures(StaticProcedure.class, ProcedureLoaderUtil.procedure("staticMethod"));
	}

	public static class StaticProcedure {

		public static void staticMethod() {
			// Test method
		}
	}

	/**
	 * Ensure ignore non {@link Procedure} {@link Method} instances.
	 */
	public void testIgnoreNonProcedureMethods() throws Exception {
		ProcedureLoaderUtil.validateProcedures(IgnoreNonProcedureMethods.class);
	}

	public static class IgnoreNonProcedureMethods {

		@SuppressWarnings("unused")
		private void ignorePrivate() {
			// Test method
		}

		void ignorePackage() {
			// Test method
		}

		protected void ignoreProtected() {
			// Test method
		}
	}

	/**
	 * Ensure can configure {@link ProcedureSource} overrides default
	 * {@link ProcedureSource}.
	 */
	public void testConfiguredProcedureSourceOverridesDefault() {
		MockProcedureSource.run((context) -> context.addProcedure("MOCK"), null, () -> {
			ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
					ProcedureLoaderUtil.procedure("MOCK", MockProcedureSource.class));
			return null;
		});
	}

	/**
	 * Ensure multiple configured {@link ProcedureSource} instances can contribute.
	 */
	public void testMultipleConfiguredProcedureSources() throws Throwable {
		AnotherMockProcedureSource.run(() -> {
			return MockProcedureSource.run((context) -> context.addProcedure("MOCK"), null, () -> {
				ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
						ProcedureLoaderUtil.procedure("MOCK", AnotherMockProcedureSource.class),
						ProcedureLoaderUtil.procedure("MOCK", MockProcedureSource.class));
				return null;
			});
		});
	}

	/**
	 * Ensure can specify {@link ProcedureProperty} instances.
	 */
	public void testProcedureProperties() throws Throwable {
		ProcedureProperty property = ProcedureLoaderUtil.property("three", "raw");
		MockProcedureSource.run((context) -> {
			ProcedureSpecification procedure = context.addProcedure("MOCK");
			procedure.addProperty("one");
			procedure.addProperty("two", "TWO");
			procedure.addProperty(property);
		}, null, () -> {
			ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
					ProcedureLoaderUtil.procedure("MOCK", MockProcedureSource.class,
							ProcedureLoaderUtil.property("one"), ProcedureLoaderUtil.property("two", "TWO"), property));
			return null;
		});
	}

	/**
	 * <p>
	 * Ensure can allow manually specifying the {@link Procedure}.
	 * <p>
	 * There are cases where the resource can not be introspected for
	 * {@link Procedure} instances. Therefore, need to allow manually specifying the
	 * {@link Procedure}.
	 */
	public void testManuallySpecifyProcedure() throws Throwable {
		MockManagedFunctionProcedureSource.run((context) -> {
			context.addProcedure(null);
		}, null, () -> {
			ProcedureLoaderUtil.validateProcedures("mock",
					ProcedureLoaderUtil.procedure(null, MockManagedFunctionProcedureSource.class));
			return null;
		});
	}

	/**
	 * Ensure handle failure in listing {@link Procedure} instances.
	 */
	public void testFailLoadProcedures() {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		MockCompilerIssues issues = new MockCompilerIssues(this);
		compiler.setCompilerIssues(issues);

		// Record failure
		Exception failure = new Exception("TEST");
		issues.recordIssue("Failed to list procedures from service Mock [" + MockProcedureSource.class.getName() + "]",
				failure);

		// Run test
		this.replayMockObjects();
		Procedure[] procedures = MockProcedureSource.run((context) -> {
			throw failure;
		}, null, () -> {
			ProcedureLoader loader = ProcedureLoaderUtil.newProcedureLoader(compiler);
			return loader.listProcedures(ListSingleProcedure.class.getName());
		});
		this.verifyMockObjects();

		// Should only load successful services
		ProcedureLoaderUtil.validateProcedures(procedures, ProcedureLoaderUtil.procedure("single"));
	}

	/**
	 * Ensure can load type.
	 */
	public void testLoadSimpleType() {
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder("simple", null);
		ProcedureLoaderUtil.validateProcedureType(type, LoadSimpleTypeProcedure.class.getName(), "simple");
	}

	public static class LoadSimpleTypeProcedure {

		public void simple() {
			// Test method
		}
	}

	/**
	 * Ensure can load complex type.
	 */
	public void testLoadComplexType() {
		ProcedureTypeBuilder type = ProcedureLoaderUtil.createProcedureTypeBuilder("complex", String.class);
		type.addObjectType(Long.class.getName(), Long.class, null);
		type.addObjectType("qualified-" + Character.class.getName(), Character.class, "qualified");
		type.addVariableType(String.class.getName());
		type.addVariableType("qualified-" + Integer.class.getName(), Integer.class.getName());
		type.addVariableType(Long.class.getName());
		type.addVariableType(Character.class.getName());
		type.addFlowType("flowOne", null);
		type.addFlowType("flowTwo", Byte.class);
		type.addEscalationType(IOException.class.getName(), IOException.class);
		type.addEscalationType(SQLException.class.getName(), SQLException.class);
		type.setNextArgumentType(Integer.class);
		ProcedureLoaderUtil.validateProcedureType(type, LoadComplexTypeProcedure.class.getName(), "complex");
	}

	@FlowInterface
	public static interface LoadComplexFlows {
		void flowOne();

		void flowTwo(Byte parameter);
	}

	public static class LoadComplexTypeProcedure {

		public Integer complex(@Parameter String parameter, Long dependency,
				@Qualified("qualified") Character qualifiedDependency, @Val String valVariable,
				@Qualified("qualified") In<Integer> inVariable, Out<Long> outVariable, Var<Character> varVariable,
				LoadComplexFlows flows) throws IOException, SQLException {
			return 0;
		}
	}

	/**
	 * Ensure can handle error in loading type.
	 */
	public void testErrorInLoadType() {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		MockCompilerIssues issues = new MockCompilerIssues(this);
		compiler.setCompilerIssues(issues);

		// Record failure
		Exception failure = new Exception("TEST");
		issues.recordIssue("Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
				+ ProcedureManagedFunctionSource.class.getName(), failure);

		// Test
		this.replayMockObjects();
		ProcedureType type = MockProcedureSource.run((context) -> context.addProcedure("error"), (context) -> {
			throw failure;
		}, () -> {
			return ProcedureLoaderUtil.loadProcedureType(ErrorInLoadProcedure.class.getName(),
					MockProcedureSource.class, "error", compiler);
		});
		this.verifyMockObjects();
		assertNull("Should not load type", type);
	}

	public static class ErrorInLoadProcedure {

		public void error() {
			// Test method
		}
	}

	/**
	 * Ensure can load simple {@link ManagedFunction}.
	 */
	public void testLoadSimpleManagedFunction() {
		String resource = "RESOURCE";
		String procedureName = "PROCEDURE";
		String propertyName = "NAME";
		String propertyValue = "VALUE";
		ProcedureTypeBuilder expected = ProcedureLoaderUtil.createProcedureTypeBuilder(procedureName, null);
		ProcedureType type = MockManagedFunctionProcedureSource.run(null, (context) -> {
			assertEquals("Incorrect resource", resource, context.getResource());
			assertEquals("Incorrect procedure name", procedureName, context.getProcedureName());
			assertEquals("Incorrect property", propertyValue, context.getSourceContext().getProperty(propertyName));
			context.setManagedFunction(() -> null, None.class, None.class);
		}, () -> {
			return ProcedureLoaderUtil.validateProcedureType(expected, resource,
					MockManagedFunctionProcedureSource.class, procedureName, propertyName, propertyValue);
		});
		assertNotNull("Should load procedure type", type);
	}

	/**
	 * Ensure can load complex {@link ManagedFunction}.
	 */
	public void testLoadComplexManagedFunction() {
		ProcedureTypeBuilder expected = ProcedureLoaderUtil.createProcedureTypeBuilder("procedure", Double.class);
		expected.addObjectType(Long.class.getName(), Long.class, null);
		expected.addObjectType("qualified-" + Character.class.getName(), Character.class, "qualified");
		expected.addVariableType(String.class.getName());
		expected.addVariableType("qualified-" + Integer.class.getName(), Integer.class.getName());
		expected.addFlowType("flowOne", null);
		expected.addFlowType("flowTwo", Byte.class);
		expected.addEscalationType(IOException.class.getName(), IOException.class);
		expected.addEscalationType(SQLException.class.getName(), SQLException.class);
		expected.setNextArgumentType(Integer.class);
		ProcedureType type = MockManagedFunctionProcedureSource.run(null, (context) -> {
			ManagedFunctionTypeBuilder<Indexed, Indexed> function = context.setManagedFunction(() -> null,
					Indexed.class, Indexed.class);
			function.addObject(Double.class).addAnnotation(this.createMock(Parameter.class));
			function.addObject(Long.class).setLabel(Long.class.getName());
			ManagedFunctionObjectTypeBuilder<Indexed> character = function.addObject(Character.class);
			character.setTypeQualifier("qualified");
			character.setLabel("qualified-" + Character.class.getName());
			function.addObject(Var.class)
					.addAnnotation(new VariableAnnotation(String.class.getName(), String.class.getName()));
			function.addObject(Var.class).addAnnotation(
					new VariableAnnotation("qualified-" + Integer.class.getName(), Integer.class.getName()));
			function.addFlow().setLabel("flowOne");
			ManagedFunctionFlowTypeBuilder<Indexed> flowTwo = function.addFlow();
			flowTwo.setLabel("flowTwo");
			flowTwo.setArgumentType(Byte.class);
			function.addEscalation(IOException.class);
			function.addEscalation(SQLException.class);
			function.setReturnType(Integer.class);
		}, () -> {
			return ProcedureLoaderUtil.validateProcedureType(expected, "resource",
					MockManagedFunctionProcedureSource.class, "procedure");
		});
		assertNotNull("Should load procedure type", type);
	}

	/**
	 * Ensure {@link CompilerIssue} if no {@link ManagedFunction} configured for
	 * {@link Procedure}.
	 */
	public void testIssueIfNoManagedFunction() {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		MockCompilerIssues issues = new MockCompilerIssues(this);
		compiler.setCompilerIssues(issues);

		// Record failure
		IllegalStateException failure = new IllegalStateException("Must provide ManagedFunction for Procedure");
		issues.recordIssue("Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
				+ ProcedureManagedFunctionSource.class.getName(), failure);

		// Test
		this.replayMockObjects();
		ProcedureType type = MockManagedFunctionProcedureSource.run(null, (context) -> {
			// Don't specify managed function
		}, () -> {
			return ProcedureLoaderUtil.loadProcedureType("resource", MockManagedFunctionProcedureSource.class, "error",
					compiler);
		});
		this.verifyMockObjects();
		assertNull("Should not load type", type);
	}

	/**
	 * Ensure {@link CompilerIssue} if multiple {@link ManagedFunction} instances
	 * configured for {@link Procedure}.
	 */
	public void testIssueIfMultipleManagedFunctions() {

		// Create compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		MockCompilerIssues issues = new MockCompilerIssues(this);
		compiler.setCompilerIssues(issues);

		// Record failure
		IllegalStateException failure = new IllegalStateException(
				"Only one ManagedFunction may be specified for a Procedure");
		issues.recordIssue("Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
				+ ProcedureManagedFunctionSource.class.getName(), failure);

		// Test
		this.replayMockObjects();
		ProcedureType type = MockManagedFunctionProcedureSource.run(null, (context) -> {
			// Incorrectly specify two managed functions
			context.setManagedFunction(() -> null, None.class, None.class);
			context.setManagedFunction(() -> null, None.class, None.class);
		}, () -> {
			return ProcedureLoaderUtil.loadProcedureType("resource", MockManagedFunctionProcedureSource.class, "error",
					compiler);
		});
		this.verifyMockObjects();
		assertNull("Should not load type", type);
	}

	/**
	 * Ensure correctly indicates if loading type within {@link Office}.
	 */
	public void testIndicateLoadingTypeForOffice() throws Exception {
		TypeProcedureSource.isType = null;
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Ensure is loading type
			TypeProcedureSource.isType = null;
			ProcedureLoader loader = ProcedureEmployer.employProcedureLoader(context.getOfficeArchitect(),
					context.getOfficeSourceContext());
			loader.loadProcedureType(MockProcedure.class.getName(), TypeProcedureSource.SOURCE_NAME, "procedure", null);
			assertTrue("Should be loading type", TypeProcedureSource.isType);

			// Ensure load
			TypeProcedureSource.isType = null;
			ProcedureArchitect<OfficeSection> procedure = ProcedureEmployer
					.employProcedureArchitect(context.getOfficeArchitect(), context.getOfficeSourceContext());
			procedure.addProcedure("procedure", MockProcedure.class.getName(), TypeProcedureSource.SOURCE_NAME,
					"procedure", false, null);
		});
		compile.compileOfficeFloor();
		assertFalse("Should be loading for use", TypeProcedureSource.isType);
	}

	/**
	 * Ensure correctly indicates if loading type within {@link OfficeSection}.
	 */
	public void testIndicateLoadingTypeForSection() throws Exception {
		TypeProcedureSource.isType = null;
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			// Load office to load section
		});
		compile.section((context) -> {

			// Ensure is loading type
			TypeProcedureSource.isType = null;
			ProcedureLoader loader = ProcedureEmployer.employProcedureLoader(context.getSectionDesigner(),
					context.getSectionSourceContext());
			loader.loadProcedureType(MockProcedure.class.getName(), TypeProcedureSource.SOURCE_NAME, "procedure", null);
			assertTrue("Should be loading type", TypeProcedureSource.isType);

			// Ensure load
			TypeProcedureSource.isType = null;
			ProcedureArchitect<SubSection> procedure = ProcedureEmployer
					.employProcedureDesigner(context.getSectionDesigner(), context.getSectionSourceContext());
			procedure.addProcedure("procedure", MockProcedure.class.getName(), TypeProcedureSource.SOURCE_NAME,
					"procedure", false, null);
		});
		compile.compileOfficeFloor();
		assertFalse("Should be loading for use", TypeProcedureSource.isType);
	}

	public static class MockProcedure {
		public void procedure() {
			// not invoked
		}
	}

}
