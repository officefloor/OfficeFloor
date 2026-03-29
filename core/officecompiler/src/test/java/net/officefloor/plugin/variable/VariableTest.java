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

package net.officefloor.plugin.variable;

import net.officefloor.compile.integrate.officefloor.AugmentManagedObjectSourceFlowTest.Section;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.Next;

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
		this.doVariableTest(OutInSection.class, null, String.class, "TEXT", null, Integer.class, 1);
	}

	public static class OutInSection {

		@Next("stepTwo")
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
		this.doVariableTest(QualifiedOutInSection.class, "QUALIFIED", String.class, "QUALIFIED", null, String.class,
				"UNQUALIFIED");
	}

	public static class QualifiedOutInSection {

		@Next("stepTwo")
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
		this.doVariableTest(VarValSection.class, null, String.class, "TEXT", null, Integer.class, 1);
	}

	public static class VarValSection {

		@Next("stepTwo")
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
		this.doVariableTest(QualifiedVarValSection.class, "QUALIFIED", String.class, "QUALIFIED", null, String.class,
				"UNQUALIFIED");
	}

	public static class QualifiedVarValSection {

		@Next("stepTwo")
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
	 * @param sectionClass    {@link Section} {@link Class}.
	 * @param varOneQualifier Variable one qualifier.
	 * @param varOneType      Variable one type.
	 * @param varOneValue     Variable one value.
	 * @param varTwoQualifier Variable two qualifier.
	 * @param varTwoType      Variable two type.
	 * @param varTwoValue     Variable two value.
	 */
	private <A, B> void doVariableTest(Class<?> sectionClass, String varOneQualifier, Class<A> varOneType,
			A varOneValue, String varTwoQualifier, Class<B> varTwoType, B varTwoValue) throws Throwable {

		// Capture variable values
		CompileVar<A> varOne = new CompileVar<>();
		CompileVar<B> varTwo = new CompileVar<>();

		// Compile section
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", sectionClass);
			context.variable(varOneQualifier, varOneType, varOne);
			context.variable(varTwoQualifier, varTwoType, varTwo);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Trigger the function
			isComplete = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.stepOne", null);
			assertTrue("Should complete", isComplete);

			// Ensure correct values
			assertEquals("Incorrect variable one value", varOneValue, varOne.getValue());
			assertEquals("Incorrect variable two value", varTwoValue, varTwo.getValue());
		}
	}

	/**
	 * <p>
	 * Ensure auto-box of primitives is same variable.
	 * <p>
	 * Treating <code>int</code> and {@link Integer} as separate variable types
	 * could get confusing and prone to errors.
	 */
	public void testMatchAutoBoxed() throws Throwable {

		// Capture primitive variables
		CompileVar<Boolean> varBoolean = new CompileVar<>();
		CompileVar<Short> varShort = new CompileVar<>();
		CompileVar<Character> varCharacter = new CompileVar<>();
		CompileVar<Integer> varInteger = new CompileVar<>();
		CompileVar<Long> varLong = new CompileVar<>();
		CompileVar<Float> varFloat = new CompileVar<>();
		CompileVar<Double> varDouble = new CompileVar<>();

		// Compile section
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", PrimitiveSection.class);
			context.variable(null, Boolean.class, varBoolean);
			context.variable(null, Short.class, varShort);
			context.variable(null, Character.class, varCharacter);
			context.variable(null, Integer.class, varInteger);
			context.variable(null, Long.class, varLong);
			context.variable(null, Float.class, varFloat);
			context.variable(null, Double.class, varDouble);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Trigger the function
			isComplete = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.stepOne", null);
			assertTrue("Should complete", isComplete);

			// Ensure correct values
			assertTrue("Incorrect boolean", varBoolean.getValue());
			assertEquals("Incorrect short", Short.valueOf((short) 1), varShort.getValue());
			assertEquals("Incorrect char", Character.valueOf((char) 2), varCharacter.getValue());
			assertEquals("Incorrect int", Integer.valueOf(3), varInteger.getValue());
			assertEquals("Incorrect long", Long.valueOf(4), varLong.getValue());
			assertEquals("Incorrect float", Float.valueOf(5.0F), varFloat.getValue());
			assertEquals("Incorrect double", Double.valueOf(6.0), varDouble.getValue());
		}
	}

	public static class PrimitiveSection {

		@Next("stepTwo")
		public void stepOne(Out<Boolean> varBoolean, Out<Short> varShort, Out<Character> varCharacter,
				Out<Integer> varInteger, Out<Long> varLong, Out<Float> varFloat, Out<Double> varDouble) {
			varBoolean.set(true);
			varShort.set((short) 1);
			varCharacter.set((char) 2);
			varInteger.set(3);
			varLong.set(4L);
			varFloat.set(5.0F);
			varDouble.set(6.0);
		}

		public void stepTwo(@Val boolean varBoolean, @Val short varShort, @Val char varChar, @Val int varInt,
				@Val long varLong, @Val float varFloat, @Val double varDouble) {
			assertTrue("Incorrect boolean", varBoolean);
			assertEquals("Incorrect short", 1, varShort);
			assertEquals("Incorrect char", 2, varChar);
			assertEquals("Incorrect int", 3, varInt);
			assertEquals("Incorrect long", 4, varLong);
			assertEquals("Incorrect float", 5.0F, varFloat);
			assertEquals("Incorrect double", 6.0, varDouble);
			isComplete = true;
		}
	}

}
