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
package net.officefloor.eclipse;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.ui.IEditorPart;

/**
 * {@link java.lang.ClassLoader} to load classes from a
 * {@link org.eclipse.core.resources.IProject}.
 * 
 * @author Daniel
 */
public class ProjectClassLoader extends URLClassLoader {

	/**
	 * Convenience method to create the {@link ProjectClassLoader} from the
	 * input {@link IEditorPart}.
	 * 
	 * @param editorPart
	 *            {@link IEditorPart}.
	 * @return {@link ProjectClassLoader}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IEditorPart editorPart)
			throws OfficeFloorPluginFailure {

		// Obtain the configuration context
		ConfigurationContext context = new FileConfigurationItem(editorPart
				.getEditorInput()).getContext();

		// Return the class loader
		return create(context);
	}

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

		// Obtain contents of resource at path
		InputStream resource = classLoader.getResourceAsStream(path);
		if (resource == null) {
			// Not found
			return null;
		}

		// Create the Project Configuration Context
		ConfigurationContext context = new ProjectConfigurationContext(
				editorPart.getEditorInput());

		// Create and return the Configuration Item
		return new InputStreamConfigurationItem(path, context, resource);
	}

	/**
	 * Initiates from the {@link ConfigurationContext}.
	 * 
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @return {@link OfficeFloorClassLoader} for the input
	 *         {@link ConfigurationContext}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(ConfigurationContext context)
			throws OfficeFloorPluginFailure {

		// Parent class loader is from office floor plugin
		ClassLoader parentClassLoader = OfficeFloorPlugin.class
				.getClassLoader();

		// Create the list of URLs
		List<URL> urls = new ArrayList<URL>();
		for (String path : context.getClasspath()) {
			urls.add(createUrl(path));
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
	}

	/**
	 * Creates the {@link URL} from the path.
	 * 
	 * @param path
	 *            Path to create as a {@link URL}.
	 * @return {@link URL} of the path.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create the {@link URL}.
	 */
	protected static URL createUrl(String path) throws OfficeFloorPluginFailure {
		try {
			if (path.endsWith(".jar") || (path.endsWith(".zip"))) {
				return new URL("file", "localhost", path);
			} else {
				return new URL("file", "localhost", path + "/");
			}
		} catch (MalformedURLException ex) {
			// Propagate
			throw new OfficeFloorPluginFailure(
					"Failed to create URL from path '" + path + "' as "
							+ ex.getMessage(), ex);
		}
	}

	/**
	 * {@link ConfigurationItem} for the {@link InputStream} of the resource.
	 */
	private static class InputStreamConfigurationItem implements ConfigurationItem {

		/**
		 * Path to the resource.
		 */
		private final String path;

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
		public InputStreamConfigurationItem(String path,
				ConfigurationContext context, InputStream resource) {
			this.path = path;
			this.context = context;
			this.resource = resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationItem#getId()
		 */
		@Override
		public String getId() {
			return this.path;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationItem#getContext()
		 */
		@Override
		public ConfigurationContext getContext() {
			return this.context;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationItem#getConfiguration()
		 */
		@Override
		public InputStream getConfiguration() throws Exception {
			return this.resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationItem#setConfiguration(java.io.InputStream)
		 */
		@Override
		public void setConfiguration(InputStream configuration)
				throws Exception {
			throw new UnsupportedOperationException(
					"Classpath resource may not be overridden");
		}

	}
}