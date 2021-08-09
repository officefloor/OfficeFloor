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

package net.officefloor.frame.impl.construct.office;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.construct.administration.RawAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.escalation.EscalationFlowFactory;
import net.officefloor.frame.impl.construct.flow.FlowMetaDataFactory;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaDataFactory;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionBuilderImpl;
import net.officefloor.frame.impl.construct.managedfunction.RawManagedFunctionMetaData;
import net.officefloor.frame.impl.construct.managedfunction.RawManagedFunctionMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.ManagedObjectAdministrationMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.asset.MonitorClockImpl;
import net.officefloor.frame.impl.execute.asset.OfficeManagerHirerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.office.LoadManagedObjectFunctionFactory;
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
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.AssetManagerHirer;
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
import net.officefloor.frame.internal.structure.MonitorClock;
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
	 * {@link RawOfficeFloorMetaData}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param rawOfficeFloorMetaData {@link RawOfficeFloorMetaData}.
	 */
	public RawOfficeMetaDataFactory(RawOfficeFloorMetaData rawOfficeFloorMetaData) {
		this.rawOfficeFloorMetaData = rawOfficeFloorMetaData;
	}

	/**
	 * Constructs the {@link RawOfficeMetaData}.
	 * 
	 * @param configuration                {@link OfficeConfiguration}.
	 * @param officeManagingManagedObjects {@link RawManagingOfficeMetaData}
	 *                                     instances for the {@link Office}.
	 * @param issues                       {@link OfficeFloorIssues}.
	 * @return {@link RawOfficeMetaData}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RawOfficeMetaData constructRawOfficeMetaData(OfficeConfiguration configuration,
			RawManagingOfficeMetaData<?>[] officeManagingManagedObjects, OfficeFloorIssues issues) {

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

		// Obtain the default asynchronous flow timeout
		long defaultAsynchronousFlowTimeout = configuration.getDefaultAsynchronousFlowTimeout();
		if (defaultAsynchronousFlowTimeout <= 0) {
			issues.addIssue(AssetType.OFFICE, officeName, "Office default " + AsynchronousFlow.class.getSimpleName()
					+ " timeout must be positive (" + defaultAsynchronousFlowTimeout + ")");
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
			RawTeamMetaData rawTeamMetaData = this.rawOfficeFloorMetaData.getRawTeamMetaData(officeFloorTeamName);
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

		// Obtain the thread local aware executor (if required)
		ThreadLocalAwareExecutor threadLocalAwareExecutor = null;
		if (isRequireThreadLocalAwareness) {
			threadLocalAwareExecutor = this.rawOfficeFloorMetaData.getThreadLocalAwareExecutor();
		}

		// Create the office details
		MonitorClockImpl monitorClockImpl = null;
		MonitorClock monitorClock = configuration.getMonitorClock();
		if (monitorClock == null) {
			// Default the office clock
			monitorClockImpl = new MonitorClockImpl();
			monitorClock = monitorClockImpl;
		}
		FunctionLoop functionLoop = new FunctionLoopImpl(defaultTeam);

		// Create the asset manager registry
		AssetManagerRegistry officeAssetManagerRegistry = new AssetManagerRegistry(monitorClock, functionLoop);

		// Determine if manually manage governance
		boolean isManuallyManageGovernance = configuration.isManuallyManageGovernance();

		// Create the governance factory
		RawGovernanceMetaDataFactory rawGovernanceFactory = new RawGovernanceMetaDataFactory(officeName, officeTeams);

		// Register the governances to office
		GovernanceConfiguration<?, ?>[] governanceConfigurations = configuration.getGovernanceConfiguration();
		GovernanceMetaData<?, ?>[] governanceMetaDatas = new GovernanceMetaData[governanceConfigurations.length];
		List<RawGovernanceMetaData<?, ?>> rawGovernanceMetaDataList = new LinkedList<RawGovernanceMetaData<?, ?>>();
		Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData = new HashMap<String, RawGovernanceMetaData<?, ?>>();
		NEXT_GOVERNANCE: for (int i = 0; i < governanceConfigurations.length; i++) {
			GovernanceConfiguration governanceConfiguration = governanceConfigurations[i];

			// Create the raw governance
			RawGovernanceMetaData<?, ?> rawGovernance = rawGovernanceFactory.createRawGovernanceMetaData(
					governanceConfiguration, i, officeAssetManagerRegistry, defaultAsynchronousFlowTimeout, issues);
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
			RawManagedObjectMetaData<?, ?> rawMoMetaData = this.rawOfficeFloorMetaData
					.getRawManagedObjectMetaData(mosName);
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

		// Create the raw bound managed object factory
		RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = new RawBoundManagedObjectMetaDataFactory(
				officeAssetManagerRegistry, registeredMo, rawGovernanceMetaData);

		// Obtain the process bound managed object instances
		ManagedObjectConfiguration<?>[] processManagedObjectConfiguration = configuration
				.getProcessManagedObjectConfiguration();
		if (processManagedObjectConfiguration == null) {
			// Provide no process Managed Object configurations
			processManagedObjectConfiguration = new ManagedObjectConfiguration[0];
		}
		final RawBoundManagedObjectMetaData[] processBoundManagedObjects = rawBoundManagedObjectFactory
				.constructBoundManagedObjectMetaData(processManagedObjectConfiguration, ManagedObjectScope.PROCESS,
						null, officeManagingManagedObjects, boundInputManagedObjects, AssetType.OFFICE, officeName,
						defaultAsynchronousFlowTimeout, issues);

		// Create the map of process bound managed objects by name
		Map<String, RawBoundManagedObjectMetaData> processScopeMo = new HashMap<>();
		for (RawBoundManagedObjectMetaData mo : processBoundManagedObjects) {
			processScopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Obtain the thread bound managed object instances
		ManagedObjectConfiguration<?>[] threadManagedObjectConfiguration = configuration
				.getThreadManagedObjectConfiguration();
		final RawBoundManagedObjectMetaData[] threadBoundManagedObjects;
		if ((threadManagedObjectConfiguration == null) || (threadManagedObjectConfiguration.length == 0)) {
			threadBoundManagedObjects = new RawBoundManagedObjectMetaData[0];
		} else {
			threadBoundManagedObjects = rawBoundManagedObjectFactory.constructBoundManagedObjectMetaData(
					threadManagedObjectConfiguration, ManagedObjectScope.THREAD, processScopeMo, null, null,
					AssetType.OFFICE, officeName, defaultAsynchronousFlowTimeout, issues);
		}

		// Load the thread bound managed objects to scope managed objects
		Map<String, RawBoundManagedObjectMetaData> threadScopeMo = new HashMap<>(processScopeMo);
		for (RawBoundManagedObjectMetaData mo : threadBoundManagedObjects) {
			threadScopeMo.put(mo.getBoundManagedObjectName(), mo);
		}

		// Create the raw office meta-data
		RawOfficeMetaData rawOfficeMetaData = new RawOfficeMetaData(officeName, this.rawOfficeFloorMetaData,
				officeTeams, registeredMo, processBoundManagedObjects, threadBoundManagedObjects, threadScopeMo,
				isManuallyManageGovernance, rawGovernanceMetaData);

		// Create the raw managed function factory
		RawManagedFunctionMetaDataFactory rawFunctionFactory = new RawManagedFunctionMetaDataFactory(rawOfficeMetaData,
				rawBoundManagedObjectFactory);

		// Construct the meta-data of the managed functions within the office
		List<RawManagedFunctionMetaData<?, ?>> rawFunctionMetaDatas = new LinkedList<>();
		List<ManagedFunctionMetaData<?, ?>> functionMetaDatas = new LinkedList<>();
		for (ManagedFunctionConfiguration<?, ?> functionConfiguration : configuration
				.getManagedFunctionConfiguration()) {

			// Construct the managed function
			RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = rawFunctionFactory
					.constructRawManagedFunctionMetaData(functionConfiguration, officeAssetManagerRegistry,
							defaultAsynchronousFlowTimeout, issues);
			if (rawFunctionMetaData == null) {
				continue; // issue in constructing function
			}

			// Obtain the function meta-data and register
			ManagedFunctionMetaData<?, ?> functionMetaData = rawFunctionMetaData.getManagedFunctionMetaData();
			rawFunctionMetaDatas.add(rawFunctionMetaData);
			functionMetaDatas.add(functionMetaData);
		}

		// Construct the state manager keep alive function
		ManagedFunctionBuilderImpl<None, None> stateManagerKeepAliveConfiguration = new ManagedFunctionBuilderImpl<>(
				"_STATE_MANAGER_KEEP_ALIVE_", () -> (moContext) -> {
				});
		RawManagedFunctionMetaData<?, ?> rawStateManagerKeepAliveFunction = rawFunctionFactory
				.constructRawManagedFunctionMetaData(stateManagerKeepAliveConfiguration, officeAssetManagerRegistry,
						defaultAsynchronousFlowTimeout, issues);
		rawFunctionMetaDatas.add(rawStateManagerKeepAliveFunction);
		ManagedFunctionMetaData<?, ?> stateManagerKeepAliveFunction = rawStateManagerKeepAliveFunction
				.getManagedFunctionMetaData();

		// Construct the load object managed functions (in deterministic order)
		List<String> scopeMoNames = new LinkedList<>(threadScopeMo.keySet());
		Collections.sort(scopeMoNames);
		Map<String, ManagedFunctionMetaData<?, ?>> loadObjectMetaDatas = new HashMap<>();
		for (String scopeMoName : scopeMoNames) {

			// Obtain the object type (use first instance)
			Class<?> objectType = threadScopeMo.get(scopeMoName).getRawBoundManagedObjectInstanceMetaData()[0]
					.getManagedObjectMetaData().getObjectType();

			// Create the managed function configuration
			ManagedFunctionBuilderImpl<LoadManagedObjectFunctionFactory.Dependencies, None> loadObjectConfiguration = new ManagedFunctionBuilderImpl<>(
					"_LOAD_" + scopeMoName, new LoadManagedObjectFunctionFactory());
			loadObjectConfiguration.linkParameter(LoadManagedObjectFunctionFactory.Dependencies.PARAMETER,
					LoadManagedObjectFunctionFactory.LoadManagedObjectParameter.class);
			loadObjectConfiguration.linkManagedObject(LoadManagedObjectFunctionFactory.Dependencies.MANAGED_OBJECT,
					scopeMoName, objectType);

			// Construct the managed function
			RawManagedFunctionMetaData<?, ?> rawFunctionMetaData = rawFunctionFactory
					.constructRawManagedFunctionMetaData(loadObjectConfiguration, officeAssetManagerRegistry,
							defaultAsynchronousFlowTimeout, issues);
			if (rawFunctionMetaData == null) {
				continue; // issue in constructing function
			}

			// Register the function for further setup
			rawFunctionMetaDatas.add(rawFunctionMetaData);

			// Capture the load object meta data for loading the object
			loadObjectMetaDatas.put(scopeMoName, rawFunctionMetaData.getManagedFunctionMetaData());
		}

		// Create the function locator
		ManagedFunctionLocator functionLocator = new ManagedFunctionLocatorImpl(
				functionMetaDatas.toArray(new ManagedFunctionMetaData[0]));

		// Create the listing of startup functions to later populate
		ManagedFunctionInvocation[] startupFunctionInvocations = configuration.getStartupFunctions();
		int startupFunctionsLength = (startupFunctionInvocations == null ? 0 : startupFunctionInvocations.length);
		OfficeStartupFunction[] startupFunctions = new OfficeStartupFunction[startupFunctionsLength];

		// Obtain the thread synchronisers
		ThreadSynchroniserFactory[] threadSynchronisers = configuration.getThreadSynchronisers();

		// Create the listing of escalations to later populate
		EscalationConfiguration[] officeEscalationConfigurations = configuration.getEscalationConfiguration();
		int officeEscalationsLength = (officeEscalationConfigurations == null ? 0
				: officeEscalationConfigurations.length);
		EscalationFlow[] officeEscalations = new EscalationFlow[officeEscalationsLength];
		EscalationProcedure officeEscalationProcedure = new EscalationProcedureImpl(officeEscalations);

		// Obtain the OfficeFloor escalation
		EscalationFlow officeFloorEscalation = this.rawOfficeFloorMetaData.getOfficeFloorEscalation();

		// Create the thread meta-data
		ThreadMetaData threadMetaData = new ThreadMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(threadBoundManagedObjects), governanceMetaDatas,
				maxFunctionChainLength, threadSynchronisers, officeEscalationProcedure, officeFloorEscalation);

		// Obtain the executive
		Executive executive = rawOfficeFloorMetaData.getExecutive();

		// Create the process meta-data
		ProcessMetaData processMetaData = new ProcessMetaDataImpl(
				this.constructDefaultManagedObjectMetaData(processBoundManagedObjects), threadMetaData);

		// Obtain the profiler
		Profiler profiler = configuration.getProfiler();

		// Load the startup functions
		for (int i = 0; i < startupFunctionsLength; i++) {
			ManagedFunctionInvocation startupFunctionInvocation = startupFunctionInvocations[i];

			// Obtain the function meta-data for the startup function
			ManagedFunctionMetaData<?, ?> startupFunctionMetaData = ConstructUtil.getFunctionMetaData(
					startupFunctionInvocation, functionLocator, issues, AssetType.OFFICE, officeName,
					"Startup Function " + i);
			if (startupFunctionMetaData == null) {
				continue; // startup function not found
			}

			// Create the startup flow meta-data
			FlowMetaData startupFlow = ConstructUtil.newFlowMetaData(startupFunctionMetaData, false);

			// Obtain the startup function parameter
			Object parameter = startupFunctionInvocation.getArgument();

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

		// Create the office manager hirer and default office manager
		OfficeManagerHirerImpl officeManagerHirer = new OfficeManagerHirerImpl(monitorClockImpl, monitorOfficeInterval,
				functionLoop);

		// Obtain the managed execution factory
		ManagedExecutionFactory managedExecutionFactory = this.rawOfficeFloorMetaData.getManagedExecutionFactory();

		// Load the office meta-data
		OfficeMetaData officeMetaData = new OfficeMetaDataImpl(officeName, officeManagerHirer, monitorClock,
				functionLoop, threadLocalAwareExecutor, executive, managedExecutionFactory,
				functionMetaDatas.toArray(new ManagedFunctionMetaData[0]), functionLocator, processMetaData,
				stateManagerKeepAliveFunction, loadObjectMetaDatas, startupFunctions, profiler);

		// Create the factories
		FlowMetaDataFactory flowMetaDataFactory = new FlowMetaDataFactory(officeMetaData);
		EscalationFlowFactory escalationFlowFactory = new EscalationFlowFactory(officeMetaData);
		RawAdministrationMetaDataFactory rawAdminFactory = new RawAdministrationMetaDataFactory(officeMetaData,
				flowMetaDataFactory, escalationFlowFactory, officeTeams);
		ManagedObjectAdministrationMetaDataFactory moAdminFactory = new ManagedObjectAdministrationMetaDataFactory(
				rawAdminFactory, threadScopeMo, processScopeMo);

		// Obtain the execution strategies
		ThreadFactory[] defaultexecutionStrategy = this.rawOfficeFloorMetaData.getDefaultExecutionStrategy();
		Map<String, ThreadFactory[]> executionStrategies = this.rawOfficeFloorMetaData.getExecutionStrategies();

		// Have the managed objects managed by the office
		for (RawManagingOfficeMetaData<?> officeManagingManagedObject : officeManagingManagedObjects) {
			officeManagingManagedObject.manageByOffice(officeMetaData, processBoundManagedObjects, moAdminFactory,
					defaultexecutionStrategy, executionStrategies, officeAssetManagerRegistry,
					defaultAsynchronousFlowTimeout, issues);
		}

		// Link functions within the meta-data of the office
		for (RawManagedFunctionMetaData<?, ?> rawFunctionMetaData : rawFunctionMetaDatas) {
			if (!rawFunctionMetaData.loadOfficeMetaData(officeMetaData, flowMetaDataFactory, escalationFlowFactory,
					rawAdminFactory, officeAssetManagerRegistry, defaultAsynchronousFlowTimeout, issues)) {
				return null;
			}
		}

		// Link governance within meta-data of the office
		for (RawGovernanceMetaData<?, ?> rawGovernance : rawGovernanceMetaDataList) {
			if (!rawGovernance.loadOfficeMetaData(officeMetaData, flowMetaDataFactory, escalationFlowFactory, issues)) {
				return null;
			}
		}

		// Obtain all the asset managers for the office
		AssetManagerHirer[] assetManagerHirers = officeAssetManagerRegistry.getAssetManagerHirers();
		officeManagerHirer.setAssetManagerHirers(assetManagerHirers);

		// Return the raw office meta-data
		rawOfficeMetaData.officeMetaData = officeMetaData;
		return rawOfficeMetaData;
	}

	/**
	 * Constructs the default {@link ManagedObjectMetaData} listing from the input
	 * {@link RawBoundManagedObjectMetaData} instances.
	 * 
	 * @param rawBoundManagedObjects {@link RawBoundManagedObjectMetaData}
	 *                               instances.
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
