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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context for the {@link WorkContainer} and {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ContainerContext {

	/**
	 * Flags for the {@link JobNode} to wait and be activated at a later time
	 * once a dependency is ready.
	 */
	void flagJobToWait();

	/**
	 * <p>
	 * Adds a setup {@link Task} to be executed before the current
	 * {@link JobNode}.
	 * <p>
	 * This is available to {@link ManagedObject} and {@link Duty} setup. Note
	 * that if it is added during {@link Task} execution it will be executed
	 * after the {@link Task}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the setup {@link Task}.
	 * @param parameter
	 *            Parameter for the {@link Task}.
	 */
	void addSetupTask(TaskMetaData<?, ?, ?> taskMetaData, Object parameter);

	/**
	 * <p>
	 * Adds a {@link GovernanceActivity} to be executed before the current
	 * {@link JobNode}.
	 * <p>
	 * This is available to {@link ManagedObject} and {@link Duty} setup. Note
	 * that if it is added during {@link Task} execution it will be executed
	 * after the {@link Task}.
	 * 
	 * @param activity
	 *            {@link GovernanceActivity}.
	 */
	void addGovernanceActivity(GovernanceActivity<?, ?> activity);

}