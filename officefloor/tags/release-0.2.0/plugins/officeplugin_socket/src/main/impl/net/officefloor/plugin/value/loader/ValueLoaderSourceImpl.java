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
package net.officefloor.plugin.value.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link ValueLoaderSource} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderSourceImpl implements ValueLoaderSource {

	/**
	 * Loads the value onto the object.
	 * 
	 * @param object
	 *            {@link Object} to have value loaded on it.
	 * @param method
	 *            {@link Method} to load the values.
	 * @param parameters
	 *            Values to be loaded into the {@link Method}.
	 * @throws Exception
	 *             If fails to load the values.
	 */
	public static void loadValue(Object object, Method method,
			Object... parameters) throws Exception {
		try {

			// Load the value
			method.invoke(object, parameters);

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
	 * Creates {@link StatelessValueLoaderFactory} instances for the type.
	 * 
	 * @param type
	 *            Type to create {@link StatelessValueLoaderFactory} instances.
	 * @param nameTranslator
	 *            {@link NameTranslator}.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @param indexing
	 *            {@link StateIndexing}.
	 * @param aliasMappings
	 *            Alias mappings.
	 * @return {@link StatelessValueLoaderFactory} instances for the type.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoaderFactory}
	 *             instances.
	 */
	public static StatelessValueLoaderFactory[] createValueLoaderFactories(
			Class<?> type, NameTranslator nameTranslator,
			ObjectInstantiator objectInstantiator, StateIndexing indexing,
			Map<String, String> aliasMappings) throws Exception {

		// Extract the value loader meta-data
		List<StatelessValueLoaderFactory> factories = new LinkedList<StatelessValueLoaderFactory>();
		NEXT_METHOD: for (Method method : type.getMethods()) {

			// Ensure a public void method
			if (!Modifier.isPublic(method.getModifiers())) {
				continue NEXT_METHOD;
			}
			if ((method.getReturnType() != null)
					&& (method.getReturnType() != Void.TYPE)) {
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

			// Use transformed property name for comparison
			propertyName = nameTranslator.translate(propertyName);

			// Register the value loader factory for the property
			registerStatelessValueLoaderFactory(factories, method,
					propertyName, nameTranslator, objectInstantiator, indexing,
					aliasMappings);

			// Register the aliases
			for (String aliasName : aliasMappings.keySet()) {
				String propertyAlias = aliasMappings.get(aliasName);

				// Determine if alias for the property
				propertyAlias = nameTranslator.translate(propertyAlias);
				if (!propertyName.equals(propertyAlias)) {
					continue; // not alias for property
				}

				// Use transformed alias for comparison
				aliasName = nameTranslator.translate(aliasName);

				// Register the alias for the property
				registerStatelessValueLoaderFactory(factories, method,
						aliasName, nameTranslator, objectInstantiator,
						indexing, aliasMappings);
			}
		}

		// Return the factories
		return factories.toArray(new StatelessValueLoaderFactory[0]);
	}

	/**
	 * Register the {@link StatelessValueLoaderFactory}.
	 * 
	 * @param factories
	 *            Listing to have the {@link StatelessValueLoaderFactory} added.
	 * @param method
	 *            {@link Method} to load the value.
	 * @param propertyName
	 *            Property name for the {@link StatelessValueLoaderFactory}.
	 * @param nameTranslator
	 *            {@link NameTranslator}.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @param indexing
	 *            {@link StateIndexing}.
	 * @param aliasMappings
	 *            Alias mappings.
	 * @throws Exception
	 *             If fails to register the {@link StatelessValueLoaderFactory}.
	 */
	private static void registerStatelessValueLoaderFactory(
			List<StatelessValueLoaderFactory> factories, Method method,
			String propertyName, NameTranslator nameTranslator,
			ObjectInstantiator objectInstantiator, StateIndexing indexing,
			Map<String, String> aliasMappings) throws Exception {

		// Obtain the method details
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();

		// Ensure have parameters
		if (parameterTypes.length == 0) {
			return;
		}

		// Register the appropriate value loader meta-data
		switch (parameterTypes.length) {
		case 1:
			// Register for single parameter
			Class<?> objectType = parameterTypes[0];
			if (objectType.isPrimitive()) {
				return; // ignore loading primitives
			} else if (String.class.isAssignableFrom(objectType)) {
				// Register string loader
				factories.add(new SingleParameterValueLoaderFactory(
						propertyName, methodName));
			} else {
				// Create the value loader for loaded object
				StatelessValueLoaderFactory[] objectTypeFactories = createValueLoaderFactories(
						objectType, nameTranslator, objectInstantiator,
						indexing, aliasMappings);
				StatelessValueLoader valueLoader = createValueLoader(
						objectType, objectTypeFactories, nameTranslator);

				// Register object loader
				factories.add(new ObjectParameterValueLoaderFactory(
						propertyName, methodName, objectType,
						objectInstantiator, valueLoader, indexing));
			}
			break;

		case 2:
			// Register for keyed values

			// Ensure first parameter is a String
			if (!String.class.isAssignableFrom(parameterTypes[0])) {
				return; // must be String
			}

			objectType = parameterTypes[1];
			if (objectType.isPrimitive()) {
				return; // ignore loading primitives
			} else if (String.class.isAssignableFrom(objectType)) {
				// Register keyed string loader
				factories.add(new KeyedParameterValueLoaderFactory(
						propertyName, methodName));

			} else {
				// Create the value loader for loaded object
				StatelessValueLoaderFactory[] objectTypeFactories = createValueLoaderFactories(
						objectType, nameTranslator, objectInstantiator,
						indexing, aliasMappings);
				StatelessValueLoader valueLoader = createValueLoader(
						objectType, objectTypeFactories, nameTranslator);

				// Register keyed object loader
				factories.add(new KeyedObjectValueLoaderFactory(propertyName,
						methodName, objectType, objectInstantiator,
						valueLoader, indexing));
			}
			break;

		default:
			// Ignore method as not loader
			return;
		}
	}

	/**
	 * Creates the {@link StatelessValueLoader}.
	 * 
	 * @param clazz
	 *            {@link Class} to create the specific
	 *            {@link StatelessValueLoader}.
	 * @param factories
	 *            {@link StatelessValueLoaderFactory} instances for the
	 *            {@link StatelessValueLoader}.
	 * @param nameTranslator
	 *            {@link NameTranslator}.
	 * @return {@link StatelessValueLoader}.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoader}.
	 */
	private static StatelessValueLoader createValueLoader(Class<?> clazz,
			StatelessValueLoaderFactory[] factories,
			NameTranslator nameTranslator) throws Exception {

		// Create the value loaders for the class
		Map<String, StatelessValueLoader> valueLoaders = new HashMap<String, StatelessValueLoader>();
		for (StatelessValueLoaderFactory factory : factories) {
			String propertyName = factory.getPropertyName();
			StatelessValueLoader valueLoader = factory.createValueLoader(clazz);
			valueLoaders.put(propertyName, valueLoader);
		}

		// Return the root value loader
		return new RootStatelessValueLoader(valueLoaders, nameTranslator);
	}

	/**
	 * {@link StatelessValueLoaderFactory} instances.
	 */
	private StatelessValueLoaderFactory[] factories;

	/**
	 * <p>
	 * Indicates the number of objects within the state.
	 * <p>
	 * This is a fixed number as based on type within initialisation.
	 */
	private int numberOfObjectsInState;

	/**
	 * {@link NameTranslator}.
	 */
	private NameTranslator nameTranslator;

	/*
	 * =================== ValueLoaderSource ===========================
	 */

	@Override
	public void init(Class<?> type, boolean isCaseSensitive,
			Map<String, String> aliasMappings,
			ObjectInstantiator objectInstantiator) throws Exception {

		// Create the indexing
		StateIndexing indexing = new StateIndexing();

		// Create the translator
		this.nameTranslator = new NameTranslatorImpl(isCaseSensitive);

		// Load the factories for the type
		this.factories = createValueLoaderFactories(type, this.nameTranslator,
				objectInstantiator, indexing, aliasMappings);

		// Next index will be number in state (as first index is 0)
		this.numberOfObjectsInState = indexing.nextIndex();
	}

	@Override
	public <T> ValueLoaderFactory<T> sourceValueLoaderFactory(Class<T> clazz)
			throws Exception {
		// Create and return the value loader factory for the class
		return new ValueLoaderFactoryImpl<T>(this.numberOfObjectsInState,
				createValueLoader(clazz, this.factories, this.nameTranslator));
	}

}