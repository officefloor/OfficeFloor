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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.OfficeManager;
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
 * @author Daniel Sagenschneider
 */
public class OfficeMetaDataImpl implements OfficeMetaData {

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager;

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
	 * {@link EscalationProcedure} for the {@link Office}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * {@link OfficeFloor} {@link EscalationFlow}.
	 */
	private final EscalationFlow officeFloorEscalation;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param officeManager
	 *            {@link OfficeManager}.
	 * @param workMetaDatas
	 *            {@link WorkMetaData} of the {@link Work} that can be done
	 *            within the {@link Office}.
	 * @param processMetaData
	 *            {@link ProcessMetaData} of the {@link ProcessState} instances
	 *            created within this {@link Office}.
	 * @param startupTasks
	 *            {@link OfficeStartupTask} instances.
	 * @param escalationProcedure
	 *            {@link EscalationProcedure} for the {@link Office}.
	 * @param officeFloorEscalation
	 *            {@link OfficeFloor} {@link EscalationFlow}.
	 */
	public OfficeMetaDataImpl(String officeName, OfficeManager officeManager,
			WorkMetaData<?>[] workMetaDatas, ProcessMetaData processMetaData,
			OfficeStartupTask[] startupTasks,
			EscalationProcedure escalationProcedure,
			EscalationFlow officeFloorEscalation) {
		this.officeName = officeName;
		this.officeManager = officeManager;
		this.workMetaDatas = workMetaDatas;
		this.processMetaData = processMetaData;
		this.startupTasks = startupTasks;
		this.escalationProcedure = escalationProcedure;
		this.officeFloorEscalation = officeFloorEscalation;
	}

	/*
	 * ==================== OfficeMetaData ==============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public OfficeManager getOfficeManager() {
		return this.officeManager;
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
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
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
				this, managedObjectEscalationHandler,
				this.officeFloorEscalation);

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
		TaskMetaData<W, ?, ?> taskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create the Job Node for the initial job
		JobNode jobNode = flow.createJobNode(taskMetaData, null, parameter);

		// Return the Job Node
		return jobNode;
	}

}