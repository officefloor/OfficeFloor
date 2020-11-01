/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;

/**
 * {@link ManagedFunctionInvocation} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionInvocationImpl implements ManagedFunctionInvocation {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * Argument to the {@link ManagedFunction}. May be <code>null</code>.
	 */
	private final Object argument;

	/**
	 * Instantiate.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param argument     Argument to the {@link ManagedFunction}. May be
	 *                     <code>null</code>.
	 */
	public ManagedFunctionInvocationImpl(String functionName, Object argument) {
		this.functionName = functionName;
		this.argument = argument;
	}

	/*
	 * ===================== ManagedFunctionInvocation ======================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public Object getArgument() {
		return this.argument;
	}

}
