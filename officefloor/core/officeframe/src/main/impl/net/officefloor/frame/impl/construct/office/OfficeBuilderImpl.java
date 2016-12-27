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

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.impl.construct.administrator.AdministratorBuilderImpl;
import net.officefloor.frame.impl.construct.function.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.function.TaskBuilderImpl;
import net.officefloor.frame.impl.construct.function.TaskEscalationConfigurationImpl;
import net.officefloor.frame.impl.construct.governance.GovernanceBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.team.Team;

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
	 * Listing of {@link GovernanceConfiguration}.
	 */
	private final List<GovernanceConfiguration<?, ?>> governances = new LinkedList<GovernanceConfiguration<?, ?>>();

	/**
	 * Listing of {@link ThreadState} bound {@link ManagedObjectConfiguration}.
	 */
	private final List<ManagedObjectConfiguration<?>> threadManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Listing of {@link ProcessState} bound {@link Administrator}.
	 */
	private final List<AdministratorSourceConfiguration<?, ?>> processAdministrator = new LinkedList<AdministratorSourceConfiguration<?, ?>>();

	/**
	 * Listing of {@link ThreadState} bound {@link Administrator}.
	 */
	private final List<AdministratorSourceConfiguration<?, ?>> threadAdministrator = new LinkedList<AdministratorSourceConfiguration<?, ?>>();

	/**
	 * Listing of {@link ManagedFunctionConfiguration}.
	 */
	private final List<TaskBuilderImpl<?, ?>> functions = new LinkedList<TaskBuilderImpl<?, ?>>();

	/**
	 * Listing of registered {@link OfficeEnhancer} instances.
	 */
	private final List<OfficeEnhancer> officeEnhancers = new LinkedList<OfficeEnhancer>();

	/**
	 * Listing of the {@link EscalationFlow} instances.
	 */
	private final List<ManagedFunctionEscalationConfiguration> escalations = new LinkedList<ManagedFunctionEscalationConfiguration>();

	/**
	 * List of start up {@link ManagedFunction} instances for the
	 * {@link Office}.
	 */
	private final List<ManagedFunctionReference> startupFunctions = new LinkedList<ManagedFunctionReference>();

	/**
	 * Interval in milli-seconds to monitor the {@link Office}. Default is 1
	 * second.
	 */
	private long monitorOfficeInterval = 1000;

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
	public void setMonitorOfficeInterval(long monitorOfficeInterval) {
		this.monitorOfficeInterval = monitorOfficeInterval;
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
	public <E, F extends Enum<F>> GovernanceBuilder<F> addGovernance(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory) {
		GovernanceBuilderImpl<E, F> builder = new GovernanceBuilderImpl<E, F>(governanceName, extensionInterface,
				governanceFactory);
		this.governances.add(builder);
		return builder;
	}

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addProcessAdministrator(
			String processAdministratorName, Class<AS> adminsistratorSource) {
		AdministratorBuilderImpl<I, A, AS> builder = new AdministratorBuilderImpl<I, A, AS>(processAdministratorName,
				adminsistratorSource);
		this.processAdministrator.add(builder);
		return builder;
	}

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addThreadAdministrator(
			String threadAdministratorName, Class<AS> adminsistratorSource) {
		AdministratorBuilderImpl<I, A, AS> builder = new AdministratorBuilderImpl<I, A, AS>(threadAdministratorName,
				adminsistratorSource);
		this.threadAdministrator.add(builder);
		return builder;
	}

	@Override
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> addManagedFunction(String functionName,
			ManagedFunctionFactory<O, F> mangedFunctionFactory) {
		TaskBuilderImpl<O, F> functionBuilder = new TaskBuilderImpl<>(functionName, mangedFunctionFactory);
		this.functions.add(functionBuilder);
		return functionBuilder;
	}

	@Override
	public void addOfficeEnhancer(OfficeEnhancer officeEnhancer) {
		this.officeEnhancers.add(officeEnhancer);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
		this.escalations.add(new TaskEscalationConfigurationImpl(typeOfCause,
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
	public long getMonitorOfficeInterval() {
		return this.monitorOfficeInterval;
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
	public GovernanceConfiguration<?, ?>[] getGovernanceConfiguration() {
		return this.governances.toArray(new GovernanceConfiguration[this.governances.size()]);
	}

	@Override
	public AdministratorSourceConfiguration<?, ?>[] getProcessAdministratorSourceConfiguration() {
		return this.processAdministrator.toArray(new AdministratorSourceConfiguration[0]);
	}

	@Override
	public AdministratorSourceConfiguration<?, ?>[] getThreadAdministratorSourceConfiguration() {
		return this.threadAdministrator.toArray(new AdministratorSourceConfiguration[0]);
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
	public ManagedFunctionEscalationConfiguration[] getEscalationConfiguration() {
		return this.escalations.toArray(new ManagedFunctionEscalationConfiguration[0]);
	}

	@Override
	public FlowNodeBuilder<?> getFlowNodeBuilder(String namespace, String functionName) {

		// Obtain the function builder
		String namespacedFunctionName = getNamespacedName(namespace, functionName);
		for (TaskBuilderImpl<?, ?> builder : this.functions) {
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