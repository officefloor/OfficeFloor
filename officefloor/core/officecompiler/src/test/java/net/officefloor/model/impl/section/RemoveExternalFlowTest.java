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