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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * <p>
 * Proxy for a {@link ManagedObjectContainer}.
 * <p>
 * This is used to contain {@link ManagedObject} instances bound to the process.
 * 
 * @author Daniel
 */
public class ManagedObjectContainerProxy implements ManagedObjectContainer {

	/**
	 * Index of the {@link ManagedObject} for this
	 * {@link ManagedObjectContainer} within the {@link ProcessState}.
	 */
	protected final int index;

	/**
	 * Initiate.
	 * 
	 * @param index
	 *            Index of the {@link ManagedObject} for this
	 *            {@link ManagedObjectContainer} within the {@link ProcessState}
	 *            .
	 */
	public ManagedObjectContainerProxy(int index) {
		// Store state
		this.index = index;
	}

	/*
	 * ================= ManagedObjectContainer ==============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * loadManagedObject(net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean loadManagedObject(JobContext executionContext,
			JobNode jobNode, JobActivateSet notifySet) {
		return jobNode.getThreadState().getProcessState()
				.getManagedObjectContainer(this.index).loadManagedObject(
						executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * coordinateManagedObject
	 * (net.officefloor.frame.internal.structure.WorkContainer,
	 * net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public <W extends Work> void coordinateManagedObject(
			WorkContainer<W> workContainer, JobContext executionContext,
			JobNode jobNode, JobActivateSet notifySet) {
		jobNode.getThreadState().getProcessState().getManagedObjectContainer(
				this.index).coordinateManagedObject(workContainer,
				executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * isManagedObjectReady(net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean isManagedObjectReady(JobContext executionContext,
			JobNode jobNode, JobActivateSet notifySet) {
		return jobNode.getThreadState().getProcessState()
				.getManagedObjectContainer(this.index).isManagedObjectReady(
						executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ManagedObjectContainer#getObject
	 * ()
	 */
	@Override
	public Object getObject(ThreadState threadState) {
		return threadState.getProcessState().getManagedObjectContainer(
				this.index).getObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * getManagedObject()
	 */
	@Override
	public ManagedObject getManagedObject(ThreadState threadState) {
		return threadState.getProcessState().getManagedObjectContainer(
				this.index).getManagedObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * unloadManagedObject()
	 */
	@Override
	public void unloadManagedObject() {
		// Leave to process to unload
	}

}
