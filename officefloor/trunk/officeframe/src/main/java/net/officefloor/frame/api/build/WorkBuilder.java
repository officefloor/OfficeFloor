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

/**
 * Meta-data of the {@link net.officefloor.frame.api.execute.Work}.
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
	 * Links a {@link net.officefloor.frame.internal.structure.ProcessState}
	 * bound {@link net.officefloor.frame.spi.managedobject.ManagedObject} to
	 * this {@link Work}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            to be referenced locally by this {@link Work}.
	 * @param linkName
	 *            Link name identifying the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}
	 *            bound
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void registerProcessManagedObject(String workManagedObjectName,
			String linkName) throws BuildException;

	/**
	 * Registers the translation of the key to the Id of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            to be referenced locally by this {@link Work}.
	 * @param managedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            referenced locally within the
	 *            {@link net.officefloor.frame.api.manage.Office}.
	 * @throws BuildException
	 *             Build failure.
	 */
	DependencyMappingBuilder addWorkManagedObject(String workManagedObjectName,
			String managedObjectName) throws BuildException;

	/**
	 * Registers the
	 * {@link net.officefloor.frame.spi.administration.Administrator} to
	 * administor the resulting {@link Work}.
	 * 
	 * @param workAdministratorName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            to be referenced locally by this {@link Work}.
	 * @param administratorId
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
	 * @throws BuildException
	 *             Build failure.
	 */
	AdministrationBuilder registerAdministration(String workAdministratorName,
			String administratorId) throws BuildException;

	/**
	 * Specifies the initial {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param initialTask
	 *            Initial {@link net.officefloor.frame.api.execute.Task}.
	 * @throws BuildException
	 *             Build failure.
	 */
	void setInitialTask(String initialTaskName) throws BuildException;

	/**
	 * Creates the {@link TaskBuilder} to build a {@link Task} for this
	 * {@link Work}.
	 * 
	 * @param P
	 *            Type of the parameter to the {@link Task}.
	 * @param M
	 *            {@link Enum} providing the listing of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances.
	 * @param F
	 *            {@link Enum} providing hte listing of
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances.
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @param managedObjectListingEnum
	 *            {@link Enum} providing the listing of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances.
	 * @param flowListingEnum
	 *            {@link Enum} providing hte listing of
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances.
	 * @return Specific {@link TaskBuilder}.
	 */
	<P extends Object, M extends Enum<M>, F extends Enum<F>> TaskBuilder<P, W, M, F> addTask(
			String taskName, Class<P> parameterType,
			Class<M> managedObjectListingEnum, Class<F> flowListingEnum);

	/**
	 * Creates the {@link TaskBuilder} to build a {@link Task} for this
	 * {@link Work}.
	 * 
	 * @param P
	 *            Type of the parameter to the {@link Task}.
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param parameterType
	 *            Type of parameter to the {@link Task}.
	 * @return Specific {@link TaskBuilder}.
	 */
	<P extends Object> TaskBuilder<P, W, Indexed, Indexed> addTask(
			String taskName, Class<P> parameterType);

}
