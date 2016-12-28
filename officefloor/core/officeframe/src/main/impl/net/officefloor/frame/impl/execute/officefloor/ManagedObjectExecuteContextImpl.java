/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.util.TimerTask;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectExecuteContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteContextImpl<F extends Enum<F>> implements ManagedObjectExecuteContext<F> {

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
	private final FlowMetaData[] processLinks;

	/**
	 * {@link OfficeMetaData} to create {@link ProcessState} instances.
	 */
	private final OfficeMetaData officeMetaData;

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
	 */
	public ManagedObjectExecuteContextImpl(ManagedObjectMetaData<?> managedObjectMetaData, int processMoIndex,
			FlowMetaData[] processLinks, OfficeMetaData officeMetaData) {
		this.managedObjectMetaData = managedObjectMetaData;
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * =============== ManagedObjectExecuteContext =============================
	 */

	@Override
	public void invokeProcess(F key, Object parameter, ManagedObject managedObject, long delay, FlowCallback callback) {
		this.invokeProcess(key.ordinal(), parameter, managedObject, delay, callback);
	}

	@Override
	public void invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) {

		// Obtain the flow meta-data
		if ((flowIndex < 0) || (flowIndex >= this.processLinks.length)) {
			String validIndexes = (this.processLinks.length == 0 ? " [no processes linked]"
					: " [valid only 0 to " + (this.processLinks.length - 1) + "]");
			throw new IllegalArgumentException("Invalid process index " + flowIndex + validIndexes);
		}
		FlowMetaData flowMetaData = this.processLinks[flowIndex];

		// Create the function in a new process
		final FunctionState function = this.officeMetaData.createProcess(flowMetaData, parameter, callback, null,
				managedObject, this.managedObjectMetaData, this.processMoIndex);

		// Trigger the process
		if (delay > 0) {
			// Delay execution of the process
			ManagedObjectExecuteContextImpl.this.officeMetaData.getOfficeTimer().schedule(new TimerTask() {
				@Override
				public void run() {
					// Must delegate to not hold up timer thread
					ManagedObjectExecuteContextImpl.this.officeMetaData.getFunctionLoop().delegateFunction(function);
				}
			}, delay);

		} else {
			// Execute the process immediately on current thread
			ManagedObjectExecuteContextImpl.this.officeMetaData.getFunctionLoop().executeFunction(function);
		}
	}

}