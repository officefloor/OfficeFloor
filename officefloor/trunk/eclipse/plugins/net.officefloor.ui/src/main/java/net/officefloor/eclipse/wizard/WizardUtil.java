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
package net.officefloor.eclipse.wizard;

import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Utility for working with the {@link IWizard}.
 * 
 * @author Daniel Sagenschneider
 */
public class WizardUtil {

	/**
	 * Runs the {@link IWizard}.
	 * 
	 * @param wizard
	 *            {@link IWizard}.
	 * @param editPart
	 *            {@link AbstractOfficeFloorEditPart}.
	 * @return <code>true</code> if successful.
	 */
	public static boolean runWizard(IWizard wizard,
			AbstractOfficeFloorEditPart<?, ?, ?> editPart) {
		return runWizard(wizard, editPart.getEditor());
	}

	/**
	 * Runs the {@link IWizard}.
	 * 
	 * @param wizard
	 *            {@link IWizard}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 * @return <code>true</code> if successful.
	 */
	public static boolean runWizard(IWizard wizard,
			AbstractOfficeFloorEditor<?, ?> editor) {
		try {

			// Obtain the editor size
			IEditorSite editorSite = editor.getEditorSite();

			// Initiate the wizard
			if (wizard instanceof IWorkbenchWizard) {
				IWorkbenchWizard workbenchWizard = (IWorkbenchWizard) wizard;

				// Obtain the work bench
				IWorkbench workbench = editorSite.getWorkbenchWindow()
						.getWorkbench();

				// Obtain the selection
				IStructuredSelection structuredSelection = null;
				ISelection selection = editorSite.getPage().getSelection();
				if (selection instanceof IStructuredSelection) {
					structuredSelection = (IStructuredSelection) selection;
				}

				// Initiate the workbench wizard
				workbenchWizard.init(workbench, structuredSelection);
			}

			// Create the wizard dialog
			WizardDialog dialog = new WizardDialog(editorSite.getShell(),
					wizard);
			dialog.setBlockOnOpen(true);
			int status = dialog.open();

			// Determine if successful
			boolean isSuccessful = (status == WizardDialog.OK);

			// Return whether successful
			return isSuccessful;

		} catch (Throwable ex) {
			editor.messageError(ex);
			return false;
		}
	}

	/**
	 * All access via static methods.
	 */
	private WizardUtil() {
	}
}
