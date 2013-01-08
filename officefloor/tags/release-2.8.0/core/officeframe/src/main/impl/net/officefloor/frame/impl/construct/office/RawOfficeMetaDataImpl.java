/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactoryImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.asset.OfficeManagerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.impl.execute.office.OfficeStartupTaskImpl;
import net.officefloor.frame.impl.execute.process.ProcessMetaDataImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeFloorMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawTeamMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaDataFactory;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;

/**
 * {@link RawOfficeMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeMetaDataImpl implements RawOfficeMetaDataFactory,
		RawOfficeMetaData {

	/**
	 * Obtains the {@link RawOfficeMetaDataFactory}.
	 * 
	 * @return {@link RawOfficeMetaDataFactory}.
	 */
	public static RawOfficeMetaDataFactory getFactory() {
		return new RawOfficeMetaDataImpl(null, null, null, null, null, null,
				null, null, false, null, null, null, null);
	}

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link RawOfficeFloorMetaData} containing this {@link Office}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData;

	/**
	 * {@link TeamManagement} instances by their {@link Office} registered
	 * names.
	 */
	private final Map<String, TeamManagement> teams;

	/**
	 * {@link Team} to enable the worker ({@link Thread}) of the responsible
	 * {@link Team} to continue on to execute the next {@link Job}.
	 */
	private final Team continueTeam;

	/**
	 * {@link RawManagedObjectMetaData} instances by their {@link Office}
	 * registered names.
	 */
	private final Map<String, RawManagedObjectMetaData<?, ?>> managedObjectMetaData;

	/**
	 * {@link ProcessState} {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData[] processBoundManagedObjects;

	/**
	 * {@link ThreadState} {@link RawBoundManagedObjectMetaData}.
	 */
	private final RawBoundManagedObjectMetaData[] threadBoundManagedObjects;

	/**
	 * Scope {@link RawBoundManagedObjectMetaData} of the {@link Office} by the
	 * {@link ProcessState} and {@link ThreadState} bound names.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> scopeMo;

	/**
	 * Indicates whether to manually manage {@link Governance}.
	 */
	private final boolean isManuallyManageGovernance;

	/**
	 * {@link RawGovernanceMetaData} of the {@link Office} by its {@link Office}
	 * registered name.
	 */
	private final Map<String, RawGovernanceMetaData<?, ?>> governanceMetaData;

	/**
	 * {@link ProcessState} {@link RawBoundAdministratorMetaData}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators;

	/**
	 * {@link ThreadState} {@link RawBoundAdministratorMetaData}.
	 */
	private final RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators;

	/**
	 * Scope {@link RawBoundAdministratorMetaData} of the {@link Office} by the
	 * {@link ProcessState} and {@link ThreadState} bound names.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins;

	/**
	 * {@link OfficeMetaData}.
	 */
	private OfficeMetaData officeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            {@link Office} names.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData} containing this {@link Office}.
	 * @param teams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            registered names.
	 * @param continueTeam
	 *            {@link Team} to enable the worker ({@link Thread}) of the
	 *            responsible {@link Team} to continue on to execute the next
	 *            {@link Job}.
	 * @param managedObjectMetaData
	 *            {@link RawManagedObjectMetaData} instances by their
	 *            {@link Office} registered names.
	 * @param processBoundManagedObjects
	 *            {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param threadBoundManagedObjects
	 *            {@link ThreadState} {@link RawBoundManagedObjectMetaData}
	 *            instances.
	 * @param scopeMo
	 *            Scope {@link RawBoundManagedObjectMetaData} of the
	 *            {@link Office} by the {@link ProcessState} and
	 *            {@link ThreadState} bound names.
	 * @param isManuallyManageGovernance
	 *            Indicates whether to manually manage {@link Governance}.
	 * @param governanceMetaData
	 *            {@link RawGovernanceMetaData} of the {@link Office} by its
	 *            {@link Office} registered name.
	 * @param processBoundAdministrators
	 *            {@link ProcessState} {@link RawBoundAdministratorMetaData}
	 *            instances.
	 * @param threadBoundAdministrators
	 *            {@link ThreadState} {@link RawBoundAdministratorMetaData}
	 *            instances.
	 * @param scopeAdmins
	 *            Scope {@link RawBoundAdministratorMetaData} of the
	 *            {@link Office} by the {@link ProcessState} and
	 *            {@link ThreadState} bound names.
	 */
	private RawOfficeMetaDataImpl(String officeName,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			Map<String, TeamManagement> teams, Team continueTeam,
			Map<String, RawManagedObjectMetaData<?, ?>> managedObjectMetaData,
			RawBoundManagedObjectMetaData[] processBoundManagedObjects,
			RawBoundManagedObjectMetaData[] threadBoundManagedObjects,
			Map<String, RawBoundManagedObjectMetaData> scopeMo,
			boolean isManuallyManageGovernance,
			Map<String, RawGovernanceMetaData<?, ?>> governanceMetaData,
			RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators,
			RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators,
			Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins) {
		this.officeName = officeName;
		this.rawOfficeFloorMetaData = rawOfficeFloorMetaData;
		this.teams = teams;
		this.continueTeam = continueTeam;
		this.managedObjectMetaData = managedObjectMetaData;
		this.processBoundManagedObjects = processBoundManagedObjects;
		this.threadBoundManagedObjects = threadBoundManagedObjects;
		this.scopeMo = scopeMo;
		this.isManuallyManageGovernance = isManuallyManageGovernance;
		this.governanceMetaData = governanceMetaData;
		this.processBoundAdministrators = processBoundAdministrators;
		this.threadBoundAdministrators = threadBoundAdministrators;
		this.scopeAdmins = scopeAdmins;
	}

	/*
	 * ============= RawOfficeMetaDataFactory ===========================
	 */

	@Override
	public RawOfficeMetaData constructRawOfficeMetaData(
			OfficeConfiguration configuration, SourceContext sourceContext,
			OfficeFloorIssues issues,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects,
			RawOfficeFloorMetaData rawOfficeFloorMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawGovernanceMetaDataFactory rawGovernanceMetaDataFactory,
			RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory,
			RawWorkMetaDataFactory rawWorkFactory,
			RawTaskMetaDataFactory rawTaskFactory,
			TeamManagement continueTeamManagement) {

		// Obtain the name of the office
		String officeName = configuration.getOfficeName();
		if (ConstructUtil.isBlank(officeName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR,
					OfficeFloor.class.getSimpleName(),
					"Office registered without name");
			return null; // can not continue
		}

		// Obtain the monitor interval for the office manager
		long monitorOfficeInterval = configuration.getMonitorOfficeInterval();
		if (monitorOfficeInterval <= 0) {
			issues.addIssue(AssetType.OFFICE, officeName,
					"Monitor office interval must be greater than zero");
			return null; // can not continue
		}

		// Create the office manager and asset manager factory
		OfficeManagerImpl officeManager = new OfficeManagerImpl(officeName,
				monitorOfficeInterval);
		AssetManagerFactoryImpl officeAssetManagerFactory = new AssetManagerFactoryImpl(
				officeManager);

		// Enhance the office
		OfficeEnhancerContextImpl.enhanceOffice(officeName, configuration,
				issues);

		// Obtain the continue team
		Team continueTeam = continueTeamManagement.getTeam();

		// Register the teams to office
		Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>();
		for (LinkedTeamConfiguration teamConfig : configuration
				.getRegisteredTeams()) {

			// Ensure have office name for team
			String officeTeamName = teamConfig.getOfficeTeamName();
			if (ConstructUtil.isBlank(officeTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Team registered to Office without name");
				continue; // can not register team
			}

			// Ensure have office floor name for team
			String officeFloorTeamName = teamConfig.getOfficeFloorTeamName();
			if (ConstructUtil.isBlank(officeFloorTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No Office Floor Team name for Office Team '"
								+ officeTeamName + "'");
				continue; // can not register team
			}

			// Obtain the team
			RawTeamMetaData rawTeamMetaData = rawOfficeFloorMetaData
					.getRawTeamMetaData(officeFloorTeamName);
			if (rawTeamMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName, "Unknown Team '"
						+ officeFloorTeamName
						+ "' not available to register to Office");
				continue; // can not register team
			}

			// Register the team
			officeTeams
					.put(officeTeamName, rawTeamMetaData.getTeamManagement());
		}

		// Determine if manually manage governance (and deactivation strategy)
		boolean isManuallyManageGovernance = configuration
				.isManuallyManageGovernance();
		GovernanceDeactivationStrategy governanceDeactivationStrategy = (isManuallyManageGovernance ? GovernanceDeactivationStrategy.DISREGARD
				: GovernanceDeactivationStrategy.ENFORCE);

		// Register the governances to office
		GovernanceConfiguration<?, ?>[] governanceConfigurations = configuration
				.getGovernanceConfiguration();
		GovernanceMetaData<?, ?>[] governanceMetaDatas = new GovernanceMetaData[governanceConfigurations.length];
		List<RawGovernanceMetaData<?, ?>> rawGovernanceMetaDataList = new LinkedList<RawGovernanceMetaData<?, ?>>();
		Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData = new HashMap<String, RawGovernanceMetaData<?, ?>>();
		NEXT_GOVERNANCE: for (int i = 0; i < governanceConfigurations.length; i++) {
			GovernanceConfiguration<?, ?> governanceConfiguration = governanceConfigurations[i];

			// Create the raw governance
			RawGovernanceMetaData<?, ?> rawGovernance = rawGovernanceMetaDataFactory
					.createRawGovernanceMetaData(governanceConfiguration, i,
							sourceContext, officeTeams, continueTeam,
							officeName, issues);
			if (rawGovernance == null) {
				// Not able to create governance
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unable to configure governance '"
								+ governanceConfiguration.getGovernanceName()
								+ "'");
				continue NEXT_GOVERNANCE;
			}

			// Register the raw Governance
			rawGovernanceMetaData.put(rawGovernance.getGovernanceName(),
					rawGovernance);
			rawGovernanceMetaDataList.add(rawGovernance);

			// Obtain the Governance and add to listing
			GovernanceMetaData<?, ?> governanceMetaData = rawGovernance
					.getGovernanceMetaData();
			governanceMetaDatas[i] = governanceMetaData;
		}

		// Register the managed object sources to office
		Map<String, RawManagedObjectMetaData<?, ?>> registeredMo = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		for (LinkedManagedObjectSourceConfiguration mos : configuration
				.getRegisteredManagedObjectSources()) {

			// Ensure have office name for managed object
			String moName = mos.getOfficeManagedObjectName();
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Managed Object registered to Office without name");
				continue; // can not register managed object
			}

			// Ensure have name of managed object source
			String mosName = mos.getOfficeFloorManagedObjectSourceName();
			if (ConstructUtil.isBlank(mosName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No Managed Object Source name for Office Managed Object '"
								+ moName + "'");
				continue; // can not register managed object
			}

			// Obtain the raw managed object source meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = rawOfficeFloorMetaData
					.getRawManagedObjectMetaData(mosName);
			if (rawMoMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unknown Managed Object Source '" + mosName
								+ "' not available to register to Office");
				continue; // can not register managed object
			}

			// Register the managed object
			registeredMo.put(moName, rawMoMetaData);
		}

		// Create the bound input managed object mapping
		Map<String, String> boundInputManagedObjects = new HashMap<String, String>();
		BoundInputManagedObjectConfiguration[] boundInputConfigurations = configuration
				.getBoundInputManagedObjectConfiguration();
		if (boundInputConfigurations != null) {
			for (BoundInputManagedObjectConfiguration boundInputConfiguration : boundInputConfigurations) {

				// Obtain the input managed object name
				String inputManagedObjectName = boundInputConfiguration
						.getInputManagedObjectName();
				if (ConstructUtil.isBlank(inputManagedObjectName)) {
					issues.addIssue(AssetType.OFFICE, officeName,
							"No input Managed Object name for binding");
					continue; // can not provide input
				}

				// Obtain the bound managed object source name
				String boundManagedObjectSourceName = boundInputConfiguration
						.getBoundManagedObjectSourceName();
				if (ConstructUtil.isBlank(boundManagedObjectSourceName)) {
					issues.addIssue(AssetType.OFFICE, officeName,
							"No bound Managed Object Source name for input Managed Object '"
									+ inputManagedObjectName + "'");
					continue; // can not provide binding
				}

				// Ensure not already bound input managed object
				if (boundInputManagedObjects
						.containsKey(inputManagedObjectName)) {
					issues.addIssue(AssetType.OFFICE, officeName,
							"Input Managed Object '" + inputManagedObjectName
									+ "' bound more than once");
					continue; // already bound
				}

				// Add the input managed object binding
				boundInputManagedObjects.put(inputManagedObjectName,
						boundManagedObjectSourceName);
			}
		}

		// Obtain the process bound managed object instances
		ManagedObjectConfiguration<?>[] processManagedObjectConfiguration = configuration
				.getProcessManagedObjectConfiguration();
		if (processManagedObjectConfiguration == null) {
			// Provide no process Managed Object configurations
			processManagedObjectConfiguration = new ManagedObjectConfiguration[0];
		}
		final RawBoundManagedObjectMetaData[] processBoundManagedObjects = rawBoundManagedObjectFactory
				.constructBoundManagedObjectMetaData(
						processManagedObjectConfiguration, issues,
						ManagedObjectScope.PROCESS, AssetType.OFFICE,
						officeName, officeAssetManagerFactory, registeredMo,
						null, officeManagingManagedObjects,
						boundInputManagedObjects, rawGovernanceMetaData);

		// Create the map of process bound managed objects by name
		Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<String, RawBoundManagedObjectMetaData>();
		for (RawBoundManagedObjectMetaData mo : processBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the process bound administrator instances
		AdministratorSourceConfiguration<?, ?>[] processAdministratorConfiguration = configuration
				.getProcessAdministratorSourceConfiguration();
		final RawBoundAdministratorMetaData<?, ?>[] processBoundAdministrators;
		if ((processAdministratorConfiguration == null)
				|| (processAdministratorConfiguration.length == 0)) {
			processBoundAdministrators = new RawBoundAdministratorMetaData[0];
		} else {
			processBoundAdministrators = rawBoundAdministratorFactory
					.constructRawBoundAdministratorMetaData(
							processAdministratorConfiguration, sourceContext,
							issues, AdministratorScope.PROCESS,
							AssetType.OFFICE, officeName, officeTeams,
							continueTeam, scopeMo);
		}

		// Create the map of process bound administrators by name
		Map<String, RawBoundAdministratorMetaData<?, ?>> scopeAdmins = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();
		for (RawBoundAdministratorMetaData<?, ?> admin : processBoundAdministrators) {
			scopeAdmins.put(admin.getBoundAdministratorName(), admin);
		}

		// Obtain the thread bound managed object instances
		ManagedObjectConfiguration<?>[] threadManagedObjectConfiguration = configuration
				.getThreadManagedObjectConfiguration();
		final RawBoundManagedObjectMetaData[] threadBoundManagedObjects;
		if ((threadManagedObjectConfiguration == null)
				|| (threadManagedObjectConfiguration.length == 0)) {
			threadBoundManagedObjects = new RawBoundManagedObjectMetaData[0];
		} else {
			threadBoundManagedObjects = rawBoundManagedObjectFactory
					.constructBoundManagedObjectMetaData(
							threadManagedObjectConfiguration, issues,
							ManagedObjectScope.THREAD, AssetType.OFFICE,
							officeName, officeAssetManagerFactory,
							registeredMo, scopeMo, null, null,
							rawGovernanceMetaData);
		}

		// Load the thread bound managed objects to scope managed objects
		for (RawBoundManagedObjectMetaData mo : threadBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the thread bound administrator instances
		AdministratorSourceConfiguration<?, ?>[] threadAdministratorConfiguration = configuration
				.getThreadAdministratorSourceConfiguration();
		final RawBoundAdministratorMetaData<?, ?>[] threadBoundAdministrators;
		if ((threadAdministratorConfiguration == null)
				|| (threadAdministratorConfiguration.length == 0)) {
			threadBoundAdministrators = new RawBoundAdministratorMetaData[0];
		} else {
			threadBoundAdministrators = rawBoundAdministratorFactory
					.constructRawBoundAdministratorMetaData(
							threadAdministratorConfiguration, sourceContext,
							issues, AdministratorScope.THREAD,
							AssetType.OFFICE, officeName, officeTeams,
							continueTeam, scopeMo);
		}

		// Load the thread bound administrators to scope administrators
		for (RawBoundAdministratorMetaData<?, ?> admin : threadBoundAdministrators) {
			scopeAdmins.put(admin.getBoundAdministratorName(), admin);
		}

		// Create the raw office meta-data
		RawOfficeMetaDataImpl rawOfficeMetaData = new RawOfficeMetaDataImpl(
				officeName, rawOfficeFloorMetaData, officeTeams, continueTeam,
				registeredMo, processBoundManagedObjects,
				threadBoundManagedObjects, scopeMo, isManuallyManageGovernance,
				rawGovernanceMetaData, processBoundAdministrators,
				threadBoundAdministrators, scopeAdmins);

		// Construct the meta-data of the work carried out within the office
		List<RawWorkMetaData<?>> rawWorkMetaDatas = new LinkedList<RawWorkMetaData<?>>();
		List<WorkMetaData<?>> workMetaDatas = new LinkedList<WorkMetaData<?>>();
		for (WorkConfiguration<?> workConfiguration : configuration
				.getWorkConfiguration()) {

			// Construct the work
			RawWorkMetaData<?> rawWorkMetaData = rawWorkFactory
					.constructRawWorkMetaData(workConfiguration, sourceContext,
							issues, rawOfficeMetaData,
							officeAssetManagerFactory,
							rawBoundManagedObjectFactory,
							rawBoundAdministratorFactory, rawTaskFactory);
			if (rawWorkMetaData == null) {
				continue; // issue in constructing work
			}

			// Obtain the work meta-data and register
			WorkMetaData<?> workMetaData = rawWorkMetaData.getWorkMetaData();
			rawWorkMetaDatas.add(rawWorkMetaData);
			workMetaDatas.add(workMetaData);
		}

		// Create the listing of startup tasks to later populate
		TaskNodeReference[] startupTaskReferences = configuration
				.getStartupTasks();
		int startupTasksLength = (startupTaskReferences == null ? 0
				: startupTaskReferences.length);
		OfficeStartupTask[] startupTasks = new OfficeStartupTask[startupTasksLength];

		// Create the listing of escalations to later populate
		TaskEscalationConfiguration[] officeEscalationConfigurations = configuration
				.getEscalationConfiguration();
		int officeEscalationsLength = (officeEscalationConfigurations == null ? 0
				: officeEscalationConfigurations.length);
		EscalationFlow[] officeEscalations = new EscalationFlow[officeEscalationsLength];
		EscalationProcedure officeEscalationProcedure = new EscalationProcedureImpl(
				officeEscalations);

		// Obtain the office floor escalation
		EscalationFlow officeFloorEscalation = rawOfficeFloorMetaData
				.getOfficeFloorEscalation();

		// Create the thread meta-data
		ThreadMetaData threadMetaData = new ThreadMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(threadBoundManagedObjects),
				governanceMetaDatas,
				this.constructAdministratorMetaData(threadBoundAdministrators),
				governanceDeactivationStrategy);

		// Create the process meta-data
		ProcessMetaData processMetaData = new ProcessMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(processBoundManagedObjects),
				this.constructAdministratorMetaData(processBoundAdministrators),
				threadMetaData);

		// Obtain the Process Context Listeners
		ProcessContextListener[] processContextListeners = rawOfficeFloorMetaData
				.getProcessContextListeners();

		// Obtain the profiler
		Profiler profiler = configuration.getProfiler();

		// Create the office meta-data
		rawOfficeMetaData.officeMetaData = new OfficeMetaDataImpl(officeName,
				officeManager, workMetaDatas.toArray(new WorkMetaData[0]),
				processMetaData, processContextListeners, startupTasks,
				officeEscalationProcedure, officeFloorEscalation, profiler);

		// Create the meta-data locator
		OfficeMetaDataLocator metaDataLocator = new OfficeMetaDataLocatorImpl(
				rawOfficeMetaData.officeMetaData);

		// Load the startup tasks
		for (int i = 0; i < startupTasksLength; i++) {

			// Obtain the task meta-data for the startup task
			TaskMetaData<?, ?, ?> startupTaskMetaData = ConstructUtil
					.getTaskMetaData(startupTaskReferences[i], metaDataLocator,
							issues, AssetType.OFFICE, officeName,
							"Startup Task " + i, true);
			if (startupTaskMetaData == null) {
				continue; // startup task not found
			}

			// Create the startup flow meta-data.
			// Always asynchronous as runs in own process.
			FlowMetaData<?> startupFlow = ConstructUtil.newFlowMetaData(
					FlowInstigationStrategyEnum.ASYNCHRONOUS,
					startupTaskMetaData, officeAssetManagerFactory,
					AssetType.OFFICE, officeName, "StartupTask" + i, issues);

			// TODO consider providing a parameter to the startup task
			Object parameter = null;

			// Create and load the startup task
			startupTasks[i] = new OfficeStartupTaskImpl(startupFlow, parameter);
		}

		// Load the office escalations
		for (int i = 0; i < officeEscalationsLength; i++) {
			TaskEscalationConfiguration escalationConfiguration = officeEscalationConfigurations[i];

			// Obtain the type of issue being handled by escalation
			Class<? extends Throwable> typeOfCause = escalationConfiguration
					.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Type of cause not provided for office escalation " + i);
				continue; // must type type of cause
			}

			// Obtain the task meta-data for the escalation
			TaskMetaData<?, ?, ?> escalationTaskMetaData = ConstructUtil
					.getTaskMetaData(officeEscalationConfigurations[i]
							.getTaskNodeReference(), metaDataLocator, issues,
							AssetType.OFFICE, officeName, "Office Escalation "
									+ i, true);
			if (escalationTaskMetaData == null) {
				continue; // escalation task not found
			}

			// Create and load the escalation
			officeEscalations[i] = new EscalationFlowImpl(typeOfCause,
					escalationTaskMetaData);
		}

		// Link tasks within the meta-data of the office
		for (RawWorkMetaData<?> rawWorkMetaData : rawWorkMetaDatas) {
			rawWorkMetaData.linkOfficeMetaData(metaDataLocator,
					officeAssetManagerFactory, issues);
		}
		this.linkTasks(metaDataLocator, officeAssetManagerFactory,
				threadBoundAdministrators, issues);
		this.linkTasks(metaDataLocator, officeAssetManagerFactory,
				processBoundAdministrators, issues);

		// Link tasks for Governance
		for (RawGovernanceMetaData<?, ?> rawGovernance : rawGovernanceMetaDataList) {
			rawGovernance.linkOfficeMetaData(metaDataLocator,
					officeAssetManagerFactory, issues);
		}

		// Have the managed objects managed by the office
		for (RawManagingOfficeMetaData<?> officeManagingManagedObject : officeManagingManagedObjects) {
			officeManagingManagedObject.manageByOffice(
					processBoundManagedObjects, metaDataLocator, officeTeams,
					continueTeamManagement, officeAssetManagerFactory, issues);
		}

		// Return the raw office meta-data
		return rawOfficeMetaData;
	}

	/**
	 * Constructs the default {@link ManagedObjectMetaData} listing from the
	 * input {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param rawBoundManagedObjects
	 *            {@link RawBoundManagedObjectMetaData} instances.
	 * @return Default {@link ManagedObjectMetaData} instances.
	 */
	private ManagedObjectMetaData<?>[] constructDefaultManagedObjectMetaData(
			RawBoundManagedObjectMetaData[] rawBoundManagedObjects) {
		ManagedObjectMetaData<?>[] moMetaData = new ManagedObjectMetaData[rawBoundManagedObjects.length];
		for (int i = 0; i < moMetaData.length; i++) {
			RawBoundManagedObjectMetaData boundMetaData = rawBoundManagedObjects[i];

			// Obtain the default managed object meta-data
			int defaultInstanceIndex = boundMetaData.getDefaultInstanceIndex();
			if (defaultInstanceIndex < 0) {
				continue; // issue obtaining bound instance
			}

			// Obtain the bound instance meta-data
			RawBoundManagedObjectInstanceMetaData<?> boundMetaDataInstance = boundMetaData
					.getRawBoundManagedObjectInstanceMetaData()[defaultInstanceIndex];

			// Load the default managed object meta-data
			moMetaData[i] = boundMetaDataInstance.getManagedObjectMetaData();
		}
		return moMetaData;
	}

	/**
	 * Constructs the {@link AdministratorMetaData} listing from the input
	 * {@link RawBoundAdministratorMetaData} instances.
	 * 
	 * @param rawBoundAdministratorMetaData
	 *            {@link RawBoundAdministratorMetaData} instances.
	 * @return {@link AdministratorMetaData} instances.
	 */
	private AdministratorMetaData<?, ?>[] constructAdministratorMetaData(
			RawBoundAdministratorMetaData<?, ?>[] rawBoundAdministratorMetaData) {
		AdministratorMetaData<?, ?>[] adminMetaData = new AdministratorMetaData[rawBoundAdministratorMetaData.length];
		for (int i = 0; i < adminMetaData.length; i++) {
			adminMetaData[i] = rawBoundAdministratorMetaData[i]
					.getAdministratorMetaData();
		}
		return adminMetaData;
	}

	/**
	 * Links the {@link TaskMetaData} instances into the
	 * {@link RawBoundAdministratorMetaData} instances.
	 * 
	 * @param taskMetaDataLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param rawBoundManagedObjects
	 *            {@link RawBoundAdministratorMetaData} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	private void linkTasks(OfficeMetaDataLocator taskMetaDataLocator,
			AssetManagerFactory assetManagerFactory,
			RawBoundAdministratorMetaData<?, ?>[] rawBoundAdministrators,
			OfficeFloorIssues issues) {
		for (RawBoundAdministratorMetaData<?, ?> rawBoundAdministrator : rawBoundAdministrators) {
			rawBoundAdministrator.linkOfficeMetaData(taskMetaDataLocator,
					assetManagerFactory, issues);
		}
	}

	/*
	 * ============= RawOfficeMetaData =======================================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public RawOfficeFloorMetaData getRawOfficeFloorMetaData() {
		return this.rawOfficeFloorMetaData;
	}

	@Override
	public Map<String, TeamManagement> getTeams() {
		return this.teams;
	}

	@Override
	public Team getContinueTeam() {
		return this.continueTeam;
	}

	@Override
	public Map<String, RawManagedObjectMetaData<?, ?>> getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public RawBoundManagedObjectMetaData[] getProcessBoundManagedObjects() {
		return this.processBoundManagedObjects;
	}

	@Override
	public RawBoundManagedObjectMetaData[] getThreadBoundManagedObjects() {
		return this.threadBoundManagedObjects;
	}

	@Override
	public boolean isManuallyManageGovernance() {
		return this.isManuallyManageGovernance;
	}

	@Override
	public Map<String, RawGovernanceMetaData<?, ?>> getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public Map<String, RawBoundManagedObjectMetaData> getOfficeScopeManagedObjects() {
		return this.scopeMo;
	}

	@Override
	public RawBoundAdministratorMetaData<?, ?>[] getProcessBoundAdministrators() {
		return this.processBoundAdministrators;
	}

	@Override
	public RawBoundAdministratorMetaData<?, ?>[] getThreadBoundAdministrators() {
		return this.threadBoundAdministrators;
	}

	@Override
	public Map<String, RawBoundAdministratorMetaData<?, ?>> getOfficeScopeAdministrators() {
		return this.scopeAdmins;
	}

	@Override
	public OfficeMetaData getOfficeMetaData() {
		return this.officeMetaData;
	}

}