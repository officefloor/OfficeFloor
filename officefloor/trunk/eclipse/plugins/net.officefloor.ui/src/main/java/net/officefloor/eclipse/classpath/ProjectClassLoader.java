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
package net.officefloor.eclipse.classpath;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.repository.project.ProjectConfigurationContext;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IEditorPart;

/**
 * {@link java.lang.ClassLoader} to load classes from a
 * {@link org.eclipse.core.resources.IProject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProjectClassLoader extends URLClassLoader {

	/**
	 * Default {@link ClassLoader}.
	 */
	public static final ClassLoader DEFAULT_CLASS_LOADER = OfficeFloorPlugin.class
			.getClassLoader();

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * Convenience method to find a {@link ConfigurationItem} on the class path
	 * of the project of the input {@link IEditorPart}.
	 * 
	 * @param editorPart
	 *            {@link IEditorPart}.
	 * @param path
	 *            Path to {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem} or <code>null</code> if not found.
	 */
	public static ConfigurationItem findConfigurationItem(
			IEditorPart editorPart, String path) {

		// Create the class loader
		ProjectClassLoader classLoader = create(editorPart);

		// Return the configuration item
		return classLoader.findConfigurationItem(path);
	}

	/**
	 * Convenience method to create the {@link ProjectClassLoader} from the
	 * input {@link IEditorPart}.
	 * 
	 * @param editorPart
	 *            {@link IEditorPart}.
	 * @return {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IEditorPart editorPart) {

		// Obtain the project
		IProject project = ProjectConfigurationContext.getProject(editorPart
				.getEditorInput());

		// Return the class loader
		return create(project);
	}

	/**
	 * Convenience method to create the {@link ProjectClassLoader} from an
	 * {@link IProject}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IProject project) {
		return create(project, null);
	}

	/**
	 * Initiates from the {@link IProject} with the parent {@link ClassLoader}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @param parentClassLoader
	 *            Parent {@link ClassLoader}.
	 * @return {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IProject project,
			ClassLoader parentClassLoader) {

		// Compute the Project's class path
		String[] computedClassPath;
		try {
			// Create the Java Project
			IJavaProject javaProject = JavaCore.create(project);

			// Obtain the class path
			computedClassPath = JavaRuntime
					.computeDefaultRuntimeClassPath(javaProject);

		} catch (Throwable ex) {
			LogUtil.logError("Failed to compute class path for project", ex);
			computedClassPath = new String[0]; // provide empty class path
		}

		// Return the class loader
		return create(computedClassPath, parentClassLoader);
	}

	/**
	 * Initiates from the specified class path.
	 * 
	 * @param classpath
	 *            Class path.
	 * @param parentClassLoader
	 *            Parent {@link ClassLoader}.
	 * @return {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(String[] classpath,
			ClassLoader parentClassLoader) {

		// Create the list of URLs
		List<URL> urls = new ArrayList<URL>();
		for (String path : classpath) {

			// Obain the URL
			URL url = createUrl(path);
			if (url == null) {
				continue; // must create URL
			}

			// Add the URL
			urls.add(url);
		}

		// Return the created class loader
		return new ProjectClassLoader(urls.toArray(new URL[0]),
				parentClassLoader);
	}

	/**
	 * Ensure creates via static
	 * {@link #create(ConfigurationContext, ClassLoader)} method.
	 * 
	 * @param urls
	 *            URLs.
	 * @param parent
	 *            Parent {@link java.lang.ClassLoader}.
	 */
	private ProjectClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.configurationContext = new ClassLoaderConfigurationContext(this);
	}

	/**
	 * Obtains the {@link ConfigurationContext}.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	/**
	 * Finds the {@link ConfigurationItem}.
	 * 
	 * @param path
	 *            Path of the {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem} or <code>null</code> if not found.
	 */
	public ConfigurationItem findConfigurationItem(String path) {
		return this.findConfigurationItem(path, this.configurationContext);
	}

	/**
	 * Finds the {@link ConfigurationItem} and uses the input
	 * {@link ConfigurationContext}.
	 * 
	 * @param path
	 *            Path of the {@link ConfigurationItem}.
	 * @param context
	 *            {@link ConfigurationContext} for the {@link ConfigurationItem}
	 *            .
	 * @return {@link ConfigurationItem} or <code>null</code> if not found.
	 */
	private ConfigurationItem findConfigurationItem(String path,
			ConfigurationContext context) {

		// Obtain contents of resource at path
		InputStream resource = this.getResourceAsStream(path);
		if (resource == null) {
			// Not found
			return null;
		}

		// Create and return the Configuration Item
		return new InputStreamConfigurationItem(path, context, resource);
	}

	/**
	 * Creates the {@link URL} from the path.
	 * 
	 * @param path
	 *            Path to create as a {@link URL}.
	 * @return {@link URL} of the path or <code>null</code> if fails to create
	 *         the {@link URL}.
	 */
	protected static URL createUrl(String path) {
		try {
			if (path.endsWith(".jar") || (path.endsWith(".zip"))) {
				return new URL("file", null, path);
			} else {
				return new URL("file", null, path + "/");
			}
		} catch (MalformedURLException ex) {
			// Indicate error in adding URL
			LogUtil.logError("Failed to create URL from path '" + path
					+ "' as " + ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * {@link ConfigurationItem} for the {@link InputStream} of the resource.
	 */
	private static class InputStreamConfigurationItem implements
			ConfigurationItem {

		/**
		 * Location to the resource.
		 */
		private final String location;

		/**
		 * {@link ConfigurationContext}.
		 */
		private final ConfigurationContext context;

		/**
		 * {@link InputStream} to the resource.
		 */
		private final InputStream resource;

		/**
		 * Initiate.
		 * 
		 * @param path
		 *            Path to the resource.
		 * @param context
		 *            {@link ConfigurationContext}.
		 * @param resource
		 *            {@link InputStream} to the resource.
		 */
		public InputStreamConfigurationItem(String location,
				ConfigurationContext context, InputStream resource) {
			this.location = location;
			this.context = context;
			this.resource = resource;
		}

		/*
		 * ================= ConfigurationItem ============================
		 */

		@Override
		public String getLocation() {
			return this.location;
		}

		@Override
		public ConfigurationContext getContext() {
			return this.context;
		}

		@Override
		public InputStream getConfiguration() throws Exception {
			return this.resource;
		}

		@Override
		public void setConfiguration(InputStream configuration)
				throws Exception {
			throw new UnsupportedOperationException(
					"Classpath resource may not be overridden");
		}
	}

}