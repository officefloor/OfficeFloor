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

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.governance.ActivateGovernanceTask;
import net.officefloor.frame.impl.execute.governance.DisregardGovernanceTask;
import net.officefloor.frame.impl.execute.governance.EnforceGovernanceTask;
import net.officefloor.frame.impl.execute.governance.GovernGovernanceTask;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.impl.execute.governance.GovernanceTaskDependency;
import net.officefloor.frame.impl.execute.governance.GovernanceWork;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.ActiveGovernanceControl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
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
	 * Name of the {@link Task} to activate {@link Governance}.
	 */
	private static final String TASK_ACTIVATE = "ACTIVATE";

	/**
	 * Name of the {@link Task} to provide {@link Governance} over a
	 * {@link ManagedObject}.
	 */
	private static final String TASK_GOVERN = "GOVERN";

	/**
	 * Name of the {@link Task} to activate {@link Governance}.
	 */
	private static final String TASK_ENFORCE = "ENFORCE";

	/**
	 * Name of the {@link Task} to activate {@link Governance}.
	 */
	private static final String TASK_DISREGARD = "DISREGARD";

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
	 * Name of {@link Work} for the {@link Governance}.
	 */
	private final String workName;

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
	 * @param workName
	 *            Name of {@link Work} for the {@link Governance}.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaDataImpl}.
	 */
	public RawGovernanceMetaDataImpl(String governanceName,
			int governanceIndex, Class<I> extensionInterfaceType,
			String workName, GovernanceMetaDataImpl<I, F> governanceMetaData) {
		this.governanceName = governanceName;
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceType = extensionInterfaceType;
		this.workName = workName;
		this.governanceMetaData = governanceMetaData;
	}

	/*
	 * ==================== RawGovernanceMetaDataFactory ==================
	 */

	@Override
	public <i, f extends Enum<f>> RawGovernanceMetaData<i, f> createRawGovernanceMetaData(
			GovernanceConfiguration<i, f> configuration, int governanceIndex,
			SourceContext sourceContext, String officeName,
			OfficeBuilder officeBuilder, OfficeFloorIssues issues) {

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
		Class<i> extensionInterfaceType = configuration
				.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No extension interface type provided");
			return null; // can not carry on
		}

		// Obtain the team name for the governance
		String teamName = configuration.getTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Must specify Team responsible for executing Governance");
			return null; // can not carry on
		}

		// Create the work for the governance tasks
		final String governanceWorkName = "GOVERNANCE_" + governanceName;
		WorkBuilder<GovernanceWork> workBuilder = officeBuilder.addWork(
				governanceWorkName, new GovernanceWork());

		// Register the governance tasks
		this.registerGovernanceTask(workBuilder, TASK_ACTIVATE,
				new ActivateGovernanceTask<f>(), teamName,
				GovernanceControl.class);
		this.registerGovernanceTask(workBuilder, TASK_GOVERN,
				new GovernGovernanceTask<f>(), teamName,
				ActiveGovernanceControl.class);
		this.registerGovernanceTask(workBuilder, TASK_ENFORCE,
				new EnforceGovernanceTask<f>(), teamName,
				GovernanceControl.class);
		this.registerGovernanceTask(workBuilder, TASK_DISREGARD,
				new DisregardGovernanceTask<f>(), teamName,
				GovernanceControl.class);

		// Create the Governance Meta-Data
		GovernanceMetaDataImpl<i, f> governanceMetaData = new GovernanceMetaDataImpl<i, f>(
				governanceName, governanceFactory);

		// Create the raw Governance meta-data
		RawGovernanceMetaData<i, f> rawGovernanceMetaData = new RawGovernanceMetaDataImpl<i, f>(
				governanceName, governanceIndex, extensionInterfaceType,
				governanceWorkName, governanceMetaData);

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

	/**
	 * Registers the {@link Governance} {@link Task}.
	 * 
	 * @param workBuilder
	 *            {@link WorkBuilder} of the {@link Governance} {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param taskFactory
	 *            {@link TaskFactory}.
	 * @param teamName
	 *            Name of the {@link Team} to execute the {@link Governance}
	 *            {@link Task}.
	 * @param controlType
	 *            {@link GovernanceControl} or {@link ActiveGovernanceControl}
	 *            type.
	 */
	private <f extends Enum<f>> void registerGovernanceTask(
			WorkBuilder<GovernanceWork> workBuilder,
			String taskName,
			TaskFactory<GovernanceWork, GovernanceTaskDependency, f> taskFactory,
			String teamName, Class<?> controlType) {
		TaskBuilder<GovernanceWork, GovernanceTaskDependency, f> taskBuilder = workBuilder
				.addTask(taskName, taskFactory);
		taskBuilder.setTeam(teamName);
		taskBuilder.linkParameter(GovernanceTaskDependency.GOVERNANCE_CONTROL,
				controlType);
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

		// Locate the tasks
		FlowMetaData<?> activateFlow = this.getFlowMetaData(TASK_ACTIVATE,
				taskLocator, assetManagerFactory, issues);
		FlowMetaData<?> governFlow = this.getFlowMetaData(TASK_GOVERN,
				taskLocator, assetManagerFactory, issues);
		FlowMetaData<?> enforceFlow = this.getFlowMetaData(TASK_ENFORCE,
				taskLocator, assetManagerFactory, issues);
		FlowMetaData<?> disregardFlow = this.getFlowMetaData(TASK_DISREGARD,
				taskLocator, assetManagerFactory, issues);

		// Load flows into governance meta-data
		this.governanceMetaData.loadFlows(activateFlow, governFlow,
				enforceFlow, disregardFlow);
	}

	/**
	 * Obtains the {@link FlowMetaData}.
	 * 
	 * @param taskName
	 *            Name of {@link Governance} {@link Task}.
	 * @param taskLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link FlowMetaData} for the {@link Governance} {@link Task}.
	 */
	private FlowMetaData<?> getFlowMetaData(String taskName,
			OfficeMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues) {

		// Obtain the task meta-data
		TaskMetaData<?, ?, ?> taskMetaData = taskLocator.getTaskMetaData(
				this.workName, taskName);
		if (taskMetaData == null) {
			issues.addIssue(AssetType.GOVERNANCE, this.governanceName,
					"Can not obtain " + Governance.class.getSimpleName() + " "
							+ Task.class.getSimpleName() + " " + taskName);
			return null; // no flow meta-data for task
		}

		// Create the flow meta-data
		FlowMetaData<?> flowMetaData = ConstructUtil.newFlowMetaData(
				FlowInstigationStrategyEnum.PARALLEL, taskMetaData,
				assetManagerFactory, AssetType.GOVERNANCE, this.governanceName,
				this.workName + "-" + taskName, issues);

		// Return the flow meta-data
		return flowMetaData;
	}

}