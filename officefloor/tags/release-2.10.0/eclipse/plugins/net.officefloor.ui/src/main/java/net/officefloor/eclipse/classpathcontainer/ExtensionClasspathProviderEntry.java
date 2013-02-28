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
package net.officefloor.eclipse.classpathcontainer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;

import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Entry for an {@link ExtensionClasspathProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtensionClasspathProviderEntry {

	/**
	 * {@link ExtensionClasspathProvider} class name.
	 */
	private String extensionClassName;

	/**
	 * Default constructor for retrieving.
	 */
	public ExtensionClasspathProviderEntry() {
	}

	/**
	 * Initiate.
	 * 
	 * @param extensionClassName
	 *            {@link ExtensionClasspathProvider} class name.
	 */
	public ExtensionClasspathProviderEntry(String extensionClassName) {
		this.extensionClassName = extensionClassName;
	}

	/**
	 * Obtains the {@link ExtensionClasspathProvider} class name.
	 * 
	 * @return {@link ExtensionClasspathProvider} class name or
	 *         <code>null</code> if not applicable.
	 */
	public String getExtensionClassName() {
		return this.extensionClassName;
	}

	/**
	 * Specifies the {@link ExtensionClasspathProvider} class name.
	 * 
	 * @param extensionClassName
	 *            {@link ExtensionClasspathProvider} class name.
	 */
	public void setExtensionClassName(String extensionClassName) {
		this.extensionClassName = extensionClassName;
	}

	/**
	 * Obtains the {@link IClasspathEntry} instances for the
	 * {@link ExtensionClasspathProvider} of this
	 * {@link ExtensionClasspathProviderEntry}.
	 * 
	 * @param providers
	 *            {@link ExtensionClasspathProvider} instances by their class
	 *            names.
	 * @param container
	 *            {@link OfficeFloorClasspathContainer}.
	 * @return {@link IClasspathEntry} instances for the
	 *         {@link ExtensionClasspathProvider} of this
	 *         {@link ExtensionClasspathProviderEntry}.
	 */
	public IClasspathEntry[] getClasspathEntries(
			Map<String, ExtensionClasspathProvider> providers,
			OfficeFloorClasspathContainer container) {

		// Create the listing
		List<IClasspathEntry> entries = new LinkedList<IClasspathEntry>();

		// Obtain the extension class path provider
		ExtensionClasspathProvider provider = providers
				.get(this.extensionClassName);
		if (provider != null) {
			// Obtain the class path entries for the provisions
			for (ClasspathProvision provision : provider
					.getClasspathProvisions()) {
				// Obtain the class path entry
				IClasspathEntry classpathEntry = ClasspathUtil
						.createClasspathEntry(provision, container);
				if (classpathEntry != null) {
					// Have class path entry so register
					entries.add(classpathEntry);
				}
			}
		}

		// Return the class path entries
		return entries.toArray(new IClasspathEntry[0]);
	}

}