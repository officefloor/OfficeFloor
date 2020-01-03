package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceModel;

/**
 * Tests adding/removing a {@link WoofGovernanceAreaModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeGovernanceAreaTest extends AbstractWoofChangesTestCase {

	/**
	 * Tests adding a {@link WoofGovernanceAreaModel}.
	 */
	public void testAddArea() {

		// Obtain the governance to add area
		WoofGovernanceModel governance = this.model.getWoofGovernances().get(0);

		// Add the governance area
		Change<WoofGovernanceAreaModel> change = this.operations
				.addGovernanceArea(governance, 200, 201);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate adding the governance
		this.assertChange(change, change.getTarget(), "Add governance area",
				true);
	}

	/**
	 * Tests removing a {@link WoofGovernanceAreaModel}.
	 */
	public void testRemoveArea() {

		// Obtain the governance area to remove
		WoofGovernanceModel governance = this.model.getWoofGovernances().get(0);
		WoofGovernanceAreaModel area = governance.getGovernanceAreas().get(0);

		// Remove the governance area
		Change<WoofGovernanceAreaModel> change = this.operations
				.removeGovernanceArea(area);
		this.assertChange(change, area, "Remove governance area", true);
	}
}
