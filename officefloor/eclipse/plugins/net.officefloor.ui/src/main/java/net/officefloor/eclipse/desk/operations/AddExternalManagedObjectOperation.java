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

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * {@link Operation} to add the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddExternalManagedObjectOperation extends
		AbstractDeskChangeOperation<DeskEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param deskChanges
	 *            {@link DeskChanges}.
	 */
	public AddExternalManagedObjectOperation(DeskChanges deskChanges) {
		super("Add external object", DeskEditPart.class, deskChanges);
	}

	@Override
	protected Change<?> getChange(DeskChanges changes, Context context) {

		// Obtain the desk edit part
		final DeskEditPart editPart = context.getEditPart();

		// Create the populated External Managed Object
		final ExternalManagedObjectModel mo = new ExternalManagedObjectModel();
		BeanDialog dialog = editPart.createBeanDialog(mo, "X", "Y");
		dialog.registerPropertyInput("Object Type",
				new ClasspathClassInput(editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the external managed object
		Change<ExternalManagedObjectModel> change = changes
				.addExternalManagedObject(mo.getExternalManagedObjectName(), mo
						.getObjectType());

		// Position the model
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}