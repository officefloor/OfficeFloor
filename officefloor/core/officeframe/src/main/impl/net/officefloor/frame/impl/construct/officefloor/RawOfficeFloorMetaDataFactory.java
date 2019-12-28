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
package net.officefloor.frame.impl.construct.officefloor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.executive.RawExecutiveMetaData;
import net.officefloor.frame.impl.construct.executive.RawExecutiveMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaDataFactory;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaDataFactory;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.clock.ClockFactoryImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationHandlerEscalationFlow;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.execution.ThreadFactoryManufacturer;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.impl.execute.officefloor.DefaultOfficeFloorEscalationHandler;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectSourceInstanceImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorMetaDataImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedObjectSourceInstance;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Factory for creating {@link RawOfficeFloorMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawOfficeFloorMetaDataFactory {

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor threadLocalAwareExecutor;

	/**
	 * Instantiate.
	 * 
	 * @param threadLocalAwareExecutor {@link ThreadLocalAwareExecutor}.
	 */
	public RawOfficeFloorMetaDataFactory(ThreadLocalAwareExecutor threadLocalAwareExecutor) {
		this.threadLocalAwareExecutor = threadLocalAwareExecutor;
	}

	/**
	 * Constructs the {@link RawOfficeFloorMetaData} from the
	 * {@link OfficeFloorConfiguration}.
	 * 
	 * @param configuration {@link OfficeFloorConfiguration}.
	 * @param issues        {@link OfficeFloorIssues}.
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RawOfficeFloorMetaData constructRawOfficeFloorMetaData(OfficeFloorConfiguration configuration,
			OfficeFloorIssues issues) {

		// Name of OfficeFloor for reporting issues
		String officeFloorName = configuration.getOfficeFloorName();
		if (ConstructUtil.isBlank(officeFloorName)) {
			issues.addIssue(AssetType.OFFICE_FLOOR, "Unknown", "Name not provided for OfficeFloor");

			// Not that important to name the OfficeFloor, so provide default
			officeFloorName = OfficeFloor.class.getSimpleName();
		}

		// Obtain the maximum time to wait for OfficeFloor to start
		long maxStartupWaitTime = configuration.getMaxStartupWaitTime();
		if (maxStartupWaitTime <= 0) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName, "Must provide positive startup wait time");
			return null; // must have positive startup time
		}

		// Create listing of additional OfficeFloor listeners
		List<OfficeFloorListener> officeFloorListeners = new LinkedList<>();

		// Create the default clock factory
		ClockFactoryImpl defaultClockFactory = new ClockFactoryImpl();
		Supplier<ClockFactory> clockFactoryProvider = () -> {
			if (!officeFloorListeners.contains(defaultClockFactory)) {
				officeFloorListeners.add(defaultClockFactory);
			}
			return defaultClockFactory;
		};

		// Obtain the Source Context
		SourceContext sourceContext = configuration.getSourceContext(officeFloorName, clockFactoryProvider);
		if (sourceContext == null) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
					"No " + SourceContext.class.getSimpleName() + " provided from configuration");

			// Use default source context
			sourceContext = new SourceContextImpl(officeFloorName, false,
					Thread.currentThread().getContextClassLoader(), clockFactoryProvider.get());
		}

		// Create the managed object source factory
		RawManagedObjectMetaDataFactory rawMosFactory = new RawManagedObjectMetaDataFactory(sourceContext,
				configuration);

		// Construct the managed object sources
		Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		List<RawManagedObjectMetaData<?, ?>> mosListing = new LinkedList<RawManagedObjectMetaData<?, ?>>();
		Map<String, List<RawManagingOfficeMetaData>> officeManagedObjects = new HashMap<String, List<RawManagingOfficeMetaData>>();
		List<ThreadCompletionListener> threadCompletionListenerList = new LinkedList<>();
		for (ManagedObjectSourceConfiguration mosConfiguration : configuration.getManagedObjectSourceConfiguration()) {

			// Construct the managed object source
			RawManagedObjectMetaData<?, ?> mosMetaData = rawMosFactory
					.constructRawManagedObjectMetaData(mosConfiguration, officeFloorName, issues);
			if (mosMetaData == null) {
				return null; // issue with managed object source
			}

			// Obtain the managed object source name
			String managedObjectSourceName = mosMetaData.getManagedObjectName();
			if (mosRegistry.containsKey(managedObjectSourceName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
						"Managed object sources registered with the same name '" + managedObjectSourceName + "'");
				continue; // maintain only first managed object source
			}

			// Obtain details for the office managing the managed object
			RawManagingOfficeMetaData managingOfficeMetaData = mosMetaData.getRawManagingOfficeMetaData();
			if (managingOfficeMetaData == null) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Managing Object Source did not specify managing office meta-data");
				return null; // must have a managing office
			}
			String managingOfficeName = managingOfficeMetaData.getManagingOfficeName();
			if (ConstructUtil.isBlank(managingOfficeName)) {
				issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
						"Managed Object Source did not specify a managing Office");
				return null; // must have a managing office
			}

			// Register the managed object source
			mosRegistry.put(managedObjectSourceName, mosMetaData);
			mosListing.add(mosMetaData);

			// Register for being managed by the office
			List<RawManagingOfficeMetaData> officeManagingManagedObjects = officeManagedObjects.get(managingOfficeName);
			if (officeManagingManagedObjects == null) {
				officeManagingManagedObjects = new LinkedList<RawManagingOfficeMetaData>();
				officeManagedObjects.put(managingOfficeName, officeManagingManagedObjects);
			}
			officeManagingManagedObjects.add(managingOfficeMetaData);

			// Register the thread completion listeners
			ThreadCompletionListener[] completionListeners = mosMetaData.getThreadCompletionListeners();
			if (completionListeners != null) {
				for (ThreadCompletionListener completionListener : completionListeners) {
					threadCompletionListenerList.add(completionListener);
				}
			}
		}

		// Construct the teams
		Map<String, RawTeamMetaData> teamRegistry = new HashMap<String, RawTeamMetaData>();
		List<TeamManagement> teamListing = new LinkedList<TeamManagement>();

		// Obtain the thread decorator
		Consumer<Thread> threadDecorator = configuration.getThreadDecorator();

		// Obtain the thread completion listeners
		ThreadCompletionListener[] threadCompletionListeners = threadCompletionListenerList
				.toArray(new ThreadCompletionListener[0]);

		// Create the execution factory
		ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(threadCompletionListeners);

		// Create thread factory manufacturer (managed execution factory and executive)
		ThreadFactoryManufacturer threadFactoryManufacturer = new ThreadFactoryManufacturer(managedExecutionFactory,
				threadDecorator);

		// Create the executive
		Executive executive;
		ThreadFactory[] defaultExecutionStrategy;
		Map<String, ThreadFactory[]> executionStrategies;
		Map<String, TeamOversight> teamOversights;
		ExecutiveConfiguration<?> executiveConfiguration = configuration.getExecutiveConfiguration();
		if (executiveConfiguration != null) {
			// Create the configured Executive
			RawExecutiveMetaDataFactory rawExecutiveFactory = new RawExecutiveMetaDataFactory(sourceContext,
					threadFactoryManufacturer);
			RawExecutiveMetaData rawExecutive = rawExecutiveFactory
					.constructRawExecutiveMetaData(executiveConfiguration, officeFloorName, issues);
			executive = rawExecutive.getExecutive();
			defaultExecutionStrategy = null;
			executionStrategies = rawExecutive.getExecutionStrategies();
			teamOversights = rawExecutive.getTeamOversights();
		} else {
			// No Executive configured, so use default
			DefaultExecutive defaultExecutive = new DefaultExecutive(threadFactoryManufacturer);
			executive = defaultExecutive;
			defaultExecutionStrategy = defaultExecutive.getExcutionStrategies()[0].getThreadFactories();
			executionStrategies = defaultExecutive.getExecutionStrategyMap();
			teamOversights = defaultExecutive.getTeamOversightMap();
		}

		// Create the team factory
		RawTeamMetaDataFactory rawTeamFactory = new RawTeamMetaDataFactory(sourceContext, executive, teamOversights,
				threadFactoryManufacturer, this.threadLocalAwareExecutor);

		// Construct the configured teams
		for (TeamConfiguration<?> teamConfiguration : configuration.getTeamConfiguration()) {

			// Construct the raw team meta-data
			RawTeamMetaData rawTeamMetaData = rawTeamFactory.constructRawTeamMetaData(teamConfiguration,
					officeFloorName, issues);
			if (rawTeamMetaData == null) {
				return null; // issue with team
			}

			// Obtain the team name
			String teamName = rawTeamMetaData.getTeamName();
			if (teamRegistry.containsKey(teamName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
						"Teams registered with the same name '" + teamName + "'");
				continue; // maintain only first team
			}

			// Obtain the team
			TeamManagement team = rawTeamMetaData.getTeamManagement();

			// Register the team
			teamRegistry.put(teamName, rawTeamMetaData);
			teamListing.add(team);
		}

		// Construct the break chain team
		TeamConfiguration<?> breakTeamConfiguration = configuration.getBreakChainTeamConfiguration();
		RawTeamMetaData breakTeamMetaData = rawTeamFactory.constructRawTeamMetaData(breakTeamConfiguration,
				officeFloorName, issues);
		TeamManagement breakChainTeamManagement = breakTeamMetaData.getTeamManagement();
		teamListing.add(breakChainTeamManagement);

		// Undertake OfficeFloor escalation on any team available
		FunctionLoop officeFloorFunctionLoop = new FunctionLoopImpl(null);
		OfficeMetaData officeFloorManagement = new OfficeMetaDataImpl("Management", null, null, null,
				officeFloorFunctionLoop, null, null, null, null, null, null, null, null);

		// Obtain the escalation handler for the OfficeFloor
		EscalationHandler officeFloorEscalationHandler = configuration.getEscalationHandler();
		if (officeFloorEscalationHandler == null) {
			// Provide default OfficeFloor escalation handler
			officeFloorEscalationHandler = new DefaultOfficeFloorEscalationHandler();
		}
		EscalationFlow officeFloorEscalation = new EscalationHandlerEscalationFlow(officeFloorEscalationHandler,
				officeFloorManagement);

		// Create the raw office floor meta-data
		RawOfficeFloorMetaData rawMetaData = new RawOfficeFloorMetaData(executive, defaultExecutionStrategy,
				executionStrategies, teamRegistry, breakChainTeamManagement, threadLocalAwareExecutor,
				managedExecutionFactory, mosRegistry, officeFloorEscalation,
				officeFloorListeners.toArray(new OfficeFloorListener[officeFloorListeners.size()]));

		// Construct the office factory
		RawOfficeMetaDataFactory rawOfficeFactory = new RawOfficeMetaDataFactory(rawMetaData);

		// Construct the offices
		List<OfficeMetaData> officeMetaDatas = new LinkedList<OfficeMetaData>();
		for (OfficeConfiguration officeConfiguration : configuration.getOfficeConfiguration()) {

			// Obtain the office name
			String officeName = officeConfiguration.getOfficeName();
			if (ConstructUtil.isBlank(officeName)) {
				issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName, "Office added without a name");
				return null; // office must have name
			}

			// Obtain the managed objects being managed by the office
			List<RawManagingOfficeMetaData> officeManagingManagedObjectList = officeManagedObjects.get(officeName);
			RawManagingOfficeMetaData[] officeManagingManagedObjects = (officeManagingManagedObjectList == null
					? new RawManagingOfficeMetaData[0]
					: officeManagingManagedObjectList.toArray(new RawManagingOfficeMetaData[0]));

			// Unregister managed objects as check later all managed by offices
			officeManagedObjects.remove(officeName);

			// Construct the raw office meta-data
			RawOfficeMetaData rawOfficeMetaData = rawOfficeFactory.constructRawOfficeMetaData(officeConfiguration,
					officeManagingManagedObjects, issues);
			if (rawOfficeMetaData == null) {
				return null; // issue with office
			}

			// Add the office meta-data to listing
			OfficeMetaData officeMetaData = rawOfficeMetaData.getOfficeMetaData();
			officeMetaDatas.add(officeMetaData);
		}

		// Issue if office not exist for the managed object source
		if (officeManagedObjects.size() > 0) {
			for (String officeName : officeManagedObjects.keySet()) {
				for (RawManagingOfficeMetaData managingOfficeMetaData : officeManagedObjects.get(officeName)) {
					String managedObjectSourceName = managingOfficeMetaData.getRawManagedObjectMetaData()
							.getManagedObjectName();
					issues.addIssue(AssetType.MANAGED_OBJECT, managedObjectSourceName,
							"Can not find managing office '" + officeName + "'");
				}
			}
		}

		// Obtain the listing of managed object source instances
		List<ManagedObjectSourceInstance> mosInstances = new LinkedList<ManagedObjectSourceInstance>();
		for (RawManagedObjectMetaData<?, ?> rawMoMetaData : mosListing) {
			ManagedObjectSourceInstance mosInstance = new ManagedObjectSourceInstanceImpl(
					rawMoMetaData.getManagedObjectSource(),
					rawMoMetaData.getRawManagingOfficeMetaData().getManagedObjectExecuteManagerFactory(),
					rawMoMetaData.getManagedObjectPool());
			mosInstances.add(mosInstance);
		}

		// Create the office floor meta-data
		rawMetaData.officeFloorMetaData = new OfficeFloorMetaDataImpl(teamListing.toArray(new TeamManagement[0]),
				mosInstances.toArray(new ManagedObjectSourceInstance[0]),
				officeMetaDatas.toArray(new OfficeMetaData[0]), maxStartupWaitTime);

		// Return the raw meta-data
		return rawMetaData;
	}

}