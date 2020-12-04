/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.officefloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.build.OfficeVisitor;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
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
		Supplier<ClockFactory> clockFactoryProvider = () -> defaultClockFactory;

		// Obtain the profiles
		String[] profiles = configuration.getProfiles();

		// Obtain the Source Context
		SourceContext sourceContext = configuration.getSourceContext(officeFloorName, clockFactoryProvider);
		if (sourceContext == null) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName,
					"No " + SourceContext.class.getSimpleName() + " provided from configuration");

			// Use default source context
			sourceContext = new SourceContextImpl(officeFloorName, false, profiles,
					Thread.currentThread().getContextClassLoader(), clockFactoryProvider.get());
		}

		// Create the managed object source factory
		RawManagedObjectMetaDataFactory rawMosFactory = new RawManagedObjectMetaDataFactory(sourceContext,
				configuration);

		// Create the start up notify object
		Object startupNotify = new Object();

		// Construct the managed object sources
		Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry = new HashMap<String, RawManagedObjectMetaData<?, ?>>();
		List<RawManagedObjectMetaData<?, ?>> mosListing = new LinkedList<RawManagedObjectMetaData<?, ?>>();
		Map<String, List<RawManagingOfficeMetaData>> officeManagedObjects = new HashMap<String, List<RawManagingOfficeMetaData>>();
		List<ThreadCompletionListener> threadCompletionListenerList = new LinkedList<>();
		for (ManagedObjectSourceConfiguration mosConfiguration : configuration.getManagedObjectSourceConfiguration()) {

			// Construct the managed object source
			RawManagedObjectMetaData<?, ?> mosMetaData = rawMosFactory
					.constructRawManagedObjectMetaData(mosConfiguration, startupNotify, officeFloorName, issues);
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
		TeamOversight teamOversight;
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
			teamOversight = rawExecutive.getTeamOversight();
		} else {
			// No Executive configured, so use default
			DefaultExecutive defaultExecutive = new DefaultExecutive(threadFactoryManufacturer);
			executive = defaultExecutive;
			defaultExecutionStrategy = defaultExecutive.getExcutionStrategies()[0].getThreadFactories();
			executionStrategies = defaultExecutive.getExecutionStrategyMap();
			teamOversight = null;
		}

		// Register executive with default clock factory
		defaultClockFactory.setExecutive(executive);

		// Create the team factory
		RawTeamMetaDataFactory rawTeamFactory = new RawTeamMetaDataFactory(sourceContext, executive, teamOversight,
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

		// Undertake OfficeFloor escalation on any team available
		FunctionLoop officeFloorFunctionLoop = new FunctionLoopImpl(null);
		OfficeMetaData officeFloorManagement = new OfficeMetaDataImpl("Management", null, null, officeFloorFunctionLoop,
				null, null, null, null, null, null, null, null, null, null);

		// Obtain the escalation handler for the OfficeFloor
		EscalationHandler officeFloorEscalationHandler = configuration.getEscalationHandler();
		if (officeFloorEscalationHandler == null) {
			// Provide default OfficeFloor escalation handler
			officeFloorEscalationHandler = new DefaultOfficeFloorEscalationHandler();
		}
		EscalationFlow officeFloorEscalation = new EscalationHandlerEscalationFlow(officeFloorEscalationHandler,
				officeFloorManagement);

		// Create the raw OfficeFloor meta-data
		RawOfficeFloorMetaData rawMetaData = new RawOfficeFloorMetaData(executive, defaultExecutionStrategy,
				executionStrategies, teamRegistry, startupNotify, this.threadLocalAwareExecutor,
				managedExecutionFactory, mosRegistry, officeFloorEscalation,
				officeFloorListeners.toArray(new OfficeFloorListener[officeFloorListeners.size()]));

		// Construct the office factory
		RawOfficeMetaDataFactory rawOfficeFactory = new RawOfficeMetaDataFactory(rawMetaData);

		// Obtain the Office visitors
		OfficeVisitor[] officeVisitors = configuration.getOfficeVisitors();

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

			// Visit the Office
			for (OfficeVisitor officeVisitor : officeVisitors) {
				officeVisitor.visit(officeMetaData);
			}
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

		// Obtain the managed object source instances by their name
		Map<String, OrderedGroupingStruct> mosInstances = new HashMap<>();
		for (RawManagedObjectMetaData<?, ?> rawMoMetaData : mosListing) {

			// Create the managed object source instance
			ManagedObjectSourceInstance mosInstance = new ManagedObjectSourceInstanceImpl(
					rawMoMetaData.getManagedObjectSource(),
					rawMoMetaData.getRawManagingOfficeMetaData().getManagedObjectExecuteManagerFactory(),
					rawMoMetaData.getManagedObjectPool(), rawMoMetaData.getServiceReadiness());

			// Register the instance
			String mosName = rawMoMetaData.getManagedObjectName();
			mosInstances.put(mosName, new OrderedGroupingStruct(mosName,
					rawMoMetaData.getManagedObjectSourceConfiguration(), mosInstance));
		}

		// Populate the before listings
		for (OrderedGroupingStruct item : mosInstances.values()) {

			// Load the befores
			String[] beforeNames = item.configuration.getStartupBefore();
			for (int i = 0; i < beforeNames.length; i++) {
				String beforeMosName = beforeNames[i];
				if (beforeMosName == null) {
					issues.addIssue(AssetType.MANAGED_OBJECT, item.name,
							"Null start up before " + ManagedObjectSource.class.getSimpleName() + " name for " + i);
					return null; // must provide valid name
				}

				// As this must be before, then target is in next group
				OrderedGroupingStruct afterMos = mosInstances.get(beforeMosName);
				if (afterMos == null) {
					issues.addIssue(AssetType.MANAGED_OBJECT, item.name,
							"Unknown " + ManagedObjectSource.class.getSimpleName() + " '" + beforeMosName
									+ "' to start up before");
					return null; // invalid configuration
				}
				afterMos.nextGroupFrom.add(item.name);
			}

			// Load the afters
			String[] afterNames = item.configuration.getStartupAfter();
			for (int i = 0; i < afterNames.length; i++) {
				String afterMosName = afterNames[i];
				if (afterMosName == null) {
					issues.addIssue(AssetType.MANAGED_OBJECT, item.name,
							"Null start up after " + ManagedObjectSource.class.getSimpleName() + " name for " + i);
					return null; // must provide valid name
				}

				// As this is after, then this must be in next group
				OrderedGroupingStruct afterMos = mosInstances.get(afterMosName);
				if (afterMos == null) {
					issues.addIssue(AssetType.MANAGED_OBJECT, item.name, "Unknown "
							+ ManagedObjectSource.class.getSimpleName() + " '" + afterMosName + "' to start up after");
					return null; // invalid configuration
				}
				item.nextGroupFrom.add(afterMos.name);
			}
		}

		// Obtain the grouped ordered start up
		List<List<ManagedObjectSourceInstance>> groupedMosListing = new LinkedList<>();
		try {
			// Create the sorted instances
			List<OrderedGroupingStruct> orderedMosInstances = new ArrayList<>(mosInstances.values());
			orderedMosInstances.sort((a, b) -> {

				// Determine before relationships
				boolean isAnextGroupFromB = a.nextGroupFrom.contains(b.name);
				boolean isBnextGroupFromA = b.nextGroupFrom.contains(a.name);

				// Compare based on relationship
				if (isAnextGroupFromB && isBnextGroupFromA) {
					// Cyclic dependency
					String[] names = new String[] { a.name, b.name };
					Arrays.sort(names);
					throw new CyclicStartupException(
							"Can not have cyclic start up (" + names[0] + ", " + names[1] + ")");
				} else if (isAnextGroupFromB) {
					// A next group from B, so A must come after
					return 1;
				} else if (isBnextGroupFromA) {
					// B next group from A, so B must come after
					return -1;
				} else if (a.nextGroupFrom.size() != b.nextGroupFrom.size()) {
					// Have most relationships later (pushes no relationships to start)
					return a.nextGroupFrom.size() - b.nextGroupFrom.size();
				} else {
					// No relationship, so just sort by name
					return String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name);
				}
			});

			// Load the ordered grouping
			List<ManagedObjectSourceInstance> groupedInstances = new LinkedList<>();
			groupedMosListing.add(groupedInstances);
			Set<String> allLoadedMosNames = new HashSet<>();
			Set<String> currentGroupMosNames = new HashSet<>();
			for (OrderedGroupingStruct itemStruct : orderedMosInstances) {

				// Determine if must be in next group from current group
				if (itemStruct.nextGroupFrom.stream()
						.anyMatch((beforeName) -> currentGroupMosNames.contains(beforeName))) {

					// Must be done after mos in current group, so start new group
					groupedInstances = new LinkedList<>();
					currentGroupMosNames.clear();
					groupedMosListing.add(groupedInstances);
				}

				// Add to current group
				groupedInstances.add(itemStruct.mosInstance);
				currentGroupMosNames.add(itemStruct.name);

				// Determine if cycle
				itemStruct.nextGroupFrom.stream().forEach((beforeName) -> {
					// All should have been loaded previously (otherwise cycle)
					if (!allLoadedMosNames.contains(beforeName)) {
						throw new CyclicStartupException("Cycle in " + ManagedObjectSource.class.getSimpleName()
								+ " start up (" + itemStruct.name + ", " + beforeName + ")");
					}
				});
				allLoadedMosNames.add(itemStruct.name);
			}

		} catch (CyclicStartupException ex) {
			issues.addIssue(AssetType.OFFICE_FLOOR, officeFloorName, ex.getMessage());
			return null; // can not have cycle in start up
		}

		// Transform into ordered grouped array
		ManagedObjectSourceInstance[][] groupedMosInstances = new ManagedObjectSourceInstance[groupedMosListing
				.size()][];
		for (int i = 0; i < groupedMosInstances.length; i++) {
			List<ManagedObjectSourceInstance> mosGrouping = groupedMosListing.get(i);
			groupedMosInstances[i] = mosGrouping.toArray(new ManagedObjectSourceInstance[mosGrouping.size()]);
		}

		// Create the office floor meta-data
		rawMetaData.officeFloorMetaData = new OfficeFloorMetaDataImpl(teamListing.toArray(new TeamManagement[0]),
				groupedMosInstances, officeMetaDatas.toArray(new OfficeMetaData[0]), maxStartupWaitTime);

		// Return the raw meta-data
		return rawMetaData;
	}

	/**
	 * Struct to hold the order grouping of {@link ManagedObjectSourceInstance}.
	 */
	private static class OrderedGroupingStruct {

		/**
		 * Name of the {@link ManagedObjectSourceInstance}.
		 */
		private final String name;

		/**
		 * Names of the {@link ManagedObjectSourceInstance} instances that must be in
		 * the previous group load from this {@link ManagedObjectSourceInstance}.
		 */
		private final Set<String> nextGroupFrom = new HashSet<>();

		/**
		 * {@link ManagedObjectSourceConfiguration} for the
		 * {@link ManagedObjectSourceInstance}.
		 */
		private final ManagedObjectSourceConfiguration<?, ?> configuration;

		/**
		 * {@link ManagedObjectSourceInstance} instances.
		 */
		private final ManagedObjectSourceInstance<?> mosInstance;

		/**
		 * Instantiate.
		 * 
		 * @param name          Name of the {@link ManagedObjectSourceInstance}.
		 * @param configuration {@link ManagedObjectSourceConfiguration} for the
		 *                      {@link ManagedObjectSourceInstance}.
		 * @param mosInstance   {@link ManagedObjectSourceInstance} instances.
		 */
		public OrderedGroupingStruct(String name, ManagedObjectSourceConfiguration<?, ?> configuration,
				ManagedObjectSourceInstance<?> mosInstance) {
			this.name = name;
			this.configuration = configuration;
			this.mosInstance = mosInstance;
		}
	}

	/**
	 * Thrown to indicate a cyclic start up.
	 */
	private static class CyclicStartupException extends RuntimeException {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Initiate.
		 * 
		 * @param message Initiate with description for {@link OfficeFloorIssues}.
		 */
		public CyclicStartupException(String message) {
			super(message);
		}
	}

}