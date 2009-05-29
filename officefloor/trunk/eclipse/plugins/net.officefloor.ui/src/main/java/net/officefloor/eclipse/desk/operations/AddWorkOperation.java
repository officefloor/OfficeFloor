/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.wizard.worksource.WorkInstance;
import net.officefloor.eclipse.wizard.worksource.WorkSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;

/**
 * Adds a {@link WorkModel} to the {@link DeskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddWorkOperation extends AbstractDeskChangeOperation<DeskEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public AddWorkOperation(DeskChanges deskChanges) {
		super("Add work", DeskEditPart.class, deskChanges);
	}

	/*
	 * ================ AbstractDeskChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the work instance
		WorkInstance work = WorkSourceWizard.getWorkInstance(context
				.getEditPart(), null);
		if (work == null) {
			return null; // must have work to add
		}

		// Obtain the add work change
		Change<WorkModel> change = changes.addWork(work.getWorkName(), work
				.getWorkSourceClassName(), work.getPropertylist(), work
				.getWorkType(), work.getTaskTypeNames());

		// Position the work
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}