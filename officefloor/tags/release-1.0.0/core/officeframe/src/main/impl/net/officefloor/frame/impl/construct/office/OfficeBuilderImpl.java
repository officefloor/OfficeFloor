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

package net.officefloor.frame.impl.construct.office;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.administrator.AdministratorBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.task.TaskEscalationConfigurationImpl;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.construct.work.WorkBuilderImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

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
	 * Listing of {@link WorkConfiguration}.
	 */
	private final List<WorkBuilderImpl<?>> works = new LinkedList<WorkBuilderImpl<?>>();

	/**
	 * Listing of registered {@link OfficeEnhancer} instances.
	 */
	private final List<OfficeEnhancer> officeEnhancers = new LinkedList<OfficeEnhancer>();

	/**
	 * Listing of the {@link EscalationFlow} instances.
	 */
	private final List<TaskEscalationConfiguration> escalations = new LinkedList<TaskEscalationConfiguration>();

	/**
	 * List of start up {@link Task} instances for the {@link Office}.
	 */
	private final List<TaskNodeReference> startupTasks = new LinkedList<TaskNodeReference>();

	/**
	 * Interval in milli-seconds to monitor the {@link Office}. Default is 1
	 * second.
	 */
	private long monitorOfficeInterval = 1000;

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
		this.teams.add(new LinkedTeamConfigurationImpl(officeTeamName,
				officeFloorTeamName));
	}

	@Override
	public void registerManagedObjectSource(String officeManagedObjectName,
			String officeFloorManagedObjectSourceName) {
		this.managedObjectSources
				.add(new LinkedManagedObjectSourceConfigurationImpl(
						officeManagedObjectName,
						officeFloorManagedObjectSourceName));
	}

	@Override
	public void setBoundInputManagedObject(String inputManagedObjectName,
			String managedObjectSourceName) {
		this.boundInputManagedObjects
				.add(new BoundInputManagedObjectConfigurationImpl(
						inputManagedObjectName, managedObjectSourceName));
	}

	@Override
	@SuppressWarnings("rawtypes")
	public DependencyMappingBuilder addThreadManagedObject(
			String threadManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(
				threadManagedObjectName, officeManagedObjectName);
		this.threadManagedObjects.add(builder);
		return builder;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public DependencyMappingBuilder addProcessManagedObject(
			String processManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(
				processManagedObjectName, officeManagedObjectName);
		this.processManagedObjects.add(builder);
		return builder;
	}

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addProcessAdministrator(
			String processAdministratorName, Class<AS> adminsistratorSource) {
		AdministratorBuilderImpl<I, A, AS> builder = new AdministratorBuilderImpl<I, A, AS>(
				processAdministratorName, adminsistratorSource);
		this.processAdministrator.add(builder);
		return builder;
	}

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addThreadAdministrator(
			String threadAdministratorName, Class<AS> adminsistratorSource) {
		AdministratorBuilderImpl<I, A, AS> builder = new AdministratorBuilderImpl<I, A, AS>(
				threadAdministratorName, adminsistratorSource);
		this.threadAdministrator.add(builder);
		return builder;
	}

	@Override
	public <W extends Work> WorkBuilder<W> addWork(String workName,
			WorkFactory<W> workFactory) {
		WorkBuilderImpl<W> workBuilder = new WorkBuilderImpl<W>(workName,
				workFactory);
		this.works.add(workBuilder);
		return workBuilder;
	}

	@Override
	public void addOfficeEnhancer(OfficeEnhancer officeEnhancer) {
		this.officeEnhancers.add(officeEnhancer);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause,
			String workName, String taskName) {
		this.escalations.add(new TaskEscalationConfigurationImpl(typeOfCause,
				new TaskNodeReferenceImpl(workName, taskName, typeOfCause)));
	}

	@Override
	public void addStartupTask(String workName, String taskName) {
		// No argument to a start up task
		this.startupTasks.add(new TaskNodeReferenceImpl(workName, taskName,
				null));
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
	public LinkedManagedObjectSourceConfiguration[] getRegisteredManagedObjectSources() {
		return this.managedObjectSources
				.toArray(new LinkedManagedObjectSourceConfiguration[0]);
	}

	@Override
	public BoundInputManagedObjectConfiguration[] getBoundInputManagedObjectConfiguration() {
		return this.boundInputManagedObjects
				.toArray(new BoundInputManagedObjectConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getProcessManagedObjectConfiguration() {
		return this.processManagedObjects
				.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getThreadManagedObjectConfiguration() {
		return this.threadManagedObjects
				.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public AdministratorSourceConfiguration<?, ?>[] getProcessAdministratorSourceConfiguration() {
		return this.processAdministrator
				.toArray(new AdministratorSourceConfiguration[0]);
	}

	@Override
	public AdministratorSourceConfiguration<?, ?>[] getThreadAdministratorSourceConfiguration() {
		return this.threadAdministrator
				.toArray(new AdministratorSourceConfiguration[0]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <W extends Work> WorkConfiguration<W>[] getWorkConfiguration() {
		return this.works.toArray(new WorkConfiguration[0]);
	}

	@Override
	public OfficeEnhancer[] getOfficeEnhancers() {
		return this.officeEnhancers.toArray(new OfficeEnhancer[0]);
	}

	@Override
	public TaskEscalationConfiguration[] getEscalationConfiguration() {
		return this.escalations.toArray(new TaskEscalationConfiguration[0]);
	}

	@Override
	public FlowNodeBuilder<?> getFlowNodeBuilder(String namespace,
			String workName, String taskName) {

		// Obtain the work builder
		String namespacedWorkName = getNamespacedName(namespace, workName);
		WorkBuilderImpl<?> workBuilder = null;
		for (WorkBuilderImpl<?> builder : this.works) {
			if (namespacedWorkName.equals(builder.getWorkName())) {
				workBuilder = builder;
			}
		}
		if (workBuilder == null) {
			return null; // no work builder by name
		}

		// Obtain the task builder (flow node builder)
		return workBuilder.getTaskBuilder(namespace, taskName);
	}

	@Override
	public TaskNodeReference[] getStartupTasks() {
		return this.startupTasks.toArray(new TaskNodeReference[0]);
	}

}