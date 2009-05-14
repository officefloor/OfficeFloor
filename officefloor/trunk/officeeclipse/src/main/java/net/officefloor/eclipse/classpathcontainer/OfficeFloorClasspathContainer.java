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
package net.officefloor.eclipse.classpathcontainer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ClasspathProvision;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.main.OfficeFloorMain;
import net.officefloor.model.Model;
import net.officefloor.plugin.xml.XmlUnmarshaller;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * {@link IClasspathContainer} for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorClasspathContainer implements IClasspathContainer {

	/**
	 * {@link IClasspathContainer} path Id.
	 */
	public static String CONTAINER_ID = "net.officefloor.eclipse.OFFICE_FLOOR";

	/**
	 * Listing of {@link Class} instances with one per framework jar.
	 */
	private static final Class<?>[] frameworkClasses = new Class<?>[] {
			OfficeFrame.class, OfficeFloorMain.class, XmlUnmarshaller.class,
			Model.class };

	/**
	 * Ensures the {@link OfficeFloor} {@link IClasspathEntry} entries are
	 * available on the {@link IJavaProject} including the
	 * {@link IClasspathEntry} instances of the extensions.
	 * 
	 * @param javaProject
	 *            {@link IJavaProject}.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 * @param extensionClassNames
	 *            Class names of the extensions that may be
	 *            {@link ExtensionClasspathProvider} to contribute to the class
	 *            path.
	 * @throws Exception
	 *             If fails to adjust class path for extensions.
	 */
	public static void addExtensionToProjectClasspath(IJavaProject javaProject,
			IProgressMonitor monitor, String... extensionClassNames)
			throws Exception {

		// Determine if the class path entry is on project
		IClasspathEntry[] entries = javaProject.getRawClasspath();
		int officeFloorEntryIndex = -1;
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];

			// Ensure entry is a container
			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {

				// Obtain the container ID of the entry
				IPath containerPath = entry.getPath();
				String containerId = containerPath.segment(0);

				// Determine if the Office Floor container
				if (OfficeFloorClasspathContainer.CONTAINER_ID
						.equals(containerId)) {
					// Found the office floor entry
					officeFloorEntryIndex = i;
				}
			}
		}

		// Flag if class path dirty and requires changes
		boolean isOfficeFloorClasspathDirty = false;

		// Obtain office floor entry (may required creating if not available)
		IClasspathEntry officeFloorEntry;
		if (officeFloorEntryIndex >= 0) {
			// Already existing
			officeFloorEntry = entries[officeFloorEntryIndex];
		} else {
			// Add the entry (and flag class path dirty)
			officeFloorEntry = JavaCore.newContainerEntry(new Path(
					OfficeFloorClasspathContainer.CONTAINER_ID));
			isOfficeFloorClasspathDirty = true;
		}

		// Extract the extension class names from the path
		List<String> entryClassNames = new LinkedList<String>();
		String[] pathSegments = officeFloorEntry.getPath().segments();
		for (int i = 1; i < pathSegments.length; i++) {
			String pathSegment = pathSegments[i];
			entryClassNames.add(pathSegment);
		}

		// Determine if need to include further entries
		for (String extensionClassName : extensionClassNames) {
			if (!entryClassNames.contains(extensionClassName)) {
				// Missing extension entry, so add and flag class path dirty
				entryClassNames.add(extensionClassName);
				isOfficeFloorClasspathDirty = true;
			}
		}

		// Make change to the class path if dirty
		if (isOfficeFloorClasspathDirty) {

			// Create the office floor entry path
			IPath officeFloorPath = new Path(
					OfficeFloorClasspathContainer.CONTAINER_ID);
			for (String entryClassName : entryClassNames) {
				officeFloorPath = officeFloorPath.append(entryClassName);
			}

			// Create the adjusted office floor entry
			officeFloorEntry = JavaCore.newContainerEntry(officeFloorPath);

			// Adjust the project's class path
			IClasspathEntry[] newEntries;
			if (officeFloorEntryIndex >= 0) {
				// Overwrite office floor entry
				newEntries = entries;
				newEntries[officeFloorEntryIndex] = officeFloorEntry;
			} else {
				// Append the office floor class path container
				newEntries = new IClasspathEntry[entries.length + 1];
				for (int i = 0; i < entries.length; i++) {
					newEntries[i] = entries[i];
				}
				newEntries[newEntries.length - 1] = officeFloorEntry;
			}

			// Register the new class path entries for project
			javaProject.setRawClasspath(newEntries, monitor);
		}
	}

	/**
	 * {@link IPath}.
	 */
	private final IPath path;

	/**
	 * Listing of the {@link IClasspathEntry} instances.
	 */
	private List<IClasspathEntry> classPaths = new LinkedList<IClasspathEntry>();

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            {@link IPath}.
	 * @param javaProject
	 *            {@link IJavaProject}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public OfficeFloorClasspathContainer(IPath path, IJavaProject javaProject)
			throws Exception {
		this.path = path;

		// Ensure the framework class paths available
		for (Class<?> frameworkClass : frameworkClasses) {
			this.classPaths.add(ClasspathUtil
					.createClasspathEntry(frameworkClass));
		}

		// Add the extension class paths (skipping container id)
		List<String> classNames = new LinkedList<String>();
		String[] pathSegments = path.segments();
		for (int i = 1; i < pathSegments.length; i++) {
			classNames.add(pathSegments[i]);
		}
		this.addExtensionClasspaths(classNames.toArray(new String[0]));
	}

	/**
	 * <p>
	 * Adds the {@link IClasspathEntry} instances for the names of the extension
	 * classes.
	 * <p>
	 * Note: that this method looks up extensions and extracts the class paths
	 * from them. Should the extension not be found, then its class paths will
	 * not be added.
	 * 
	 * @param extensionClassNames
	 *            Names of the classes for which there may be extensions.
	 * @throws Exception
	 *             If fails to add extension class paths.
	 */
	public void addExtensionClasspaths(String... extensionClassNames)
			throws Exception {

		// Obtain the extension to class provider map
		Map<String, ExtensionClasspathProvider> providers = ExtensionUtil
				.createClasspathProvidersByExtensionClassNames();

		// Iterate over the class names adding the class paths
		for (String extensionClassName : extensionClassNames) {

			// Obtain the extension class path provider
			ExtensionClasspathProvider provider = providers
					.get(extensionClassName);
			if (provider != null) {

				// Add the class path values for the extension
				for (ClasspathProvision provision : provider
						.getClasspathProvisions()) {

					// Obtain the class path entry
					IClasspathEntry classpathEntry = ClasspathUtil
							.createClasspathEntry(provision);

					// Add the class path entry
					this.addClasspathEntry(classpathEntry);
				}
			}
		}
	}

	/**
	 * Adds the {@link IClasspathEntry} if necessary so as to not create
	 * duplicate {@link IClasspathEntry} instances.
	 * 
	 * @param classpathEntry
	 *            {@link IClasspathEntry}.
	 */
	public void addClasspathEntry(IClasspathEntry classpathEntry) {

		// Determine if already exists
		for (IClasspathEntry entry : this.classPaths) {
			if (entry.equals(classpathEntry)) {
				// Already available, so do not add
				return;
			}
		}

		// Not available, so add class path entry
		this.classPaths.add(classpathEntry);
	}

	/*
	 * ======================= IClasspathContainer =============================
	 */

	@Override
	public String getDescription() {
		return "Office Floor";
	}

	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return this.path;
	}

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		// Return the class path entries
		return this.classPaths.toArray(new IClasspathEntry[0]);
	}

}