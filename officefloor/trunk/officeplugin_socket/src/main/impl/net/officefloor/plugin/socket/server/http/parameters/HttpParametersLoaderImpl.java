/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.parameters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * {@link HttpParametersLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderImpl<T> implements HttpParametersLoader<T> {

	/**
	 * <p>
	 * Mapping of specific {@link Class} to the {@link Loader} instances to load
	 * values onto the object.
	 * <p>
	 * This is necessary as the concrete object may vary but still be of the
	 * type and as such will have varying {@link Method} instances to load the
	 * values.
	 * <p>
	 * Size is 2 as typically will require type and concrete object.
	 */
	private final Map<Class<?>, Loader[]> typeToLoaders = new HashMap<Class<?>, Loader[]>(
			2);

	/**
	 * Type of object to be loaded.
	 */
	private Class<T> type;

	/**
	 * Indicates if matching on parameter names is case sensitive.
	 */
	private boolean isCaseSensitive;

	/**
	 * Parameter key at the index of the {@link Loader} array to load the
	 * multiple values onto the Object.
	 */
	private String[] parameterKeys;

	/**
	 * Mapping of the parameter name to the index of the {@link Loader} array to
	 * load the value onto the Object.
	 */
	private Map<String, Integer> parameterNameToLoaderIndex;

	/**
	 * {@link LoaderFactory} instances with their indexes corresponding to the
	 * indexes of the parameter key/name mappings.
	 */
	private LoaderFactory[] loaderFactories;

	/**
	 * Obtains the {@link Loader} instances for the type.
	 * 
	 * @param type
	 *            Type to extract the {@link Loader} instances.
	 * @return {@link Loader} instances for the type.
	 * @throws HttpParametersException
	 *             If fails to obtain the {@link Method} array for the type.
	 */
	private Loader[] getLoaders(Class<?> type) throws HttpParametersException {

		// Ensure thread-safe in lazy loading
		synchronized (this.typeToLoaders) {

			// Lazy create the loaders for the type
			Loader[] loaders = this.typeToLoaders.get(type);
			if (loaders == null) {
				// Obtain the loaders
				loaders = new Loader[this.loaderFactories.length];
				for (int i = 0; i < loaders.length; i++) {
					LoaderFactory loaderFactory = this.loaderFactories[i];
					try {
						loaders[i] = loaderFactory.createLoader(type);
					} catch (Exception ex) {
						// Should be same type but likely now not, so check
						if (!this.type.isAssignableFrom(type)) {
							// Incorrect object type
							throw new HttpParametersException(
									"Object being loaded (type "
											+ type.getName()
											+ ") is not compatible to mapping type "
											+ this.type.getName());
						} else {
							// Unknown failure, just propagate
							throw new HttpParametersException(ex);
						}
					}
				}

				// Register the loaders
				this.typeToLoaders.put(type, loaders);
			}

			// Return the loaders
			return loaders;
		}
	}

	/**
	 * Obtains the parameter name for comparison.
	 * 
	 * @param rawParameterName
	 *            Raw parameter name.
	 * @return Parameter name.
	 */
	private String getComparisonParameterName(String rawParameterName) {
		return (this.isCaseSensitive ? rawParameterName : rawParameterName
				.toLowerCase());
	}

	/*
	 * ==================== HttpParametersLoader =========================
	 */

	@Override
	public void init(Class<T> type, Map<String, String> aliasMappings,
			boolean isCaseSensitive) throws Exception {
		this.type = type;

		// Provide empty alias mappings if null
		if (aliasMappings == null) {
			aliasMappings = Collections.emptyMap();
		}

		// Extract the listing of parameter names and loaders
		List<String> parameterNames = new LinkedList<String>();
		List<LoaderFactory> parameterNameLoaderFactories = new LinkedList<LoaderFactory>();
		List<String> parameterKeys = new LinkedList<String>();
		List<LoaderFactory> parameterKeyLoaderFactories = new LinkedList<LoaderFactory>();
		NEXT_METHOD: for (Method method : this.type.getMethods()) {

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

			// Ensure there is a parameter name (something after 'set')
			String parameterName = methodName.substring(SETTER_PREFIX.length());
			if ((parameterName == null) || (parameterName.length() == 0)) {
				continue NEXT_METHOD;
			}

			// Ensure all parameters are Strings
			Class<?>[] parameterTypes = method.getParameterTypes();
			for (Class<?> parameterType : parameterTypes) {
				if (!String.class.isAssignableFrom(parameterType)) {
					continue NEXT_METHOD;
				}
			}

			// Register the appropriate loader factory
			switch (parameterTypes.length) {
			case 1:
				// Register for single parameter
				parameterNames.add(parameterName);
				parameterNameLoaderFactories
						.add(new SingleParameterLoaderFactory(methodName));
				break;

			case 2:
				// Register for multiple values
				parameterKeys.add(parameterName);
				parameterKeyLoaderFactories
						.add(new MultipleValuesLoaderFactory(methodName,
								parameterName));
				break;

			default:
				// Ignore method as not loader
				continue NEXT_METHOD;
			}
		}

		// Create the listing of the factories
		this.loaderFactories = new LoaderFactory[parameterKeyLoaderFactories
				.size()
				+ parameterNameLoaderFactories.size()];

		// Register the parameter keys and corresponding loader factories
		this.parameterKeys = parameterKeys.toArray(new String[0]);
		for (int i = 0; i < parameterKeyLoaderFactories.size(); i++) {

			// Obtain the loader factory and parameter key
			LoaderFactory loaderFactory = parameterKeyLoaderFactories.get(i);
			String parameterKey = parameterKeys.get(i);

			// Register the loader factory and parameter key
			this.loaderFactories[i] = loaderFactory;
			this.parameterKeys[i] = this
					.getComparisonParameterName(parameterKey);
		}

		// Register the parameter names and mapping of parameter to loader index
		this.parameterNameToLoaderIndex = new HashMap<String, Integer>(
				parameterNames.size() + aliasMappings.size());
		for (int i = 0; i < parameterNames.size(); i++) {

			// Obtain the loader factory and parameter name
			LoaderFactory loaderFactory = parameterNameLoaderFactories.get(i);
			String parameterName = parameterNames.get(i);

			// Determine the corresponding index (as after parameter keys)
			int index = this.parameterKeys.length + i;

			// Register the loader factory and parameter name
			this.loaderFactories[index] = loaderFactory;
			parameterName = this.getComparisonParameterName(parameterName);
			this.parameterNameToLoaderIndex.put(parameterName, new Integer(
					index));
		}

		// Load any aliases
		for (String alias : aliasMappings.keySet()) {

			// Obtain the parameter name for alias
			String parameterName = aliasMappings.get(alias);
			parameterName = this.getComparisonParameterName(parameterName);

			// Obtain the loader index for the alias
			Integer loaderIndex = this.parameterNameToLoaderIndex
					.get(parameterName);
			if (loaderIndex == null) {
				// Unknown parameter for alias
				throw new Exception("Parameter '" + parameterName
						+ "' for alias '" + alias
						+ "' can not be found on type " + this.type.getName());
			}

			// Register the alias
			alias = this.getComparisonParameterName(alias);
			this.parameterNameToLoaderIndex.put(alias, loaderIndex);
		}
	}

	@Override
	public <O extends T> void loadParameters(HttpRequest httpRequest,
			final O object) throws HttpParametersException {

		// Obtain the load methods for the object
		final Loader[] loaders = this.getLoaders(object.getClass());

		// Create the parameters parser
		HttpParametersParser parser = new HttpParametersParserImpl();

		// Parse the parameters and load onto the object
		parser.parseHttpParameters(httpRequest,
				new HttpParametersParseHandler() {
					@Override
					public void handleHttpParameter(String name, String value)
							throws HttpParametersException {

						// Obtain the compare name for the parameter
						String compareParameterName = HttpParametersLoaderImpl.this
								.getComparisonParameterName(name);

						// Obtain index for single parameter
						Integer loaderIndex = HttpParametersLoaderImpl.this.parameterNameToLoaderIndex
								.get(compareParameterName);

						// Determine if single parameter
						if (loaderIndex == null) {
							// Not single, so determine if multiple values
							for (int i = 0; i < HttpParametersLoaderImpl.this.parameterKeys.length; i++) {
								String key = HttpParametersLoaderImpl.this.parameterKeys[i];

								// Ignore if name is not bigger than key
								if (name.length() <= key.length()) {
									continue;
								}

								// Ensure parameter name starts with key
								if (!compareParameterName.startsWith(key)) {
									continue;
								}

								// Found the loader, so specify index
								loaderIndex = new Integer(i);
								break; // found index
							}
						}

						// Determine if found the loader
						if (loaderIndex != null) {
							// Have loader index, so load value
							Loader loader = loaders[loaderIndex.intValue()];
							try {
								loader.loadValue(object, name, value);
							} catch (Exception ex) {
								// Propagate failure to load value
								throw new HttpParametersException(ex);
							}
						}
					}
				});
	}

	/**
	 * Factory to create a {@link Loader}.
	 */
	private interface LoaderFactory {

		/**
		 * Creates the {@link Loader} for the {@link Class}.
		 * 
		 * @param type
		 *            {@link Class}.
		 * @return {@link Loader} for the {@link Class}.
		 * @throws Exception
		 *             If fails to create the {@link Loader}.
		 */
		Loader createLoader(Class<?> type) throws Exception;
	}

	/**
	 * Loader of {@link HttpRequest} parameter onto the object.
	 */
	private interface Loader {

		/**
		 * Loads the value onto the object.
		 * 
		 * @param object
		 *            Object to have the value loaded onto it.
		 * @param name
		 *            Name of the {@link HttpRequest} parameter.
		 * @param value
		 *            Value of the {@link HttpRequest} parameter.
		 * @throws Exception
		 *             If fails to load the value.
		 */
		void loadValue(Object object, String name, String value)
				throws Exception;
	}

	/**
	 * Single parameter {@link LoaderFactory}.
	 */
	private class SingleParameterLoaderFactory implements LoaderFactory {

		/**
		 * {@link Method} name.
		 */
		private final String methodName;

		/**
		 * Initiate.
		 * 
		 * @param methodName
		 *            {@link Method} name.
		 */
		public SingleParameterLoaderFactory(String methodName) {
			this.methodName = methodName;
		}

		/*
		 * =============== LoaderFactory ==========================
		 */

		@Override
		public Loader createLoader(Class<?> type) throws Exception {

			// Obtain the method
			final Method method = type.getMethod(this.methodName, String.class);

			// Return the loader
			return new Loader() {
				@Override
				public void loadValue(Object object, String name, String value)
						throws Exception {
					method.invoke(object, value);
				}
			};
		}
	}

	/**
	 * Multiple values {@link LoaderFactory}.
	 */
	public class MultipleValuesLoaderFactory implements LoaderFactory {

		/**
		 * {@link Method} name.
		 */
		private final String methodName;

		/**
		 * Parameter key.
		 */
		private String parameterKey;

		/**
		 * Initiate.
		 * 
		 * @param methodName
		 *            {@link Method} name.
		 * @param parameterKey
		 *            Parameter key.
		 */
		public MultipleValuesLoaderFactory(String methodName,
				String parameterKey) {
			this.methodName = methodName;
			this.parameterKey = parameterKey;
		}

		/*
		 * =============== LoaderFactory ==========================
		 */

		@Override
		public Loader createLoader(Class<?> type) throws Exception {

			// Obtain the method
			final Method method = type.getMethod(this.methodName, String.class,
					String.class);

			// Return the loader
			return new Loader() {
				@Override
				public void loadValue(Object object, String name, String value)
						throws Exception {

					// Obtain the key from the name
					String key = name
							.substring(MultipleValuesLoaderFactory.this.parameterKey
									.length());

					// Load the value
					method.invoke(object, key, value);
				}
			};
		}
	}

}