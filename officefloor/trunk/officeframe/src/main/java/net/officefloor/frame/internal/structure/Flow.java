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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.spi.team.Job;

/**
 * <p>
 * Represents a sequence of {@link Job} instances that are completed one after
 * another.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link Flow}
 * instances for a {@link ThreadState}.
 * 
 * @author Daniel
 */
public interface Flow extends FlowAsset, FlowFuture,
		LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a new {@link JobNode} contained in this {@link Flow}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link JobNode}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link JobNode}.
	 * @return New {@link JobNode}.
	 */
	JobNode createJobNode(TaskMetaData<?, ?, ?> taskMetaData,
			JobNode parallelNodeOwner, Object parameter);

	/**
	 * Flags that the input {@link JobNode} has completed.
	 * 
	 * @param jobNode
	 *            {@link JobNode} that has completed.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances
	 *            waiting on this {@link Flow} if all {@link JobNode} instances
	 *            of this {@link Flow} are complete.
	 */
	void jobNodeComplete(JobNode jobNode, JobNodeActivateSet activateSet);

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}