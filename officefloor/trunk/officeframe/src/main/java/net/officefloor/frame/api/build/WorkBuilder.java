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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Work}.
 * 
 * @author Daniel
 */
public interface WorkBuilder<W extends Work> {

	/**
	 * Specifies the {@link WorkFactory}.
	 * 
	 * @param factory
	 *            {@link WorkFactory}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void setWorkFactory(WorkFactory<W> factory) throws BuildException;

	/**
	 * Links a {@link ProcessState} bound {@link ManagedObject} to this
	 * {@link Work}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the {@link ManagedObject} to be referenced locally by
	 *            this {@link Work}.
	 * @param linkName
	 *            Link name identifying the {@link ProcessState} bound
	 *            {@link ManagedObject}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void registerProcessManagedObject(String workManagedObjectName,
			String linkName) throws BuildException;

	/**
	 * Registers the translation of the key to the Id of the
	 * {@link ManagedObject}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the {@link ManagedObject} to be referenced locally by
	 *            this {@link Work}.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject} referenced locally within
	 *            the {@link Office}.
	 * @throws BuildException
	 *             Build failure.
	 */
	DependencyMappingBuilder addWorkManagedObject(String workManagedObjectName,
			String managedObjectName) throws BuildException;

	/**
	 * Registers the {@link Administrator} to administor the resulting
	 * {@link Work}.
	 * 
	 * @param workAdministratorName
	 *            Name of the {@link Administrator} to be referenced locally by
	 *            this {@link Work}.
	 * @param administratorId
	 *            Id of the {@link AdministratorSource}.
	 * @throws BuildException
	 *             Build failure.
	 */
	AdministrationBuilder registerAdministration(String workAdministratorName,
			String administratorId) throws BuildException;

	/**
	 * Specifies the initial {@link Task} of the {@link Work}.
	 * 
	 * @param initialTask
	 *            Initial {@link Task}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void setInitialTask(String initialTaskName) throws BuildException;

	/**
	 * Creates the {@link TaskBuilder} to build a {@link Task} for this
	 * {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @param managedObjectListingEnum
	 *            {@link Enum} providing the listing of {@link ManagedObject}
	 *            instances.
	 * @param flowListingEnum
	 *            {@link Enum} providing the listing of {@link Flow} instances.
	 * @return Specific {@link TaskBuilder}.
	 */
	<P extends Object, M extends Enum<M>, F extends Enum<F>> TaskBuilder<P, W, M, F> addTask(
			String taskName, Class<P> parameterType,
			Class<M> managedObjectListingEnum, Class<F> flowListingEnum);

	/**
	 * Creates the {@link TaskBuilder} to build a {@link Task} for this
	 * {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @return Specific {@link TaskBuilder}.
	 */
	<P extends Object> TaskBuilder<P, W, Indexed, Indexed> addTask(
			String taskName, Class<P> parameterType);

}
