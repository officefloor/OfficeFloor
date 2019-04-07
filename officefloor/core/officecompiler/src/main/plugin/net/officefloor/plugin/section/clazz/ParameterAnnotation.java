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

import java.lang.reflect.Method;

/**
 * {@link Parameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterAnnotation {

	/**
	 * Parameter type.
	 */
	private final Class<?> parameterType;

	/**
	 * Index within the {@link Method} for the parameter.
	 */
	private final int parameterIndex;

	/**
	 * Instantiate.
	 * 
	 * @param parameterType  Parameter type.
	 * @param parameterIndex Index within the {@link Method} for the parameter.
	 */
	public ParameterAnnotation(Class<?> parameterType, int parameterIndex) {
		this.parameterType = parameterType;
		this.parameterIndex = parameterIndex;
	}

	/**
	 * Obtains the parameter type.
	 * 
	 * @return Parameter type.
	 */
	public Class<?> getParameterType() {
		return parameterType;
	}

	/**
	 * Obtains index within the {@link Method} for the parameter.
	 * 
	 * @return Index within the {@link Method} for the parameter.
	 */
	public int getParameterIndex() {
		return parameterIndex;
	}

}