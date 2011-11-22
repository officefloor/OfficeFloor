/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.governance;

import java.util.Map;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataImpl<I, F extends Enum<F>> implements
		RawGovernanceMetaDataFactory, RawGovernanceMetaData<I, F> {

	/**
	 * Obtains the {@link RawGovernanceMetaDataFactory}.
	 * 
	 * @return {@link RawGovernanceMetaDataFactory}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RawGovernanceMetaDataFactory getFactory() {
		return new RawGovernanceMetaDataImpl(null, -1, null, null, null);
	}

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Index of this {@link RawGovernanceMetaData} within the
	 * {@link ProcessState}.
	 */
	private final int governanceIndex;

	/**
	 * Extension interface type.
	 */
	private final Class<I> extensionInterfaceType;

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private final GovernanceConfiguration<I, F> governanceConfiguration;

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaDataImpl<I, F> governanceMetaData;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceIndex
	 *            Index of this {@link RawGovernanceMetaData} within the
	 *            {@link ProcessState}.
	 * @param extensionInterfaceType
	 *            Extension interface type.
	 * @param governanceConfiguration
	 *            {@link GovernanceConfiguration}.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaDataImpl}.
	 */
	public RawGovernanceMetaDataImpl(String governanceName,
			int governanceIndex, Class<I> extensionInterfaceType,
			GovernanceConfiguration<I, F> governanceConfiguration,
			GovernanceMetaDataImpl<I, F> governanceMetaData) {
		this.governanceName = governanceName;
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceType = extensionInterfaceType;
		this.governanceConfiguration = governanceConfiguration;
		this.governanceMetaData = governanceMetaData;
	}

	/*
	 * ==================== RawGovernanceMetaDataFactory ==================
	 */

	@Override
	public <i, f extends Enum<f>> RawGovernanceMetaData<i, f> createRawGovernanceMetaData(
			GovernanceConfiguration<i, f> configuration, int governanceIndex,
			SourceContext sourceContext, Map<String, Team> officeTeams,
			String officeName, OfficeFloorIssues issues) {

		// Obtain the governance name
		String governanceName = configuration.getGovernanceName();
		if (ConstructUtil.isBlank(governanceName)) {
			issues.addIssue(AssetType.OFFICE, officeName,
					"Governance added without a name");
			return null; // can not carry on
		}

		// Obtain the governance factory
		GovernanceFactory<? super i, f> governanceFactory = configuration
				.getGovernanceFactory();
		if (governanceFactory == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName, "No "
					+ GovernanceFactory.class.getSimpleName() + " provided");
			return null; // can not carry on
		}

		// Obtain the extension interface type
		Class<i> extensionInterfaceType = configuration.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No extension interface type provided");
			return null; // can not carry on
		}

		// Obtain the team name for the governance
		String teamName = configuration.getTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(
					AssetType.GOVERNANCE,
					governanceName,
					"Must specify " + Team.class.getSimpleName()
							+ " responsible for "
							+ Governance.class.getSimpleName() + " activities");
			return null; // can not carry on
		}

		// Obtain the team
		Team team = officeTeams.get(teamName);
		if (team == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Can not find " + Team.class.getSimpleName() + " by name '"
							+ teamName + "'");
			return null; // can not carry on
		}

		// Create the Governance Meta-Data
		GovernanceMetaDataImpl<i, f> governanceMetaData = new GovernanceMetaDataImpl<i, f>(
				governanceName, governanceFactory, team);

		// Create the raw Governance meta-data
		RawGovernanceMetaData<i, f> rawGovernanceMetaData = new RawGovernanceMetaDataImpl<i, f>(
				governanceName, governanceIndex, extensionInterfaceType,
				configuration, governanceMetaData);

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

	/*
	 * =================== RawGovernanceMetaData ==================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public Class<I> getExtensionInterfaceType() {
		return this.extensionInterfaceType;
	}

	@Override
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	@Override
	public GovernanceMetaData<I, F> getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public void linkOfficeMetaData(OfficeMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the listing of flow meta-data
		GovernanceFlowConfiguration<F>[] flowConfigurations = this.governanceConfiguration
				.getFlowConfiguration();
		FlowMetaData<?>[] flowMetaDatas = new FlowMetaData[flowConfigurations.length];
		for (int i = 0; i < flowMetaDatas.length; i++) {
			GovernanceFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

			// Ensure have flow configuration
			if (flowConfiguration == null) {
				continue;
			}

			// Obtain the task reference
			TaskNodeReference taskNodeReference = flowConfiguration
					.getInitialTask();
			if (taskNodeReference == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.governanceName,
						"No task referenced for flow index " + i);
				continue; // no reference task for flow
			}

			// Obtain the task meta-data
			TaskMetaData<?, ?, ?> taskMetaData = ConstructUtil.getTaskMetaData(
					taskNodeReference, taskLocator, issues,
					AssetType.GOVERNANCE, this.governanceName, "flow index "
							+ i, true);
			if (taskMetaData == null) {
				continue; // no initial task for flow
			}

			// Obtain the flow instigation strategy
			FlowInstigationStrategyEnum instigationStrategy = flowConfiguration
					.getInstigationStrategy();
			if (instigationStrategy == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.governanceName,
						"No instigation strategy provided for flow index " + i);
				continue; // no instigation strategy
			}

			// Create and add the flow meta-data
			flowMetaDatas[i] = ConstructUtil.newFlowMetaData(
					instigationStrategy, taskMetaData, assetManagerFactory,
					AssetType.GOVERNANCE, this.governanceName, "Flow" + i,
					issues);
		}

		// Create the escalation procedure
		GovernanceEscalationConfiguration[] escalationConfigurations = this.governanceConfiguration
				.getEscalations();
		EscalationFlow[] escalations = new EscalationFlow[escalationConfigurations.length];
		for (int i = 0; i < escalations.length; i++) {
			GovernanceEscalationConfiguration escalationConfiguration = escalationConfigurations[i];

			// Obtain the type of cause
			Class<? extends Throwable> typeOfCause = escalationConfiguration
					.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.getGovernanceName(),
						"No escalation type for escalation index " + i);
				continue; // no escalation type
			}

			// Obtain the escalation handler
			TaskNodeReference escalationReference = escalationConfiguration
					.getTaskNodeReference();
			if (escalationReference == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.getGovernanceName(),
						"No task referenced for escalation index " + i);
				continue; // no escalation handler referenced
			}
			TaskMetaData<?, ?, ?> escalationTaskMetaData = ConstructUtil
					.getTaskMetaData(escalationReference, taskLocator, issues,
							AssetType.GOVERNANCE, this.getGovernanceName(),
							"escalation index " + i, true);
			if (escalationTaskMetaData == null) {
				continue; // no escalation handler
			}

			// Create and add the escalation
			escalations[i] = new EscalationFlowImpl(typeOfCause,
					escalationTaskMetaData);
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(
				escalations);

		// Load the remaining state
		this.governanceMetaData.loadRemainingState(flowMetaDatas,
				escalationProcedure);
	}

}