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
package net.officefloor.eclipse.common.persistence;

import java.io.InputStream;

import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * Implementation of {@link net.officefloor.model.persistence.ConfigurationItem}
 * for a {@link org.eclipse.core.resources.IFile}.
 * 
 * @author Daniel
 */
public class FileConfigurationItem implements ConfigurationItem {

	/**
	 * {@link IFile} containing the configuration.
	 */
	protected final IFile file;

	/**
	 * Progress monitor.
	 */
	protected final IProgressMonitor monitor;

	/**
	 * Obtains the {@link IFile} from the input {@link IEditorInput}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput}.
	 * @return {@link IFile} for the input {@link IEditorInput}.
	 */
	static IFile getFile(IEditorInput editorInput) {
		return ((IFileEditorInput) editorInput).getFile();
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
	 * Convenience constructor for use by {@link org.eclipse.ui.IEditorPart}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput} for the
	 *            {@link org.eclipse.ui.IEditorPart}.
	 */
	public FileConfigurationItem(IEditorInput editorInput) {
		this(editorInput, null);
	}

	/**
	 * Convenience constructor for use by {@link org.eclipse.ui.IEditorPart}.
	 * 
	 * @param editorInput
	 *            {@link IEditorInput} for the
	 *            {@link org.eclipse.ui.IEditorPart}.
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
		// Store state
		this.file = file;
		this.monitor = monitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.persistence.ConfigurationItem#getId()
	 */
	public String getId() {
		return this.file.getProjectRelativePath().toPortableString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.persistence.ConfigurationItem#getConfiguration()
	 */
	public InputStream getConfiguration() throws Exception {
		return this.file.getContents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.persistence.ConfigurationItem#setConfiguration(java.io.InputStream)
	 */
	public void setConfiguration(InputStream configuration) throws Exception {
		this.file.setContents(configuration, true, true, this.monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.persistence.ConfigurationItem#getContext()
	 */
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
