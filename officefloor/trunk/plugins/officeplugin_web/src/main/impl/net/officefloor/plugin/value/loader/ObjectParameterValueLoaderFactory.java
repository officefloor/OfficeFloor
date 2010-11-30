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

/**
 * {@link StatelessValueLoaderFactory} to load an object parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectParameterValueLoaderFactory implements
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
	public ObjectParameterValueLoaderFactory(String propertyName,
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
	 * ========================= ValueLoaderFactory =====================
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
				this.objectType);

		// Return the value loader
		return new StatelessValueLoader() {
			@Override
			public void loadValue(Object object, String name, String value,
					Object[] state) throws Exception {

				// Load the parameter only once
				Object parameter = state[ObjectParameterValueLoaderFactory.this.stateIndex];
				if (parameter == null) {
					// Instantiate the parameter object
					parameter = ObjectParameterValueLoaderFactory.this.objectInstantiator
							.instantiate(ObjectParameterValueLoaderFactory.this.objectType);

					// Record on state for possible further loading
					state[ObjectParameterValueLoaderFactory.this.stateIndex] = parameter;

					// Load the parameter
					ValueLoaderSourceImpl.loadValue(object, loaderMethod,
							parameter);
				}

				// Load the remaining object
				ObjectParameterValueLoaderFactory.this.valueLoader.loadValue(
						parameter, name, value, state);
			}
		};
	}

}