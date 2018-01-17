/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
import java.util.Timer;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaDataFactory;
import net.officefloor.frame.impl.construct.managedfunction.RawManagedFunctionMetaData;
import net.officefloor.frame.impl.construct.managedfunction.RawManagedFunctionMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.asset.OfficeClockImpl;
import net.officefloor.frame.impl.execute.asset.OfficeManagerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.office.OfficeManagerProcessState;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.impl.execute.office.OfficeStartupFunctionImpl;
import net.officefloor.frame.impl.execute.process.ProcessMetaDataImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.internal.structure.ThreadMetaData;

/**
 * Factory for creating the {@link RawOfficeMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeMetaDataFactory {

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param configuration
	 *            {@link OfficeConfiguration}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param officeManagingManagedObjects
	 *            {@link RawManagingOfficeMetaData} instances.
	 * @param rawOfficeFloorMetaData
	 *            {@link RawOfficeFloorMetaData}.
	 * @param rawBoundManagedObjectFactory
	 *            {@link RawBoundManagedObjectMetaDataFactory}.
	 * @param rawGovernanceMetaDataFactory
	 *            {@link RawGovernanceMetaDataFactory}.
	 * @param rawBoundAdministratorFactory
	 *            {@link RawAdministrationMetaDataFactory}.
	 * @param rawFunctionFactory
	 *            {@link RawManagedFunctionMetaDataFactory}.
	 * @return {@link RawOfficeMetaData}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RawOfficeMetaData constructRawOfficeMetaData(OfficeConfiguration configuration, OfficeFloorIssues issues,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects, RawOfficeFloorMetaData rawOfficeFloorMetaData,
			RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory,
			RawGovernanceMetaDataFactory rawGovernanceFactory, RawAdministrationMetaDataFactory rawAdminFactory,
			RawManagedFunctionMetaDataFactory rawFunctionFactory) {

		// Obtain the name of the office
		String officeName = configuration.getOfficeName();
		if (ConstructUtil.isBlank(officeName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, OfficeFloor.class.getSimpleName(),
					"Office registered without name");
			return null; // can not continue
		}

		// Obtain the monitor interval for the office manager
		long monitorOfficeInterval = configuration.getMonitorOfficeInterval();
		if (monitorOfficeInterval < 0) {
			issues.addIssue(AssetType.OFFICE, officeName, "Monitor office interval can not be negative");
			return null; // can not continue
		}

		// Obtain the maximum function state chain length
		int maxFunctionChainLength = configuration.getMaximumFunctionStateChainLength();
		if (maxFunctionChainLength <= 0) {
			issues.addIssue(AssetType.OFFICE, officeName,
					"Maximum " + FunctionState.class.getSimpleName() + " chain length must be positive");
			return null; // can not continue
		}

		// Enhance the office
		OfficeEnhancerContextImpl.enhanceOffice(officeName, configuration, issues);

		// Register the teams to office
		boolean isRequireThreadLocalAwareness = false;
		Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>();
		for (LinkedTeamConfiguration teamConfig : configuration.getRegisteredTeams()) {

			// Ensure have office name for team
			String officeTeamName = teamConfig.getOfficeTeamName();
			if (ConstructUtil.isBlank(officeTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName, "Team registered to Office without name");
				return null; // can not register team
			}

			// Ensure have OfficeFloor name for team
			String officeFloorTeamName = teamConfig.getOfficeFloorTeamName();
			if (ConstructUtil.isBlank(officeFloorTeamName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No OfficeFloor Team name for Office Team '" + officeTeamName + "'");
				return null; // can not register team
			}

			// Obtain the team
			RawTeamMetaData rawTeamMetaData = rawOfficeFloorMetaData.getRawTeamMetaData(officeFloorTeamName);
			if (rawTeamMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unknown Team '" + officeFloorTeamName + "' not available to register to Office");
				return null; // can not register team
			}

			// Determine if requires thread local awareness
			if (rawTeamMetaData.isRequireThreadLocalAwareness()) {
				isRequireThreadLocalAwareness = true;
			}

			// Register the team
			officeTeams.put(officeTeamName, rawTeamMetaData.getTeamManagement());
		}

		// Obtain the default team for the office
		TeamManagement defaultTeam = null;
		String officeDefaultTeamName = configuration.getOfficeDefaultTeamName();
		if (officeDefaultTeamName != null) {
			// Use the specified default team
			defaultTeam = officeTeams.get(officeDefaultTeamName);
			if (defaultTeam == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No default team " + officeDefaultTeamName + " linked to Office");
				return null;
			}
		}

		// Obtain the break chain team
		TeamManagement breakChainTeam = rawOfficeFloorMetaData.getBreakChainTeamManagement();

		// Obtain the thread local aware executor (if required)
		ThreadLocalAwareExecutor threadLocalAwareExecutor = null;
		if (isRequireThreadLocalAwareness) {
			threadLocalAwareExecutor = rawOfficeFloorMetaData.getThreadLocalAwareExecutor();
		}

		// Create the office details
		OfficeClockImpl officeClockImpl = null;
		OfficeClock officeClock = configuration.getOfficeClock();
		if (officeClock == null) {
			// Default the office clock
			officeClockImpl = new OfficeClockImpl();
			officeClock = officeClockImpl;
		}
		FunctionLoop functionLoop = new FunctionLoopImpl(defaultTeam);
		Timer timer = new Timer(true);

		// Create the office manager process state
		OfficeManagerProcessState officeManagerProcessState = new OfficeManagerProcessState(officeClock,
				maxFunctionChainLength, breakChainTeam, functionLoop);

		// Create the asset manager factory
		AssetManagerFactory officeAssetManagerFactory = new AssetManagerFactory(officeManagerProcessState, officeClock,
				functionLoop);

		// Determine if manually manage governance
		boolean isManuallyManageGovernance = configuration.isManuallyManageGovernance();

		// Register the governances to office
		GovernanceConfiguration<?, ?>[] governanceConfigurations = configuration.getGovernanceConfiguration();
		GovernanceMetaData<?, ?>[] governanceMetaDatas = new GovernanceMetaData[governanceConfigurations.length];
		List<RawGovernanceMetaData<?, ?>> rawGovernanceMetaDataList = new LinkedList<RawGovernanceMetaData<?, ?>>();
		Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData = new HashMap<String, RawGovernanceMetaData<?, ?>>();
		NEXT_GOVERNANCE: for (int i = 0; i < governanceConfigurations.length; i++) {
			GovernanceConfiguration governanceConfiguration = governanceConfigurations[i];

			// Create the raw governance
			RawGovernanceMetaData<?, ?> rawGovernance = rawGovernanceFactory
					.createRawGovernanceMetaData(governanceConfiguration, i, officeTeams, officeName, issues);
			if (rawGovernance == null) {
				// Not able to create governance
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unable to configure governance '" + governanceConfiguration.getGovernanceName() + "'");
				continue NEXT_GOVERNANCE;
			}

			// Register the raw Governance
			rawGovernanceMetaData.put(rawGovernance.getGovernanceName(), rawGovernance);
			rawGovernanceMetaDataList.add(rawGovernance);

			// Obtain the Governance and add to listing
			GovernanceMetaData<?, ?> governanceMetaData = rawGovernance.getGovernanceMetaData();
			governanceMetaDatas[i] = governanceMetaData;
		}

		// Register the managed object sources to office
		Map<String, RawManagedObjectMetaData<?, ?>> registeredMo = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		for (LinkedManagedObjectSourceConfiguration mos : configuration.getRegisteredManagedObjectSources()) {

			// Ensure have office name for managed object
			String moName = mos.getOfficeManagedObjectName();
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(AssetType.OFFICE, officeName, "Managed Object registered to Office without name");
				continue; // can not register managed object
			}

			// Ensure have name of managed object source
			String mosName = mos.getOfficeFloorManagedObjectSourceName();
			if (ConstructUtil.isBlank(mosName)) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"No Managed Object Source name for Office Managed Object '" + moName + "'");
				continue; // can not register managed object
			}

			// Obtain the raw managed object source meta-data
			RawManagedObjectMetaData<?, ?> rawMoMetaData = rawOfficeFloorMetaData.getRawManagedObjectMetaData(mosName);
			if (rawMoMetaData == null) {
				issues.addIssue(AssetType.OFFICE, officeName,
						"Unknown Managed Object Source '" + mosName + "' not available to register to Office");
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
				String inputManagedObjectName = boundInputConfiguration.getInputManagedObjectName();
				if (ConstructUtil.isBlank(inputManagedObjectName)) {
					issues.addIssue(AssetType.OFFICE, officeName, "No input Managed Object name for binding");
					continue; // can not provide input
				}

				// Obtain the bound managed object source name
				String boundManagedObjectSourceName = boundInputConfiguration.getBoundManagedObjectSourceName();
				if (ConstructUtil.isBlank(boundManagedObjectSourceName)) {
					issues.addIssue(AssetType.OFFICE, officeName,
							"No bound Managed Object Source name for input Managed Object '" + inputManagedObjectName
									+ "'");
					continue; // can not provide binding
				}

				// Ensure not already bound input managed object
				if (boundInputManagedObjects.containsKey(inputManagedObjectName)) {
					issues.addIssue(AssetType.OFFICE, officeName,
							"Input Managed Object '" + inputManagedObjectName + "' bound more than once");
					continue; // already bound
				}

				// Add the input managed object binding
				boundInputManagedObjects.put(inputManagedObjectName, boundManagedObjectSourceName);
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
				.constructBoundManagedObjectMetaData(processManagedObjectConfiguration, issues,
						ManagedObjectScope.PROCESS, AssetType.OFFICE, officeName, officeAssetManagerFactory,
						registeredMo, null, officeManagingManagedObjects, boundInputManagedObjects,
						rawGovernanceMetaData);

		// Create the map of process bound managed objects by name
		Map<String, RawBoundManagedObjectMetaData> scopeMo = new HashMap<String, RawBoundManagedObjectMetaData>();
		for (RawBoundManagedObjectMetaData mo : processBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the thread bound managed object instances
		ManagedObjectConfiguration<?>[] threadManagedObjectConfiguration = configuration
				.getThreadManagedObjectConfiguration();
		final RawBoundManagedObjectMetaData[] threadBoundManagedObjects;
		if ((threadManagedObjectConfiguration == null) || (threadManagedObjectConfiguration.length == 0)) {
			threadBoundManagedObjects = new RawBoundManagedObjectMetaData[0];
		} else {
			threadBoundManagedObjects = rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(
					threadManagedObjectConfiguration, issues, ManagedObjectScope.THREAD, AssetType.OFFICE, officeName,
					officeAssetManagerFactory, registeredMo, scopeMo, null, null, rawGovernanceMetaData);
		}

		// Load the thread bound managed objects to scope managed objects
		for (RawBoundManagedObjectMetaData mo : threadBoundManagedObjects) {
			scopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Create the raw office meta-data
		RawOfficeMetaData rawOfficeMetaData = new RawOfficeMetaData(officeName, rawOfficeFloorMetaData, officeTeams,
				registeredMo, processBoundManagedObjects, threadBoundManagedObjects, scopeMo,
				isManuallyManageGovernance, rawGovernanceMetaData);

		// Construct the meta-data of the managed functions within the office
		List<RawManagedFunctionMetaData<?, ?>> rawFunctionMetaDatas = new LinkedList<>();
		List<ManagedFunctionMetaData<?, ?>> functionMetaDatas = new LinkedList<>();
		for (ManagedFunctionConfiguration<?, ?> functionConfiguration : configuration
				.getManagedFunctionConfiguration()) {

			// Construct the managed function
			RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = rawFunctionFactory
					.constructRawManagedFunctionMetaData(functionConfiguration, rawOfficeMetaData,
							officeAssetManagerFactory, rawBoundManagedObjectFactory, issues);
			if (rawFunctionMetaData == null) {
				continue; // issue in constructing function
			}

			// Obtain the function meta-data and register
			ManagedFunctionMetaData<?, ?> functionMetaData = rawFunctionMetaData.getManagedFunctionMetaData();
			rawFunctionMetaDatas.add(rawFunctionMetaData);
			functionMetaDatas.add(functionMetaData);
		}

		// Create the function locator
		ManagedFunctionLocator functionLocator = new ManagedFunctionLocatorImpl(
				functionMetaDatas.toArray(new ManagedFunctionMetaData[0]));

		// Create the listing of startup functions to later populate
		ManagedFunctionReference[] startupFunctionReferences = configuration.getStartupFunctions();
		int startupFunctionsLength = (startupFunctionReferences == null ? 0 : startupFunctionReferences.length);
		OfficeStartupFunction[] startupFunctions = new OfficeStartupFunction[startupFunctionsLength];

		// Create the listing of escalations to later populate
		EscalationConfiguration[] officeEscalationConfigurations = configuration.getEscalationConfiguration();
		int officeEscalationsLength = (officeEscalationConfigurations == null ? 0
				: officeEscalationConfigurations.length);
		EscalationFlow[] officeEscalations = new EscalationFlow[officeEscalationsLength];
		EscalationProcedure officeEscalationProcedure = new EscalationProcedureImpl(officeEscalations);

		// Obtain the OfficeFloor escalation
		EscalationFlow officeFloorEscalation = rawOfficeFloorMetaData.getOfficeFloorEscalation();

		// Create the thread meta-data
		ThreadMetaData threadMetaData = new ThreadMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(threadBoundManagedObjects), governanceMetaDatas,
				maxFunctionChainLength, breakChainTeam, officeEscalationProcedure, officeFloorEscalation);

		// Create the process meta-data
		ProcessMetaData processMetaData = new ProcessMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(processBoundManagedObjects), threadMetaData);

		// Obtain the profiler
		Profiler profiler = configuration.getProfiler();

		// Load the startup functions
		for (int i = 0; i < startupFunctionsLength; i++) {

			// Obtain the function meta-data for the startup function
			ManagedFunctionMetaData<?, ?> startupFunctionMetaData = ConstructUtil.getFunctionMetaData(
					startupFunctionReferences[i], functionLocator, issues, AssetType.OFFICE, officeName,
					"Startup Function " + i);
			if (startupFunctionMetaData == null) {
				continue; // startup function not found
			}

			// Create the startup flow meta-data
			FlowMetaData startupFlow = ConstructUtil.newFlowMetaData(startupFunctionMetaData, false);

			// TODO consider providing a parameter to the startup function
			Object parameter = null;

			// Create and load the startup function
			startupFunctions[i] = new OfficeStartupFunctionImpl(startupFlow, parameter);
		}

		// Load the office escalations
		for (int i = 0; i < officeEscalationsLength; i++) {
			EscalationConfiguration escalationConfiguration = officeEscalationConfigurations[i];

			// Obtain the type of issue being handled by escalation
			Class<? extends Throwable> typeOfCause = escalationConfiguration.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(AssetType.OFFICE, officeName, "Type of cause not provided for office escalation " + i);
				continue; // must type type of cause
			}

			// Obtain the function meta-data for the escalation
			ManagedFunctionMetaData<?, ?> escalationFunctionMetaData = ConstructUtil.getFunctionMetaData(
					officeEscalationConfigurations[i].getManagedFunctionReference(), functionLocator, issues,
					AssetType.OFFICE, officeName, "Office Escalation " + i);
			if (escalationFunctionMetaData == null) {
				continue; // escalation function not found
			}

			// Create and load the escalation
			officeEscalations[i] = new EscalationFlowImpl(typeOfCause, escalationFunctionMetaData);
		}

		// Obtain all the asset managers for the office
		AssetManager[] assetManagers = officeAssetManagerFactory.getAssetManagers();

		// Create the office manager
		OfficeManagerImpl officeManager = new OfficeManagerImpl(officeName, monitorOfficeInterval, assetManagers,
				officeClockImpl, functionLoop, timer);

		// Obtain the managed execution factory
		ManagedExecutionFactory managedExecutionFactory = rawOfficeFloorMetaData.getManagedExecutionFactory();

		// Load the office meta-data
		OfficeMetaData officeMetaData = new OfficeMetaDataImpl(officeName, officeManager, officeClock, timer,
				functionLoop, threadLocalAwareExecutor, managedExecutionFactory,
				functionMetaDatas.toArray(new ManagedFunctionMetaData[0]), functionLocator, processMetaData,
				startupFunctions, profiler);

		// Create the factories
		FlowMetaDataFactory flowMetaDataFactory = new FlowMetaDataFactory();
		EscalationFlowFactory escalationFlowFactory = new EscalationFlowFactory();

		// Have the managed objects managed by the office
		for (RawManagingOfficeMetaData<?> officeManagingManagedObject : officeManagingManagedObjects) {
			officeManagingManagedObject.manageByOffice(officeMetaData, processBoundManagedObjects, issues);
		}

		// Link functions within the meta-data of the office
		for (RawManagedFunctionMetaData<?, ?> rawFunctionMetaData : rawFunctionMetaDatas) {
			if (!rawFunctionMetaData.loadOfficeMetaData(officeMetaData, flowMetaDataFactory, escalationFlowFactory,
					rawAdminFactory, officeTeams, issues)) {
				return null;
			}
		}

		// Link governance within meta-data of the office
		for (RawGovernanceMetaData<?, ?> rawGovernance : rawGovernanceMetaDataList) {
			if (!rawGovernance.loadOfficeMetaData(officeMetaData, flowMetaDataFactory, escalationFlowFactory, issues)) {
				return null;
			}
		}

		// Return the raw office meta-data
		rawOfficeMetaData.officeMetaData = officeMetaData;
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

}