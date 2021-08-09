/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;

/**
 * Script {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptManagedFunction extends StaticManagedFunction<Indexed, Indexed> {

	/**
	 * Decorates the {@link ScriptEngine}.
	 */
	@FunctionalInterface
	public static interface ScriptEngineDecorator {

		/**
		 * Decorates the {@link ScriptEngine}.
		 * 
		 * @param engine {@link ScriptEngine}.
		 * @throws Exception If fails to decorate the {@link ScriptEngine}.
		 */
		void decorate(ScriptEngine engine) throws Exception;
	}

	/**
	 * {@link ThreadLocal} for {@link Invocable}.
	 */
	private final ThreadLocal<Invocable> invocable = new ThreadLocal<>();

	/**
	 * {@link ScriptEngineManager}.
	 */
	private final ScriptEngineManager engineManager;

	/**
	 * {@link ScriptEngine} name.
	 */
	private final String engineName;

	/**
	 * {@link ScriptEngineDecorator}.
	 */
	private final ScriptEngineDecorator scriptEngineDecorator;

	/**
	 * Setup script.
	 */
	private final String setupScript;

	/**
	 * Script.
	 */
	private final String script;

	/**
	 * Name of the function.
	 */
	private final String functionName;

	/**
	 * {@link ClassDependencyFactory} instances.
	 */
	private final ClassDependencyFactory[] parameterFactories;

	/**
	 * {@link ScriptExceptionTranslator}.
	 */
	private final ScriptExceptionTranslator scriptExceptionTranslator;

	/**
	 * Instantiate.
	 * 
	 * @param engineManager             {@link ScriptEngineManager}.
	 * @param engineName                {@link ScriptEngine} name.
	 * @param scriptEngineDecorator     {@link ScriptEngineDecorator}.
	 * @param setupScript               Setup script.
	 * @param script                    Script.
	 * @param functionName              Name of the function.
	 * @param parameterFactories        {@link ClassDependencyFactory} instances.
	 * @param scriptExceptionTranslator {@link ScriptExceptionTranslator}.
	 */
	public ScriptManagedFunction(ScriptEngineManager engineManager, String engineName,
			ScriptEngineDecorator scriptEngineDecorator, String setupScript, String script, String functionName,
			ClassDependencyFactory[] parameterFactories, ScriptExceptionTranslator scriptExceptionTranslator) {
		this.engineManager = engineManager;
		this.engineName = engineName;
		this.scriptEngineDecorator = scriptEngineDecorator;
		this.setupScript = setupScript;
		this.script = script;
		this.functionName = functionName;
		this.parameterFactories = parameterFactories;
		this.scriptExceptionTranslator = scriptExceptionTranslator;
	}

	/*
	 * ======================== ManagedFunction ===========================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the invocable
		Invocable invocable = this.invocable.get();
		if (invocable == null) {

			// Script Engine Manager (not thread safe)
			synchronized (this.engineManager) {

				// Create and configure the engine
				ScriptEngine engine = this.engineManager.getEngineByName(engineName);
				this.scriptEngineDecorator.decorate(engine);
				if (this.setupScript != null) {
					engine.eval(this.setupScript);
				}
				engine.eval(this.script);

				// Obtain the invocable
				invocable = (Invocable) engine;

				// Load for re-use
				this.invocable.set(invocable);
			}
		}

		// Obtain the arguments
		Object[] arguments = new Object[this.parameterFactories.length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = this.parameterFactories[i].createDependency(context);
		}

		try {
			// Invoke the function
			context.setNextFunctionArgument(invocable.invokeFunction(this.functionName, arguments));

		} catch (ScriptException ex) {
			// Translate and throw
			Throwable translated = this.scriptExceptionTranslator.translate(ex);
			throw translated != null ? translated : ex;
		}
	}

}
