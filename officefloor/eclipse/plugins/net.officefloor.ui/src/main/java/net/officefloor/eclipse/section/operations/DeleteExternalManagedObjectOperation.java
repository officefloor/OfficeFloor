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
import net.officefloor.eclipse.section.editparts.ExternalManagedObjectEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionChanges;

/**
 * {@link Operation} to delete an {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteExternalManagedObjectOperation
		extends AbstractSectionChangeOperation<ExternalManagedObjectEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public DeleteExternalManagedObjectOperation(SectionChanges sectionChanges) {
		super("Delete external managed object", ExternalManagedObjectEditPart.class, sectionChanges);
	}

	/*
	 * =============== AbstractDeskChangeOperation ========================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the external managed object
		ExternalManagedObjectModel externalManagedObject = context.getEditPart().getCastedModel();

		// Remove the external managed object
		return changes.removeExternalManagedObject(externalManagedObject);
	}

}