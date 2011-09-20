/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
	public void loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet) {
		this.delegate.loadManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet);
	}

	@Override
	public boolean governManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet) {
		return this.delegate.governManagedObjects(managedObjectIndexes,
				jobContext, jobNode, activateSet);
	}

	@Override
	public boolean coordinateManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet) {
		return this.delegate.coordinateManagedObjects(managedObjectIndexes,
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