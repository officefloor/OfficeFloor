/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.office;

import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.TypeQualificationModel;

/**
 * Refactors the {@link OfficeManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeManagedObjectTest extends AbstractOfficeChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeManagedObjectModel officeMo = this.model.getOfficeManagedObjects().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeManagedObjectTypeQualification(officeMo,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Managed Object Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeManagedObjectModel officeMos = this.model.getOfficeManagedObjects().get(0);
		TypeQualificationModel typeQualification = officeMos.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeManagedObjectTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Managed Object Type Qualification", true);
	}

}
