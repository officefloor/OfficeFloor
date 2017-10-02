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
package net.officefloor.web.value.load;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.web.value.load.PropertyKey;
import net.officefloor.web.value.load.StatelessValueLoader;
import net.officefloor.web.value.load.StatelessValueLoaderFactory;

/**
 * {@link StatelessValueLoaderFactory} to load a string parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class SingleParameterValueLoaderFactory implements
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
	public SingleParameterValueLoaderFactory(String propertyName,
			String methodName) {
		this.propertyName = propertyName;
		this.methodName = methodName;
	}

	/*
	 * ======================== ValueLoaderFactory =========================
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
				String.class);

		// Return the new value loader
		return new StatelessValueLoader() {
			@Override
			public void loadValue(Object object, String name, int nameIndex,
					String value, Map<PropertyKey, Object> state)
					throws Exception {
				ValueLoaderSource.loadValue(object, loaderMethod, value);
			}
		};
	}

}