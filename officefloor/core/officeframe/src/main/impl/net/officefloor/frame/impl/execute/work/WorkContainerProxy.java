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
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.TeamIdentifier;

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
			JobNodeActivateSet notifySet, ContainerContext context) {
		return this.delegate.isManagedObjectsReady(managedObjectIndexes,
				executionContext, jobNode, notifySet, context);
	}

	@Override
	public void loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet, TeamIdentifier currentTeam,
			ContainerContext context) {
		this.delegate.loadManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet, currentTeam, context);
	}

	@Override
	public void governManagedObjects(ManagedObjectIndex[] managedObjectIndexes,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context) {
		this.delegate.governManagedObjects(managedObjectIndexes, jobContext,
				jobNode, activateSet, context);
	}

	@Override
	public void coordinateManagedObjects(
			ManagedObjectIndex[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet notifySet, ContainerContext context) {
		this.delegate.coordinateManagedObjects(managedObjectIndexes,
				executionContext, jobNode, notifySet, context);
	}

	@Override
	public void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext, ContainerContext containerContext)
			throws Throwable {
		this.delegate.administerManagedObjects(duty, adminContext,
				containerContext);
	}

	@Override
	public Object getObject(ManagedObjectIndex moIndex, ThreadState threadState) {
		return this.delegate.getObject(moIndex, threadState);
	}

	@Override
	public void unloadWork(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {
		// Not the last job for the work, so do not unload
	}

}