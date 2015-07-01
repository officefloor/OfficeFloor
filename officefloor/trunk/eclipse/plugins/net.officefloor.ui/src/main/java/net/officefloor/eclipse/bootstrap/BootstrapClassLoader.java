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
package net.officefloor.eclipse.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * {@link ClassLoader} for {@link Bootstrap}.
 * 
 * @author Daniel Sagenschneider
 */
public class BootstrapClassLoader extends ClassLoader {

	/**
	 * Delegate {@link ClassLoader} to find location of {@link Class} instances.
	 */
	private final ClassLoader delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link ClassLoader}.
	 */
	public BootstrapClassLoader(ClassLoader delegate) {

		// Use the Bootstrap Class Loader as parent
		super(null);

		// Store delegate class loader to use to find classes
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		// Transform the class name into resource name
		String resourceName = name.replace('.', '/') + ".class";

		// Obtain the resource content from the delegate
		InputStream inputStream = this.delegate
				.getResourceAsStream(resourceName);
		if (inputStream == null) {
			// Indicate class can not be found
			throw new ClassNotFoundException(name);
		}

		// Obtain the class definition
		byte[] classBytes;
		try {
			ByteArrayOutputStream classContents = new ByteArrayOutputStream();
			for (int value; (value = inputStream.read()) != -1;) {
				classContents.write(value);
			}
			classBytes = classContents.toByteArray();
		} catch (IOException ex) {
			throw new ClassNotFoundException(
					"Failed reading contents of class file for class " + name,
					ex);
		}

		// Define the class
		Class<?> clazz = this.defineClass(name, classBytes, 0,
				classBytes.length);

		// Return the class
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	@Override
	protected URL findResource(String name) {
		return this.delegate.getResource(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#findResources(java.lang.String)
	 */
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return this.delegate.getResources(name);
	}

}
