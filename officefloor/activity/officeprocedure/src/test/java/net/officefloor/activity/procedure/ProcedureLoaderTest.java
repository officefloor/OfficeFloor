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

import net.officefloor.activity.procedure.java.ClassProcedureServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.test.OfficeFrameTestCase;

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
		ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
				ProcedureLoaderUtil.procedure("single", ClassProcedureServiceFactory.class));
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
		ProcedureLoaderUtil.validateProcedures(ListMultipleProcedures.class,
				ProcedureLoaderUtil.procedure("one", ClassProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("two", ClassProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("three", ClassProcedureServiceFactory.class));
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
		ProcedureLoaderUtil.validateProcedures(StaticProcedure.class,
				ProcedureLoaderUtil.procedure("staticMethod", ClassProcedureServiceFactory.class));
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
	 * Ensure can listing from multiple {@link ProcedureService} instances.
	 */
	public void testAdditionalServices() {
		MockProcedureService.run((clazz) -> new String[] { "MOCK" }, null, () -> {
			ProcedureLoaderUtil.validateProcedures(ListSingleProcedure.class,
					ProcedureLoaderUtil.procedure("single", ClassProcedureServiceFactory.class),
					ProcedureLoaderUtil.procedure("MOCK", MockProcedureService.class));
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
			return loader.listProcedures(ListSingleProcedure.class);
		});
		this.verifyMockObjects();

		// Should only load successful services
		ProcedureLoaderUtil.validateProcedures(procedures,
				ProcedureLoaderUtil.procedure("single", ClassProcedureServiceFactory.class));
	}

	/**
	 * Ensure can load type.
	 */
	public void testLoadSimpleType() {
		ProcedureLoaderUtil.validateProcedureType(LoadSimpleTypeProcedure.class, "simple",
				ClassProcedureServiceFactory.class, null);
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
		ProcedureLoaderUtil.validateProcedureType(LoadComplexTypeProcedure.class, "complex",
				ClassProcedureServiceFactory.class, (type) -> {
					type.setReturnType(Integer.class);
					type.addObject(String.class).setLabel(String.class.getName());
					type.addEscalation(IOException.class);
				});
	}

	public static class LoadComplexTypeProcedure {

		public Integer complex(String parameter) throws IOException {
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
		ManagedFunctionType<Indexed, Indexed> type = MockProcedureService.run(null, (context) -> {
			throw failure;
		}, () -> {
			return ProcedureLoaderUtil.loadProcedureType(LoadSimpleTypeProcedure.class, "error",
					MockProcedureService.class, compiler);
		});
		this.verifyMockObjects();
		assertNull("Should not load type", type);
	}

}