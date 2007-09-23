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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.eclipse.common.persistence.FileConfigurationItem;
import net.officefloor.repository.ConfigurationContext;

import org.eclipse.ui.IEditorPart;

/**
 * {@link java.lang.ClassLoader} to load classes from a
 * {@link org.eclipse.core.resources.IProject}.
 * 
 * @author Daniel
 */
public class ProjectClassLoader extends URLClassLoader {

	/**
	 * Convience method to create the {@link ProjectClassLoader} from the input
	 * {@link IEditorPart}.
	 * 
	 * @param editor
	 *            {@link IEditorPart}.
	 * @return {@link ProjectClassLoader}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to create {@link ProjectClassLoader}.
	 */
	public static ProjectClassLoader create(IEditorPart editor)
			throws OfficeFloorPluginFailure {

		// Obtain the configuration context
		ConfigurationContext context = new FileConfigurationItem(editor
				.getEditorInput()).getContext();

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

		// Parent class loader is from office floor plugin
		ClassLoader parentClassLoader = OfficeFloorPlugin.class.getClassLoader();

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
}