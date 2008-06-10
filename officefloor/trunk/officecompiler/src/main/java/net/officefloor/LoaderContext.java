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
	public <O> O createInstance(Class<O> type, String className)
			throws Exception {

		// Obtain the instance of the class
		Class<O> clazz = this.obtainClass(className, type);

		// Return a new instance of the class
		return clazz.newInstance();
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

		// Obtain the class
		Class<?> clazz;
		try {
			clazz = this.classLoader.loadClass(className);
		} catch (ClassNotFoundException ex) {
			throw new Exception("Unknown type '" + className + "'");
		}

		return clazz;
	}

	/**
	 * Creates the {@link Class} of the <code>typeName</code> that must be a
	 * sub-type of <code>superType</code>.
	 * 
	 * @param typeName
	 *            Type name of the {@link Class} to return.
	 * @param superType
	 *            Super type that the returned {@link Class} must be assignable
	 *            to.
	 * @return {@link Class} of <code>typeName</code>.
	 * @throws Exception
	 *             If fails to obtain the {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	public <T> Class<T> obtainClass(String typeName,
			Class<T> superType) throws Exception {

		// Obtain the class
		Class<?> clazz = this.obtainClass(typeName);

		// Ensure the class is of appropriate type
		if (!superType.isAssignableFrom(clazz)) {
			throw new Exception("Type '" + typeName
					+ "' is not assignable to super type "
					+ superType.getName());
		}

		// Return as appropriate type
		return (Class<T>) clazz;
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
