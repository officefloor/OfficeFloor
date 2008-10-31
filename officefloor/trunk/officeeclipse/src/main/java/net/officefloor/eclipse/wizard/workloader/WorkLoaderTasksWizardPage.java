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
package net.officefloor.eclipse.wizard.workloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} to select the {@link TaskModel} instances of the
 * {@link WorkModel} to include.
 * 
 * @author Daniel
 */
public class WorkLoaderTasksWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link DeskWorkModel} name.
	 */
	private Text workName;

	/**
	 * Display of the tasks.
	 */
	private List tasks;

	/**
	 * Mapping of the {@link TaskModel} by its task name.
	 */
	private Map<String, TaskModel<?, ?>> nameToTaskMap = null;

	/**
	 * {@link WorkModel}.
	 */
	private WorkModel<?> workModel = null;

	/**
	 * Initiate.
	 */
	protected WorkLoaderTasksWizardPage() {
		super("WorkLoader tasks");

		// Specify page details
		this.setTitle("Select tasks");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link WorkModel}.
	 * 
	 * @param workModel
	 *            {@link WorkModel}.
	 */
	public void setWorkModel(WorkModel<?> workModel) {

		// Do nothing if same work model
		if (this.workModel == workModel) {
			return;
		}

		// Specify the work model
		this.workModel = workModel;

		// Specify based on whether have work
		String[] taskNames;
		if (this.workModel == null) {
			// No tasks
			taskNames = new String[0];
			this.nameToTaskMap = null;

		} else {
			// Create the listing of the task names
			java.util.List<TaskModel<?, ?>> tasks = this.workModel.getTasks();
			taskNames = new String[tasks.size()];
			this.nameToTaskMap = new HashMap<String, TaskModel<?, ?>>(tasks
					.size());
			for (int i = 0; i < taskNames.length; i++) {
				TaskModel<?, ?> task = tasks.get(i);
				String taskName = task.getTaskName();

				// Specify task name and register task
				taskNames[i] = taskName;
				this.nameToTaskMap.put(taskName, task);
			}
		}

		// Load the tasks to select from
		this.tasks.setItems(taskNames);

		// Nothing should be selected, so page not completed
		this.setPageComplete(false);
	}

	/**
	 * Obtains the name of the {@link DeskWorkModel}.
	 * 
	 * @return Name of the {@link DeskWorkModel}.
	 */
	public String getWorkName() {
		return this.workName.getText();
	}

	/**
	 * Obtains the selected {@link TaskModel} instances.
	 * 
	 * @return Selected {@link TaskModel} instances.
	 */
	public java.util.List<TaskModel<?, ?>> getSelectedTaskModels() {

		// Ensure have tasks registered
		if (this.nameToTaskMap == null) {
			return new ArrayList<TaskModel<?, ?>>(0);
		}

		// Obtain the selection
		String[] selectedTaskNames = this.tasks.getSelection();
		java.util.List<TaskModel<?, ?>> selectedTasks = new ArrayList<TaskModel<?, ?>>(
				selectedTaskNames.length);
		for (String selectedTaskName : selectedTaskNames) {

			// Obtain the selected task
			TaskModel<?, ?> selectedTask = this.nameToTaskMap
					.get(selectedTaskName);

			// Load if have the selected task
			if (selectedTask != null) {
				selectedTasks.add(selectedTask);
			}
		}

		// Return the selected tasks
		return selectedTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Provide control to specify name
		Composite name = new Composite(page, SWT.NONE);
		name.setLayout(new GridLayout(2, false));
		new Label(name, SWT.None).setText("Work name: ");
		this.workName = new Text(name, SWT.SINGLE | SWT.BORDER);
		this.workName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				WorkLoaderTasksWizardPage.this.handlePageChange();
			}
		});

		// Provide control to select tasks
		this.tasks = new List(page, SWT.MULTI | SWT.BORDER);
		this.tasks.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.tasks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkLoaderTasksWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no work)
		this.setWorkModel(null);
		this.handlePageChange();

		// Specify control
		this.setControl(page);
	}

	/**
	 * Handles changes to the page.
	 */
	private void handlePageChange() {

		// Ensure work has a name
		String workName = this.workName.getText();
		if ((workName == null) || (workName.trim().length() == 0)) {
			this.setErrorMessage("Must provide work name");
			this.setPageComplete(false);
			return;
		}

		// Ensure that one or more tasks are selected
		int selectionCount = WorkLoaderTasksWizardPage.this.tasks
				.getSelectionCount();
		if (selectionCount == 0) {
			this.setErrorMessage("Must select at least one task");
			this.setPageComplete(false);
			return;
		}

		// Make complete
		this.setErrorMessage(null);
		this.setPageComplete(true);
	}
}
