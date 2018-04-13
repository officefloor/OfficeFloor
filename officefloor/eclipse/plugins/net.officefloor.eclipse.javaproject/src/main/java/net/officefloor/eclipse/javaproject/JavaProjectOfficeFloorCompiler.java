/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.javaproject;

import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import net.officefloor.compile.OfficeFloorCompiler;

/**
 * Bridge for {@link IJavaProject} to {@link OfficeFloorCompiler} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaProjectOfficeFloorCompiler {

	/**
	 * {@link IJavaProject}.
	 */
	private final IJavaProject javaProject;

	/**
	 * Cached {@link ClassLoader} for the {@link IJavaProject}.
	 */
	private ClassLoader classLoader = null;

	/**
	 * Cached {@link OfficeFloorCompiler}.
	 */
	private OfficeFloorCompiler compiler = null;

	/**
	 * Instantiate.
	 * 
	 * @param javaProject
	 *            {@link IJavaProject}.
	 */
	public JavaProjectOfficeFloorCompiler(IJavaProject javaProject) {
		this.javaProject = javaProject;

		/*
		 * Listen to java changes to see if class path has become invalid.
		 * 
		 * Note: rather than determine if particular project is changed, easier to just
		 * reconstruct class path again on any change.
		 */
		JavaCore.addElementChangedListener((event) -> {
			this.classLoader = null;
			this.compiler = null;
		});
	}

	/**
	 * Obtains the {@link IJavaProject}.
	 * 
	 * @return {@link IJavaProject}.
	 */
	public IJavaProject getJavaProject() {
		return this.javaProject;
	}

	/**
	 * Obtains the {@link ClassLoader} for the {@link IJavaProject}.
	 * 
	 * @return {@link ClassLoader} for the {@link IJavaProject}.
	 * @throws Exception
	 *             If fails to extract class path from {@link IJavaProject}.
	 */
	public ClassLoader getClassLoader() throws Exception {

		// Lazy load
		if (this.classLoader == null) {

			// Obtain the class path for the project
			IClasspathEntry[] entries = this.javaProject.getResolvedClasspath(true);
			URL[] urls = new URL[entries.length];
			for (int i = 0; i < entries.length; i++) {
				urls[i] = entries[i].getPath().toFile().toURI().toURL();
			}

			// Create the class loader
			this.classLoader = new URLClassLoader(urls);
		}

		// Return the class loader
		return this.classLoader;
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 * @throws Exception
	 *             If fails to extract class path from {@link IJavaProject}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() throws Exception {

		// Lazy load
		if (this.compiler == null) {

			// Obtain the class loader
			ClassLoader classLoader = this.getClassLoader();

			// Create the OfficeFloor compiler
			this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		}

		// Return the compiler
		return this.compiler;
	}

}