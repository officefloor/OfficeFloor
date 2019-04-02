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
package net.officefloor.polyglot.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotFunctionTest;
import net.officefloor.polyglot.test.CollectionTypes;
import net.officefloor.polyglot.test.JavaObject;
import net.officefloor.polyglot.test.ObjectTypes;
import net.officefloor.polyglot.test.ParameterTypes;
import net.officefloor.polyglot.test.PrimitiveTypes;
import net.officefloor.polyglot.test.VariableTypes;

/**
 * Tests adapting JavaScript function for {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptFunctionTest extends AbstractPolyglotFunctionTest {

	private static final Invocable DIRECT;

	static {
		try {
			// Obtain the engine
			final String engineName = "graal.js";
			ScriptEngineManager engineManager = new ScriptEngineManager();
			ScriptEngine engine = engineManager.getEngineByName(engineName);

			// Load the setup
			InputStream setup = ScriptFunctionTest.class.getClassLoader().getResourceAsStream("javascript/Setup.js");
			engine.eval(new InputStreamReader(setup));

			// Load the script
			InputStream content = ScriptFunctionTest.class.getClassLoader()
					.getResourceAsStream("javascript/Functions.js");
			engine.eval(new InputStreamReader(content));

			// Invoke the function
			Invocable invocable = (Invocable) engine;

			// Load for use
			DIRECT = invocable;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Invokes the JavaScript function directly.
	 * 
	 * @param functionName Name of the function.
	 * @param returnType   Expected return type.
	 * @param arguments    Arguments for the function.
	 * @return Result of function.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T directInvokeFunction(String functionName, Class<T> returnType, Object... arguments)
			throws Exception {
		Object result = DIRECT.invokeFunction(functionName, arguments);
		if (returnType == null) {
			assertNull("Should not have return", result);
			return null;
		} else {
			assertEquals("Incorrect return type", returnType, result.getClass());
			return (T) result;
		}
	}

	/*
	 * ======================= AbstractPolyglotFunctionTest =======================
	 */

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) throws Exception {
		return directInvokeFunction("primitives", PrimitiveTypes.class, _boolean, _byte, _short, _char, _int, _long,
				_float, _double);
	}

	@Override
	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "primitives");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.primitives";
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray)
			throws Exception {
		return directInvokeFunction("objects", ObjectTypes.class, string, object, primitiveArray, objectArray);
	}

	@Override
	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "objects");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.objects";
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map)
			throws Exception {
		return directInvokeFunction("collections", CollectionTypes.class, list, set, map);
	}

	@Override
	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "collections");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.collections";
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) throws Exception {
		return directInvokeFunction("variables", VariableTypes.class, val, in, out, var);
	}

	@Override
	protected String variables(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "variables");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.variables";
	}

	@Override
	protected ParameterTypes parameter(String parameter) throws Exception {
		return directInvokeFunction("parameter", ParameterTypes.class, parameter);
	}

	@Override
	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "parameter");
		office.link(pass, function.getOfficeSectionInput("parameter"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

}