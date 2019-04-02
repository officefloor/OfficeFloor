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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionParameterFactory;

/**
 * Script {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScriptManagedFunction extends StaticManagedFunction<Indexed, Indexed> {

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
	 * {@link ManagedFunctionParameterFactory} instances.
	 */
	private final ManagedFunctionParameterFactory[] parameterFactories;

	/**
	 * Instantiate.
	 * 
	 * @param engineManager      {@link ScriptEngineManager}.
	 * @param engineName         {@link ScriptEngine} name.
	 * @param setupScript        Setup script.
	 * @param script             Script.
	 * @param functionName       Name of the function.
	 * @param parameterFactories {@link ManagedFunctionParameterFactory} instances.
	 */
	public ScriptManagedFunction(ScriptEngineManager engineManager, String engineName, String setupScript,
			String script, String functionName, ManagedFunctionParameterFactory[] parameterFactories) {
		this.engineManager = engineManager;
		this.engineName = engineName;
		this.setupScript = setupScript;
		this.script = script;
		this.functionName = functionName;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ======================== ManagedFunction ===========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the invocable
		Invocable invocable = this.invocable.get();
		if (invocable == null) {

			// Create and configure the engine
			ScriptEngine engine = this.engineManager.getEngineByName(engineName);
			if (this.setupScript != null) {
				engine.eval(this.setupScript);
			}
			engine.eval(this.script);

			// Obtain the invocable
			invocable = (Invocable) engine;

			// Load for re-use
			this.invocable.set(invocable);
		}

		// Obtain the arguments
		Object[] arguments = new Object[this.parameterFactories.length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = this.parameterFactories[i].createParameter(context);
		}

		// Invoke the function
		return invocable.invokeFunction(this.functionName, arguments);
	}

}