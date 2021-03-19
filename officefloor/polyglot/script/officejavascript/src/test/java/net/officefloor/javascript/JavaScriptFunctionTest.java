/*-
 * #%L
 * JavaScript
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.javascript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
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
 * Tests adapting JavaScript function for {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptFunctionTest extends AbstractPolyglotProcedureTest {

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
				synchronized (engineManager) {

					// Obtain the engine
					final String engineName = "graal.js";
					ScriptEngine engine = engineManager.getEngineByName(engineName);
					Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
					bindings.put("polyglot.js.allowAllAccess", true);

					// Load the script
					InputStream content = JavaScriptFunctionTest.class.getClassLoader()
							.getResourceAsStream("javascript/Functions.js");
					Reader reader = new InputStreamReader(content);
					engine.eval(reader);

					// Invoke the function
					Invocable invocable = (Invocable) engine;

					// Return for use
					return invocable;
				}
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
	protected Class<? extends ProcedureSourceServiceFactory> getProcedureSourceServiceFactoryClass() {
		return JavaScriptProcedureSourceServiceFactory.class;
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
		} catch (ScriptException ex) {
			throw new JavaScriptProcedureSourceServiceFactory().getScriptExceptionTranslator().translate(ex);
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
