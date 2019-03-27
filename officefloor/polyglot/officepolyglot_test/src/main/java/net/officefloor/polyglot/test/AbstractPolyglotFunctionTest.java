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
package net.officefloor.polyglot.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.test.variable.MockVar;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;

/**
 * Abstract tests for a polyglot function via a {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPolyglotFunctionTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can use primitive types.
	 */
	public void testDirectPrimitives() {
		PrimitiveTypes types = this.primitives((byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		assertPrimitives(types);
	}

	protected abstract PrimitiveTypes primitives(byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double);

	/**
	 * Ensure can invoke primitive types.
	 */
	public void testInvokePrimitives() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<String> functionName = new Closure<>();
		CompileVar<PrimitiveTypes> primitives = new CompileVar<>();
		compiler.office((context) -> {

			// Load inputs
			value(context, Byte.valueOf((byte) 1));
			value(context, Short.valueOf((short) 2));
			value(context, Character.valueOf('3'));
			value(context, Integer.valueOf(4));
			value(context, Long.valueOf(5));
			value(context, Float.valueOf(6.0f));
			value(context, Double.valueOf(7.0));

			// Capture result
			OfficeSection result = context.addSection("RESULT", PrimitiveReturn.class);
			context.variable(null, PrimitiveTypes.class, primitives);

			// Load polyglot function
			functionName.value = this.primitives(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertPrimitives(primitives.getValue());
	}

	public static class PrimitiveReturn {
		public void service(@Parameter PrimitiveTypes result, Out<PrimitiveTypes> out) {
			out.set(result);
		}
	}

	protected abstract String primitives(CompileOfficeContext context, OfficeSectionInput handleResult);

	private static void assertPrimitives(PrimitiveTypes types) {
		assertEquals("byte", 1, types.getByte());
		assertEquals("short", 2, types.getShort());
		assertEquals("char", '3', types.getChar());
		assertEquals("integer", 4, types.getInt());
		assertEquals("long", 5, types.getLong());
		assertEquals("float", 6.0f, types.getFloat());
		assertEquals("double", 7.0, types.getDouble());
	}

	/**
	 * Ensure can pass in a Java object.
	 */
	public void testDirectObject() {
		String string = "TEST";
		JavaObject object = new JavaObject("test");
		int[] primitiveArray = new int[] { 1 };
		JavaObject[] objectArray = new JavaObject[] { object };
		ObjectTypes types = this.objects(string, object, primitiveArray, objectArray);
		assertObjects(types, string, object, primitiveArray, objectArray);
	}

	protected abstract ObjectTypes objects(String string, JavaObject object, int[] primitiveArray,
			JavaObject[] objectArray);

	/**
	 * Ensure can invoke object types.
	 */
	public void testInvokeObject() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		String string = "TEST";
		JavaObject object = new JavaObject("test");
		int[] primitiveArray = new int[] { 1 };
		JavaObject[] objectArray = new JavaObject[] { object };
		CompileVar<ObjectTypes> var = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {

			// Load object
			value(context, string);
			value(context, object);
			value(context, primitiveArray);
			value(context, objectArray);

			// Capture result
			OfficeSection result = context.addSection("RESULT", ObjectReturn.class);
			context.variable(null, ObjectTypes.class, var);

			// Load polyglot function
			functionName.value = this.objects(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		ObjectTypes types = var.getValue();
		assertObjects(types, string, object, primitiveArray, objectArray);
	}

	public static class ObjectReturn {
		public void service(@Parameter ObjectTypes result, Out<ObjectTypes> out) {
			out.set(result);
		}
	}

	protected abstract String objects(CompileOfficeContext context, OfficeSectionInput handleResult);

	private static void assertObjects(ObjectTypes types, String string, JavaObject object, int[] primitiveArray,
			JavaObject[] objectArray) {
		assertEquals("string", string, types.getString());
		assertSame("object", object, types.getObject());
		assertSame("primitiveArray", primitiveArray, types.getPrimitiveArray());
		assertSame("objectArray", objectArray, types.getObjectArray());
	}

	/**
	 * Ensure can pass collections.
	 */
	public void testDirectCollections() {
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		CollectionTypes types = this.collections(list, set, map);
		assertCollections(types, list, set, map);
	}

	protected abstract CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map);

	/**
	 * Ensure can invoke collections.
	 */
	public void testInvokeCollections() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		CompileVar<CollectionTypes> var = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {

			// Load collections
			value(context, list);
			value(context, set);
			value(context, map);

			// Capture result
			OfficeSection result = context.addSection("RESULT", CollectionReturn.class);
			context.variable(null, CollectionTypes.class, var);

			// Load polyglot function
			functionName.value = this.collections(context, result.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertCollections(var.getValue(), list, set, map);
	}

	public static class CollectionReturn {
		public void service(@Parameter CollectionTypes result, Out<CollectionTypes> out) {
			out.set(result);
		}
	}

	protected abstract String collections(CompileOfficeContext context, OfficeSectionInput handleResult);

	private static void assertCollections(CollectionTypes types, List<Integer> list, Set<Character> set,
			Map<String, JavaObject> map) {
		assertSame("list", list, types.getList());
		assertSame("set", set, types.getSet());
		assertSame("map", map, types.getMap());
	}

	/**
	 * Ensure can handle variables.
	 */
	public void testDirectVariables() {
		MockVar<String> in = new MockVar<>("2");
		MockVar<JavaObject> out = new MockVar<>();
		MockVar<Integer> var = new MockVar<>(3);
		VariableTypes types = this.variables('1', in, out, var);
		assertVariables(types, out.get(), var.get());
	}

	protected abstract VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var);

	/**
	 * Ensure can using variables.
	 */
	public void testInvokeVariables() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<Character> val = new CompileVar<Character>('1');
		CompileVar<String> in = new CompileVar<>("2");
		CompileVar<JavaObject> out = new CompileVar<>();
		CompileVar<Integer> var = new CompileVar<>(3);
		CompileVar<VariableTypes> result = new CompileVar<>();
		Closure<String> functionName = new Closure<>();
		compiler.office((context) -> {

			// Capture variables
			context.variable(null, Character.class, val);
			context.variable(null, String.class, in);
			context.variable(null, JavaObject.class, out);
			context.variable(null, Integer.class, var);

			// Capture result
			OfficeSection section = context.addSection("RESULT", VariableReturn.class);
			context.variable(null, VariableTypes.class, result);

			// Load polyglot function
			functionName.value = this.variables(context, section.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertVariables(result.getValue(), out.getValue(), var.getValue());
	}

	public static class VariableReturn {
		public void service(@Parameter VariableTypes result, Out<VariableTypes> out) {
			out.set(result);
		}
	}

	protected abstract String variables(CompileOfficeContext context, OfficeSectionInput handleResult);

	private static void assertVariables(VariableTypes types, JavaObject out, Integer var) {
		assertEquals("val", '1', types.getVal());
		assertEquals("in", "2", types.getIn());
		assertEquals("var", 3, types.getVar());
		assertEquals("update out", "test", out.getIdentifier());
		assertEquals("update var", Integer.valueOf(4), var);
	}

	/**
	 * Ensure can provide {@link Parameter}.
	 */
	public void testDirectParameter() {
		ParameterTypes types = this.parameter("test");
		assertParameter(types);
	}

	protected abstract ParameterTypes parameter(String parameter);

	/**
	 * Ensure can use parameter.
	 */
	public void testInvokeParameter() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<ParameterTypes> result = new CompileVar<>();
		compiler.office((context) -> {

			// Pass parameter
			OfficeSection passSection = context.addSection("PASS", ParameterPass.class);

			// Capture result
			OfficeSection resultSection = context.addSection("RESULT", ParameterReturn.class);
			context.variable(null, ParameterTypes.class, result);

			// Load polyglot function
			this.parameter(passSection.getOfficeSectionOutput("use"), context,
					resultSection.getOfficeSectionInput("service"));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "PASS.service", null);
		assertParameter(result.getValue());
	}

	public static class ParameterPass {
		@NextFunction("use")
		public String service() {
			return "test";
		}
	}

	public static class ParameterReturn {
		public void service(@Parameter ParameterTypes result, Out<ParameterTypes> out) {
			out.set(result);
		}
	}

	protected abstract void parameter(OfficeSectionOutput pass, CompileOfficeContext context,
			OfficeSectionInput handleResult);

	private static void assertParameter(ParameterTypes types) {
		assertEquals("parameter", "test", types.getParameter());
	}

	/**
	 * Loads a value.
	 * 
	 * @param context {@link CompileOfficeContext}.
	 * @param value   Value.
	 */
	private static void value(CompileOfficeContext context, Object value) {
		context.getOfficeArchitect()
				.addOfficeManagedObjectSource(value.getClass().getName(), new ValueManagedObjectSource(value))
				.addOfficeManagedObject(value.getClass().getName(), ManagedObjectScope.THREAD);
	}

}