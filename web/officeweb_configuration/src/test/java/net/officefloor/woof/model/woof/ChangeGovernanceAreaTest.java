/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
