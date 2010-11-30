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
 * {@link StatelessValueLoaderFactory} to load keyed string parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class KeyedParameterValueLoaderFactory implements
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
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @param methodName
	 *            {@link Method} name.
	 */
	public KeyedParameterValueLoaderFactory(String propertyName,
			String methodName) {
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
	public StatelessValueLoader createValueLoader(Class<?> clazz)
			throws Exception {

		// Obtain the loader method
		final Method loaderMethod = clazz.getMethod(this.methodName,
				String.class, String.class);

		// Return the new value loader
		return new StatelessValueLoader() {
			@Override
			public void loadValue(Object object, String propertyName,
					String value, Object[] state) throws Exception {

				// Obtain the keyed value
				int keyEnd = propertyName.indexOf('}');
				if (keyEnd < 0) {
					return; // No key so do not load
				}
				String key = propertyName.substring(0, keyEnd);

				// Load the value
				ValueLoaderSourceImpl.loadValue(object, loaderMethod, key,
						value);
			}
		};
	}

}