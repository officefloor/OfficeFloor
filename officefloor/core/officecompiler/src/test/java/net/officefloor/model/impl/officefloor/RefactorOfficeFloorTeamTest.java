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

package net.officefloor.model.impl.officefloor;

import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * Refactors the {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeFloorTeamTest extends AbstractOfficeFloorChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeFloorTeamModel officeFloorTeam = this.model.getOfficeFloorTeams().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeFloorTeamTypeQualification(officeFloorTeam,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Team Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeFloorTeamModel officeFloorTeam = this.model.getOfficeFloorTeams().get(0);
		TypeQualificationModel typeQualification = officeFloorTeam.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeFloorTeamTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Team Type Qualification", true);
	}

}
