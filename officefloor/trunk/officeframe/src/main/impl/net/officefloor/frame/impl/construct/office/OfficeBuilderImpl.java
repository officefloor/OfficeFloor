/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
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
import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.administrator.AdministratorBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.task.TaskNodeReferenceImpl;
import net.officefloor.frame.impl.construct.work.WorkBuilderImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Implements the {@link OfficeBuilder}.
 * 
 * @author Daniel
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

		// Return the namspaced name
		return (namespace == null ? "" : namespace + ".") + name;
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
	 * {@link EscalationHandler} for this {@link Office}.
	 */
	private EscalationHandler officeEscalationHandler = null;

	/**
	 * List of start up {@link Task} instances for the {@link Office}.
	 */
	private final List<TaskNodeReference> startupTasks = new LinkedList<TaskNodeReference>();

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
	@SuppressWarnings("unchecked")
	public DependencyMappingBuilder addThreadManagedObject(
			String threadManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(
				threadManagedObjectName, officeManagedObjectName);
		this.threadManagedObjects.add(builder);
		return builder;
	}

	@Override
	@SuppressWarnings("unchecked")
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
	public void setOfficeEscalationHandler(
			EscalationHandler officeEscalationHandler) {
		this.officeEscalationHandler = officeEscalationHandler;
	}

	@Override
	public void addStartupTask(String workName, String taskName) {
		this.startupTasks.add(new TaskNodeReferenceImpl(workName, taskName));
	}

	/*
	 * ================= OfficeConfiguration ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
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
	public EscalationHandler getOfficeEscalationHandler() {
		return this.officeEscalationHandler;
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