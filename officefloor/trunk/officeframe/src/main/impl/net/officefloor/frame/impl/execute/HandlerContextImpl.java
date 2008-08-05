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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.execute.EscalationHandler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.impl.OfficeImpl;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Implementation of the {@link HandlerContext}.
 * 
 * @author Daniel
 */
public class HandlerContextImpl<F extends Enum<F>> implements HandlerContext<F> {

	/**
	 * Index of the {@link ManagedObject} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	protected final int processMoIndex;

	/**
	 * List of initial {@link FlowMetaData} of the process links for the
	 * {@link net.officefloor.frame.api.execute.Handler}.
	 */
	protected final FlowMetaData<?>[] processLinks;

	/**
	 * {@link OfficeImpl} to create
	 * {@link net.officefloor.frame.internal.structure.ProcessState} instances.
	 */
	protected final OfficeImpl office;

	/**
	 * Initiate.
	 * 
	 * @param processMoIndex
	 *            Index of the {@link ManagedObject} using the
	 *            {@link net.officefloor.frame.api.execute.Handler} within the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * @param processLinks
	 *            List of {@link FlowMetaData} of the process links for the
	 *            {@link net.officefloor.frame.api.execute.Handler}.
	 * @param office
	 *            {@link OfficeImpl}.
	 */
	public HandlerContextImpl(int processMoIndex,
			FlowMetaData<?>[] processLinks, OfficeImpl office) {
		this.processMoIndex = processMoIndex;
		this.processLinks = processLinks;
		this.office = office;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.HandlerContext#invokeProcess(F,
	 *      java.lang.Object,
	 *      net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void invokeProcess(F key, Object parameter,
			ManagedObject managedObject) {
		this.invokeProcess(key.ordinal(), parameter, managedObject, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.HandlerContext#invokeProcess(int,
	 *      java.lang.Object,
	 *      net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject) {
		this.invokeProcess(processIndex, parameter, managedObject, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.HandlerContext#invokeProcess(java.lang.Enum,
	 *      java.lang.Object,
	 *      net.officefloor.frame.spi.managedobject.ManagedObject,
	 *      net.officefloor.frame.api.execute.EscalationHandler)
	 */
	@Override
	public void invokeProcess(F key, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler) {
		this.invokeProcess(key.ordinal(), parameter, managedObject,
				escalationHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.HandlerContext#invokeProcess(int,
	 *      java.lang.Object,
	 *      net.officefloor.frame.spi.managedobject.ManagedObject,
	 *      net.officefloor.frame.api.execute.EscalationHandler)
	 */
	@Override
	public void invokeProcess(int processIndex, Object parameter,
			ManagedObject managedObject, EscalationHandler escalationHandler) {

		// Obtain the flow meta-data
		FlowMetaData<?> flowMetaData = this.processLinks[processIndex];

		// Create the task in a new process
		Job task = this.office.createProcess(flowMetaData, parameter,
				managedObject, this.processMoIndex, escalationHandler);

		// Activate the Task
		task.activateJob();
	}

}
