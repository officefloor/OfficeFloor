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
package net.officefloor.plugin.variable;

import net.officefloor.compile.integrate.officefloor.AugmentManagedObjectSourceFlowTest.Section;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.Qualified;
import net.officefloor.plugin.section.clazz.NextFunction;

/**
 * Test using the {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableTest extends OfficeFrameTestCase {

	/**
	 * Indicates if complete.
	 */
	private static boolean isComplete = false;

	/**
	 * Ensure can {@link Out} then {@link In}.
	 */
	public void testOutIn() throws Throwable {
		this.doVariableTest(OutInSection.class);
	}

	public static class OutInSection {

		@NextFunction("stepTwo")
		public void stepOne(Out<String> text, Out<Integer> number) {
			text.set("TEXT");
			number.set(1);
		}

		public void stepTwo(In<String> text, In<Integer> number) {
			assertEquals("Incorrect text", "TEXT", text.get());
			assertEquals("Incorrect number", Integer.valueOf(1), number.get());
			isComplete = true;
		}
	}

	/**
	 * Ensure can qualified {@link Out} then {@link In}.
	 */
	public void testQualifiedOutIn() throws Throwable {
		this.doVariableTest(QualifiedOutInSection.class);
	}

	public static class QualifiedOutInSection {

		@NextFunction("stepTwo")
		public void stepOne(@Qualified("QUALIFIED") Out<String> qualified, Out<String> unqualified) {
			qualified.set("QUALIFIED");
			unqualified.set("UNQUALIFIED");
		}

		public void stepTwo(@Qualified("QUALIFIED") In<String> qualified, In<String> unqualified) {
			assertEquals("Incorrect text", "QUALIFIED", qualified.get());
			assertEquals("Incorrect number", "UNQUALIFIED", unqualified.get());
			isComplete = true;
		}
	}

	/**
	 * Ensure can {@link Var} then {@link Val}.
	 */
	public void testVarVal() throws Throwable {
		this.doVariableTest(VarValSection.class);
	}

	public static class VarValSection {

		@NextFunction("stepTwo")
		public void stepOne(Var<String> text, Var<Integer> number) {
			text.set("TEXT");
			number.set(1);
		}

		public void stepTwo(@Val String text, @Val Integer number) {
			assertEquals("Incorrect text", "TEXT", text);
			assertEquals("Incorrect number", Integer.valueOf(1), number);
			isComplete = true;
		}
	}

	/**
	 * Ensure can qualified {@link Var} then {@link Val}.
	 */
	public void testQualifiedVarVal() throws Throwable {
		this.doVariableTest(QualifiedVarValSection.class);
	}

	public static class QualifiedVarValSection {

		@NextFunction("stepTwo")
		public void stepOne(@Qualified("QUALIFIED") Var<String> qualified, Var<String> unqualified) {
			qualified.set("QUALIFIED");
			unqualified.set("UNQUALIFIED");
		}

		public void stepTwo(@Qualified("QUALIFIED") @Val String qualified, @Val String unqualified) {
			assertEquals("Incorrect text", "QUALIFIED", qualified);
			assertEquals("Incorrect number", "UNQUALIFIED", unqualified);
			isComplete = true;
		}
	}

	/**
	 * Undertakes the variable testing.
	 * 
	 * @param sectionClass {@link Section} {@link Class}.
	 */
	private void doVariableTest(Class<?> sectionClass) throws Throwable {

		// Compile section
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> context.addSection("SECTION", sectionClass));
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Trigger the function
		isComplete = false;
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.stepOne", null);
		assertTrue("Should complete", isComplete);
	}

}