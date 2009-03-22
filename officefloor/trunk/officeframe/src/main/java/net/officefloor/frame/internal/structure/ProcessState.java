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
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * State of a process within the {@link Office}.
 * 
 * @author Daniel
 */
public interface ProcessState extends FlowFuture {

	/**
	 * Obtains the lock for the process.
	 * 
	 * @return Lock of the process.
	 */
	Object getProcessLock();

	/**
	 * Obtains the {@link ProcessMetaData} for this {@link ProcessState}.
	 * 
	 * @return {@link ProcessMetaData} for this {@link ProcessState}.
	 */
	ProcessMetaData getProcessMetaData();

	/**
	 * <p>
	 * Creates a {@link Flow} for the new {@link ThreadState} bound to this
	 * {@link ProcessState}.
	 * <p>
	 * The new {@link ThreadState} is available from the returned {@link Flow}.
	 * 
	 * @return {@link Flow} for the new {@link ThreadState} bound to this
	 *         {@link ProcessState}.
	 */
	<W extends Work> Flow createThread(FlowMetaData<W> flowMetaData);

	/**
	 * Flags that the input {@link ThreadState} has complete.
	 * 
	 * @param thread
	 *            {@link ThreadState} that has completed.
	 */
	void threadComplete(ThreadState thread);

	/**
	 * Obtains the {@link ManagedObjectContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link ManagedObjectContainer} to be returned.
	 * @return {@link ManagedObjectContainer} for the index.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * Obtains the {@link AdministratorContainer} for the input index.
	 * 
	 * @param index
	 *            Index of the {@link AdministratorContainer} to be returned.
	 * @return {@link AdministratorContainer} for the index.
	 */
	AdministratorContainer<?, ?> getAdministratorContainer(int index);

	/**
	 * Obtains the {@link Escalation} for the {@link EscalationHandler} provided
	 * by the {@link ManagedObjectSource}.
	 * 
	 * @return {@link Escalation} or <code>null</code> if
	 *         {@link ManagedObjectSource} did not provide a
	 *         {@link EscalationHandler} or this {@link ProcessState} was not
	 *         invoked by a {@link ManagedObjectSource}.
	 */
	Escalation getManagedObjectSourceEscalation();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Office}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link Office}.
	 */
	EscalationProcedure getOfficeEscalationProcedure();

	/**
	 * Obtains the catch all {@link Escalation} for the {@link OfficeFloor}.
	 * 
	 * @return Catch all {@link Escalation} for the {@link OfficeFloor}.
	 */
	Escalation getOfficeFloorEscalation();

	/**
	 * Registers a {@link ProcessCompletionListener} with this
	 * {@link ProcessState}.
	 * 
	 * @param listener
	 *            {@link ProcessCompletionListener}.
	 */
	void registerProcessCompletionListener(ProcessCompletionListener listener);

}