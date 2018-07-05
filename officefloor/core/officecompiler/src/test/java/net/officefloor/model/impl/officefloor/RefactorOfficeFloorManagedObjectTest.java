/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.officefloor;

import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * Refactors the {@link OfficeFloorManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeFloorManagedObjectTest extends AbstractOfficeFloorChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeFloorManagedObjectTypeQualification(officeMo,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Managed Object Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		TypeQualificationModel typeQualification = officeMo.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeFloorManagedObjectTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Managed Object Type Qualification", true);
	}

}