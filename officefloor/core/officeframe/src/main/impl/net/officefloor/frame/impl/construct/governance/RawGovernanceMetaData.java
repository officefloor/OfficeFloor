/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Raw meta-data for a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaData<E, F extends Enum<F>> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Index of this {@link RawGovernanceMetaData} within the {@link ProcessState}.
	 */
	private final int governanceIndex;

	/**
	 * Extension type.
	 */
	private final Class<E> extensionType;

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private final GovernanceConfiguration<E, F> governanceConfiguration;

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaDataImpl<E, F> governanceMetaData;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceIndex
	 *            Index of this {@link RawGovernanceMetaData} within the
	 *            {@link ProcessState}.
	 * @param extensionType
	 *            Extension interface type.
	 * @param governanceConfiguration
	 *            {@link GovernanceConfiguration}.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaDataImpl}.
	 */
	public RawGovernanceMetaData(String governanceName, int governanceIndex, Class<E> extensionType,
			GovernanceConfiguration<E, F> governanceConfiguration, GovernanceMetaDataImpl<E, F> governanceMetaData) {
		this.governanceName = governanceName;
		this.governanceIndex = governanceIndex;
		this.extensionType = extensionType;
		this.governanceConfiguration = governanceConfiguration;
		this.governanceMetaData = governanceMetaData;
	}

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	public String getGovernanceName() {
		return this.governanceName;
	}

	/**
	 * Obtains the extension type used by the {@link Governance}.
	 * 
	 * @return Extension type used by the {@link Governance}.
	 */
	public Class<E> getExtensionType() {
		return this.extensionType;
	}

	/**
	 * Obtains the index to obtain the {@link Governance} from the
	 * {@link ProcessState}.
	 * 
	 * @return Index to obtain the {@link Governance} from the {@link ProcessState}.
	 */
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	/**
	 * Links the {@link ManagedFunctionMetaData} instances to enable {@link Flow} of
	 * execution.
	 * 
	 * @return <code>true</code> if successfully loaded the {@link OfficeMetaData}.
	 */
	public GovernanceMetaData<E, F> getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	/**
	 * Obtains the {@link GovernanceMetaData}.
	 *
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param flowMetaDataFactory
	 *            {@link FlowMetaDataFactory}.
	 * @param escalationFlowFactory
	 *            {@link EscalationFlowFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link GovernanceMetaData}.
	 */
	public boolean loadOfficeMetaData(OfficeMetaData officeMetaData, FlowMetaDataFactory flowMetaDataFactory,
			EscalationFlowFactory escalationFlowFactory, OfficeFloorIssues issues) {

		// Obtain the listing of flow meta-data
		FlowMetaData[] flowMetaDatas = flowMetaDataFactory.createFlowMetaData(
				this.governanceConfiguration.getFlowConfiguration(), AssetType.GOVERNANCE, this.governanceName, issues);
		if (flowMetaDatas == null) {
			return false; // not loaded
		}

		// Create the escalation procedure
		EscalationFlow[] escalations = escalationFlowFactory.createEscalationFlows(
				this.governanceConfiguration.getEscalations(), AssetType.GOVERNANCE, this.governanceName, issues);
		if (escalations == null) {
			return false; // not loaded
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalations);

		// Load the remaining state
		this.governanceMetaData.loadOfficeMetaData(officeMetaData, flowMetaDatas, escalationProcedure);

		// As here, successful
		return true;
	}

}
