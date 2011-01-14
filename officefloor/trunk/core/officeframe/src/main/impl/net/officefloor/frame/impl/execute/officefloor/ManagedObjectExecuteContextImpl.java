/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.impl.execute.officefloor;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteContextImpl<F extends Enum<F>> implements
		ManagedObjectExecuteContext<F> {

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> managedObjectMetaData;

	/**
	 * Index of the {@link ManagedObject} within the {@link ProcessState}.
	 */
	private final int processMoIndex;

	/**
	 * {@link FlowMetaData} in index order for the {@link ManagedObjectSource}.
	 */
	private final FlowMetaData<?>[] processLinks;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link ProcessTicker}.
	 */
	private final ProcessTicker processTicker;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 * @param processMoIndex
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState}.
	 * @param processLinks
	 *            {@link FlowMetaData} in index order for the
	 *            {@link ManagedObjectSource}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData} to create {@link ProcessState}
	 *            instances.
	 * @param processTicker
	 *            {@link ProcessTicker}.
	 */
	public ManagedObjectExecuteContextImpl(
			ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData<?>[] processLinks, OfficeMetaData officeMetaData,
			ProcessTicker processTicker) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.officeMetaData = officeMetaData;
		this.processTicker = processTicker;
	}

	/*
	 * =============== ManagedObjectExecuteContext =============================
	 */

	@Override
	public void invokeProcess(F key, Object parameter,
			ManagedObject managedObject) {
		this.invokeProcess(key.ordinal(), parameter, managedObject, null);
	}

	@Override
	public void invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject) {
		this.invokeProcess(processIndex, parameter, managedObject, null);
	}

	@Override
	public void invokeProcess(F key, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler) {
		this.invokeProcess(key.ordinal(), parameter, managedObject,
				escalationHandler);
	}

	@Override
	public void invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler) {

		// Obtain the flow meta-data
		if ((processIndex < 0) || (processIndex >= this.processLinks.length)) {
			String validIndexes = (this.processLinks.length == 0 ? " [no processes linked]"
					: " [valid only 0 to " + (this.processLinks.length - 1)
							+ "]");
			throw new IllegalArgumentException("Invalid process index "
					+ processIndex + validIndexes);
		}
		FlowMetaData<?> flowMetaData = this.processLinks[processIndex];

		// Create the job in a new process
		JobNode jobNode = this.officeMetaData.createProcess(flowMetaData,
				parameter, managedObject, this.managedObjectMetaData,
				this.processMoIndex, escalationHandler);

		// Indicate process started and register to be notified of completion
		if (this.processTicker != null) {
			this.processTicker.processStarted();
			jobNode.getFlow().getThreadState().getProcessState()
					.registerProcessCompletionListener(this.processTicker);
		}

		// Activate the Job
		// Must register before activating job to have trigger on completion.
		jobNode.activateJob();
	}

}