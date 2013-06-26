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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * {@link IWizardPage} to align {@link TaskFlowModel} and
 * {@link TaskEscalationModel} instances of the {@link TaskModel} instances for
 * the {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceAlignFlowsEscalationsWizardPage extends WizardPage {

	/**
	 * {@link WorkInstance} being refactored.
	 */
	private final WorkInstance workInstance;

	/**
	 * Page.
	 */
	private Composite page;

	/**
	 * {@link TabFolder} providing a {@link TabItem} for each {@link TaskModel}.
	 */
	private TabFolder tabFolder;

	/**
	 * Mapping of {@link TaskModel} name to its {@link ConformInput} for the
	 * {@link TaskFlowModel} instances.
	 */
	private Map<String, ConformInput> taskFlowConforms = new HashMap<String, ConformInput>();

	/**
	 * Mapping of {@link TaskModel} name to its {@link ConformInput} for the
	 * {@link TaskEscalationModel} instances.
	 */
	private Map<String, ConformInput> taskEscalationConforms = new HashMap<String, ConformInput>();

	/**
	 * Conforms the {@link Work}.
	 * 
	 * @param workInstance
	 *            {@link WorkInstance} being refactored.
	 */
	public WorkSourceAlignFlowsEscalationsWizardPage(WorkInstance workInstance) {
		super("Refactor Task Flows and Escalations");
		this.workInstance = workInstance;
		this.setTitle("Refactor flows and escalations of tasks");
	}

	/**
	 * Loads the {@link WorkSourceInstance} and mapping of {@link TaskType} name
	 * to {@link WorkTaskModel} name.
	 * 
	 * @param workTaskNameMapping
	 *            Mapping of {@link TaskType} name to {@link WorkTaskModel}
	 *            name.
	 * @param workSourceInstance
	 *            {@link WorkSourceInstance} of the selected {@link WorkSource}.
	 */
	public void loadWorkTaskMappingAndWorkSourceInstance(
			Map<String, String> workTaskNameMapping,
			WorkSourceInstance workSourceInstance) {

		// Clear page to load new details
		if (this.tabFolder != null) {
			this.tabFolder.dispose();
			this.tabFolder = null;
			this.taskFlowConforms.clear();
			this.taskEscalationConforms.clear();
		}

		// Add the tab folder for task types
		this.tabFolder = new TabFolder(this.page, SWT.NONE);
		this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		for (TaskType<?, ?, ?> taskType : workSourceInstance.getWorkType()
				.getTaskTypes()) {

			// Obtain the corresponding tasks for the task type
			List<TaskModel> tasks = new LinkedList<TaskModel>();
			String taskName = taskType.getTaskName();
			String workTaskName = workTaskNameMapping.get(taskName);
			WorkTaskModel workTask = null;
			if (!(EclipseUtil.isBlank(workTaskName))) {
				for (WorkTaskModel checkTask : this.workInstance.getWorkModel()
						.getWorkTasks()) {
					if (workTaskName.equals(checkTask.getWorkTaskName())) {
						workTask = checkTask;
					}
				}
			}
			if (workTask != null) {
				for (WorkTaskToTaskModel conn : workTask.getTasks()) {
					TaskModel task = conn.getTask();
					if (task != null) {
						tasks.add(task);
					}
				}
			}
			if (tasks.size() == 0) {
				continue; // no need to go further as no tasks for work task
			}

			// Obtain the list of flows for task type
			List<String> flowTypeNames = new LinkedList<String>();
			for (TaskFlowType<?> flowType : taskType.getFlowTypes()) {
				flowTypeNames.add(flowType.getFlowName());
			}

			// Obtain the list of escalations for task type
			List<String> escalationTypes = new LinkedList<String>();
			for (TaskEscalationType escalationType : taskType
					.getEscalationTypes()) {
				escalationTypes.add(escalationType.getEscalationType()
						.getName());
			}

			// Add tab for each of the tasks
			for (TaskModel task : tasks) {

				// Add the tab for the task
				TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
				tabItem.setText(task.getTaskName());

				// Create the composite to contain both flows and escalations
				Composite tabContents = new Composite(this.tabFolder, SWT.NONE);
				tabContents.setLayout(new GridLayout(1, false));

				// Obtain the flow names for the task
				List<String> flowNames = new LinkedList<String>();
				for (TaskFlowModel flow : task.getTaskFlows()) {
					flowNames.add(flow.getFlowName());
				}

				// Add the conform of flows for task
				new Label(tabContents, SWT.NONE).setText("Flows");
				ConformInput flowInput = new ConformInput();
				flowInput.setConform(flowNames.toArray(new String[0]),
						flowTypeNames.toArray(new String[0]));
				InputHandler<ConformModel> flowHandler = new InputHandler<ConformModel>(
						tabContents, flowInput);
				flowHandler.getControl().setLayoutData(
						new GridData(SWT.FILL, SWT.FILL, true, true));
				this.taskFlowConforms.put(task.getTaskName(), flowInput);

				// Obtain the escalation types for task
				List<String> escalationTypeNames = new LinkedList<String>();
				for (TaskEscalationModel escalation : task.getTaskEscalations()) {
					escalationTypeNames.add(escalation.getEscalationType());
				}

				// Add the conform of escalations for task
				new Label(tabContents, SWT.NONE).setText("Escalations");
				ConformInput escalationInput = new ConformInput();
				escalationInput.setConform(escalationTypeNames
						.toArray(new String[0]), escalationTypes
						.toArray(new String[0]));
				InputHandler<ConformModel> escalationHandler = new InputHandler<ConformModel>(
						tabContents, escalationInput);
				escalationHandler.getControl().setLayoutData(
						new GridData(SWT.FILL, SWT.FILL, true, true));
				this.taskEscalationConforms.put(task.getTaskName(),
						escalationInput);

				// Specify tab contents
				tabItem.setControl(tabContents);
			}
		}

		// Refresh the page
		this.page.layout(true);
	}

	/**
	 * Obtains the mapping of {@link TaskFlowType} name to {@link TaskFlowModel}
	 * name for a particular {@link TaskModel} name.
	 * 
	 * @return Mapping of {@link TaskFlowType} name to {@link TaskFlowModel}
	 *         name for a particular {@link TaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskFlowNameMappingForTask() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.taskFlowConforms.keySet()) {
			ConformInput input = this.taskFlowConforms.get(key);
			map.put(key, input.getTargetItemToExistingItemMapping());
		}

		// Return the map
		return map;
	}

	/**
	 * Obtains the mapping of {@link TaskEscalationType} type to
	 * {@link TaskEscalationModel} type for a particular {@link TaskModel} name.
	 * 
	 * @return Mapping of {@link TaskFlowType} name to
	 *         {@link TaskEscalationModel} name for a particular
	 *         {@link TaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskEscalationTypeMappingForTask() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.taskEscalationConforms.keySet()) {
			ConformInput input = this.taskEscalationConforms.get(key);
			map.put(key, input.getTargetItemToExistingItemMapping());
		}

		// Return the map
		return map;
	}

	/*
	 * ================== IDialogPage =========================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		this.page = new Composite(parent, SWT.NONE);
		this.page.setLayout(new GridLayout(1, false));

		// Controls of page loaded when work task mapping loaded

		// Specify the control
		this.setControl(this.page);
	}

}