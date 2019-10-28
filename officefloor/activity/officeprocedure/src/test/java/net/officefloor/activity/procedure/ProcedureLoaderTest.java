/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.procedure;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureSpecification;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;

/**
 * Tests the {@link ProcedureLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderTest extends OfficeFrameTestCase {

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
	 * Ensure can configure {@link ProcedureService} overrides default
	 * {@link ProcedureService}.
	 */
	public void testConfiguredProcedureServiceOverridesDefault() {
		MockProcedureService.run((context) -> context.addProcedure("MOCK"), null, () -> {
			ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
					ProcedureLoaderUtil.procedure("MOCK", MockProcedureService.class));
			return null;
		});
	}

	/**
	 * Ensure multiple configured {@link ProcedureService} instances can contribute.
	 */
	public void testMultipleConfiguredProcedureServices() throws Throwable {
		AnotherMockProcedureService.run(() -> {
			return MockProcedureService.run((context) -> context.addProcedure("MOCK"), null, () -> {
				ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
						ProcedureLoaderUtil.procedure("MOCK", AnotherMockProcedureService.class),
						ProcedureLoaderUtil.procedure("MOCK", MockProcedureService.class));
				return null;
			});
		});
	}

	/**
	 * Ensure can specify {@link ProcedureProperty} instances.
	 */
	public void testProcedureProperties() throws Throwable {
		ProcedureProperty property = ProcedureLoaderUtil.property("three", "raw");
		MockProcedureService.run((context) -> {
			ProcedureSpecification procedure = context.addProcedure("MOCK");
			procedure.addProperty("one");
			procedure.addProperty("two", "TWO");
			procedure.addProperty(property);
		}, null, () -> {
			ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
					ProcedureLoaderUtil.procedure("MOCK", MockProcedureService.class,
							ProcedureLoaderUtil.property("one"), ProcedureLoaderUtil.property("two", "TWO"), property));
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
		issues.recordIssue("Failed to list procedures from service Mock [" + MockProcedureService.class.getName() + "]",
				failure);

		// Run test
		this.replayMockObjects();
		Procedure[] procedures = MockProcedureService.run((clazz) -> {
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
		type.addEscalationType(IOException.class.getSimpleName(), IOException.class);
		type.addEscalationType(SQLException.class.getSimpleName(), SQLException.class);
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
		ProcedureType type = MockProcedureService.run((context) -> context.addProcedure("error"), (context) -> {
			throw failure;
		}, () -> {
			return ProcedureLoaderUtil.loadProcedureType(ErrorInLoadProcedure.class.getName(),
					MockProcedureService.class, "error", compiler);
		});
		this.verifyMockObjects();
		assertNull("Should not load type", type);
	}

	public static class ErrorInLoadProcedure {

		public void error() {
			// Test method
		}
	}

}