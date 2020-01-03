package net.officefloor.model.impl.office;

import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.TypeQualificationModel;

/**
 * Refactors the {@link OfficeManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeTeamTest extends AbstractOfficeChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeTeamModel officeTeam = this.model.getOfficeTeams().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeTeamTypeQualification(officeTeam, "QUALIFIER",
				"TYPE");
		this.assertChange(change, change.getTarget(), "Add Team Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeTeamModel officeTeam = this.model.getOfficeTeams().get(0);
		TypeQualificationModel typeQualification = officeTeam.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations.removeOfficeTeamTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Team Type Qualification", true);
	}

}