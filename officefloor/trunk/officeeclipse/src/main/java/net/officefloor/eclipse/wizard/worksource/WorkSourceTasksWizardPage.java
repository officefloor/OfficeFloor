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
package net.officefloor.eclipse.wizard.worksource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * {@link IWizardPage} to select the {@link TaskModel} instances of the
 * {@link WorkModel} to include.
 * 
 * @author Daniel
 */
public class WorkSourceTasksWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link DeskWorkModel} name.
	 */
	private Text workName;

	/**
	 * Display of the tasks.
	 */
	private Table tasks;

	/**
	 * Mapping of the {@link TaskModel} by its task name.
	 */
	private Map<String, WorkTaskModel> nameToTaskMap = null;

	/**
	 * {@link WorkModel}.
	 */
	private WorkModel workModel = null;

	/**
	 * Initiate.
	 */
	protected WorkSourceTasksWizardPage() {
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
	 * @param suggestedWorkName
	 *            Suggested work name.
	 */
	public void loadWorkModel(WorkModel workModel, String suggestedWorkName) {

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
			List<WorkTaskModel> tasks = this.workModel.getWorkTasks();
			taskNames = new String[tasks.size()];
			this.nameToTaskMap = new HashMap<String, WorkTaskModel>(tasks
					.size());
			for (int i = 0; i < taskNames.length; i++) {
				WorkTaskModel task = tasks.get(i);
				String taskName = task.getWorkTaskName();

				// Specify task name and register task
				taskNames[i] = taskName;
				this.nameToTaskMap.put(taskName, task);
			}
		}

		// Load the tasks to choose from (by default all chosen)
		this.tasks.removeAll();
		for (String taskName : taskNames) {
			TableItem item = new TableItem(this.tasks, SWT.LEFT);
			item.setText(taskName);
			item.setChecked(true);
		}

		// Specify the suggested work name
		this.workName.setText(suggestedWorkName);

		// Initiate state
		this.handlePageChange();
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
	public List<WorkTaskModel> getChosenTaskModels() {

		// Ensure have tasks registered
		if (this.nameToTaskMap == null) {
			return new ArrayList<WorkTaskModel>(0);
		}

		// Obtain the listing of checked rows
		List<TableItem> checkedRows = new LinkedList<TableItem>();
		for (TableItem row : this.tasks.getItems()) {
			if (row.getChecked()) {
				checkedRows.add(row);
			}
		}

		// Obtain the subsequent tasks
		List<WorkTaskModel> chosenTasks = new ArrayList<WorkTaskModel>(
				checkedRows.size());
		for (TableItem checkedRow : checkedRows) {

			// Obtain the chosen task name
			String taskName = checkedRow.getText();

			// Obtain the selected task
			WorkTaskModel task = this.nameToTaskMap.get(taskName);

			// Load if have the task
			if (task != null) {
				chosenTasks.add(task);
			}
		}

		// Return the chosen tasks
		return chosenTasks;
	}

	@Override
	public void createControl(Composite parent) {

		// Create the page for the work loader
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Provide control to specify name
		Composite name = new Composite(page, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		name.setLayout(new GridLayout(2, false));
		new Label(name, SWT.None).setText("Work name: ");
		this.workName = new Text(name, SWT.SINGLE | SWT.BORDER);
		this.workName.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		this.workName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				WorkSourceTasksWizardPage.this.handlePageChange();
			}
		});

		// Provide control to select tasks
		this.tasks = new Table(page, SWT.CHECK);
		this.tasks.setHeaderVisible(false);
		this.tasks.setLinesVisible(false);
		this.tasks.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.tasks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Ignore everything except check box changes
				if (e.detail != SWT.CHECK) {
					return;
				}

				// Handle check box change
				WorkSourceTasksWizardPage.this.handlePageChange();
			}
		});

		// Initiate state (currently no work)
		this.loadWorkModel(null, "");
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

		// Ensure that one or more tasks are chosen
		int selectionCount = this.getChosenTaskModels().size();
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