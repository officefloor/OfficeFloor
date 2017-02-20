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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.FlowBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.function.EscalationConfigurationImpl;
import net.officefloor.frame.impl.construct.governance.GovernanceBuilderImpl;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionBuilderImpl;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implements the {@link OfficeBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuilderImpl implements OfficeBuilder, OfficeConfiguration {

	/**
	 * Obtains the name with the added namespace.
	 * 
	 * @param namespace
	 *            Namespace.
	 * @param name
	 *            Name.
	 * @return Name within the namespace.
	 */
	public static String getNamespacedName(String namespace, String name) {

		// Null name indicates no name
		if (name == null) {
			return null;
		}

		// Return the name spaced name
		return (ConstructUtil.isBlank(namespace) ? "" : namespace + ".") + name;
	}

	/**
	 * Name of this {@link Office}.
	 */
	private final String officeName;

	/**
	 * Listing of {@link LinkedTeamConfiguration}.
	 */
	private final List<LinkedTeamConfiguration> teams = new LinkedList<LinkedTeamConfiguration>();

	/**
	 * Default {@link Team}.
	 */
	private String defaultOfficeTeamName = null;

	/**
	 * Listing of {@link LinkedManagedObjectSourceConfiguration}.
	 */
	private final List<LinkedManagedObjectSourceConfiguration> managedObjectSources = new LinkedList<LinkedManagedObjectSourceConfiguration>();

	/**
	 * Listing of {@link BoundInputManagedObjectConfiguration}.
	 */
	private final List<BoundInputManagedObjectConfiguration> boundInputManagedObjects = new LinkedList<BoundInputManagedObjectConfiguration>();

	/**
	 * Listing of {@link ProcessState} bound {@link ManagedObjectConfiguration}.
	 */
	private final List<ManagedObjectConfiguration<?>> processManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Flags whether to manually manage {@link Governance} via
	 * {@link Administration} instances.
	 */
	private boolean isManuallyManageGovernance = false;

	/**
	 * Listing of {@link GovernanceConfiguration}.
	 */
	private final List<GovernanceConfiguration<?, ?>> governances = new LinkedList<GovernanceConfiguration<?, ?>>();

	/**
	 * Listing of {@link ThreadState} bound {@link ManagedObjectConfiguration}.
	 */
	private final List<ManagedObjectConfiguration<?>> threadManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Listing of {@link ManagedFunctionConfiguration}.
	 */
	private final List<ManagedFunctionBuilderImpl<?, ?>> functions = new LinkedList<ManagedFunctionBuilderImpl<?, ?>>();

	/**
	 * Listing of registered {@link OfficeEnhancer} instances.
	 */
	private final List<OfficeEnhancer> officeEnhancers = new LinkedList<OfficeEnhancer>();

	/**
	 * Listing of the {@link EscalationFlow} instances.
	 */
	private final List<EscalationConfiguration> escalations = new LinkedList<EscalationConfiguration>();

	/**
	 * List of start up {@link ManagedFunction} instances for the
	 * {@link Office}.
	 */
	private final List<ManagedFunctionReference> startupFunctions = new LinkedList<ManagedFunctionReference>();

	/**
	 * {@link OfficeClock}.
	 */
	private OfficeClock clock = null;

	/**
	 * Interval in milli-seconds to monitor the {@link Office}. Default is 1
	 * second.
	 */
	private long monitorOfficeInterval = 1000;

	/**
	 * <p>
	 * Maximum {@link FunctionState} chain depth before it is broken.
	 * <p>
	 * Default set high enough to effectively have no breaking.
	 */
	private int maximumFunctionStateChainLength = Integer.MAX_VALUE;

	/**
	 * {@link Profiler}.
	 */
	private Profiler profiler = null;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of this {@link Office}.
	 */
	public OfficeBuilderImpl(String officeName) {
		this.officeName = officeName;
	}

	/*
	 * ============ OfficeBuilder =========================================
	 */

	@Override
	public void setOfficeClock(OfficeClock clock) {
		this.clock = clock;
	}

	@Override
	public void setMonitorOfficeInterval(long monitorOfficeInterval) {
		this.monitorOfficeInterval = monitorOfficeInterval;
	}

	@Override
	public void setMaximumFunctionStateChainLength(int maximumFunctionStateChainLength) {
		this.maximumFunctionStateChainLength = maximumFunctionStateChainLength;
	}

	@Override
	public void registerTeam(String officeTeamName, String officeFloorTeamName) {
		this.teams.add(new LinkedTeamConfigurationImpl(officeTeamName, officeFloorTeamName));
	}

	@Override
	public void setDefaultTeam(String officeTeamName) {
		this.defaultOfficeTeamName = officeTeamName;
	}

	@Override
	public void registerManagedObjectSource(String officeManagedObjectName, String officeFloorManagedObjectSourceName) {
		this.managedObjectSources.add(new LinkedManagedObjectSourceConfigurationImpl(officeManagedObjectName,
				officeFloorManagedObjectSourceName));
	}

	@Override
	public void setBoundInputManagedObject(String inputManagedObjectName, String managedObjectSourceName) {
		this.boundInputManagedObjects
				.add(new BoundInputManagedObjectConfigurationImpl(inputManagedObjectName, managedObjectSourceName));
	}

	@Override
	@SuppressWarnings("rawtypes")
	public DependencyMappingBuilder addThreadManagedObject(String threadManagedObjectName,
			String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(threadManagedObjectName,
				officeManagedObjectName);
		this.threadManagedObjects.add(builder);
		return builder;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public DependencyMappingBuilder addProcessManagedObject(String processManagedObjectName,
			String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(processManagedObjectName,
				officeManagedObjectName);
		this.processManagedObjects.add(builder);
		return builder;
	}

	@Override
	public void setManuallyManageGovernance(boolean isManuallyManageGovernance) {
		this.isManuallyManageGovernance = isManuallyManageGovernance;
	}

	@Override
	public <E, F extends Enum<F>> GovernanceBuilder<F> addGovernance(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory) {
		GovernanceBuilderImpl<E, F> builder = new GovernanceBuilderImpl<E, F>(governanceName, extensionInterface,
				governanceFactory);
		this.governances.add(builder);
		return builder;
	}

	@Override
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> addManagedFunction(String functionName,
			ManagedFunctionFactory<O, F> mangedFunctionFactory) {
		ManagedFunctionBuilderImpl<O, F> functionBuilder = new ManagedFunctionBuilderImpl<>(functionName,
				mangedFunctionFactory);
		this.functions.add(functionBuilder);
		return functionBuilder;
	}

	@Override
	public void addOfficeEnhancer(OfficeEnhancer officeEnhancer) {
		this.officeEnhancers.add(officeEnhancer);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				new ManagedFunctionReferenceImpl(functionName, typeOfCause)));
	}

	@Override
	public void addStartupFunction(String functionName) {
		// No argument to a start up function
		this.startupFunctions.add(new ManagedFunctionReferenceImpl(functionName, null));
	}

	@Override
	public void setProfiler(Profiler profiler) {
		this.profiler = profiler;
	}

	/*
	 * ================= OfficeConfiguration ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public OfficeClock getOfficeClock() {
		return this.clock;
	}

	@Override
	public long getMonitorOfficeInterval() {
		return this.monitorOfficeInterval;
	}

	@Override
	public int getMaximumFunctionStateChainLength() {
		return this.maximumFunctionStateChainLength;
	}

	@Override
	public OfficeBuilder getBuilder() {
		return this;
	}

	@Override
	public LinkedTeamConfiguration[] getRegisteredTeams() {
		return this.teams.toArray(new LinkedTeamConfiguration[0]);
	}

	@Override
	public String getOfficeDefaultTeamName() {
		return this.defaultOfficeTeamName;
	}

	@Override
	public LinkedManagedObjectSourceConfiguration[] getRegisteredManagedObjectSources() {
		return this.managedObjectSources.toArray(new LinkedManagedObjectSourceConfiguration[0]);
	}

	@Override
	public BoundInputManagedObjectConfiguration[] getBoundInputManagedObjectConfiguration() {
		return this.boundInputManagedObjects.toArray(new BoundInputManagedObjectConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getProcessManagedObjectConfiguration() {
		return this.processManagedObjects.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getThreadManagedObjectConfiguration() {
		return this.threadManagedObjects.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public boolean isManuallyManageGovernance() {
		return this.isManuallyManageGovernance;
	}

	@Override
	public GovernanceConfiguration<?, ?>[] getGovernanceConfiguration() {
		return this.governances.toArray(new GovernanceConfiguration[this.governances.size()]);
	}

	@Override
	public ManagedFunctionConfiguration<?, ?>[] getManagedFunctionConfiguration() {
		return this.functions.toArray(new ManagedFunctionConfiguration[0]);
	}

	@Override
	public OfficeEnhancer[] getOfficeEnhancers() {
		return this.officeEnhancers.toArray(new OfficeEnhancer[0]);
	}

	@Override
	public EscalationConfiguration[] getEscalationConfiguration() {
		return this.escalations.toArray(new EscalationConfiguration[0]);
	}

	@Override
	public FlowBuilder<?> getFlowBuilder(String namespace, String functionName) {

		// Obtain the function builder
		String namespacedFunctionName = getNamespacedName(namespace, functionName);
		for (ManagedFunctionBuilderImpl<?, ?> builder : this.functions) {
			if (namespacedFunctionName.equals(builder.getFunctionName())) {
				return builder;
			}
		}

		// As here, no function by name
		return null;
	}

	@Override
	public ManagedFunctionReference[] getStartupFunctions() {
		return this.startupFunctions.toArray(new ManagedFunctionReference[0]);
	}

	@Override
	public Profiler getProfiler() {
		return this.profiler;
	}

}