/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.model.desk.ExternalFlowModel;

/**
 * Tests adding an {@link ExternalFlowModel}.
 * 
 * @author Daniel
 */
public class AddExternalFlowTest extends AbstractDeskOperationsTestCase {

	/**
	 * Ensures that can add an {@link ExternalFlowModel}.
	 */
	public void testAddExternalFlow() {
		Change<ExternalFlowModel> change = this.operations.addExternalFlow(
				"FLOW", String.class.getName());
		this.assertChange(change, null, "Add external flow FLOW", true);
		change.apply();
		assertEquals("Incorrect target", this.desk.getExternalFlows().get(0),
				change.getTarget());
	}

	/**
	 * Ensure ordering of {@link ExternalFlowModel} instances.
	 */
	public void testAddMultipleExternalFlowsEnsuringOrdering() {

		// Create the changes
		Change<ExternalFlowModel> changeB = this.operations.addExternalFlow(
				"FLOW_B", Integer.class.getName());
		Change<ExternalFlowModel> changeA = this.operations.addExternalFlow(
				"FLOW_A", String.class.getName());
		Change<ExternalFlowModel> changeC = this.operations.addExternalFlow(
				"FLOW_C", Object.class.getName());

		// Add the external flows
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateDesk();

		// Revert
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupDesk();
	}

}