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
package net.officefloor.frame.impl;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Implementation of the {@link net.officefloor.frame.api.manage.WorkManager}.
 * 
 * @author Daniel
 */
public class WorkManagerImpl<W extends Work> implements WorkManager {

	/**
	 * {@link WorkMetaData}.
	 */
	protected final WorkMetaData<W> workMetaData;

	/**
	 * {@link OfficeImpl} where this {@link Work} is being executed.
	 */
	protected final OfficeImpl office;

	/**
	 * Initiate.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param office
	 *            {@link OfficeImpl}.
	 */
	public WorkManagerImpl(WorkMetaData<W> workMetaData, OfficeImpl office) {
		// Store state
		this.workMetaData = workMetaData;
		this.office = office;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.WorkManager#invokeWork(java.lang.Object)
	 */
	public FlowFuture invokeWork(Object parameter) {

		// Obtain the Initial Flow meta-data
		FlowMetaData<W> flowMetaData = this.workMetaData
				.getInitialFlowMetaData();

		// Create the task within a new process
		Job taskContainer = this.office.createProcess(flowMetaData,
				parameter, null, 0);

		// Assign the Task to the Team
		taskContainer.activateJob();

		// Indicate when complete
		return taskContainer.getThreadState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.manage.WorkManager#getManagedObject(java.lang.String)
	 */
	public ManagedObject getManagedObject(String managedObjectId)
			throws Exception {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

}
