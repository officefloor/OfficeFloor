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
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link WorkManager}.
 * 
 * @author Daniel
 */
public class WorkManagerImpl<W extends Work> implements WorkManager {

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

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
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param office
	 *            {@link OfficeImpl}.
	 */
	public WorkManagerImpl(String workName, WorkMetaData<W> workMetaData,
			OfficeImpl office) {
		this.workName = workName;
		this.workMetaData = workMetaData;
		this.office = office;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.manage.WorkManager#invokeWork(java.lang.Object)
	 */
	public FlowFuture invokeWork(Object parameter)
			throws NoInitialTaskException {

		// Obtain the Initial Flow meta-data
		FlowMetaData<W> flowMetaData = this.workMetaData
				.getInitialFlowMetaData();

		// Ensure there is an initial task for the work
		if (flowMetaData.getInitialTaskMetaData() == null) {
			throw new NoInitialTaskException("No initial task for work '"
					+ this.workName + "'");
		}

		// Create the job node within a new process
		JobNode jobNode = this.office.createProcess(flowMetaData, parameter,
				null, 0, null);

		// Assign the job node to the Team
		jobNode.activateJob();

		// Indicate when thread of work complete
		return jobNode.getThreadState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.manage.WorkManager#getManagedObject(java.lang
	 * .String)
	 */
	public ManagedObject getManagedObject(String managedObjectId)
			throws Exception {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

}
