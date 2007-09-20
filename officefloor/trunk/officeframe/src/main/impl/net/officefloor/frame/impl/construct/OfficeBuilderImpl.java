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

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.LinkedTeamConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;

/**
 * Implements the {@link net.officefloor.frame.api.build.OfficeBuilder}.
 * 
 * @author Daniel
 */
public class OfficeBuilderImpl implements OfficeBuilder, OfficeConfiguration {

	/**
	 * Listing of {@link net.officefloor.frame.spi.team.Team} name translations
	 * to {@link net.officefloor.frame.api.manage.OfficeFloor}.
	 */
	private final List<LinkedTeamConfigurationImpl> teams = new LinkedList<LinkedTeamConfigurationImpl>();

	/**
	 * Listing of
	 * {@link net.officefloor.frame.internal.configuration.LinkedManagedObjectConfiguration}.
	 */
	private final List<LinkedManagedObjectConfigurationImpl> managedObjects = new LinkedList<LinkedManagedObjectConfigurationImpl>();

	/**
	 * Registry of {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * instances by their names.
	 */
	private final Map<String, DependencyMappingBuilderImpl> processManagedObjects = new HashMap<String, DependencyMappingBuilderImpl>();

	/**
	 * Listing of registered {@link WorkBuilder} instances.
	 */
	private final Map<String, WorkBuilderImpl<?>> works = new HashMap<String, WorkBuilderImpl<?>>();

	/**
	 * Registry of the {@link AdministratorBuilderImpl} instances by their Id.
	 */
	private final Map<String, AdministratorBuilderImpl<?>> administrators = new HashMap<String, AdministratorBuilderImpl<?>>();

	/**
	 * List of start up {@link net.officefloor.frame.api.execute.Task} instances
	 * for the Office.
	 */
	private final List<TaskNodeReference> startupTasks = new LinkedList<TaskNodeReference>();

	/**
	 * Name of this {@link net.officefloor.frame.api.manage.Office}.
	 */
	private String officeName;

	/**
	 * Specify name of this {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param officeName
	 *            Name of this {@link net.officefloor.frame.api.manage.Office}.
	 */
	void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	/*
	 * ====================================================================
	 * OfficeBuilder
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeBuilder#registerTeam(java.lang.String,
	 *      java.lang.String)
	 */
	public void registerTeam(String teamName, String teamId)
			throws BuildException {
		this.teams.add(new LinkedTeamConfigurationImpl(teamId, teamName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeBuilder#registerManagedObject(java.lang.String,
	 *      java.lang.String)
	 */
	public void registerManagedObject(String managedObjectName,
			String managedObjectId) throws BuildException {
		this.managedObjects.add(new LinkedManagedObjectConfigurationImpl(
				managedObjectName, managedObjectId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeBuilder#addProcessManagedObject(java.lang.String,
	 *      java.lang.String)
	 */
	public DependencyMappingBuilder addProcessManagedObject(String linkName,
			String managedObjectName) throws BuildException {
		// Create the dependency mapping builder
		DependencyMappingBuilderImpl dependencyMappingBuilder = new DependencyMappingBuilderImpl(
				linkName, managedObjectName);

		// Register the Managed Object
		this.processManagedObjects.put(linkName, dependencyMappingBuilder);

		// Return the dependency mapping builder
		return dependencyMappingBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.construct.Office#addWork(net.officefloor.frame.api.work.WorkMetaData)
	 */
	public void addWork(String name, WorkBuilder<?> workBuilder)
			throws BuildException {

		// Ensure is correct type
		if (!(workBuilder instanceof WorkBuilderImpl)) {
			throw new BuildException(WorkBuilder.class.getName()
					+ " must be a " + WorkBuilderImpl.class.getName()
					+ " but is a " + workBuilder.getClass().getName());
		}

		// Specify name
		WorkBuilderImpl<?> impl = (WorkBuilderImpl<?>) workBuilder;
		impl.setWorkName(name);

		// Register the Work
		this.works.put(name, impl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeBuilder#addTaskAdministrator(java.lang.String,
	 *      net.officefloor.frame.api.build.TaskAdministratorBuilder)
	 */
	public void addAdministrator(String id, AdministratorBuilder<?> builder)
			throws BuildException {

		// Ensure is correct type
		if (!(builder instanceof AdministratorBuilderImpl)) {
			throw new BuildException(AdministratorBuilder.class.getName()
					+ " must be a " + AdministratorBuilderImpl.class.getName()
					+ " but is a " + builder.getClass().getName());
		}

		// Specify the name
		AdministratorBuilderImpl<?> impl = (AdministratorBuilderImpl<?>) builder;
		impl.setAdministratorName(id);

		// Add
		this.administrators.put(id, impl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.OfficeBuilder#addStartupTask(java.lang.String,
	 *      java.lang.String)
	 */
	public void addStartupTask(String workName, String taskName) {
		// Add the start up task
		this.startupTasks.add(new TaskNodeReferenceImpl(workName, taskName));
	}

	/*
	 * ====================================================================
	 * OfficeConfiguration
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getOfficeName()
	 */
	public String getOfficeName() {
		return this.officeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getRegisteredTeams()
	 */
	public LinkedTeamConfiguration[] getRegisteredTeams() {
		return this.teams.toArray(new LinkedTeamConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getRegisteredManagedObjects()
	 */
	public LinkedManagedObjectConfiguration[] getRegisteredManagedObjects() {
		return this.managedObjects
				.toArray(new LinkedManagedObjectConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getManagedObjectConfiguration()
	 */
	public ManagedObjectConfiguration[] getManagedObjectConfiguration()
			throws ConfigurationException {
		return this.processManagedObjects.values().toArray(
				new ManagedObjectConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getWorkConfiguration()
	 */
	@SuppressWarnings("unchecked")
	public <W extends Work> WorkConfiguration<W>[] getWorkConfiguration()
			throws ConfigurationException {
		return this.works.values().toArray(new WorkConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getAdministratorSourceConfiguration()
	 */
	public AdministratorSourceConfiguration[] getAdministratorSourceConfiguration()
			throws ConfigurationException {
		return this.administrators.values().toArray(
				new AdministratorSourceConfiguration[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.OfficeConfiguration#getStartupTasks()
	 */
	public TaskNodeReference[] getStartupTasks() {
		// Return the list of start up tasks
		return this.startupTasks.toArray(new TaskNodeReference[0]);
	}

}

/**
 * Implementation of
 * {@link net.officefloor.frame.internal.configuration.LinkedTeamConfiguration}.
 */
class LinkedTeamConfigurationImpl implements LinkedTeamConfiguration {

	/**
	 * {@link net.officefloor.frame.spi.team.Team} Id.
	 */
	private final String teamId;

	/**
	 * {@link net.officefloor.frame.spi.team.Team} name.
	 */
	private final String teamName;

	/**
	 * Initiate.
	 * 
	 * @param teamId
	 *            {@link net.officefloor.frame.spi.team.Team} Id.
	 * @param teamName
	 *            {@link net.officefloor.frame.spi.team.Team} name.
	 */
	public LinkedTeamConfigurationImpl(String teamId, String teamName) {
		this.teamId = teamId;
		this.teamName = teamName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.LinkedTeamConfiguration#getTeamId()
	 */
	public String getTeamId() {
		return this.teamId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.configuration.LinkedTeamConfiguration#getTeamName()
	 */
	public String getTeamName() {
		return this.teamName;
	}

}