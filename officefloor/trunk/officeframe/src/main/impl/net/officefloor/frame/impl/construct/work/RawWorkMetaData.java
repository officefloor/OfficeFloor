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

import net.officefloor.frame.api.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.task.RawTaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Raw meta-data of {@link Work}.
 * 
 * @author Daniel
 */
public interface RawWorkMetaData<W extends Work> {

	/**
	 * Obtains the name of the {@link Work}.
	 * 
	 * @return Name of the {@link Work}.
	 */
	String getWorkName();

	/**
	 * Obtains the {@link RawOfficeMetaData} of the {@link Office} containing
	 * this {@link Work}.
	 * 
	 * @return {@link RawOfficeMetaData}.
	 */
	RawOfficeMetaData getRawOfficeMetaData();

	/**
	 * Constructs the {@link RawWorkManagedObjectMetaData} for the
	 * {@link ManagedObject} of the {@link Work}.
	 * 
	 * @param workManagedObjectName
	 *            Name of the {@link ManagedObject} within the {@link Work}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return Constructed {@link RawWorkManagedObjectMetaData} or
	 *         <code>null</code> if issue in constructing it.
	 */
	RawWorkManagedObjectMetaData constructRawWorkManagedObjectMetaData(
			String workManagedObjectName, OfficeFloorIssues issues);

	/**
	 * Constructs the {@link RawWorkAdministratorMetaData} for the
	 * {@link Administrator} of the {@link Work}.
	 * 
	 * @param workAdministratorName
	 *            Name of the {@link Administrator} within the {@link Work}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return Constructed {@link RawWorkAdministratorMetaData} or
	 *         <code>null</code> if issue in constructing it.
	 */
	RawWorkAdministratorMetaData constructRawWorkAdministratorMetaData(
			String workAdministratorName, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link WorkMetaData} for this {@link RawWorkMetaData}.
	 * 
	 * @return {@link WorkMetaData}.
	 */
	WorkMetaData<W> getWorkMetaData();

	/**
	 * Obtains the {@link RawTaskMetaData} on the {@link Work} by the input
	 * name.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return {@link RawTaskMetaData} or <code>null</code> if unknown
	 *         {@link Task}.
	 */
	RawTaskMetaData<?, W, ?, ?> getRawTaskMetaData(String taskName);

}