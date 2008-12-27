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
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Proxy for the {@link WorkContainer}.
 * 
 * @author Daniel
 */
public class WorkContainerProxy<W extends Work> implements WorkContainer<W> {

	/**
	 * Delegate {@link WorkContainer}.
	 */
	private final WorkContainer<W> delegate;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link WorkContainer}.
	 */
	public WorkContainerProxy(WorkContainer<W> delegate) {
		this.delegate = delegate;
	}

	/*
	 * ========================== WorkContainer ============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkContainer#getWorkId()
	 */
	@Override
	public int getWorkId() {
		return this.delegate.getWorkId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#getWork(net.
	 * officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public W getWork(ThreadState threadState) {
		return this.delegate.getWork(threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#isManagedObjectsReady
	 * (int[], net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean isManagedObjectsReady(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {
		return this.delegate.isManagedObjectsReady(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#loadManagedObjects
	 * (int[], net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean loadManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {
		return this.delegate.loadManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#
	 * coordinateManagedObjects(int[],
	 * net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public void coordinateManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {
		this.delegate.coordinateManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.WorkContainer#
	 * administerManagedObjects
	 * (net.officefloor.frame.internal.structure.TaskDutyAssociation,
	 * net.officefloor.frame.internal.structure.AdministratorContext)
	 */
	@Override
	public void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext) throws Exception {
		this.delegate.administerManagedObjects(duty, adminContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.WorkContainer#getObject(int,
	 * net.officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public Object getObject(int moIndex, ThreadState threadState) {
		return this.delegate.getObject(moIndex, threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkContainer#unloadWork()
	 */
	@Override
	public void unloadWork() {
		// Not the last flow item for the work, so do not unload
	}

}
