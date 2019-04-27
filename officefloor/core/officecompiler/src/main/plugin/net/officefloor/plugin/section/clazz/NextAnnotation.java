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
 * {@link Next} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class NextAnnotation {

	/**
	 * Name of the {@link Next}.
	 */
	private final String nextName;

	/**
	 * Argument type for the {@link Next}.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param nextName     Name of the {@link Next}.
	 * @param argumentType Argument type for the {@link Next}.
	 */
	public NextAnnotation(String nextName, Class<?> argumentType) {
		this.nextName = nextName;
		this.argumentType = argumentType;
	}

	/**
	 * Instantiate.
	 * 
	 * @param next         {@link Next}.
	 * @param argumentType Argument type for the {@link Next}.
	 */
	public NextAnnotation(Next next, Class<?> argumentType) {
		this(next.value(), argumentType);
	}

	/**
	 * Instantiate.
	 * 
	 * @param nextFunction {@link NextFunction}.
	 * @param argumentType Argument type for the {@link NextFunction}.
	 */
	@SuppressWarnings("deprecation")
	public NextAnnotation(NextFunction nextFunction, Class<?> argumentType) {
		this(nextFunction.value(), argumentType);
	}

	/**
	 * Obtains the {@link Next} name.
	 * 
	 * @return {@link Next} name.
	 */
	public String getNextName() {
		return this.nextName;
	}

	/**
	 * Obtains the argument type for the {@link Next}.
	 * 
	 * @return Argument type for the {@link Next}.
	 */
	public Class<?> getArgumentType() {
		return this.argumentType;
	}
}