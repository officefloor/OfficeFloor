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
package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.ExternalFlowModel;

/**
 * Tests adding an {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddExternalFlowTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures that can add an {@link ExternalFlowModel}.
	 */
	public void testAddExternalFlow() {
		Change<ExternalFlowModel> change = this.operations.addExternalFlow(
				"FLOW", Object.class.getName());
		this.assertChange(change, null, "Add external flow FLOW", true);
		change.apply();
		assertEquals("Incorrect target", this.model.getExternalFlows().get(0),
				change.getTarget());
	}

	/**
	 * Ensure ordering of {@link ExternalFlowModel} instances.
	 */
	public void testAddMultipleExternalFlowsEnsuringOrdering() {
		Change<ExternalFlowModel> changeB = this.operations.addExternalFlow(
				"FLOW_B", Integer.class.getName());
		Change<ExternalFlowModel> changeA = this.operations.addExternalFlow(
				"FLOW_A", String.class.getName());
		Change<ExternalFlowModel> changeC = this.operations.addExternalFlow(
				"FLOW_C", Object.class.getName());
		this.assertChanges(changeB, changeA, changeC);
	}

}