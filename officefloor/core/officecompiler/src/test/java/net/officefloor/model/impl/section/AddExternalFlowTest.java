/*-
 * #%L
 * OfficeCompiler
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
