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
package net.officefloor.eclipse.configuration.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import net.officefloor.configuration.ConfigurationError;
import net.officefloor.configuration.WritableConfigurationContext;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.AbstractWritableConfigurationContext;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

/**
 * Implementation of {@link WritableConfigurationContext} for a {@link IProject}
 * providing context for a {@link IFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProjectConfigurationContext extends AbstractWritableConfigurationContext {

	/**
	 * {@link IProject} specifying context.
	 */
	private final IProject project;

	/**
	 * {@link IProgressMonitor}.
	 */
	private final IProgressMonitor monitor;

	/**
	 * Obtains the {@link WritableConfigurationItem} for the
	 * {@link IEditorInput}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 * @return {@link WritableConfigurationItem}.
	 */
	public static WritableConfigurationItem getWritableConfigurationItem(IEditorInput editorInput,
			IProgressMonitor monitor) {

		// Obtain the file and its project
		IFile file = getFile(editorInput);
		IProject project = getProject(file);

		// Create the project configuration context
		ProjectConfigurationContext context = new ProjectConfigurationContext(project, monitor);

		// Return the file configuration contxt for the editor input
		return new IFileWritableConfigurationItem(file, context);
	}

	/**
	 * Obtains the {@link IProject} from the input {@link IEditorInput}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @return {@link IProject} for the input {@link IEditorInput}.
	 */
	public static IProject getProject(IEditorInput editorInput) {
		return getFile(editorInput).getProject();
	}

	/**
	 * Obtains the {@link IProject} for the {@link IFile}.
	 * 
	 * @param file
	 *            {@link IFile}.
	 * @return {@link IProject}.
	 */
	public static IProject getProject(IFile file) {
		return file.getProject();
	}

	/**
	 * Convenience method to obtain the {@link IProject} for the
	 * {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return {@link IProject}.
	 */
	public static IProject getProject(AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		return getFile(editPart).getProject();
	}

	/**
	 * Obtains the {@link IFile} from the input {@link IEditorInput}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @return {@link IFile} for the input {@link IEditorInput}.
	 */
	public static IFile getFile(IEditorInput editorInput) {
		return ((IFileEditorInput) editorInput).getFile();
	}

	/**
	 * Convenience method to obtain the {@link IFile} for the
	 * {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return {@link IFile}.
	 */
	public static IFile getFile(AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		return getFile(editPart.getEditor().getEditorInput());
	}

	/**
	 * Ensures the {@link IFolder} exists.
	 * 
	 * @param folder
	 *            {@link IFolder}.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 * @throws CoreException
	 *             If fails to ensure {@link IFolder} exists.
	 */
	private static void ensureFolderExists(IContainer folder, IProgressMonitor monitor) throws CoreException {
		if (folder instanceof IProject) {
			return; // Assume project created
		} else if (folder.exists()) {
			return; // Folder exists
		} else {
			// Ensure parent folder exists
			ensureFolderExists(folder.getParent(), monitor);
			((IFolder) folder).create(true, true, monitor);
		}
	}

	/**
	 * Initiate with the {@link IProject} providing context.
	 * 
	 * @param project
	 *            {@link IProject} providing context.
	 */
	public ProjectConfigurationContext(IProject project) {
		this(project, null);
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 */
	public ProjectConfigurationContext(IEditorInput editorInput) {
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
	 */
	public ProjectConfigurationContext(IProject project, IProgressMonitor monitor) {
		super((location) -> {
			try {
				// Obtain the file
				IFile file = project.getFile(Path.fromPortableString(location));

				// Ensure the file exists
				if (!file.exists()) {
					return null; // no configuration for file not existing
				}

				// Return the input to file
				return file.getContents();

			} catch (CoreException ex) {
				throw new IOException(ex);
			}

		}, (location, isCreate, content) -> {
			try {
				// Obtain the file
				IFile file = project.getFile(Path.fromPortableString(location));

				// Ensure the parent folders exist
				ensureFolderExists(file.getParent(), monitor);

				// Determine if create or update
				if (isCreate) {
					// Ensure the file does not exists
					if (file.exists()) {
						throw new IOException("File '" + location + "' can not be created as already exists");
					}

					// Create the file
					file.create(content, false, monitor);

				} else {

					// Ensure the file does exists
					if (!file.exists()) {
						throw new IOException("File '" + location + "' can not be updated as not exist");
					}

					// Update the file
					file.setContents(content, false, true, monitor);
				}
			} catch (CoreException ex) {
				throw new IOException(ex);
			}

		}, (location) -> {
			try {
				// Obtain the file
				IFile file = project.getFile(Path.fromPortableString(location));

				// Do nothing if file not exists
				if (!(file.exists())) {
					return;
				}

				// Delete the file
				file.delete(true, monitor);
			} catch (CoreException ex) {
				throw new IOException(ex);
			}

		}, null);

		// Store state
		this.project = project;
		this.monitor = monitor;
	}

	/**
	 * Obtain the {@link IProject}.
	 * 
	 * @return {@link IProject}.
	 */
	public IProject getProject() {
		return this.project;
	}

	/**
	 * {@link WritableConfigurationItem} for the {@link IFile}.
	 */
	private static class IFileWritableConfigurationItem implements WritableConfigurationItem {

		/**
		 * {@link IFile}.
		 */
		private final IFile file;

		/**
		 * {@link WritableConfigurationContext}.
		 */
		private final ProjectConfigurationContext context;

		/**
		 * Instantiate.
		 * 
		 * @param file
		 *            {@link IFile}.
		 * @param context
		 *            {@link ProjectConfigurationContext}.
		 */
		public IFileWritableConfigurationItem(IFile file, ProjectConfigurationContext context) {
			this.file = file;
			this.context = context;
		}

		/*
		 * ===================== WritableConfigurationItem =====================
		 */

		@Override
		public InputStream getInputStream() throws ConfigurationError {
			try {
				return this.file.getContents();
			} catch (CoreException ex) {
				throw new ConfigurationError(file.getFullPath().toOSString());
			}
		}

		@Override
		public Reader getReader() throws ConfigurationError {
			return new InputStreamReader(this.getInputStream());
		}

		@Override
		public WritableConfigurationContext getContext() {
			return this.context;
		}

		@Override
		public InputStream getRawConfiguration() throws IOException {
			try {
				return this.file.getContents();
			} catch (CoreException ex) {
				throw new IOException(ex);
			}
		}

		@Override
		public void setConfiguration(InputStream contents) throws IOException {
			try {
				this.file.setContents(contents, false, true, this.context.monitor);
			} catch (CoreException ex) {
				throw new IOException(ex);
			}
		}
	}

}