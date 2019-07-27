/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.bridge;

import java.net.URL;

/**
 * {@link ClassLoader} {@link EnvironmentBridge}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassLoaderEnvironmentBridge implements EnvironmentBridge {

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Instantiate with default {@link ClassLoader}.
	 */
	public ClassLoaderEnvironmentBridge() {
		this.classLoader = this.getClass().getClassLoader();
	}

	/**
	 * Instantiate.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 */
	public ClassLoaderEnvironmentBridge(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Attempts to load the {@link Class}.
	 * 
	 * @param className Name of {@link Class}.
	 * @return {@link Class} or <code>null</code>.
	 */
	private Class<?> loadClass(String className) {
		try {
			return this.classLoader.loadClass(className);
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Attempts to load the resource.
	 * 
	 * @param resourcePath Path to resource.
	 * @return {@link URL}.
	 */
	private URL loadResource(String resourcePath) {
		return this.classLoader.getResource(resourcePath);
	}

	/*
	 * ================= EnvironmentBridge =====================
	 */

	@Override
	public boolean isClassOnClassPath(String className) {
		return (this.loadClass(className) != null);
	}

	@Override
	public boolean isSuperType(String className, String superType) {

		// Ensure have class
		Class<?> superClass = this.loadClass(superType);
		if (superClass == null) {
			return false;
		}

		// Ensure have class
		Class<?> clazz = this.loadClass(className);
		if (clazz == null) {
			return false;
		}

		// Return whether super type
		return superClass.isAssignableFrom(clazz);
	}

	@Override
	public void selectClass(String searchText, String superType, SelectionHandler handler) {

		// Ensure have class
		Class<?> superClass = this.loadClass(superType);
		if (superClass == null) {
			handler.error(new ClassNotFoundException("Can not find super type " + superType));
			return;
		}

		// Not deriving, so just return search text
		handler.selected(searchText);
	}

	@Override
	public boolean isResourceOnClassPath(String resourcePath) {
		return (this.loadResource(resourcePath) != null);
	}

	@Override
	public void selectClassPathResource(String searchText, SelectionHandler handler) {

		// Not deriving, so just return search text
		handler.selected(searchText);
	}

}