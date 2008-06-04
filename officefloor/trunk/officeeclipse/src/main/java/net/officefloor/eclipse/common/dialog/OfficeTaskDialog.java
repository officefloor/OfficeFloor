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
package net.officefloor.eclipse.common.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.common.persistence.ProjectConfigurationContext;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.officefloor.OfficeFloorLoader;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link Dialog} to create an {@link OfficeTaskModel}.
 * 
 * @author Daniel
 */
public class OfficeTaskDialog extends Dialog {

	/**
	 * Obtains the {@link OfficeTaskModel}.
	 * 
	 * @param target
	 *            Either the {@link OfficeTaskModel} or the
	 *            {@link OfficeFloorOfficeModel}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 * @return {@link OfficeTaskModel} or <code>null</code> if none selected
	 *         or issue in obtaining.
	 */
	public static OfficeTaskModel getOfficeTaskModel(Object target,
			AbstractOfficeFloorEditor<?> editor) {

		// Obtain the office task
		OfficeTaskModel task = null;
		try {
			if (target instanceof OfficeTaskModel) {
				// Office task specified
				task = (OfficeTaskModel) target;

			} else if (target instanceof OfficeFloorOfficeModel) {
				// Obtain the task from the office
				OfficeFloorOfficeModel office = (OfficeFloorOfficeModel) target;

				// Obtain the project
				IProject project = ProjectConfigurationContext
						.getProject(editor.getEditorInput());

				// Obtain the task of the office to link
				OfficeTaskDialog dialog = new OfficeTaskDialog(editor
						.getEditorSite().getShell(), office, project);
				task = dialog.createOfficeTask();
				if (task != null) {
					// Add the task to the office, so may connect
					office.addTask(task);
				}
			}
		} catch (Exception ex) {
			// No task should be returned
			task = null;

			// Obtain the stack trace
			StringWriter buffer = new StringWriter();
			ex.printStackTrace(new PrintWriter(buffer));

			// Indicate error
			MessageDialog.openError(editor.getEditorSite().getShell(),
					"Office Floor", buffer.toString());
		}

		// Return the task
		return task;
	}

	/**
	 * Listing of {@link OfficeTaskModel} instances to choose from.
	 */
	private final OfficeTaskModel[] officeTasks;

	/**
	 * List of {@link OfficeTaskModel} instances.
	 */
	private List officeTaskList;

	/**
	 * Reports errors.
	 */
	private Label errorText;

	/**
	 * {@link OfficeTaskModel} to return.
	 */
	private OfficeTaskModel officeTask;

	/**
	 * Initiate.
	 * 
	 * @param parentShell
	 *            Parent {@link Shell}.
	 * @param office
	 *            {@link OfficeFloorOfficeModel} to select an
	 *            {@link OfficeTaskModel} from.
	 * @param project
	 *            {@link IProject} to obtain the {@link ConfigurationItem} of
	 *            the {@link OfficeModel}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public OfficeTaskDialog(Shell parentShell, OfficeFloorOfficeModel office,
			IProject project) throws Exception {
		super(parentShell);

		// Obtain the configuration context of the project
		ConfigurationContext configurationContext = ProjectClassLoader.create(
				project).getConfigurationContext();

		// Obtain the listing of office tasks
		this.officeTasks = new OfficeFloorLoader().loadOfficeTasks(office,
				configurationContext);
	}

	/**
	 * Creates the {@link OfficeTaskModel}.
	 * 
	 * @return {@link OfficeTaskModel} or <code>null</code> if not created.
	 */
	public OfficeTaskModel createOfficeTask() {

		// Block to open
		this.setBlockOnOpen(true);
		this.open();

		// Return the office task
		return this.officeTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// Create parent composite
		Composite composite = (Composite) super.createDialogArea(parent);

		// Selection of office tasks
		new Label(composite, SWT.WRAP).setText("Select Office task:");
		this.officeTaskList = new List(composite, SWT.SINGLE | SWT.BORDER);
		for (OfficeTaskModel officeTask : this.officeTasks) {
			this.officeTaskList.add(officeTask.getWorkName() + "."
					+ officeTask.getTaskName());
		}

		// Error text
		this.errorText = new Label(composite, SWT.WRAP);
		this.errorText.setText("");
		this.errorText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		this.errorText.setBackground(errorText.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		this.errorText.setForeground(ColorConstants.red);

		// Return the composite
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		try {

			// Ensure item selected
			int[] selected = this.officeTaskList.getSelectionIndices();
			if ((selected == null) || (selected.length != 1)) {
				// Single item not selected
				this.errorText.setText("Must select a task");
				return;
			}

			// Obtain the office task
			this.officeTask = this.officeTasks[selected[0]];

		} catch (Throwable ex) {
			// Failed, report error and do not close dialog
			this.errorText.setText(ex.getClass().getSimpleName() + ": "
					+ ex.getMessage());
			return;
		}

		// Successful
		super.okPressed();
	}

}
