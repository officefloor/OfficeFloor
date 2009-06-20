/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Builds details of a {@link ManagedObjectSource} being managed by an
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagingOfficeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the name to bind the {@link ManagedObject} within the
	 * {@link ProcessState} within the {@link Office}.
	 * 
	 * @param processBoundManagedObjectName
	 *            Name to bind the {@link ManagedObject} within the
	 *            {@link ProcessState} within the {@link Office}.
	 */
	void setProcessBoundManagedObjectName(String processBoundManagedObjectName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link Task} within the managing {@link Office}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} instigated by the
	 *            {@link ManagedObjectSource}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 */
	void linkProcess(F key, String workName, String taskName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link Task} within the managing {@link Office}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} instigated by the
	 *            {@link ManagedObjectSource}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 */
	void linkProcess(int flowIndex, String workName, String taskName);

}