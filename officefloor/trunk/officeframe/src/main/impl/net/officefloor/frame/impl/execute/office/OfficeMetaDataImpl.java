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

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupTask;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link OfficeMetaData} implementation.
 * 
 * @author Daniel
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link WorkMetaData} of the {@link Work} that can be done within the
	 * {@link Office}.
	 */
	private final WorkMetaData<?>[] workMetaDatas;

	/**
	 * {@link ProcessMetaData} of the {@link ProcessState} instances created
	 * within this {@link Office}.
	 */
	private final ProcessMetaData processMetaData;

	/**
	 * {@link OfficeStartupTask} instances.
	 */
	private final OfficeStartupTask[] startupTasks;

	/**
	 * Catch all {@link EscalationHandler} for this {@link Office}. May be
	 * <code>null</code>.
	 */
	private final EscalationHandler officeEscalationHandler;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param workMetaDatas
	 *            {@link WorkMetaData} of the {@link Work} that can be done
	 *            within the {@link Office}.
	 * @param processMetaData
	 *            {@link ProcessMetaData} of the {@link ProcessState} instances
	 *            created within this {@link Office}.
	 * @param startupTasks
	 *            {@link OfficeStartupTask} instances.
	 * @param officeEscalationHandler
	 *            Catch all {@link EscalationHandler} for this {@link Office}.
	 *            May be <code>null</code>.
	 */
	public OfficeMetaDataImpl(String officeName,
			WorkMetaData<?>[] workMetaDatas, ProcessMetaData processMetaData,
			OfficeStartupTask[] startupTasks,
			EscalationHandler officeEscalationHandler) {
		this.officeName = officeName;
		this.workMetaDatas = workMetaDatas;
		this.processMetaData = processMetaData;
		this.startupTasks = startupTasks;
		this.officeEscalationHandler = officeEscalationHandler;
	}

	/*
	 * ==================== OfficeMetaData ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public ProcessMetaData getProcessMetaData() {
		return this.processMetaData;
	}

	@Override
	public WorkMetaData<?>[] getWorkMetaData() {
		return this.workMetaDatas;
	}

	@Override
	public OfficeStartupTask[] getStartupTasks() {
		return this.startupTasks;
	}

	@Override
	public <W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter) {
		return this.createProcess(flowMetaData, parameter, null, -1, null);
	}

	@Override
	public <W extends Work> JobNode createProcess(FlowMetaData<W> flowMetaData,
			Object parameter, ManagedObject managedObject, int processMoIndex,
			EscalationHandler managedObjectEscalationHandler) {

		// Create the Process State
		ProcessState processState = new ProcessStateImpl(this.processMetaData,
				managedObjectEscalationHandler, this.officeEscalationHandler);

		// Determine if require loading the managed object
		if (managedObject != null) {
			// Obtain the container for the managed object
			ManagedObjectContainerImpl moc = (ManagedObjectContainerImpl) processState
					.getManagedObjectContainer(processMoIndex);

			// Load the managed object
			moc.loadManagedObject(managedObject);
		}

		// Create the Flow
		Flow flow = processState.createThread(flowMetaData);

		// Obtain the task meta-data
		TaskMetaData<?, W, ?, ?> taskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create the Job Node for the initial job
		JobNode jobNode = flow.createJobNode(taskMetaData, null, parameter);

		// Return the Job Node
		return jobNode;
	}

}