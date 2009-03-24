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
package net.officefloor.frame.impl.construct.work;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.administrator.AdministratorBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.task.TaskBuilderImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link WorkBuilder} implementation.
 * 
 * @author Daniel
 */
public class WorkBuilderImpl<W extends Work> implements WorkBuilder<W>,
		WorkConfiguration<W> {

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link WorkFactory}.
	 */
	private final WorkFactory<W> workFactory;

	/**
	 * Listing of {@link Work} bound {@link ManagedObject} configuration.
	 */
	private final List<ManagedObjectConfiguration<?>> workManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Listing of {@link Work} bound {@link Administrator} configuration.
	 */
	private final List<AdministratorSourceConfiguration<?, ?>> workAdministrators = new LinkedList<AdministratorSourceConfiguration<?, ?>>();

	/**
	 * Name of the initial {@link Task} for the {@link Work}.
	 */
	private String initialTaskName;

	/**
	 * Listing of {@link TaskConfiguration}.
	 */
	private final List<TaskBuilderImpl<W, ?, ?>> tasks = new LinkedList<TaskBuilderImpl<W, ?, ?>>();

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory}.
	 */
	public WorkBuilderImpl(String workName, WorkFactory<W> workFactory) {
		this.workName = workName;
		this.workFactory = workFactory;
	}

	/**
	 * Obtains the {@link TaskBuilder}.
	 * 
	 * @param namespace
	 *            Name space to identify the {@link TaskBuilder}.
	 * @param taskName
	 *            Name of the {@link TaskBuilder}.
	 * @return {@link TaskBuilder}.
	 */
	public TaskBuilder<W, ?, ?> getTaskBuilder(String namespace, String taskName) {

		// Obtain the task builder
		TaskBuilderImpl<W, ?, ?> taskBuilder = null;
		for (TaskBuilderImpl<W, ?, ?> task : this.tasks) {
			if (taskName.equals(task.getTaskName())) {
				taskBuilder = task;
			}
		}

		// Return the task builder (whether have or not)
		return taskBuilder;
	}

	/*
	 * =============== WorkBuilder ========================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public DependencyMappingBuilder addWorkManagedObject(
			String workManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl(
				workManagedObjectName, officeManagedObjectName);
		this.workManagedObjects.add(builder);
		return builder;
	}

	@Override
	public <I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addWorkAdministrator(
			String workAdministratorName, Class<AS> adminsistratorSource) {
		AdministratorBuilderImpl<I, A, AS> builder = new AdministratorBuilderImpl<I, A, AS>(
				workAdministratorName, adminsistratorSource);
		this.workAdministrators.add(builder);
		return builder;
	}

	@Override
	public void setInitialTask(String initialTaskName) {
		this.initialTaskName = initialTaskName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, F extends Enum<F>> TaskBuilder<W, D, F> addTask(
			String taskName, TaskFactory<? super W, D, F> taskFactory) {
		TaskBuilderImpl builder = new TaskBuilderImpl(taskName, taskFactory);
		this.tasks.add(builder);
		return builder;
	}

	/*
	 * ================== WorkConfiguration ===============================
	 */

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	@Override
	public ManagedObjectConfiguration<?>[] getManagedObjectConfiguration() {
		return this.workManagedObjects
				.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public AdministratorSourceConfiguration<?, ?>[] getAdministratorConfiguration() {
		return this.workAdministrators
				.toArray(new AdministratorSourceConfiguration[0]);
	}

	@Override
	public String getInitialTaskName() {
		return this.initialTaskName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskConfiguration<W, ?, ?>[] getTaskConfiguration() {
		return this.tasks.toArray(new TaskConfiguration[0]);
	}

}
