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