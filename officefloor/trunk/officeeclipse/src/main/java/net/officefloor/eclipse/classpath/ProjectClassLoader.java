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
package net.officefloor.eclipse.classpath;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;

/**
 * {@link java.lang.ClassLoader} to load classes from a
 * {@link org.eclipse.core.resources.IProject}.
 * 
 * @author Daniel
 */
public class ProjectClassLoader extends URLClassLoader {

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
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IEditorPart editorPart)
			throws OfficeFloorPluginFailure {

		// Obtain the configuration context
		ConfigurationContext context = new ProjectConfigurationContext(
				editorPart.getEditorInput());

		// Return the class loader
		return create(context);
	}

	/**
	 * Convenience method to create the {@link ProjectClassLoader} from an
	 * {@link IProject}.
	 * 
	 * @param project
	 *            {@link IProject}.
	 * @return {@link ProjectClassLoader}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IProject project)
			throws OfficeFloorPluginFailure {

		// Obtain the configuration context
		ConfigurationContext context = new ProjectConfigurationContext(project);

		// Return the class loader
		return create(context);
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
		return create(context.getId(), context.getClasspath());
	}

	/**
	 * Initiates from the specified class path.
	 * 
	 * @param id
	 *            Id of the {@link ProjectClassLoader}.
	 * @param classpath
	 *            Class path.
	 * @return {@link ProjectClassLoader}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(String id, String[] classpath)
			throws OfficeFloorPluginFailure {

		// Parent class loader is from office floor plugin
		ClassLoader parentClassLoader = OfficeFloorPlugin.class
				.getClassLoader();

		// Create the list of URLs
		List<URL> urls = new ArrayList<URL>();
		for (String path : classpath) {
			urls.add(createUrl(path));
		}

		// Return the created class loader
		return new ProjectClassLoader(urls.toArray(new URL[0]),
				parentClassLoader, id);
	}

	/**
	 * Ensure creates via static
	 * {@link #create(ConfigurationContext, ClassLoader)} method.
	 * 
	 * @param urls
	 *            URLs.
	 * @param parent
	 *            Parent {@link java.lang.ClassLoader}.
	 * @param contextId
	 *            Context Id.
	 */
	private ProjectClassLoader(URL[] urls, ClassLoader parent, String contextId) {
		super(urls, parent);
		this.configurationContext = new ClassPathConfigurationContext(
				contextId, this);
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
	 *            {@link ConfigurationContext} for the {@link ConfigurationItem}.
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
	private static class InputStreamConfigurationItem implements
			ConfigurationItem {

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

	/**
	 * Class path {@link ConfigurationContext}.
	 */
	private static class ClassPathConfigurationContext implements
			ConfigurationContext {

		/**
		 * Id of this {@link ConfigurationContext}.
		 */
		private final String id;

		/**
		 * {@link ProjectClassLoader}.
		 */
		private final ProjectClassLoader classLoader;

		/**
		 * Initiate.
		 * 
		 * @param id
		 *            Id of the {@link ConfigurationContext}.
		 * @param classLoader
		 *            {@link ProjectClassLoader}.
		 */
		public ClassPathConfigurationContext(String id,
				ProjectClassLoader classLoader) {
			this.id = this.getClass().getSimpleName() + "-" + id;
			this.classLoader = classLoader;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationContext#getId()
		 */
		@Override
		public String getId() {
			return this.id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationContext#getClasspath()
		 */
		@Override
		public String[] getClasspath() {

			// Obtain the classpath as strings
			URL[] urls = this.classLoader.getURLs();
			String[] classpath = new String[urls.length];
			for (int i = 0; i < classpath.length; i++) {
				classpath[i] = urls[i].toExternalForm();
			}

			// Return the classpath
			return classpath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationContext#getConfigurationItem(java.lang.String)
		 */
		@Override
		public ConfigurationItem getConfigurationItem(String id)
				throws Exception {
			// Return the found configuration item
			return this.classLoader.findConfigurationItem(id, this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.repository.ConfigurationContext#createConfigurationItem(java.lang.String,
		 *      java.io.InputStream)
		 */
		@Override
		public ConfigurationItem createConfigurationItem(String id,
				InputStream configuration) throws Exception {
			throw new UnsupportedOperationException(
					"May not create a resource on the classpath");
		}
	}

}