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