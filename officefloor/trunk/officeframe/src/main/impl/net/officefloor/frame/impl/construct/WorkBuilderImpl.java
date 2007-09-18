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
package net.officefloor.frame.impl.construct;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkAdministratorConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;

/**
 * Implementation of the {@link net.officefloor.frame.api.build.WorkBuilder}.
 * 
 * @author Daniel
 */
public class WorkBuilderImpl<W extends Work> implements WorkBuilder<W>,
		WorkConfiguration<W> {

	/**
	 * Registry of {@link TaskBuilder} instances by their names.
	 */
	private final Map<String, TaskBuilderImpl<?, W, ?, ?>> tasks;

	/**
	 * Registry of {@link Work} bound
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * by their names.
	 */
	private final Map<String, DependencyMappingBuilderImpl> workManagedObjects;

	/**
	 * List of {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	private final List<LinkedManagedObjectConfigurationImpl> processManagedObjects;

	/**
	 * Registry of
	 * {@link net.officefloor.frame.spi.administration.Administrator} instances
	 * by thier names.
	 */
	private final Map<String, AdministrationBuilderImpl> administrators;

	/**
	 * Name of the {@link Work}.
	 */
	private String workName;

	/**
	 * {@link WorkFactory}.
	 */
	private WorkFactory<W> workFactory;

	/**
	 * Initial task for the {@link net.officefloor.frame.api.execute.Work}.
	 */
	private String initialTaskName;

	/**
	 * Initiate with the key type for the listing
	 * {@link net.officefloor.frame.api.execute.Task} instances.
	 * 
	 * @param typeOfWork
	 *            {@link Class} of the {@link Work}.
	 */
	public WorkBuilderImpl(Class<W> typeOfWork) {
		this.tasks = new HashMap<String, TaskBuilderImpl<?, W, ?, ?>>();
		this.workManagedObjects = new HashMap<String, DependencyMappingBuilderImpl>();
		this.processManagedObjects = new LinkedList<LinkedManagedObjectConfigurationImpl>();
		this.administrators = new HashMap<String, AdministrationBuilderImpl>();
	}

	/**
	 * Specifies the name of the {@link Work}.
	 * 
	 * @param name
	 *            Name of the {@link Work}.
	 */
	protected void setWorkName(String name) {
		this.workName = name;
	}

	/*
	 * ====================================================================
	 * WorkBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.work.WorkMetaData#setWorkFactory(net.officefloor.frame.api.work.WorkFactory)
	 */
	public void setWorkFactory(WorkFactory<W> factory) {
		this.workFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#registerProcessManagedObject(java.lang.String,
	 *      java.lang.String)
	 */
	public void registerProcessManagedObject(String workManagedObjectName,
			String linkName) throws BuildException {
		this.processManagedObjects
				.add(new LinkedManagedObjectConfigurationImpl(
						workManagedObjectName, linkName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#addWorkManagedObject(java.lang.String,
	 *      java.lang.String)
	 */
	public DependencyMappingBuilder addWorkManagedObject(
			String workManagedObjectName, String managedObjectName)
			throws BuildException {

		// Create the dependency mapping builder
		DependencyMappingBuilderImpl dependencyMappingBuilder = new DependencyMappingBuilderImpl(
				workManagedObjectName, managedObjectName);

		// Register the Managed Object
		this.workManagedObjects.put(workManagedObjectName,
				dependencyMappingBuilder);

		// Return the dependency mapping builder
		return dependencyMappingBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#registerAdministration(java.lang.String)
	 */
	public AdministrationBuilder registerAdministration(
			String workAdministratorName, String administratorId)
			throws BuildException {

		// Create the administration builder
		AdministrationBuilderImpl adminBuilder = new AdministrationBuilderImpl(
				workAdministratorName, administratorId);

		// Register the Administrator
		this.administrators.put(administratorId, adminBuilder);

		// Return the administration builder
		return adminBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#setInitialTask(java.lang.String)
	 */
	public void setInitialTask(String initialTaskName) throws BuildException {
		this.initialTaskName = initialTaskName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#registerTask(java.lang.String,
	 *      java.lang.Class, java.lang.Class, java.lang.Class)
	 */
	public <P, M extends Enum<M>, F extends Enum<F>> TaskBuilder<P, W, M, F> addTask(
			String taskName, Class<P> parameterType,
			Class<M> managedObjectListingEnum, Class<F> flowListingEnum) {

		// Create the Task Builder
		TaskBuilderImpl<P, W, M, F> taskBuilder = new TaskBuilderImpl<P, W, M, F>(
				taskName, null);

		// Register the Task
		this.tasks.put(taskName, taskBuilder);

		// Return the Task Builder
		return taskBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkBuilder#registerTask(java.lang.String,
	 *      java.lang.Class)
	 */
	public <P> TaskBuilder<P, W, Indexed, Indexed> addTask(String taskName,
			Class<P> parameterType) {

		// Create the Task Builder
		TaskBuilderImpl<P, W, Indexed, Indexed> taskBuilder = new TaskBuilderImpl<P, W, Indexed, Indexed>(
				taskName, null);

		// Register the Task
		this.tasks.put(taskName, taskBuilder);

		// Return the Task Builder
		return taskBuilder;
	}

	/*
	 * ====================================================================
	 * WorkConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getWorkName()
	 */
	public String getWorkName() {
		return this.workName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getWorkFactory()
	 */
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getInitialTaskName()
	 */
	public String getInitialTaskName() {
		return this.initialTaskName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getTaskConfiguration()
	 */
	public TaskConfiguration<?, W, ?, ?>[] getTaskConfiguration()
			throws ConfigurationException {
		return (TaskConfiguration<?, W, ?, ?>[]) this.tasks.values().toArray(
				new TaskConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getProcessManagedObjectConfiguration()
	 */
	public LinkedManagedObjectConfiguration[] getProcessManagedObjectConfiguration()
			throws ConfigurationException {
		return this.processManagedObjects
				.toArray(new LinkedManagedObjectConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getManagedObjectConfiguration()
	 */
	public ManagedObjectConfiguration[] getManagedObjectConfiguration()
			throws ConfigurationException {
		return this.workManagedObjects.values().toArray(
				new ManagedObjectConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.WorkConfiguration#getAdministratorConfiguration()
	 */
	public WorkAdministratorConfiguration[] getAdministratorConfiguration()
			throws ConfigurationException {
		return this.administrators.values().toArray(
				new WorkAdministratorConfiguration[0]);
	}

}
