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
import javax.script.ScriptException;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotFunctionTest;
import net.officefloor.polyglot.test.CollectionTypes;
import net.officefloor.polyglot.test.JavaObject;
import net.officefloor.polyglot.test.MockHttpObject;
import net.officefloor.polyglot.test.MockHttpParameters;
import net.officefloor.polyglot.test.ObjectTypes;
import net.officefloor.polyglot.test.ParameterTypes;
import net.officefloor.polyglot.test.PrimitiveTypes;
import net.officefloor.polyglot.test.VariableTypes;
import net.officefloor.polyglot.test.WebTypes;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;

/**
 * Tests adapting JavaScript function for {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptFunctionTest extends AbstractPolyglotFunctionTest {

	/**
	 * {@link ScriptEngineManager}.
	 */
	private static final ScriptEngineManager engineManager = new ScriptEngineManager();

	/**
	 * {@link ThreadLocal} instance {@link Invocable}.
	 */
	private static final ThreadLocal<Invocable> DIRECT = new ThreadLocal<Invocable>() {
		@Override
		protected Invocable initialValue() {
			try {
				// Obtain the engine
				final String engineName = "graal.js";
				ScriptEngine engine = engineManager.getEngineByName(engineName);

				// Load the setup
				InputStream setup = ScriptFunctionTest.class.getClassLoader()
						.getResourceAsStream("javascript/Setup.js");
				engine.eval(new InputStreamReader(setup));

				// Load the script
				InputStream content = ScriptFunctionTest.class.getClassLoader()
						.getResourceAsStream("javascript/Functions.js");
				engine.eval(new InputStreamReader(content));

				// Invoke the function
				Invocable invocable = (Invocable) engine;

				// Load for use
				return invocable;

			} catch (Exception ex) {
				throw fail(ex);
			}
		}
	};

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
		Object result = DIRECT.get().invokeFunction(functionName, arguments);
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
	protected void variables(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "variables");
		office.link(pass, function.getOfficeSectionInput("variables"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
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

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response)
			throws Exception {
		directInvokeFunction("web", null, pathParameter, queryParameter, headerParameter, cookieParameter,
				httpParameters, httpObject, response);
	}

	@Override
	protected void web(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "web");
		office.link(pass, function.getOfficeSectionInput("web"));
	}

	@Override
	protected void httpException() throws Throwable {
		try {
			directInvokeFunction("httpException", null);
			fail("Should not be successful");
		} catch (ScriptException ex) {
			throw new MockScriptFunctionSectionSource().getScriptExceptionTranslator().translate(ex);
		}
	}

	@Override
	protected void httpException(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "httpException");
		office.link(pass, function.getOfficeSectionInput("httpException"));
	}

	@Override
	protected String flow(CompileOfficeContext context, OfficeSectionInput next, OfficeSectionInput flow,
			OfficeSectionInput flowWithCallback, OfficeSectionInput flowWithParameterAndCallback,
			OfficeSectionInput flowWithParameter, OfficeSectionInput exception) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "serviceFlow");
		office.link(function.getOfficeSectionOutput("nextFunction"), next);
		office.link(function.getOfficeSectionOutput("flow"), flow);
		office.link(function.getOfficeSectionOutput("flowWithCallback"), flowWithCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameterAndCallback"), flowWithParameterAndCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameter"), flowWithParameter);
		office.link(function.getOfficeSectionOutput("exception"), exception);
		return "section.serviceFlow";
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) throws Exception {
		directInvokeFunction("asynchronousFlow", null, flowOne, flowTwo);
	}

	@Override
	protected String asynchronousFlow(CompileOfficeContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", MockScriptFunctionSectionSource.class.getName(),
				"javascript/Functions.js");
		function.addProperty(MockScriptFunctionSectionSource.PROPERTY_FUNCTION_NAME, "asynchronousFlow");
		return "section.asynchronousFlow";
	}

}