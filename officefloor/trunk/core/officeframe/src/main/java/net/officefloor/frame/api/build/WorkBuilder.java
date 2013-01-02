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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Builder of the {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkBuilder<W extends Work> {

	/**
	 * <p>
	 * Adds a {@link Work} bound {@link ManagedObject}.
	 * <p>
	 * Dependency scope:
	 * <ol>
	 * <li>Other {@link ManagedObject} instances added via this method.</li>
	 * <li>{@link ProcessState} bound {@link ManagedObject} instances linked to
	 * this {@link Work}.</li>
	 * </ol>
	 * 
	 * @param workManagedObjectName
	 *            Name of the {@link ManagedObject} to be referenced locally by
	 *            this {@link Work}.
	 * @param officeManagedObjectName
	 *            Name of the {@link ManagedObject} referenced locally within
	 *            the {@link Office}.
	 */
	DependencyMappingBuilder addWorkManagedObject(String workManagedObjectName,
			String officeManagedObjectName);

	/**
	 * Adds a {@link Work} bound {@link Administrator}.
	 * 
	 * @param workAdministratorName
	 *            Name of the {@link Administrator} to be referenced locally by
	 *            this {@link Work}.
	 * @param adminsistratorSource
	 *            {@link AdministratorSource} class.
	 * @return {@link AdministratorBuilder} for the {@link Administrator}.
	 */
	<I, A extends Enum<A>, AS extends AdministratorSource<I, A>> AdministratorBuilder<A> addWorkAdministrator(
			String workAdministratorName, Class<AS> adminsistratorSource);

	/**
	 * Specifies the initial {@link Task} of the {@link Work}.
	 * 
	 * @param initialTask
	 *            Initial {@link Task}.
	 */
	void setInitialTask(String initialTaskName);

	/**
	 * Creates the {@link TaskBuilder} to build a {@link Task} for this
	 * {@link Work}.
	 * 
	 * @param taskName
	 *            Name of task local to this {@link Work}.
	 * @param taskFactory
	 *            {@link TaskFactory} to create the {@link Task}.
	 * @return {@link TaskBuilder} for the {@link Task}.
	 */
	<D extends Enum<D>, F extends Enum<F>> TaskBuilder<W, D, F> addTask(
			String taskName, TaskFactory<? super W, D, F> taskFactory);

}