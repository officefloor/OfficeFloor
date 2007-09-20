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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.team.Team;

/**
 * Builds a Office.
 * 
 * @author Daniel
 */
public interface OfficeBuilder {

	/**
	 * Registers a {@link Team} which will execute
	 * {@link net.officefloor.frame.api.execute.Task} instances within this
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team} to be referenced locally by this
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param teamId
	 *            Id of the {@link Team}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void registerTeam(String teamName, String teamId) throws BuildException;

	/**
	 * Registers the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} within this
	 * {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param managedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            to be referenced locally by this
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @param managedObjectId
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void registerManagedObject(String managedObjectName, String managedObjectId)
			throws BuildException;

	/**
	 * Adds a {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link net.officefloor.frame.spi.managedobject.ManagedObject} to
	 * this {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param linkName
	 *            Name to reference the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            for linking into {@link Work}.
	 * @param managedObjectName
	 *            Name of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            to be process bound.
	 * @return {@link DependencyMappingBuilder} to build any necessary
	 *         dependencies for the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @throws BuildException
	 *             Build failure.
	 */
	DependencyMappingBuilder addProcessManagedObject(String linkName,
			String managedObjectName) throws BuildException;

	/**
	 * Adds a
	 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}
	 * to this {@link OfficeBuilder}.
	 * 
	 * @param id
	 *            Id to register the
	 *            {@link net.officefloor.frame.spi.administration.source.AdministratorSource}
	 *            under.
	 * @param administratorBuilder
	 *            Builder of the
	 *            {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addAdministrator(String id, AdministratorBuilder<?> administratorBuilder)
			throws BuildException;

	/**
	 * Adds {@link Work} to be done within this {@link OfficeBuilder}.
	 * 
	 * @param name
	 *            Name identifying the {@link Work}.
	 * @param workBuilder
	 *            {@link WorkBuilder} of the {@link Work} to be done.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	void addWork(String name, WorkBuilder<?> workBuilder) throws BuildException;

	/**
	 * Adds a {@link net.officefloor.frame.api.execute.Task} to invoke on start
	 * up of the {@link net.officefloor.frame.api.manage.Office}.
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} on the
	 *            {@link Work}.
	 */
	void addStartupTask(String workName, String taskName);

}
