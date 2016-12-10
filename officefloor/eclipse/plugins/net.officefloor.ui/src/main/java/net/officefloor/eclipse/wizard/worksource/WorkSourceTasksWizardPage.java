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
package net.officefloor.eclipse.wizard.worksource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Work;

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
 * {@link IWizardPage} to select the {@link TaskType} instances of the
 * {@link WorkType} to include.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceTasksWizardPage extends WizardPage {

	/**
	 * Display to obtain the {@link Work} name.
	 */
	private Text workName;

	/**
	 * Display of the {@link TaskType} instances.
	 */
	private Table tasks;

	/**
	 * {@link WorkSourceInstance}.
	 */
	private WorkSourceInstance workSourceInstance;

	/**
	 * {@link TaskType} instances by their names.
	 */
	private Map<String, TaskType<?, ?, ?>> taskTypes = null;

	/**
	 * Initiate.
	 */
	protected WorkSourceTasksWizardPage() {
		super("WorkSource tasks");

		// Specify page details
		this.setTitle("Select tasks");
		this.setPageComplete(false);
	}

	/**
	 * Specifies the {@link WorkSourceInstance}.
	 * 
	 * @param workSourceInstance
	 *            {@link WorkSourceInstance}.
	 */
	public void loadWorkSourceInstance(WorkSourceInstance workSourceInstance) {

		// Specify the work source instance and obtain work type (may be null)
		this.workSourceInstance = workSourceInstance;
		WorkType<?> workType = (this.workSourceInstance != null ? this.workSourceInstance
				.getWorkType()
				: null);
		String suggestedWorkName = (this.workSourceInstance != null ? this.workSourceInstance
				.getSuggestedWorkName()
				: "");

		// Specify the suggested work name
		this.workName.setText(suggestedWorkName);

		// Create the list of task types
		String[] taskTypeNames;
		if (workType == null) {
			// No work type, no tasks
			taskTypeNames = new String[0];
			this.taskTypes = null;

		} else {
			// Create the listing of the task type names
			TaskType<?, ?, ?>[] taskTypes = workType.getTaskTypes();
			taskTypeNames = new String[taskTypes.length];
			this.taskTypes = new HashMap<String, TaskType<?, ?, ?>>(
					taskTypes.length);
			for (int i = 0; i < taskTypeNames.length; i++) {
				TaskType<?, ?, ?> taskType = taskTypes[i];

				// Specify task type name and register task type
				String taskTypeName = taskType.getTaskName();
				taskTypeNames[i] = taskTypeName;
				this.taskTypes.put(taskTypeName, taskType);
			}
		}

		// Load the task types to choose from (by default all chosen)
		this.tasks.removeAll();
		for (String taskTypeName : taskTypeNames) {
			TableItem item = new TableItem(this.tasks, SWT.LEFT);
			item.setText(taskTypeName);
			item.setChecked(true);
		}

		// Initiate state
		this.handlePageChange();
	}

	/**
	 * Obtains the name of the {@link Work}.
	 * 
	 * @return Name of the {@link Work}.
	 */
	public String getWorkName() {
		return this.workName.getText();
	}

	/**
	 * Obtains the selected {@link TaskType} instances.
	 * 
	 * @return Selected {@link TaskType} instances.
	 */
	public List<TaskType<?, ?, ?>> getSelectedTaskTypes() {

		// Ensure have task types registered
		if (this.taskTypes == null) {
			return new ArrayList<TaskType<?, ?, ?>>(0);
		}

		// Obtain the listing of checked rows
		List<TableItem> checkedRows = new LinkedList<TableItem>();
		for (TableItem row : this.tasks.getItems()) {
			if (row.getChecked()) {
				checkedRows.add(row);
			}
		}

		// Obtain the subsequent task types
		List<TaskType<?, ?, ?>> chosenTaskTypes = new ArrayList<TaskType<?, ?, ?>>(
				checkedRows.size());
		for (TableItem checkedRow : checkedRows) {

			// Obtain the chosen task type name
			String taskTypeName = checkedRow.getText();

			// Obtain and add the selected task type
			TaskType<?, ?, ?> taskType = this.taskTypes.get(taskTypeName);
			if (taskType != null) {
				chosenTaskTypes.add(taskType);
			}
		}

		// Return the chosen task types
		return chosenTaskTypes;
	}

	@Override
	public void createControl(Composite parent) {

		// Create the page for the work source
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

		// Initiate state (currently no work source instance)
		this.loadWorkSourceInstance(null);
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
		int selectionCount = this.getSelectedTaskTypes().size();
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