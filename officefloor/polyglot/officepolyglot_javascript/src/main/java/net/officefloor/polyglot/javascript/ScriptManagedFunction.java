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
package net.officefloor.polyglot.javascript;

import javax.script.Invocable;

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
	 * {@link Invocable}.
	 */
	private final Invocable invocable;

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
	 * @param invocable          {@link Invocable}.
	 * @param functionName       Name of the function.
	 * @param parameterFactories {@link ManagedFunctionParameterFactory} instances.
	 */
	public ScriptManagedFunction(Invocable invocable, String functionName,
			ManagedFunctionParameterFactory[] parameterFactories) {
		this.invocable = invocable;
		this.functionName = functionName;
		this.parameterFactories = parameterFactories;
	}

	/*
	 * ======================== ManagedFunction ===========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

		// Obtain the arguments
		Object[] arguments = new Object[this.parameterFactories.length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = this.parameterFactories[i].createParameter(context);
		}

		// Invoke the function
		return this.invocable.invokeFunction(this.functionName, arguments);
	}

}