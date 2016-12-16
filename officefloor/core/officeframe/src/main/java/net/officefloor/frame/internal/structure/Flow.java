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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;

/**
 * Represents a sub-graph of the {@link JobNode} graph making up the
 * {@link ThreadState}. This enables the {@link FlowFuture} to know when a
 * parallel {@link Flow} is complete.
 * 
 * @author Daniel Sagenschneider
 */
public interface Flow extends FlowAsset, FlowFuture, LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a new managed {@link JobNode} contained in this {@link Flow} for
	 * the {@link Task}.
	 * 
	 * @param jobMetaData
	 *            {@link JobMetaData} for the new {@link JobNode}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link JobNode}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return New {@link JobNode}.
	 */
	JobNode createManagedJobNode(JobMetaData jobMetaData, JobNode parallelNodeOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Creates a new {@link JobNode} contained in this {@link Flow} for the
	 * {@link GovernanceActivity}.
	 * 
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @return New {@link JobNode}.
	 */
	JobNode createGovernanceNode(GovernanceActivity<?, ?> governanceActivity, JobNode parallelNodeOwner);

	/**
	 * Flags that the input {@link JobNode} has completed.
	 * 
	 * @param jobNode
	 *            {@link JobNode} that has completed.
	 * @return Optional {@link JobNode} to handle completion of the
	 *         {@link JobNode}.
	 */
	JobNode jobNodeComplete(JobNode jobNode);

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}