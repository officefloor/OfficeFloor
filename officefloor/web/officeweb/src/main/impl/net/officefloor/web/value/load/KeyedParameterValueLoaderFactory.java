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
 * {@link StatelessValueLoaderFactory} to load keyed string parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyedParameterValueLoaderFactory implements StatelessValueLoaderFactory {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * {@link Method} name.
	 */
	private final String methodName;

	/**
	 * Initiate.
	 * 
	 * @param propertyName Property name.
	 * @param methodName   {@link Method} name.
	 */
	public KeyedParameterValueLoaderFactory(String propertyName, String methodName) {
		this.propertyName = propertyName;
		this.methodName = methodName;
	}

	/*
	 * ===================== StatelessValueLoaderFactory =================
	 */

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	@Override
	public StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName, String.class, String.class);

		// Obtain the value location
		final HttpValueLocation loaderLocation = ValueLoaderSource.getLocation(loaderMethod);

		// Return the new value loader
		return new StatelessValueLoader() {

			@Override
			public void loadValue(Object object, String name, int nameIndex, String value, HttpValueLocation location,
					Map<PropertyKey, Object> state) throws HttpException {

				// Determine if match location
				if (!ValueLoaderSource.isLocationMatch(loaderLocation, location)) {
					return; // not match, so do not load
				}

				// Obtain the keyed value
				int keyEnd = name.indexOf('}', nameIndex);
				if (keyEnd < 0) {
					return; // No key so do not load
				}
				String key = name.substring(nameIndex, keyEnd);

				// Load the value
				ValueLoaderSource.loadValue(object, loaderMethod, key, value);
			}

			@Override
			public void visitValueNames(Consumer<ValueName> visitor, String namePrefix,
					List<StatelessValueLoader> visistedLoaders) {
				visitor.accept(new ValueName(namePrefix + "{}", loaderLocation));
			}
		};
	}

}
