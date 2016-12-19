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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.Task;

/**
 * Represents a sub-graph of the {@link ManagedJobNode} graph making up the
 * {@link ThreadState}. This enables knowing when to undertake the
 * {@link FlowCallback} on completion of all {@link ManagedJobNode} instances of
 * the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Flow extends LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a new managed {@link ManagedJobNode} contained in this
	 * {@link Flow} for the {@link Task}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link ManagedJobNode}.
	 * @param parallelNodeOwner
	 *            {@link ManagedJobNode} that is the parallel owner of the new
	 *            {@link ManagedJobNode}.
	 * @param parameter
	 *            Parameter for the {@link ManagedJobNode}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return New {@link ManagedJobNode}.
	 */
	ManagedJobNode createManagedJobNode(TaskMetaData<?, ?, ?> taskMetaData, ManagedJobNode parallelNodeOwner,
			Object parameter, GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Creates a new {@link ManagedJobNode} contained in this {@link Flow} for
	 * the {@link GovernanceActivity}.
	 * 
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelNodeOwner
	 *            {@link ManagedJobNode} that is the parallel owner of the new
	 *            {@link ManagedJobNode}.
	 * @return New {@link ManagedJobNode}.
	 */
	ManagedJobNode createGovernanceNode(GovernanceActivity<?, ?> governanceActivity, ManagedJobNode parallelNodeOwner);

	/**
	 * Flags that the input {@link ManagedJobNode} has completed.
	 * 
	 * @param jobNode
	 *            {@link ManagedJobNode} that has completed.
	 * @return Optional {@link JobNode} to handle completion of the
	 *         {@link ManagedJobNode}.
	 */
	JobNode managedJobNodeComplete(ManagedJobNode jobNode);

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}