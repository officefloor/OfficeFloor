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
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;

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
	public FunctionState unloadWork() {
		// Not the last job for the work, so do not unload
		return null;
	}

	/*
	 * ==================== WorkContainer delegate methods =================
	 */

	@Override
	public W getWork(ThreadState threadState) {
		return this.delegate.getWork(threadState);
	}

	@Override
	public FunctionState loadManagedObjects(ManagedObjectIndex[] managedObjectIndexes, ManagedFunction managedJobNode) {
		return this.delegate.loadManagedObjects(managedObjectIndexes, managedJobNode);
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(ManagedObjectIndex managedObjectIndex) {
		return this.delegate.getManagedObjectContainer(managedObjectIndex);
	}

	@Override
	public FunctionState administerManagedObjects(TaskDutyAssociation<?> duty, AdministratorContext adminContext)
			throws Throwable {
		return this.administerManagedObjects(duty, adminContext);
	}

	@Override
	public Object getObject(ManagedObjectIndex managedObjectIndex) {
		return this.getObject(managedObjectIndex);
	}

}