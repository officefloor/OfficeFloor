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
package net.officefloor.web.value.retrieve;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sources the {@link ValueRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueRetrieverSource {

	/**
	 * Retrieves the value from the object.
	 * 
	 * @param object
	 *            {@link Object} containing the value.
	 * @param method
	 *            {@link Method} to retrieve the value.
	 * @return Value retrieved. May be <code>null</code>.
	 * @throws Exception
	 *             If fails to retrieve the value.
	 */
	public static Object retrieveValue(Object object, Method method) throws Exception {
		try {

			// Retrieve the value
			return method.invoke(object);

		} catch (InvocationTargetException ex) {

			// Propagate cause (if possible)
			Throwable cause = ex.getCause();
			if (cause instanceof Exception) {
				throw (Exception) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				// Throw original invocation exception
				throw ex;
			}
		}
	}

	/**
	 * Recursively creates the {@link PropertyMetaData} for the type.
	 * 
	 * @param type
	 *            Type to create the {@link PropertyMetaData} instances.
	 * @param isCaseInsensitive
	 *            Indicates if case insensitve property names.
	 * @param metaDataByType
	 *            {@link PropertyMetaData} for the type to allow tracking
	 *            recursive properties (bean or its downstream property
	 *            returning same type as the bean).
	 * @return {@link PropertyMetaData} instances for the type.
	 * @throws Exception
	 *             If fails to obtain the {@link PropertyMetaData} instances.
	 */
	private static PropertyMetaData[] createPropertyMetaData(Class<?> type, boolean isCaseInsensitive,
			Map<Class<?>, PropertyMetaData[]> metaDataByType) throws Exception {

		final String GETTER_PREFIX = "get";

		// Determine if have meta-data already for type
		PropertyMetaData[] recursiveMetaData = metaDataByType.get(type);
		if (recursiveMetaData != null) {
			return recursiveMetaData;
		}

		// Iterate over the methods to obtain the properties
		List<Method> propertyMethods = new ArrayList<Method>();
		NEXT_METHOD: for (Method method : type.getMethods()) {

			// Ignore Object methods
			if (Object.class.equals(method.getDeclaringClass())) {
				continue NEXT_METHOD;
			}

			// Ensure a public method with no parameters
			if (!Modifier.isPublic(method.getModifiers())) {
				continue NEXT_METHOD;
			}
			if (method.getParameterTypes().length != 0) {
				continue NEXT_METHOD;
			}

			// Ensure the method begins with 'get'
			String methodName = method.getName();
			if (!methodName.startsWith(GETTER_PREFIX)) {
				continue NEXT_METHOD;
			}

			// Ensure there is a property name (something after 'get')
			String propertyName = methodName.substring(GETTER_PREFIX.length());
			if ((propertyName == null) || (propertyName.length() == 0)) {
				continue NEXT_METHOD;
			}

			// Ensure returns a value
			Class<?> returnType = method.getReturnType();
			if ((returnType == null) || (returnType == Void.class)) {
				continue NEXT_METHOD;
			}

			// Add the property method
			propertyMethods.add(method);
		}

		// Create the property meta-data array for the type
		PropertyMetaData[] metaData = new PropertyMetaData[propertyMethods.size()];
		metaDataByType.put(type, metaData);

		// Load property meta-data (after adding meta-data for recursion)
		for (int i = 0; i < metaData.length; i++) {

			// Obtain the method and its details
			Method method = propertyMethods.get(i);
			String methodName = method.getName();
			Class<?> returnType = method.getReturnType();

			// Obtain the property name
			String propertyName = methodName.substring(GETTER_PREFIX.length());
			if (isCaseInsensitive) {
				propertyName = propertyName.toLowerCase();
			}

			// Obtain the property meta data for the return type
			PropertyMetaData[] returnTypeMetaData = createPropertyMetaData(returnType, isCaseInsensitive,
					metaDataByType);

			// Create and register the meta data for the property
			PropertyMetaData propertyMetaData = new PropertyMetaData(propertyName, method, returnTypeMetaData);
			metaData[i] = propertyMetaData;
		}

		// Return the meta data
		return metaData;
	}

	/**
	 * Flag indicating if case insensitive.
	 */
	private boolean isCaseInsensitive;

	/**
	 * Initialises.
	 * 
	 * @param isCaseInsensitive
	 *            Indicates if property name comparison is case insensitive.
	 * @throws Exception
	 *             If fails to specify case sensitivity.
	 */
	public ValueRetrieverSource(boolean isCaseInsensitive) throws Exception {
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/**
	 * Sources the {@link ValueRetriever} for the type.
	 * 
	 * @param <T>
	 *            Type.
	 * @param type
	 *            Type.
	 * @return {@link ValueRetriever} for the <code>type</code>.
	 * @throws Exception
	 *             If fails to obtain the {@link ValueRetriever}.
	 */
	public <T> ValueRetriever<T> sourceValueRetriever(Class<T> type) throws Exception {

		// Obtain the property meta data
		PropertyMetaData[] metaData = createPropertyMetaData(type, this.isCaseInsensitive,
				new HashMap<Class<?>, PropertyMetaData[]>());

		// Return the value retriever
		return new RootValueRetrieverImpl<T>(metaData, this.isCaseInsensitive);
	}

}