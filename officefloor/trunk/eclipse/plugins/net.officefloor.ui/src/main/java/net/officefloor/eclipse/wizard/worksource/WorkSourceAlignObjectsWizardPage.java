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
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * {@link IWizardPage} to align {@link WorkTaskObjectModel} instances of the
 * {@link WorkTaskModel} instances of the {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceAlignObjectsWizardPage extends WizardPage {

	/**
	 * {@link WorkInstance} being refactored.
	 */
	private final WorkInstance workInstance;

	/**
	 * Page.
	 */
	private Composite page;

	/**
	 * {@link TabFolder} providing a {@link TabItem} for each {@link TaskType}.
	 */
	private TabFolder tabFolder;

	/**
	 * Mapping of {@link WorkTaskModel} name its {@link ConformInput}.
	 */
	private Map<String, ConformInput> workTaskObjectConforms = new HashMap<String, ConformInput>();

	/**
	 * Conforms the {@link Work}.
	 * 
	 * @param workInstance
	 *            {@link WorkInstance} being refactored.
	 */
	public WorkSourceAlignObjectsWizardPage(WorkInstance workInstance) {
		super("Refactor Work Task Objects");
		this.workInstance = workInstance;
		this.setTitle("Refactor objects of work tasks");
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
			this.workTaskObjectConforms.clear();
		}

		// Add the tab folder for task types
		this.tabFolder = new TabFolder(this.page, SWT.NONE);
		this.tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		for (TaskType<?, ?, ?> taskType : workSourceInstance.getWorkType()
				.getTaskTypes()) {

			// Add the tab for the task type
			TabItem tabItem = new TabItem(this.tabFolder, SWT.NONE);
			tabItem.setText(taskType.getTaskName());

			// Obtain the list of objects for task type
			List<String> objectTypeNames = new LinkedList<String>();
			for (TaskObjectType<?> objectType : taskType.getObjectTypes()) {
				objectTypeNames.add(objectType.getObjectName());
			}

			// Obtain the objects for corresponding work task
			List<String> workTaskObjectNames = new LinkedList<String>();
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
				for (WorkTaskObjectModel workTaskObject : workTask
						.getTaskObjects()) {
					workTaskObjectNames.add(workTaskObject.getObjectName());
				}
			}

			// Create the conform input
			ConformInput conformInput = new ConformInput();
			conformInput.setConform(workTaskObjectNames.toArray(new String[0]),
					objectTypeNames.toArray(new String[0]));

			// Create input to conform objects of task
			InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(
					this.tabFolder, conformInput);
			tabItem.setControl(inputHandler.getControl());

			// Add conform if work task
			if (workTask != null) {
				this.workTaskObjectConforms.put(workTaskName, conformInput);
			}
		}

		// Refresh the page
		this.page.layout(true);
	}

	/**
	 * Obtains the mapping of {@link TaskObjectType} name to
	 * {@link WorkTaskObjectModel} name for a particular {@link WorkTaskModel}
	 * name.
	 * 
	 * @return Mapping of {@link TaskObjectType} name to
	 *         {@link WorkTaskObjectModel} name for a particular
	 *         {@link WorkTaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskObjectNameMappingForWorkTask() {
		// Create the map
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		for (String key : this.workTaskObjectConforms.keySet()) {
			ConformInput input = this.workTaskObjectConforms.get(key);
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