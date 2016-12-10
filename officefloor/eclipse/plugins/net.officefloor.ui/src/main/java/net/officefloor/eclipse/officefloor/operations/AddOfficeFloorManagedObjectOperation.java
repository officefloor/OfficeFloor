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

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.officefloor.editparts.OfficeFloorManagedObjectSourceEditPart;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;

/**
 * {@link Operation} to add an {@link OfficeFloorManagedObjectModel}.
 *
 * @author Daniel Sagenschneider
 */
public class AddOfficeFloorManagedObjectOperation
		extends
		AbstractOfficeFloorChangeOperation<OfficeFloorManagedObjectSourceEditPart> {

	/**
	 * Initiate.
	 *
	 * @param officeFloorChanges
	 *            {@link OfficeFloorChanges}.
	 */
	public AddOfficeFloorManagedObjectOperation(
			OfficeFloorChanges officeFloorChanges) {
		super("Add managed object",
				OfficeFloorManagedObjectSourceEditPart.class,
				officeFloorChanges);
	}

	/*
	 * ============= AbstractOfficeFloorChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(OfficeFloorChanges changes, Context context) {

		// Obtain the office floor managed object source
		OfficeFloorManagedObjectSourceEditPart editPart = context.getEditPart();
		OfficeFloorManagedObjectSourceModel managedObjectSource = editPart
				.getCastedModel();

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = ModelUtil
				.getManagedObjectType(managedObjectSource, editPart.getEditor());
		if (managedObjectType == null) {
			return null; // must have managed object type
		}

		// Obtain the details for the managed object (default scope)
		String managedObjectName = managedObjectSource
				.getOfficeFloorManagedObjectSourceName();
		ManagedObjectScope managedObjectScope = ManagedObjectScope.PROCESS;

		// Create the change to add the managed object
		Change<OfficeFloorManagedObjectModel> change = changes
				.addOfficeFloorManagedObject(managedObjectName,
						managedObjectScope, managedObjectSource,
						managedObjectType);

		// Position the managed object to the right
		OfficeFloorManagedObjectModel managedObject = change.getTarget();
		context.positionModel(managedObject);
		managedObject.setX(managedObject.getX() + 100);

		// Return the change to add the managed object
		return change;
	}

}