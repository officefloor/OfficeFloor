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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskType;
import net.officefloor.eclipse.common.dialog.input.InputHandler;
import net.officefloor.eclipse.conform.ConformInput;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.conform.ConformModel;
import net.officefloor.model.desk.WorkTaskModel;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link IWizardPage} to align {@link WorkTaskModel} instances of the
 * {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkSourceAlignTasksWizardPage extends WizardPage {

	/**
	 * {@link WorkInstance} being refactored.
	 */
	private final WorkInstance workInstance;

	/**
	 * {@link ConformInput}.
	 */
	private final ConformInput input;

	/**
	 * Conforms the {@link Work}.
	 * 
	 * @param workInstance
	 *            {@link WorkInstance} being refactored.
	 */
	public WorkSourceAlignTasksWizardPage(WorkInstance workInstance) {
		super("Refactor Work Tasks");
		this.workInstance = workInstance;
		this.setTitle("Refactor tasks of work");
		this.input = new ConformInput();
	}

	/**
	 * Loads the {@link WorkSourceInstance}.
	 * 
	 * @param workSourceInstance
	 *            {@link WorkSourceInstance} of the selected {@link WorkSource}.
	 */
	public void loadWorkSourceInstance(WorkSourceInstance workSourceInstance) {

		// Obtain the listing of work tasks
		List<String> workTaskNames = new LinkedList<String>();
		for (WorkTaskModel workTaskModel : this.workInstance.getWorkModel()
				.getWorkTasks()) {
			workTaskNames.add(workTaskModel.getWorkTaskName());
		}

		// Obtain the listing of task types
		List<String> taskTypeNames = new LinkedList<String>();
		for (TaskType<?, ?, ?> taskType : workSourceInstance.getWorkType()
				.getTaskTypes()) {
			taskTypeNames.add(taskType.getTaskName());
		}

		// Provide specify tasks to conform
		this.input.setConform(workTaskNames.toArray(new String[0]),
				taskTypeNames.toArray(new String[0]));
	}

	/**
	 * Obtains the mapping of {@link TaskType} name to {@link WorkTaskModel}
	 * name.
	 * 
	 * @return Mapping of {@link TaskType} name to {@link WorkTaskModel} name.
	 */
	public Map<String, String> getWorkTaskNameMapping() {
		return this.input.getTargetItemToExistingItemMapping();
	}

	/*
	 * ================== IDialogPage =========================================
	 */

	@Override
	public void createControl(Composite parent) {
		// Create the composite to contain the graphical viewer
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout(1, false));

		// Create input to conform the work
		InputHandler<ConformModel> inputHandler = new InputHandler<ConformModel>(
				page, this.input);
		inputHandler.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		// Specify the control
		this.setControl(page);
	}
}