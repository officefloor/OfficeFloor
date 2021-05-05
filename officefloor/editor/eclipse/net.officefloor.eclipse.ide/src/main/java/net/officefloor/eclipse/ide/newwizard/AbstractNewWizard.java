/*-
 * #%L
 * [bundle] OfficeFloor Eclipse IDE
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.eclipse.ide.newwizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.officefloor.eclipse.bridge.EclipseEnvironmentBridge;
import net.officefloor.gef.bridge.EnvironmentBridge;
import net.officefloor.gef.ide.editor.AbstractAdaptedIdeEditor;
import net.officefloor.model.Model;

/**
 * New {@link Model} {@link Wizard}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractNewWizard<R extends Model> extends Wizard implements INewWizard {

	/**
	 * Title.
	 */
	private final String title;

	/**
	 * Description.
	 */
	private final String description;

	/**
	 * Initial file name.
	 */
	private final String initialFileName;

	/**
	 * Extension.
	 */
	private final String extension;

	/**
	 * Contents for the file.
	 */
	private final String itemFileContents;

	/**
	 * {@link NewWizardPage}.
	 */
	private NewWizardPage page = null;

	/**
	 * Initiate.
	 */
	public AbstractNewWizard() {
		AbstractAdaptedIdeEditor<R, ?, ?> editor = this.createEditor(new EclipseEnvironmentBridge());
		this.title = editor.getClass().getSimpleName();
		this.description = "Create the " + this.title;
		this.initialFileName = editor.fileName();

		// Obtain the extension
		if ((this.initialFileName == null) || (this.initialFileName.trim().length() == 0)) {
			throw new IllegalStateException(editor.getClass().getName() + " not providing file name");
		}
		int extensionIndex = this.initialFileName.lastIndexOf('.');
		if (extensionIndex < 0) {
			throw new IllegalStateException(editor.getClass().getName() + " provided file does not have an extension");
		}
		this.extension = this.initialFileName.substring(extensionIndex + ".".length());

		try {
			// Generate the initial file contents
			this.itemFileContents = editor.newFileContent();
		} catch (Exception ex) {
			// Handle exception
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			} else {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Creates the {@link AbstractAdaptedIdeEditor}.
	 * 
	 * @param envBridge {@link EnvironmentBridge}.
	 * @return {@link AbstractAdaptedIdeEditor}.
	 */
	protected abstract AbstractAdaptedIdeEditor<R, ?, ?> createEditor(EnvironmentBridge envBridge);

	/*
	 * ================== IWorkbenchWizard =======================
	 */

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.page = new NewWizardPage(selection, this.title, this.title, this.description, this.initialFileName,
				this.extension);
	}

	/*
	 * ================== Wizard =================================
	 */

	@Override
	public void addPages() {
		this.addPage(this.page);
	}

	@Override
	public boolean performFinish() {

		// Obtain details
		final IResource container = this.page.getItemContainer();
		final String itemName = this.page.getItemFullName();

		// Create runnable to create item
		IRunnableWithProgress op = (IProgressMonitor monitor) -> {
			try {
				AbstractNewWizard.this.doFinish(container, itemName, monitor);
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			} finally {
				monitor.done();
			}
		};

		// Create the item
		try {
			this.getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(this.getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Creates the item.
	 * 
	 * @param resource {@link IResource} of the container to create the item.
	 * @param itemName Name of the item.
	 * @param monitor  {@link IProgressMonitor}.
	 */
	private void doFinish(IResource resource, String itemName, IProgressMonitor monitor) throws Exception {

		// Indicate progress
		monitor.beginTask("Creating " + itemName, 2);

		// Obtain the Container
		if ((!resource.exists()) || (!(resource instanceof IContainer))) {
			MessageDialog.openError(this.getShell(), "Error", "Container \"" + resource + "\" does not exist.");
			return; // container must exist
		}
		IContainer container = (IContainer) resource;

		// Create the file for the item
		monitor.setTaskName("Creating item file ...");
		final IFile file = container.getFile(new Path(itemName));
		InputStream stream = new ByteArrayInputStream(this.itemFileContents.getBytes());
		if (file.exists()) {
			file.setContents(stream, true, true, monitor);
		} else {
			file.create(stream, true, monitor);
		}
		stream.close();

		// Open the file for editing
		monitor.setTaskName("Opening file for editing ...");
		this.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

}
