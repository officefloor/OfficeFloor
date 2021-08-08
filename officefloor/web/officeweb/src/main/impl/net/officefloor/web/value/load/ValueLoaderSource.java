/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.value.load;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.HttpContentParameter;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Sources the {@link ValueLoaderFactory} for a {@link Class} type.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderSource {

	/**
	 * Loads the value onto the object.
	 * 
	 * @param object
	 *            {@link Object} to have value loaded on it.
	 * @param method
	 *            {@link Method} to load the values.
	 * @param parameters
	 *            Values to be loaded into the {@link Method}.
	 * @throws HttpException
	 *             If fails to load the values.
	 */
	public static void loadValue(Object object, Method method, Object... parameters) throws HttpException {
		try {

			// Load the value
			method.invoke(object, parameters);

		} catch (InvocationTargetException ex) {
			// Propagate cause of invocation escalation
			Throwable cause = ex.getCause();
			if (cause instanceof HttpException) {
				throw (HttpException) cause;
			} else {
				throw new HttpException(cause);
			}
		} catch (Exception ex) {
			throw new HttpException(ex);
		}
	}

	/**
	 * Obtains the {@link HttpValueLocation} for the {@link Method}.
	 * 
	 * @param method
	 *            {@link Method}.
	 * @return {@link HttpValueLocation} or <code>null</code> if match all
	 *         {@link HttpValueLocation}.
	 */
	public static HttpValueLocation getLocation(Method method) {

		// Interrogate for annotations
		HttpValueLocation location = extractLocation(method);
		if (location != null) {
			return location; // defined on method
		}

		// Determine if associated field for property
		String methodName = method.getName();
		String propertyName = methodName.substring("set".length());
		propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
		Class<?> clazz = method.getDeclaringClass();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (propertyName.equals(field.getName())) {
					// Found field, so return based location details
					return extractLocation(field);
				}
			}
			clazz = clazz.getSuperclass();
		}

		// As here, matches any location
		return null;
	}

	/**
	 * Extracts the {@link HttpValueLocation} from the {@link AnnotatedElement}.
	 * 
	 * @param element
	 *            {@link AnnotatedElement}.
	 * @return Extracted {@link HttpValueLocation} or <code>null</code> if not
	 *         {@link HttpValueLocation} details.
	 */
	private static HttpValueLocation extractLocation(AnnotatedElement element) {
		if (element.isAnnotationPresent(HttpPathParameter.class)) {
			return HttpValueLocation.PATH;
		} else if (element.isAnnotationPresent(HttpQueryParameter.class)) {
			return HttpValueLocation.QUERY;
		} else if (element.isAnnotationPresent(HttpHeaderParameter.class)) {
			return HttpValueLocation.HEADER;
		} else if (element.isAnnotationPresent(HttpCookieParameter.class)) {
			return HttpValueLocation.COOKIE;
		} else if (element.isAnnotationPresent(HttpContentParameter.class)) {
			return HttpValueLocation.ENTITY;
		} else {
			return null;
		}
	}

	/**
	 * Indicates if match {@link HttpValueLocation}.
	 * 
	 * @param loaderLocation
	 *            {@link StatelessValueLoader} {@link HttpValueLocation}. May be
	 *            <code>null</code> to match any {@link HttpValueLocation}.
	 * @param valueLocation
	 *            {@link HttpValueLocation} for the value.
	 * @return <code>true</code> if match {@link HttpValueLocation}.
	 */
	public static boolean isLocationMatch(HttpValueLocation loaderLocation, HttpValueLocation valueLocation) {
		return (loaderLocation == null) || (loaderLocation == valueLocation);
	}

	/**
	 * Creates {@link StatelessValueLoaderFactory} instances for the type.
	 * 
	 * @param type
	 *            Type to create {@link StatelessValueLoaderFactory} instances.
	 * @param propertyKeyFactory
	 *            {@link PropertyKeyFactory}.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @param aliasMappings
	 *            Alias mappings.
	 * @param factoriesByType
	 *            {@link StatelessValueLoaderFactory} instances by type.
	 * @return {@link StatelessValueLoaderFactory} instances for the type.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoaderFactory}
	 *             instances.
	 */
	public static StatelessValueLoaderFactory[] createValueLoaderFactories(Class<?> type,
			PropertyKeyFactory propertyKeyFactory, ObjectInstantiator objectInstantiator,
			Map<String, String> aliasMappings, Map<Class<?>, StatelessValueLoaderFactory[]> factoriesByType)
			throws Exception {

		// Determine if already have factories for the type
		StatelessValueLoaderFactory[] factories = factoriesByType.get(type);
		if (factories != null) {
			return factories;
		}

		// Extract the value loader meta-data
		List<ValueLoaderStruct> valueLoaders = new ArrayList<ValueLoaderStruct>();
		NEXT_METHOD: for (Method method : type.getMethods()) {

			// Ensure a public void method
			if (!Modifier.isPublic(method.getModifiers())) {
				continue NEXT_METHOD;
			}
			if ((method.getReturnType() != null) && (method.getReturnType() != Void.TYPE)) {
				continue NEXT_METHOD;
			}

			// Ensure the method begins with 'set'
			String methodName = method.getName();
			final String SETTER_PREFIX = "set";
			if (!methodName.startsWith(SETTER_PREFIX)) {
				continue NEXT_METHOD;
			}

			// Ensure there is a property name (something after 'set')
			String propertyName = methodName.substring(SETTER_PREFIX.length());
			if ((propertyName == null) || (propertyName.length() == 0)) {
				continue NEXT_METHOD;
			}

			// Ensure the appropriate method parameters
			Class<?>[] parameterTypes = method.getParameterTypes();
			switch (parameterTypes.length) {
			case 1:
				// Single parameter
				Class<?> objectType = parameterTypes[0];
				if (objectType.isPrimitive()) {
					continue NEXT_METHOD; // ignore loading primitives
				}
				break;

			case 2:
				// Register for keyed values

				// Ensure first parameter is a String
				if (!String.class.isAssignableFrom(parameterTypes[0])) {
					continue NEXT_METHOD; // must be String
				}

				// Ensure second parameter is not a primative
				objectType = parameterTypes[1];
				if (objectType.isPrimitive()) {
					continue NEXT_METHOD; // ignore loading primitives
				}
				break;

			default:
				// Ignore method as not loader
				continue NEXT_METHOD;
			}

			// Add the value loader for the property
			valueLoaders.add(new ValueLoaderStruct(method, propertyName));

			// Register the aliases
			for (String aliasName : aliasMappings.keySet()) {
				String propertyAlias = aliasMappings.get(aliasName);

				// Determine if alias for the property
				PropertyKey propertyKey = propertyKeyFactory.createPropertyKey(propertyName);
				PropertyKey aliasKey = propertyKeyFactory.createPropertyKey(propertyAlias);
				if (!propertyKey.equals(aliasKey)) {
					continue; // not alias for property
				}

				// Add the alias for the property
				valueLoaders.add(new ValueLoaderStruct(method, aliasName));
			}
		}

		// Create and register the value loader factories array.
		// Must be registered before loading factories due to recursive types.
		factories = new StatelessValueLoaderFactory[valueLoaders.size()];
		factoriesByType.put(type, factories);

		// Create and load factories onto the array
		List<ObjectParameterStruct> objectParameters = new LinkedList<ObjectParameterStruct>();
		for (int i = 0; i < factories.length; i++) {
			ValueLoaderStruct struct = valueLoaders.get(i);

			// Obtain the method details
			String methodName = struct.method.getName();
			Class<?>[] parameterTypes = struct.method.getParameterTypes();

			// Obtain the property name
			String propertyName = struct.propertyName;

			// Register the appropriate value loader meta-data
			StatelessValueLoaderFactory factory = null;
			switch (parameterTypes.length) {
			case 1:
				// Register for single parameter
				Class<?> objectType = parameterTypes[0];
				if (String.class.isAssignableFrom(objectType)) {
					// String loader
					factory = new SingleParameterValueLoaderFactory(propertyName, methodName);
				} else {
					// Create the value loader for loaded object
					StatelessValueLoaderFactory[] objectTypeFactories = createValueLoaderFactories(objectType,
							propertyKeyFactory, objectInstantiator, aliasMappings, factoriesByType);

					// Object loader
					ObjectParameterValueLoaderFactory objectParameter = new ObjectParameterValueLoaderFactory(
							propertyName, methodName, objectType, objectInstantiator, propertyKeyFactory);
					factory = objectParameter;

					// Add object parameter
					objectParameters.add(new ObjectParameterStruct(objectType, objectTypeFactories, propertyKeyFactory,
							objectParameter, null));
				}
				break;

			case 2:
				// Register for keyed values
				objectType = parameterTypes[1];
				if (String.class.isAssignableFrom(objectType)) {
					// Keyed string loader
					factory = new KeyedParameterValueLoaderFactory(propertyName, methodName);

				} else {
					// Create the value loader for loaded object
					StatelessValueLoaderFactory[] objectTypeFactories = createValueLoaderFactories(objectType,
							propertyKeyFactory, objectInstantiator, aliasMappings, factoriesByType);

					// Keyed object loader
					KeyedObjectValueLoaderFactory keyedObject = new KeyedObjectValueLoaderFactory(propertyName,
							methodName, objectType, objectInstantiator, propertyKeyFactory);
					factory = keyedObject;

					// Add object parameter
					objectParameters.add(new ObjectParameterStruct(objectType, objectTypeFactories, propertyKeyFactory,
							null, keyedObject));
				}
				break;
			}

			// Load the factory
			factories[i] = factory;
		}

		// Initialise the object parameters
		for (ObjectParameterStruct objectParameter : objectParameters) {
			objectParameter.init();
		}

		// Return the factories
		return factories;
	}

	/**
	 * Creates the {@link StatelessValueLoader}.
	 * 
	 * @param clazz
	 *            {@link Class} to create the specific {@link StatelessValueLoader}.
	 * @param factories
	 *            {@link StatelessValueLoaderFactory} instances for the
	 *            {@link StatelessValueLoader}.
	 * @param propertyKeyFactory
	 *            {@link PropertyKeyFactory}.
	 * @return {@link StatelessValueLoader}.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoader}.
	 */
	private static StatelessValueLoader createValueLoader(Class<?> clazz, StatelessValueLoaderFactory[] factories,
			PropertyKeyFactory propertyKeyFactory) throws Exception {

		// Create the value loaders for the class
		Map<PropertyKey, StatelessValueLoader> valueLoaders = new HashMap<PropertyKey, StatelessValueLoader>();
		for (StatelessValueLoaderFactory factory : factories) {
			String propertyName = factory.getPropertyName();
			StatelessValueLoader valueLoader = factory.createValueLoader(clazz);
			valueLoaders.put(propertyKeyFactory.createPropertyKey(propertyName), valueLoader);
		}

		// Return the root value loader
		return new RootStatelessValueLoader(valueLoaders, propertyKeyFactory);
	}

	/**
	 * {@link StatelessValueLoaderFactory} instances.
	 */
	private StatelessValueLoaderFactory[] factories;

	/**
	 * {@link PropertyKeyFactory}.
	 */
	private PropertyKeyFactory propertyKeyFactory;

	/**
	 * Initialise.
	 * 
	 * @param type
	 *            Type to interrogate for properties to be loaded.
	 * @param isCaseInsensitive
	 *            Flag indicating if case sensitive matching.
	 * @param aliasMappings
	 *            Alias Mappings.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public ValueLoaderSource(Class<?> type, boolean isCaseInsensitive, Map<String, String> aliasMappings,
			ObjectInstantiator objectInstantiator) throws Exception {

		// Create the property key factory
		this.propertyKeyFactory = new PropertyKeyFactory(isCaseInsensitive);

		// Load the factories for the type
		this.factories = createValueLoaderFactories(type, this.propertyKeyFactory, objectInstantiator, aliasMappings,
				new HashMap<Class<?>, StatelessValueLoaderFactory[]>());
	}

	/**
	 * <p>
	 * Sources the {@link ValueLoaderFactory}.
	 * <p>
	 * The returned {@link ValueLoaderFactory} is specific to the input
	 * {@link Class}. The {@link ValueLoaderFactory} will not work with children of
	 * the {@link Class}. A new {@link ValueLoaderFactory} must be created for each
	 * child type.
	 * 
	 * @param <T>
	 *            {@link Class} type.
	 * @param clazz
	 *            {@link Class} for a dedicated {@link ValueLoaderFactory}. It is
	 *            expected that all property methods on the <code>type</code> are on
	 *            this <code>clazz</code>.
	 * @return {@link ValueLoaderFactory}.
	 * @throws Exception
	 *             If fails to source the {@link ValueLoaderFactory}.
	 */
	public <T> ValueLoaderFactory<T> sourceValueLoaderFactory(Class<T> clazz) throws Exception {
		// Create and return the value loader factory for the class
		return new ValueLoaderFactoryImpl<T>(createValueLoader(clazz, this.factories, this.propertyKeyFactory));
	}

	/**
	 * Struct containing details for the {@link StatelessValueLoaderFactory}.
	 */
	private static class ValueLoaderStruct {

		/**
		 * {@link Method}.
		 */
		public final Method method;

		/**
		 * Property name.
		 */
		public final String propertyName;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            {@link Method}.
		 * @param propertyName
		 *            Property name.
		 */
		public ValueLoaderStruct(Method method, String propertyName) {
			this.method = method;
			this.propertyName = propertyName;
		}
	}

	/**
	 * Struct containing details for the {@link ObjectParameterValueLoaderFactory}
	 * and {@link KeyedObjectValueLoaderFactory}.
	 */
	private static class ObjectParameterStruct {

		/**
		 * Type of the object being loaded.
		 */
		public final Class<?> type;

		/**
		 * {@link StatelessValueLoaderFactory} instances.
		 */
		public final StatelessValueLoaderFactory[] factories;

		/**
		 * {@link PropertyKeyFactory}.
		 */
		public final PropertyKeyFactory propertyKeyFactory;

		/**
		 * {@link ObjectParameterValueLoaderFactory}.
		 */
		public final ObjectParameterValueLoaderFactory parameter;

		/**
		 * {@link KeyedObjectValueLoaderFactory}.
		 */
		public final KeyedObjectValueLoaderFactory keyed;

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            Type of the object being loaded.
		 * @param factories
		 *            {@link StatelessValueLoaderFactory} instances.
		 * @param propertyKeyFactory
		 *            {@link PropertyKeyFactory}.
		 * @param parameter
		 *            {@link ObjectParameterValueLoaderFactory}.
		 * @param keyed
		 *            {@link KeyedObjectValueLoaderFactory}.
		 */
		public ObjectParameterStruct(Class<?> type, StatelessValueLoaderFactory[] factories,
				PropertyKeyFactory propertyKeyFactory, ObjectParameterValueLoaderFactory parameter,
				KeyedObjectValueLoaderFactory keyed) {
			this.type = type;
			this.factories = factories;
			this.propertyKeyFactory = propertyKeyFactory;
			this.parameter = parameter;
			this.keyed = keyed;
		}

		/**
		 * Initialise the object parameter.
		 * 
		 * @throws Exception
		 *             If fails to initialise.
		 */
		public void init() throws Exception {

			// Create the value loader
			StatelessValueLoader valueLoader = createValueLoader(this.type, this.factories, this.propertyKeyFactory);

			// Load into object parameter
			if (this.parameter != null) {
				this.parameter.setValueLoader(valueLoader);
			} else {
				this.keyed.setValueLoader(valueLoader);
			}
		}
	}

}
