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

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.section.editparts.SectionManagedObjectEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectModel;

/**
 * {@link Operation} to delete an {@link SectionManagedObjectModel}.
 *
 * @author Daniel Sagenschneider
 */
public class DeleteManagedObjectOperation extends AbstractSectionChangeOperation<SectionManagedObjectEditPart> {

	/**
	 * Initiate.
	 *
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public DeleteManagedObjectOperation(SectionChanges sectionChanges) {
		super("Delete managed object", SectionManagedObjectEditPart.class, sectionChanges);
	}

	/*
	 * ============= AbstractSectionChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the managed object
		SectionManagedObjectModel managedObject = context.getEditPart().getCastedModel();

		// Return the change to delete the managed object
		return changes.removeSectionManagedObject(managedObject);
	}

}