/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Proxy for the {@link WorkContainer}.
 * 
 * @author Daniel Sagenschneider
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

	@Override
	public W getWork(ThreadState threadState) {
		return this.delegate.getWork(threadState);
	}

	@Override
	public boolean isManagedObjectsReady(
			ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet) {
		return this.delegate.isManagedObjectsReady(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	@Override
	public boolean loadManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet) {
		return this.delegate.loadManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	@Override
	public void coordinateManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet) {
		this.delegate.coordinateManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	@Override
	public void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext) throws Throwable {
		this.delegate.administerManagedObjects(duty, adminContext);
	}

	@Override
	public Object getObject(ManagedObjectIndex moIndex, ThreadState threadState) {
		return this.delegate.getObject(moIndex, threadState);
	}

	@Override
	public void unloadWork(JobNodeActivateSet activateSet) {
		// Not the last job for the work, so do not unload
	}

}