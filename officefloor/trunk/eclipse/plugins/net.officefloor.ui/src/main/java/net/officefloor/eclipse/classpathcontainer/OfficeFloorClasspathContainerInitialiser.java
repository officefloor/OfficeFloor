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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * {@link ClasspathContainerInitializer} for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorClasspathContainerInitialiser extends
		ClasspathContainerInitializer {

	/**
	 * Name of file containing the {@link OfficeFloorClasspathContainer}
	 * configuration.
	 */
	public static final String CONFIGURATION_FILE_NAME = ".officefloor";

	/**
	 * {@link IPath} for the {@link OfficeFloorClasspathContainer}.
	 */
	public static final IPath OFFICE_FLOOR_CLASSPATH_CONTAINER_PATH = new Path(
			OfficeFloorClasspathContainer.CONTAINER_ID);

	/**
	 * Ensures the {@link OfficeFloorClasspathContainer} is on the
	 * {@link IJavaProject} class path.
	 * 
	 * @param project
	 *            {@link IJavaProject} to check for the
	 *            {@link OfficeFloorClasspathContainer}.
	 * @param monitor
	 *            {@link IProgressMonitor}. May be <code>null</code>.
	 * @throws CoreException
	 *             If fails in checking or adding the
	 *             {@link OfficeFloorClasspathContainer} to the
	 *             {@link IJavaProject}.
	 */
	public static void ensureOfficeFloorClasspathContainerOnProject(
			IJavaProject project, IProgressMonitor monitor)
			throws CoreException {

		// Ensure the OfficeFloor class path container is on project
		IClasspathEntry[] rawClasspaths = project.getRawClasspath();
		for (IClasspathEntry rawClasspath : rawClasspaths) {
			if ((rawClasspath.getEntryKind() == IClasspathEntry.CPE_CONTAINER)
					&& (OFFICE_FLOOR_CLASSPATH_CONTAINER_PATH
							.equals(rawClasspath.getPath()))) {
				// Already on the class path for the project
				return;
			}
		}

		// As here not on class path, so add to class path of project
		IClasspathEntry officeFloorEntry = JavaCore
				.newContainerEntry(OFFICE_FLOOR_CLASSPATH_CONTAINER_PATH);
		List<IClasspathEntry> rawEntries = new ArrayList<IClasspathEntry>(
				rawClasspaths.length + 1);
		rawEntries.addAll(Arrays.asList(rawClasspaths));
		rawEntries.add(officeFloorEntry);
		project.setRawClasspath(rawEntries.toArray(new IClasspathEntry[0]),
				monitor);
	}

	/**
	 * <p>
	 * Adds the {@link ExtensionClasspathProvider} entries to the
	 * {@link OfficeFloorClasspathContainer}.
	 * <p>
	 * This method also ensures the {@link OfficeFloorClasspathContainer} is
	 * added to the {@link IJavaProject}.
	 * 
	 * @param project
	 *            {@link IJavaProject} to add the
	 *            {@link ExtensionClasspathProvider} entries.
	 * @param monitor
	 *            {@link IProgressMonitor}. May be <code>null</code>.
	 * @param extensionClassNames
	 *            Listing of {@link ExtensionClasspathProvider} class names to
	 *            add.
	 * @throws CoreException
	 *             If fails to add {@link ExtensionClasspathProvider} entries.
	 */
	public static void addExtensionClasspathProvidersToOfficeFloorClassPath(
			IJavaProject project, IProgressMonitor monitor,
			String... extensionClassNames) throws CoreException {

		// Ensure the OfficeFloor container is on the class path
		ensureOfficeFloorClasspathContainerOnProject(project, monitor);

		// Obtain the container
		OfficeFloorClasspathContainer container = getOfficeFloorClasspathContainer(project);

		// Add the extension class names
		for (String extensionClassName : extensionClassNames) {
			container.addExtensionClasspathProvider(extensionClassName);
		}

		// Update the container for the project
		JavaCore.setClasspathContainer(container.getPath(),
				new IJavaProject[] { project },
				new IClasspathContainer[] { container }, monitor);

		// Store the changes to the container
		storeOfficeFloorClasspathContainer(container, project, monitor);
	}

	/**
	 * Obtains the {@link OfficeFloorClasspathContainer} for the
	 * {@link IJavaProject}.
	 * 
	 * @param project
	 *            {@link IJavaProject}.
	 * @return {@link OfficeFloorClasspathContainer} for the
	 *         {@link IJavaProject}.
	 * @throws CoreException
	 *             If fails to obtain the {@link OfficeFloorClasspathContainer}.
	 */
	private static OfficeFloorClasspathContainer getOfficeFloorClasspathContainer(
			IJavaProject project) throws CoreException {

		// Obtain the OfficeFloor container
		IClasspathContainer container = JavaCore.getClasspathContainer(
				OFFICE_FLOOR_CLASSPATH_CONTAINER_PATH, project);
		if (!(container instanceof OfficeFloorClasspathContainer)) {
			// Must have OfficeFloor class path container
			throw EclipseUtil.createCoreException(new IllegalStateException(
					"Must be able to obtain "
							+ OfficeFloorClasspathContainer.class
									.getSimpleName() + " for project "
							+ project.getProject().getName()));
		}

		// Return the OfficeFloor class path container
		return (OfficeFloorClasspathContainer) container;
	}

	/**
	 * Stores the {@link OfficeFloorClasspathContainer} on the
	 * {@link IJavaProject}.
	 * 
	 * @param container
	 *            {@link OfficeFloorClasspathContainer}.
	 * @param project
	 *            {@link IJavaProject}.
	 * @param monitor
	 *            {@link IProgressMonitor}. May be <code>null</code>.
	 * @throws CoreException
	 *             If fails to store the {@link OfficeFloorClasspathContainer}
	 *             on the {@link IJavaProject}.
	 */
	private static void storeOfficeFloorClasspathContainer(
			OfficeFloorClasspathContainer container, IJavaProject project,
			IProgressMonitor monitor) throws CoreException {
		try {
			// Obtain the configuration file
			IFile configurationFile = project.getProject().getFile(
					CONFIGURATION_FILE_NAME);

			// Store the content to configuration file
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			container.store(buffer);
			InputStream contents = new ByteArrayInputStream(buffer
					.toByteArray());
			if (configurationFile.exists()) {
				// Exists so overwrite file's content
				configurationFile.setContents(contents, true, false, monitor);
			} else {
				// Not exists so create
				configurationFile.create(contents, true, monitor);
			}

		} catch (Exception ex) {
			throw EclipseUtil.createCoreException(ex);
		}
	}

	/*
	 * ============= ClasspathContainerInitializer ==================
	 */

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		try {

			// Create the OfficeFloor container
			OfficeFloorClasspathContainer container = new OfficeFloorClasspathContainer(
					containerPath);

			// Obtain the OfficeFloor configuration
			IFile configurationFile = project.getProject().getFile(
					CONFIGURATION_FILE_NAME);
			if (configurationFile.exists()) {
				// Configuration exists so load
				container.load(configurationFile.getContents());
			} else {
				// Configuration not exist, so create
				storeOfficeFloorClasspathContainer(container, project, null);
			}

			// Specify the OfficeFloor container for the project
			JavaCore.setClasspathContainer(containerPath,
					new IJavaProject[] { project },
					new IClasspathContainer[] { container }, null);

		} catch (Exception ex) {
			throw EclipseUtil.createCoreException(ex);
		}
	}

	@Override
	public boolean canUpdateClasspathContainer(IPath containerPath,
			IJavaProject project) {
		// Can update the OfficeFloor class path container
		return (OfficeFloorClasspathContainer.CONTAINER_ID.equals(containerPath
				.segment(0)));
	}

	@Override
	public void requestClasspathContainerUpdate(IPath containerPath,
			IJavaProject project, IClasspathContainer containerSuggestion)
			throws CoreException {

		// Update the OfficeFloor container state
		OfficeFloorClasspathContainer container = getOfficeFloorClasspathContainer(project);
		container.updateState(containerSuggestion);
		storeOfficeFloorClasspathContainer(container, project, null);

		// Update the OfficeFloor container for the project
		JavaCore.setClasspathContainer(container.getPath(),
				new IJavaProject[] { project },
				new IClasspathContainer[] { containerSuggestion }, null);
	}

}