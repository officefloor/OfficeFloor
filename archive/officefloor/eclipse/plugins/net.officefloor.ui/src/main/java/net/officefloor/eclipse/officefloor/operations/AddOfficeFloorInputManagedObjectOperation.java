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
package net.officefloor.eclipse.officefloor.operations;

import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathClassInput;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorModel;

/**
 * Adds an {@link OfficeFloorInputManagedObject} to {@link OfficeFloorModel}.
 *
 * @author Daniel Sagenschneider
 */
public class AddOfficeFloorInputManagedObjectOperation extends
		AbstractOfficeFloorChangeOperation<OfficeFloorEditPart> {

	/**
	 * Initiate.
	 *
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public AddOfficeFloorInputManagedObjectOperation(
			OfficeFloorChanges officeFloorChanges) {
		super("Add input managed object", OfficeFloorEditPart.class,
				officeFloorChanges);
	}

	/*
	 * ================== AbstractOfficeFloorChangeOperation =================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the office floor edit part
		OfficeFloorEditPart editPart = context.getEditPart();

		// Create the populated Input Managed Object
		OfficeFloorInputManagedObjectModel inputMo = new OfficeFloorInputManagedObjectModel();
		BeanDialog dialog = editPart.createBeanDialog(inputMo, "X", "Y",
				"Bound Office Floor Managed Object Source");
		dialog.registerPropertyInput("Object Type", new ClasspathClassInput(
				editPart.getEditor()));
		if (!dialog.populate()) {
			// Not created so do not provide command
			return null;
		}

		// Add the Input Managed Object
		Change<OfficeFloorInputManagedObjectModel> change = changes
				.addOfficeFloorInputManagedObject(inputMo
						.getOfficeFloorInputManagedObjectName(), inputMo
						.getObjectType());

		// Position the model
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}