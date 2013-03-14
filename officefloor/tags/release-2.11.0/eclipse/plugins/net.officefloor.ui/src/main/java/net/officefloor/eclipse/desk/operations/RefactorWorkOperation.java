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
package net.officefloor.eclipse.desk.operations;

import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.desk.editparts.WorkEditPart;
import net.officefloor.eclipse.wizard.worksource.WorkInstance;
import net.officefloor.eclipse.wizard.worksource.WorkSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.WorkModel;

/**
 * {@link Operation} to refactor the {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorWorkOperation extends
		AbstractDeskChangeOperation<WorkEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public RefactorWorkOperation(DeskChanges deskChanges) {
		super("Refactor work", WorkEditPart.class, deskChanges);
	}

	/*
	 * ================= AbstractDeskChangeOperation ================
	 */

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the work model
		WorkEditPart editPart = context.getEditPart();
		WorkModel work = editPart.getCastedModel();

		// Obtain the refactored work instance
		WorkInstance workInstance = WorkSourceWizard.getWorkInstance(editPart,
				new WorkInstance(work));
		if (workInstance == null) {
			return null; // work not being refactored
		}

		// Obtain the align details
		Map<String, String> workTaskNameMapping = workInstance
				.getWorkTaskNameMapping();
		Map<String, Map<String, String>> workTaskToObjectNameMapping = workInstance
				.getTaskObjectNameMappingForWorkTask();
		Map<String, Map<String, String>> taskToFlowNameMapping = workInstance
				.getTaskFlowNameMappingForTask();
		Map<String, Map<String, String>> taskToEscalationTypeMapping = workInstance
				.getTaskEscalationTypeMappingForTask();

		// Return change for refactoring the work
		return changes.refactorWork(work, workInstance.getWorkName(),
				workInstance.getWorkSourceClassName(), workInstance
						.getPropertyList(), workInstance.getWorkType(),
				workTaskNameMapping, workTaskToObjectNameMapping,
				taskToFlowNameMapping, taskToEscalationTypeMapping,
				workInstance.getTaskTypeNames());
	}

}