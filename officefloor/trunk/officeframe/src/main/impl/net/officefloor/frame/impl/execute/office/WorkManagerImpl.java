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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link WorkManager}.
 * 
 * @author Daniel
 */
public class WorkManagerImpl implements WorkManager {

	/**
	 * {@link WorkMetaData}.
	 */
	private final WorkMetaData<?> workMetaData;

	/**
	 * {@link OfficeMetaData} of the {@link Office} where this {@link Work} is
	 * being executed.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Initiate.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public WorkManagerImpl(WorkMetaData<?> workMetaData,
			OfficeMetaData officeMetaData) {
		this.workMetaData = workMetaData;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * =============== WorkManager ================================
	 */

	@Override
	public FlowFuture invokeWork(Object parameter)
			throws NoInitialTaskException {

		// Obtain the Initial Flow meta-data
		FlowMetaData<?> flowMetaData = this.workMetaData
				.getInitialFlowMetaData();

		// Ensure there is an initial task for the work
		if (flowMetaData == null) {
			throw new NoInitialTaskException("No initial task for work '"
					+ this.workMetaData.getWorkName() + "'");
		}

		// Create the job node within a new process
		JobNode jobNode = this.officeMetaData.createProcess(flowMetaData,
				parameter);

		// Assign the job node to the Team
		jobNode.activateJob();

		// Indicate when process of work complete
		return jobNode.getFlow().getThreadState().getProcessState();
	}

	@Override
	public ManagedObject getManagedObject(String managedObjectName)
			throws Throwable {
		// TODO implement obtaining managed object externally from WorkManager
		throw new UnsupportedOperationException("TODO implement");
	}

}