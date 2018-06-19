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
import net.officefloor.eclipse.section.editparts.SectionManagedObjectSourceEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectSourceModel;

/**
 * {@link Operation} to delete an {@link SectionManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteManagedObjectSourceOperation
		extends AbstractSectionChangeOperation<SectionManagedObjectSourceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public DeleteManagedObjectSourceOperation(SectionChanges sectionChanges) {
		super("Delete managed object source", SectionManagedObjectSourceEditPart.class, sectionChanges);
	}

	/*
	 * =============== AbstractDeskChangeOperation ========================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the managed object source
		SectionManagedObjectSourceModel managedObjectSource = context.getEditPart().getCastedModel();

		// Remove the managed object source
		return changes.removeSectionManagedObjectSource(managedObjectSource);
	}

}