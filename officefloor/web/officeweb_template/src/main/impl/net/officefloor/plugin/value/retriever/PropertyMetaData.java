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
package net.officefloor.plugin.value.retriever;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta-data for the property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyMetaData {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} for the type.
	 */
	private final Method typeMethod;

	/**
	 * Mapping of {@link Method} to the particular type.
	 */
	private final Map<Class<?>, Method> typeToMethod = new HashMap<Class<?>, Method>();

	/**
	 * Properties on the resulting property object.
	 */
	private final PropertyMetaData[] properties;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param typeMethod
	 *            {@link Method} for the type.
	 * @param properties
	 *            Properties on the resulting property object.
	 */
	public PropertyMetaData(String propertyName, Method typeMethod,
			PropertyMetaData[] properties) {
		this.propertyName = propertyName;
		this.typeMethod = typeMethod;
		this.properties = properties;
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return Property name.
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * <p>
	 * Obtains the type {@link Method}.
	 * <p>
	 * This is the {@link Method} base on the type and may be abstract.
	 * 
	 * @return Type {@link Method}.
	 */
	public Method getTypeMethod() {
		return this.typeMethod;
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @param type
	 *            Type to obtain the {@link Method}.
	 * @return {@link Method}.
	 * @throws Exception
	 *             If fails to obtain the {@link Method}.
	 */
	public synchronized Method getMethod(Class<?> type) throws Exception {

		// Lazy obtain the method
		Method method = this.typeToMethod.get(type);
		if (method == null) {
			method = type.getMethod(this.typeMethod.getName());
			this.typeToMethod.put(type, method);
		}

		// Return the method
		return method;
	}

	/**
	 * Obtains the Properties on the resulting property object.
	 * 
	 * @return Properties on the resulting property object.
	 */
	public PropertyMetaData[] getProperties() {
		return this.properties;
	}

}