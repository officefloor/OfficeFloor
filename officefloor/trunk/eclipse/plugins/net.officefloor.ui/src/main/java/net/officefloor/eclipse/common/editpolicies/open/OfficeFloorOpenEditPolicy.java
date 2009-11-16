/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.editpolicies.open;

import java.net.URL;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.Model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ide.IDE;

/**
 * {@link EditPolicy} to handle open requests for the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorOpenEditPolicy<M extends Model> extends
		AbstractEditPolicy {

	/**
	 * {@link OpenHandler}.
	 */
	private OpenHandler<M> handler;

	/**
	 * Allows opening the {@link Model} by specifying the {@link OpenHandler}.
	 * 
	 * @param handler
	 *            {@link OpenHandler}.
	 */
	public void allowOpening(OpenHandler<M> handler) {
		this.handler = handler;
	}

	/**
	 * Does the open for the {@link AbstractOfficeFloorEditPart}.
	 * 
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart} to open.
	 */
	public void doOpen(AbstractOfficeFloorEditPart<M, ?, ?> editPart) {

		// Obtain the model
		M model = editPart.getCastedModel();

		// Open the model
		this.handler.doOpen(new OpenCommandContextImpl(model,
				editPart));
	}

	/**
	 * {@link OpenHandlerContext} implementation.
	 */
	private class OpenCommandContextImpl implements OpenHandlerContext<M> {

		/**
		 * {@link Model}.
		 */
		private final M model;

		/**
		 * {@link AbstractOfficeFloorEditPart}.
		 */
		private final AbstractOfficeFloorEditPart<M, ?, ?> editPart;

		/**
		 * Initiate.
		 * 
		 * @param model
		 *            {@link Model}.
		 * @param editPart
		 *            {@link AbstractOfficeFloorEditPart}.
		 */
		public OpenCommandContextImpl(M model,
				AbstractOfficeFloorEditPart<M, ?, ?> editPart) {
			this.model = model;
			this.editPart = editPart;
		}

		/*
		 * ================ OpenCommandContext =========================
		 */

		@Override
		public M getModel() {
			return this.model;
		}

		@Override
		public AbstractOfficeFloorEditPart<M, ?, ?> getEditPart() {
			return this.editPart;
		}

		@Override
		public void openClasspathFile(String filePath) {

			// Obtain the editor
			AbstractOfficeFloorEditor<?, ?> editor = this.editPart.getEditor();

			try {
				// Obtain the URL with full path
				ProjectClassLoader projectClassLoader = ProjectClassLoader
						.create(editor);
				URL url = projectClassLoader.getResource(filePath);
				if (url == null) {
					// Can not find item to open
					MessageDialog.openWarning(
							editor.getEditorSite().getShell(), "Open",
							"Can not find '" + filePath + "'");
					return;
				}

				// Obtain the file to open
				String urlFilePath = url.getFile();
				IPath path = new Path(urlFilePath);
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocation(path);
				if (files.length != 1) {
					// Can not find file
					MessageDialog.openWarning(
							editor.getEditorSite().getShell(), "Open",
							"Can not find '" + filePath + "' at ["
									+ urlFilePath + "]");
					return;
				}
				IFile file = files[0];

				// Open the file
				IDE.openEditor(editor.getEditorSite().getPage(), file);

			} catch (Throwable ex) {
				// Failed to open file
				MessageDialog
						.openInformation(editor.getEditorSite().getShell(),
								"Open", "Failed to open '" + filePath + "': "
										+ ex.getMessage());
			}
		}
	}

}