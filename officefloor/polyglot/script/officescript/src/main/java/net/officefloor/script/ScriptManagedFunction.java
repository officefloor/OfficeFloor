/*-
 * #%L
 * PolyglotScript
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
