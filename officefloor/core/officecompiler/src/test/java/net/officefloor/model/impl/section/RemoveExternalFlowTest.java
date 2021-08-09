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
import net.officefloor.model.section.SectionModel;

/**
 * Tests removing the {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveExternalFlowTest extends AbstractSectionChangesTestCase {

	/**
	 * Initiate to use specific setup {@link SectionModel}.
	 */
	public RemoveExternalFlowTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove an {@link ExternalFlowModel} not in the
	 * {@link SectionModel}.
	 */
	public void testRemoveExternalFlowNotInSection() {
		ExternalFlowModel extFlow = new ExternalFlowModel("NOT_IN_SECTION",
				null);
		Change<ExternalFlowModel> change = this.operations
				.removeExternalFlow(extFlow);
		this.assertChange(change, extFlow,
				"Remove external flow NOT_IN_SECTION", false,
				"External flow NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove the {@link ExternalFlowModel} from the
	 * {@link SectionModel} when other {@link ExternalFlowModel} instances on
	 * the {@link SectionModel}.
	 */
	public void testRemoveExternalFlowWhenOtherExternalFlows() {
		ExternalFlowModel extFlow = this.model.getExternalFlows().get(1);
		Change<ExternalFlowModel> change = this.operations
				.removeExternalFlow(extFlow);
		this.assertChange(change, extFlow, "Remove external flow FLOW_B", true);
	}

	/**
	 * Ensure can remove the connected {@link ExternalFlowModel} from the
	 * {@link SectionModel}.
	 */
	public void testRemoveExternalFlowWithConnections() {
		ExternalFlowModel extFlow = this.model.getExternalFlows().get(0);
		Change<ExternalFlowModel> change = this.operations
				.removeExternalFlow(extFlow);
		this.assertChange(change, extFlow, "Remove external flow FLOW", true);
	}

}
