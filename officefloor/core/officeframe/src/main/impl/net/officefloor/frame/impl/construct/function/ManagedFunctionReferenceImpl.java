/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * {@link ManagedFunctionReference} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionReferenceImpl implements ManagedFunctionReference {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * Type of argument to be passed to the referenced {@link ManagedFunction}.
	 */
	private final Class<?> argumentType;

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument to be passed to the referenced
	 *            {@link ManagedFunction}.
	 */
	public ManagedFunctionReferenceImpl(String functionName, Class<?> argumentType) {
		this.functionName = functionName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== ManagedFunctionReference ===================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}