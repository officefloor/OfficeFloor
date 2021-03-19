/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

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
