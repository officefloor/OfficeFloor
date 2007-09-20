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
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * <p>
 * Proxy for a
 * {@link net.officefloor.frame.internal.structure.ManagedObjectContainer}.
 * <p>
 * This is used to contain
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances bound
 * to the process.
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
	 *            {@link ManagedObjectContainer} within the {@link ProcessState}.
	 */
	public ManagedObjectContainerProxy(int index) {
		// Store state
		this.index = index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#loadManagedObject(net.officefloor.frame.internal.structure.ThreadState)
	 */
	public boolean loadManagedObject(ExecutionContext executionContext,
			TaskContainer taskContainer) {
		return taskContainer.getThreadState().getProcessState()
				.getManagedObjectContainer(this.index).loadManagedObject(
						executionContext, taskContainer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#coordinateManagedObject(net.officefloor.frame.internal.structure.WorkContainer,
	 *      net.officefloor.frame.spi.team.ExecutionContext,
	 *      net.officefloor.frame.spi.team.TaskContainer)
	 */
	public <W extends Work> void coordinateManagedObject(WorkContainer<W> workContainer,
			ExecutionContext executionContext, TaskContainer taskContainer) {
		taskContainer.getThreadState().getProcessState()
				.getManagedObjectContainer(this.index).coordinateManagedObject(
						workContainer, executionContext, taskContainer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#isManagedObjectReady(net.officefloor.frame.spi.team.ExecutionContext)
	 */
	public boolean isManagedObjectReady(ExecutionContext executionContext,
			TaskContainer taskContainer) {
		return taskContainer.getThreadState().getProcessState()
				.getManagedObjectContainer(this.index).isManagedObjectReady(
						executionContext, taskContainer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#getObject()
	 */
	public Object getObject(ThreadState threadState) {
		return threadState.getProcessState().getManagedObjectContainer(
				this.index).getObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#getManagedObject()
	 */
	public ManagedObject getManagedObject(ThreadState threadState) {
		return threadState.getProcessState().getManagedObjectContainer(
				this.index).getManagedObject(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#unloadManagedObject()
	 */
	public void unloadManagedObject() {
		// Leave to process to unload
	}

}
