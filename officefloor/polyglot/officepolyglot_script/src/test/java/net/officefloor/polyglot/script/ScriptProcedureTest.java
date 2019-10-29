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

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotProcedureTest;
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

/**
 * Tests adapting JavaScript function for {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptProcedureTest extends AbstractPolyglotProcedureTest {

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
				Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
				bindings.put("polyglot.js.allowAllAccess", true);

				// Load the setup
				InputStream setup = ScriptProcedureTest.class.getClassLoader()
						.getResourceAsStream("javascript/Setup.js");
				engine.eval(new InputStreamReader(setup));

				// Load the script
				InputStream content = ScriptProcedureTest.class.getClassLoader()
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

	/**
	 * Ensure no procedures when using non-functions.
	 */
	public void testNonFunctions() {
		ProcedureLoaderUtil.validateProcedures("javascript/OfficeFloorFunctionMetaData.js");
	}

	/**
	 * Ensure list {@link net.officefloor.activity.procedure.Procedure} instances.
	 */
	public void testListProcedures() {
		ProcedureLoaderUtil.validateProcedures("javascript/Functions.js",
				ProcedureLoaderUtil.procedure("asynchronousFlow", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("collections", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("httpException", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("objects", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("parameter", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("primitives", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("serviceFlow", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("variables", MockScriptProcedureServiceFactory.class),
				ProcedureLoaderUtil.procedure("web", MockScriptProcedureServiceFactory.class));
	}

	/*
	 * ======================= AbstractPolyglotProcedureTest =======================
	 */

	@Override
	protected Class<? extends ProcedureServiceFactory> getProcedureServiceFactoryClass() {
		return MockScriptProcedureServiceFactory.class;
	}

	@Override
	protected boolean isSupportExceptions() {
		return false;
	}

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) throws Exception {
		return directInvokeFunction("primitives", PrimitiveTypes.class, _boolean, _byte, _short, _char, _int, _long,
				_float, _double);
	}

	@Override
	protected void primitives(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "primitives");
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray)
			throws Exception {
		return directInvokeFunction("objects", ObjectTypes.class, string, object, primitiveArray, objectArray);
	}

	@Override
	protected void objects(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "objects");
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map)
			throws Exception {
		return directInvokeFunction("collections", CollectionTypes.class, list, set, map);
	}

	@Override
	protected void collections(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "collections");
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) throws Exception {
		return directInvokeFunction("variables", VariableTypes.class, val, in, out, var);
	}

	@Override
	protected void variables(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "variables");
	}

	@Override
	protected ParameterTypes parameter(String parameter) throws Exception {
		return directInvokeFunction("parameter", ParameterTypes.class, parameter);
	}

	@Override
	protected void parameter(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "parameter");
	}

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response)
			throws Exception {
		directInvokeFunction("web", null, pathParameter, queryParameter, headerParameter, cookieParameter,
				httpParameters, httpObject, response);
	}

	@Override
	protected void web(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "web");
	}

	@Override
	protected void httpException() throws Throwable {
		try {
			directInvokeFunction("httpException", null);
			fail("Should not be successful");
		} catch (ScriptException ex) {
			throw new MockScriptProcedureServiceFactory().getScriptExceptionTranslator().translate(ex);
		}
	}

	@Override
	protected void httpException(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "httpException");
	}

	@Override
	protected void flow(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "serviceFlow");
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) throws Exception {
		directInvokeFunction("asynchronousFlow", null, flowOne, flowTwo);
	}

	@Override
	protected void asynchronousFlow(ProcedureBuilder builder) {
		builder.setProcedure("javascript/Functions.js", "asynchronousFlow");
	}

}