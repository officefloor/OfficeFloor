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

import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;

/**
 * Adds an {@link ExternalFlowModel} to the {@link DeskModel}.
 *
 * @author Daniel Sagenschneider
 */
public class AddExternalFlowOperation extends
		AbstractDeskChangeOperation<DeskEditPart> {

	/**
	 * Initiate.
	 *
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public AddExternalFlowOperation(DeskChanges deskChanges) {
		super("Add external flow", DeskEditPart.class, deskChanges);
	}

	/*
	 * ================== AbstractDeskChangeOperation ========================
	 */

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the edit part
		DeskEditPart editPart = context.getEditPart();

		// Create the populated External Flow
		final ExternalFlowModel flow = new ExternalFlowModel();
		BeanDialog dialog = context.getEditPart().createBeanDialog(flow, "X",
				"Y");
		dialog.registerPropertyInput("Argument Type", new ClasspathClassInput(
				editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created
			return null;
		}

		// Create the change
		Change<ExternalFlowModel> change = changes.addExternalFlow(flow
				.getExternalFlowName(), flow.getArgumentType());

		// Position the flow
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}