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
package net.officefloor.eclipse.section.operations;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceEditPart;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;

/**
 * {@link Operation} to add an {@link SectionManagedObjectModel}.
 *
 * @author Daniel Sagenschneider
 */
public class AddManagedObjectOperation extends
		AbstractSectionChangeOperation<SectionManagedObjectSourceEditPart> {

	/**
	 * Initiate.
	 *
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public AddManagedObjectOperation(SectionChanges sectionChanges) {
		super("Add managed object", SectionManagedObjectSourceEditPart.class,
				sectionChanges);
	}

	/*
	 * ============= AbstractSectionChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the managed object source
		SectionManagedObjectSourceEditPart editPart = context.getEditPart();
		SectionManagedObjectSourceModel managedObjectSource = editPart
				.getCastedModel();

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = ModelUtil
				.getManagedObjectType(managedObjectSource, editPart.getEditor());
		if (managedObjectType == null) {
			return null; // must have managed object type
		}

		// Obtain the details for the managed object (default scope)
		String managedObjectName = managedObjectSource
				.getSectionManagedObjectSourceName();
		ManagedObjectScope managedObjectScope = ManagedObjectScope.PROCESS;

		// Create the change to add the managed object
		Change<SectionManagedObjectModel> change = changes
				.addSectionManagedObject(managedObjectName, managedObjectScope,
						managedObjectSource, managedObjectType);

		// Position the managed object to the right
		SectionManagedObjectModel managedObject = change.getTarget();
		context.positionModel(managedObject);
		managedObject.setX(managedObject.getX() + 100);

		// Return the change to add the managed object
		return change;
	}

}