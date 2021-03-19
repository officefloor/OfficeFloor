/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.value.load;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * {@link StatelessValueLoaderFactory} to load a keyed object.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyedObjectValueLoaderFactory implements StatelessValueLoaderFactory {

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
	 * {@link PropertyKeyFactory}.
	 */
	private final PropertyKeyFactory propertyKeyFactory;

	/**
	 * Delegate {@link StatelessValueLoader}.
	 */
	private StatelessValueLoader valueLoader;

	/**
	 * Initiate.
	 * 
	 * @param propertyName       Property name.
	 * @param methodName         {@link Method} name.
	 * @param objectType         Object type.
	 * @param objectInstantiator {@link ObjectInstantiator}.
	 * @param propertyKeyFactory {@link PropertyKeyFactory}.
	 */
	public KeyedObjectValueLoaderFactory(String propertyName, String methodName, Class<?> objectType,
			ObjectInstantiator objectInstantiator, PropertyKeyFactory propertyKeyFactory) {
		this.propertyName = propertyName;
		this.methodName = methodName;
		this.objectType = objectType;
		this.objectInstantiator = objectInstantiator;
		this.propertyKeyFactory = propertyKeyFactory;
	}

	/**
	 * Specifies the {@link StatelessValueLoader}.
	 * 
	 * @param valueLoader {@link StatelessValueLoader}.
	 */
	public void setValueLoader(StatelessValueLoader valueLoader) {
		this.valueLoader = valueLoader;
	}

	/*
	 * ================== StatelessValueLoaderFactory ==================
	 */

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	@Override
	public StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName, String.class, this.objectType);

		// Return the value loader
		return new StatelessValueLoader() {

			@Override
			public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
					Map<PropertyKey, Object> state) throws HttpException {

				// Obtain the keyed value
				int keyEnd = name.indexOf('}', nameIndex);
				if (keyEnd < 0) {
					return; // No key so do not load
				}
				String key = name.substring(nameIndex, keyEnd);

				// Obtain the full property name for the key
				String fullKeyName = name.substring(0, keyEnd);
				PropertyKey propertyKey = KeyedObjectValueLoaderFactory.this.propertyKeyFactory
						.createPropertyKey(fullKeyName);

				// Obtain the index for remaining name
				nameIndex = keyEnd + 1; // ignore '}'
				if (name.charAt(nameIndex) == '.') {
					nameIndex++; // ignore following '.'
				}

				// Load the parameter only once
				Object parameter = state.get(propertyKey);
				if (parameter == null) {
					// Instantiate the parameter object
					try {
						parameter = KeyedObjectValueLoaderFactory.this.objectInstantiator
								.instantiate(KeyedObjectValueLoaderFactory.this.objectType);
					} catch (Exception ex) {
						if (ex instanceof HttpException) {
							throw (HttpException) ex;
						} else {
							throw new HttpException(ex);
						}
					}

					// Register the keyed object
					state.put(propertyKey, parameter);

					// Load the parameter
					ValueLoaderSource.loadValue(object, loaderMethod, key, parameter);
				}

				// Load the property onto the object
				KeyedObjectValueLoaderFactory.this.valueLoader.loadValue(parameter, name, nameIndex, value, location,
						state);
			}

			@Override
			public void visitValueNames(Consumer<ValueName> visitor, String namePrefix,
					List<StatelessValueLoader> visistedLoaders) {
				KeyedObjectValueLoaderFactory.this.valueLoader.visitValueNames(visitor, namePrefix + "{}",
						visistedLoaders);
			}
		};
	}

}
