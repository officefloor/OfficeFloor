/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor;

/**
 * Context for building and compiling.
 * 
 * @author Daniel
 */
public class LoaderContext {

	/**
	 * {@link java.lang.ClassLoader} to use in building.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link java.lang.ClassLoader} to use in building.
	 */
	public LoaderContext(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * <p>
	 * Creates an instance of the input class name.
	 * <p>
	 * Will also ensure the class is of the input type.
	 * 
	 * @param type
	 *            Type that the class must be assignable to.
	 * @param className
	 *            Name of class.
	 * @return Instance of the {@link Class}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public <O> O createInstance(Class<O> type, String className)
			throws Exception {

		// Obtain the instance of the class
		Class clazz = obtainClass(className);

		// Ensure is of appropriate type
		if (!type.isAssignableFrom(clazz)) {
			throw new IllegalStateException("Class '" + className
					+ "' must be assignable to type " + type.getName());
		}

		// Return a new instance of the class
		return (O) clazz.newInstance();
	}

	/**
	 * Obtains the {@link Class} by the input class name.
	 * 
	 * @param className
	 *            Name of class.
	 * @return {@link Class}.
	 * @throws Exception
	 *             If fails.
	 */
	public Class<?> obtainClass(String className) throws Exception {
		return this.classLoader.loadClass(className);
	}

	/**
	 * Obtains the {@link java.lang.ClassLoader} for this {@link LoaderContext}.
	 * 
	 * @return {@link java.lang.ClassLoader} for this {@link LoaderContext}.
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}
