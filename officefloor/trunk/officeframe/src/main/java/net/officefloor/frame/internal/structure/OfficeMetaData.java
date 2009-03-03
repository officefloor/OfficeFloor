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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Office}.
 * 
 * @author Daniel
 */
public interface OfficeMetaData {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains the {@link ProcessMetaData} for processes within this
	 * {@link Office}.
	 * 
	 * @return {@link ProcessMetaData} for processes within this {@link Office}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * Obtains the {@link WorkMetaData} of the {@link Work} that may be done
	 * within this {@link Office}.
	 * 
	 * @return {@link WorkMetaData} instances of this {@link Office}.
	 */
	WorkMetaData<?>[] getWorkMetaData();

	/**
	 * Obtains the {@link OfficeStartupTask} instances for this {@link Office}.
	 * 
	 * @return {@link OfficeStartupTask} instances for this {@link Office}.
	 */
	OfficeStartupTask[] getStartupTasks();

	/**
	 * Creates a new {@link ProcessState} within the {@link Office} returning
	 * the starting {@link JobNode} to be executed.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link JobNode} for the
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link JobNode}.
	 * @param managedObject
	 *            {@link ManagedObject} that possibly invoked the new
	 *            {@link ProcessState}. This may be <code>null</code>.
	 * @param processMoIndex
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState}. Ignored if {@link ManagedObject} passed
	 *            in is <code>null</code>.
	 * @param managedObjectEscalationHandler
	 *            Potential {@link EscalationHandler} provided by the
	 *            {@link ManagedObject}. May be <code>null</code> to just use
	 *            the default {@link Office} {@link EscalationProcedure}.
	 *            Ignored if {@link ManagedObject} passed in is
	 *            <code>null</code>.
	 * @return {@link JobNode} to start processing the {@link ProcessState}.
	 */
	<W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter, ManagedObject managedObject, int processMoIndex,
			EscalationHandler managedObjectEscalationHandler);

	/**
	 * Creates a new {@link ProcessState} that is not triggered by a
	 * {@link ManagedObject}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the starting {@link JobNode} for the
	 *            {@link ProcessState}.
	 * @param parameter
	 *            Parameter to the starting {@link JobNode}.
	 * @return {@link JobNode} to start processing the {@link ProcessState}.
	 */
	<W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter);

}