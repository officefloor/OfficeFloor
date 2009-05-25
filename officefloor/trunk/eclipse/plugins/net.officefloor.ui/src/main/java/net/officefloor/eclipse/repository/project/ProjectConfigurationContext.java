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
package net.officefloor.eclipse.repository.project;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IEditorInput;

/**
 * Implementation of {@link ConfigurationContext} for a {@link IProject}
 * providing context for a {@link IFile}.
 * 
 * @author Daniel
 */
public class ProjectConfigurationContext implements ConfigurationContext {

	/**
	 * {@link IProject} specifying context.
	 */
	private final IProject project;

	/**
	 * Class path for the {@link IProject}.
	 */
	private final String[] classpath;

	/**
	 * {@link IProgressMonitor}.
	 */
	private final IProgressMonitor monitor;

	/**
	 * Obtains the {@link IProject} from the input {@link IEditorInput}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @return {@link IProject} for the input {@link IEditorInput}.
	 */
	public static IProject getProject(IEditorInput editorInput) {
		return FileConfigurationItem.getFile(editorInput).getProject();
	}

	/**
	 * Initiate with the {@link IProject} providing context.
	 * 
	 * @param project
	 *            {@link IProject} providing context.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to initialise.
	 */
	public ProjectConfigurationContext(IProject project)
			throws OfficeFloorPluginFailure {
		this(project, null);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to construct.
	 */
	public ProjectConfigurationContext(IEditorInput editorInput)
			throws OfficeFloorPluginFailure {
		this(getProject(editorInput), null);
	}

	/**
	 * Initiate with the {@link IProject} providing context.
	 * 
	 * @param project
	 *            {@link IProject} providing context.
	 * @param monitor
	 *            {@link IProgressMonitor}. If only retrieving may specify
	 *            <code>null</code>.
	 * @throws OfficeFloorPluginFailure
	 *             If fails to initialise.
	 */
	public ProjectConfigurationContext(IProject project,
			IProgressMonitor monitor) throws OfficeFloorPluginFailure {
		this.project = project;
		this.monitor = monitor;

		try {
			// Create the Java Project
			IJavaProject javaProject = JavaCore.create(project);

			// Obtain the class path
			this.classpath = JavaRuntime
					.computeDefaultRuntimeClassPath(javaProject);

		} catch (JavaModelException ex) {
			// Propagate
			throw new OfficeFloorPluginFailure(ex);
		} catch (CoreException ex) {
			// Propagate
			throw new OfficeFloorPluginFailure(ex);
		}
	}

	/**
	 * Obtain the {@link IProject}.
	 * 
	 * @return {@link IProject}.
	 */
	public IProject getProject() {
		return this.project;
	}

	/*
	 * ===================== ConfigurationContext ======================
	 */

	@Override
	public String getLocation() {
		return this.project.getFullPath().toPortableString();
	}

	@Override
	public String[] getClasspath() {
		return this.classpath;
	}

	@Override
	public ConfigurationItem getConfigurationItem(String location)
			throws Exception {

		// Obtain the file
		IFile file = this.project.getFile(Path.fromPortableString(location));

		// Ensure the file exists
		if (!file.exists()) {
			throw new IOException("File '" + location + "' can not be found");
		}

		// Return the configuration of the file
		return new FileConfigurationItem(file, this.monitor);
	}

	@Override
	public ConfigurationItem createConfigurationItem(String location,
			InputStream configuration) throws Exception {

		// Obtain the file
		IFile file = this.project.getFile(Path.fromPortableString(location));

		// Ensure the file does not exists
		if (file.exists()) {
			throw new IOException("File '" + location
					+ "' can not be created as already exists");
		}

		// Create the file
		file.create(configuration, true, this.monitor);

		// Return the File by the Id
		return new FileConfigurationItem(file, this.monitor);
	}

}