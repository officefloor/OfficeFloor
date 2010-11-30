/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.value.loader.NameTranslator;
import net.officefloor.plugin.value.loader.NameTranslatorImpl;

/**
 * {@link ValueRetrieverSource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueRetrieverSourceImpl implements ValueRetrieverSource {

	/**
	 * Retrieves the value from the object.
	 * 
	 * @param object
	 *            {@link Object} containing the value.
	 * @param method
	 *            {@link Method} to retrieve the value.
	 * @throws Exception
	 *             If fails to retrieve the value.
	 */
	public static Object retrieveValue(Object object, Method method)
			throws Exception {
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
	 * @param translator
	 *            {@link NameTranslator}.
	 * @return {@link PropertyMetaData} instances for the type.
	 * @throws Exception
	 *             If fails to obtain the {@link PropertyMetaData} instances.
	 */
	private static PropertyMetaData[] createPropertyMetaData(Class<?> type,
			NameTranslator translator) throws Exception {

		// Iterate over the methods to obtain the properties
		List<PropertyMetaData> metaDatas = new LinkedList<PropertyMetaData>();
		NEXT_METHOD: for (Method method : type.getMethods()) {

			// Ignore Object methods
			if (method.getDeclaringClass() == Object.class) {
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
			final String GETTER_PREFIX = "get";
			if (!methodName.startsWith(GETTER_PREFIX)) {
				continue NEXT_METHOD;
			}

			// Ensure there is a property name (something after 'get')
			String propertyName = methodName.substring(GETTER_PREFIX.length());
			if ((propertyName == null) || (propertyName.length() == 0)) {
				continue NEXT_METHOD;
			}

			// Translate the property name
			propertyName = translator.translate(propertyName);

			// Ensure returns a value
			Class<?> returnType = method.getReturnType();
			if ((returnType == null) || (returnType == Void.class)) {
				continue NEXT_METHOD;
			}

			// Obtain the property meta data for the return type
			PropertyMetaData[] returnTypeMetaData = createPropertyMetaData(
					returnType, translator);

			// Create and register the meta data for the property
			PropertyMetaData propertyMetaData = new PropertyMetaData(
					propertyName, methodName, returnTypeMetaData);
			metaDatas.add(propertyMetaData);
		}

		// Return the meta data
		return metaDatas.toArray(new PropertyMetaData[0]);
	}

	/**
	 * Case sensitive.
	 */
	private boolean isCaseSensitive;

	/*
	 * ================ ValueRetrieverSource ========================
	 */

	@Override
	public void init(boolean isCaseSensitive) throws Exception {
		this.isCaseSensitive = isCaseSensitive;
	}

	@Override
	public <T> ValueRetriever<T> sourceValueRetriever(Class<T> type)
			throws Exception {

		// Create the translator
		NameTranslator translator = new NameTranslatorImpl(this.isCaseSensitive);

		// Obtain the property meta data
		PropertyMetaData[] metaData = createPropertyMetaData(type, translator);

		// Return the value retriever
		return new RootValueRetrieverImpl<T>(metaData, translator);
	}

}