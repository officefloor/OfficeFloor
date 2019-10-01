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

import java.lang.reflect.Method;

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
	public void testListSingleProcedure() throws Exception {
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
	public void testListMultipleProcedures() throws Exception {
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
	public void testListStaticProcedure() throws Exception {
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

}