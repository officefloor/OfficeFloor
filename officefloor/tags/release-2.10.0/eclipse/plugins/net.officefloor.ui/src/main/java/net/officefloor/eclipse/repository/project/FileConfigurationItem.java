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
package net.officefloor.eclipse.repository.project;

import java.io.InputStream;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * Implementation of {@link ConfigurationItem} for a {@link IFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileConfigurationItem implements ConfigurationItem {

	/**
	 * {@link IFile} containing the configuration.
	 */
	private final IFile file;

	/**
	 * Progress monitor.
	 */
	private final IProgressMonitor monitor;

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
	 * Convenience method to obtain the {@link IProject} for the
	 * {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return {@link IProject}.
	 */
	public static IProject getProject(
			AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		return getFile(editPart).getProject();
	}

	/**
	 * Initiate with the {@link IFile} containing the configuration.
	 * 
	 * @param file
	 *            {@link IFile} containing configuration.
	 */
	public FileConfigurationItem(IFile file) {
		this(file, null);
	}

	/**
	 * Convenience constructor for use by {@link IEditorPart}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput} for the {@link IEditorPart}.
	 */
	public FileConfigurationItem(IEditorInput editorInput) {
		this(editorInput, null);
	}

	/**
	 * Convenience constructor for use by {@link IEditorPart}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput} for the {@link IEditorPart}.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 */
	public FileConfigurationItem(IEditorInput editorInput,
			IProgressMonitor monitor) {
		this(getFile(editorInput), monitor);
	}

	/**
	 * Initiate with the {@link IFile} containing the configuration and the
	 * {@link IProgressMonitor}.
	 * 
	 * @param file
	 *            {@link IFile} containing configuration.
	 * @param monitor
	 *            {@link IProgressMonitor}.
	 */
	public FileConfigurationItem(IFile file, IProgressMonitor monitor) {
		this.file = file;
		this.monitor = monitor;
	}

	/*
	 * ================== ConfigurationItem ===================================
	 */

	@Override
	public String getLocation() {
		return this.file.getProjectRelativePath().toPortableString();
	}

	@Override
	public InputStream getConfiguration() throws Exception {
		return this.file.getContents();
	}

	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		this.file.setContents(configuration, true, true, this.monitor);
	}

	@Override
	public ConfigurationContext getContext() {
		return new ProjectConfigurationContext(this.file.getProject(),
				this.monitor);
	}

	/**
	 * Obtains the name of the underlying file.
	 * 
	 * @return Name of file.
	 */
	public String getFileName() {
		return this.file.getName();
	}

	/**
	 * Returns the underlying file.
	 * 
	 * @return Underlying file.
	 */
	public IFile getFile() {
		return this.file;
	}

}