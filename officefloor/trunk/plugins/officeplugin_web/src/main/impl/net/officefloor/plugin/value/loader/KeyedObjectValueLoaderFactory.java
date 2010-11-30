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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link StatelessValueLoaderFactory} to load a keyed object.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyedObjectValueLoaderFactory implements
		StatelessValueLoaderFactory {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} name.
	 */
	private final String methodName;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * {@link ObjectInstantiator}.
	 */
	private final ObjectInstantiator objectInstantiator;

	/**
	 * Delegate {@link StatelessValueLoader}.
	 */
	private final StatelessValueLoader valueLoader;

	/**
	 * Index within the state to obtain state for {@link ValueLoader} instances
	 * created from this factory.
	 */
	private final int stateIndex;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param methodName
	 *            {@link Method} name.
	 * @param objectType
	 *            Object type.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @param delegateValueLoader
	 *            Delegate {@link StatelessValueLoader}.
	 * @param indexing
	 *            {@link StateIndexing}.
	 */
	public KeyedObjectValueLoaderFactory(String propertyName,
			String methodName, Class<?> objectType,
			ObjectInstantiator objectInstantiator,
			StatelessValueLoader delegateValueLoader, StateIndexing indexing) {
		this.propertyName = propertyName;
		this.methodName = methodName;
		this.objectType = objectType;
		this.objectInstantiator = objectInstantiator;
		this.valueLoader = delegateValueLoader;
		this.stateIndex = indexing.nextIndex();
	}

	/*
	 * ================== StatelessValueLoaderFactory ==================
	 */

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	@Override
	public StatelessValueLoader createValueLoader(Class<?> clazz)
			throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName,
				String.class, this.objectType);

		// Return the value loader
		return new StatelessValueLoader() {
			@Override
			@SuppressWarnings("unchecked")
			public void loadValue(Object object, String name, String value,
					Object[] state) throws Exception {

				// Obtain the keyed value
				int keyEnd = name.indexOf('}');
				if (keyEnd < 0) {
					return; // No key so do not load
				}
				String key = name.substring(0, keyEnd);

				// Obtain the remaining name
				int remainingStart = keyEnd + 1; // ignore '{'
				if (name.charAt(remainingStart) == '.') {
					remainingStart++; // ignore '.'
				}
				String remainingName = name.substring(remainingStart);

				// Obtain the map for keyed objects
				Map<String, Object> map = (Map<String, Object>) state[KeyedObjectValueLoaderFactory.this.stateIndex];
				if (map == null) {
					// Create the map
					map = new HashMap<String, Object>();

					// Record on state for possible further loading
					state[KeyedObjectValueLoaderFactory.this.stateIndex] = map;
				}

				// Load the keyed object only once
				Object parameter = map.get(key);
				if (parameter == null) {
					// Instantiate the parameter object
					parameter = KeyedObjectValueLoaderFactory.this.objectInstantiator
							.instantiate(KeyedObjectValueLoaderFactory.this.objectType);

					// Register the keyed object
					map.put(key, parameter);

					// Load the parameter
					ValueLoaderSourceImpl.loadValue(object, loaderMethod, key,
							parameter);
				}

				// Load the property onto the object
				KeyedObjectValueLoaderFactory.this.valueLoader.loadValue(
						parameter, remainingName, value, state);
			}
		};
	}

}