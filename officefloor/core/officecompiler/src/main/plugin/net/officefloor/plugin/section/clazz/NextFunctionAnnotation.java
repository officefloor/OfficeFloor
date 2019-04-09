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
package net.officefloor.plugin.section.clazz;

/**
 * {@link NextFunction} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class NextFunctionAnnotation {

	/**
	 * Name of the {@link NextFunction}.
	 */
	private final String nextFunctionName;

	/**
	 * Argument type for the {@link NextFunction}.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param nextFunctionName Name of the {@link NextFunction}.
	 * @param argumentType     Argument type for the {@link NextFunction}.
	 */
	public NextFunctionAnnotation(String nextFunctionName, Class<?> argumentType) {
		this.nextFunctionName = nextFunctionName;
		this.argumentType = argumentType;
	}

	/**
	 * Instantiate.
	 * 
	 * @param nextFunction {@link NextFunction}.
	 * @param argumentType Argument type for the {@link NextFunction}.
	 */
	public NextFunctionAnnotation(NextFunction nextFunction, Class<?> argumentType) {
		this(nextFunction.value(), argumentType);
	}

	/**
	 * Obtains the {@link NextFunction} name.
	 * 
	 * @return {@link NextFunction} name.
	 */
	public String getNextFunctionName() {
		return this.nextFunctionName;
	}

	/**
	 * Obtains the argument type for the {@link NextFunction}.
	 * 
	 * @return Argument type for the {@link NextFunction}.
	 */
	public Class<?> getArgumentType() {
		return this.argumentType;
	}
}