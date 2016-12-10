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
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * <p>
 * Represents a sequence of {@link Job} instances that are completed one after
 * another.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link JobSequence}
 * instances for a {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobSequence extends FlowAsset, FlowFuture,
		LinkedListSetEntry<JobSequence, ThreadState> {

	/**
	 * Creates a new {@link JobNode} contained in this {@link JobSequence} for
	 * the {@link Task}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link JobNode}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link JobNode}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return New {@link JobNode}.
	 */
	JobNode createTaskNode(TaskMetaData<?, ?, ?> taskMetaData,
			JobNode parallelNodeOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy);

	/**
	 * Creates a new {@link JobNode} contained in this {@link JobSequence} for
	 * the {@link GovernanceActivity}.
	 * 
	 * @param governanceActivity
	 *            {@link GovernanceActivity}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @return New {@link JobNode}.
	 */
	JobNode createGovernanceNode(GovernanceActivity<?, ?> governanceActivity,
			JobNode parallelNodeOwner);

	/**
	 * Flags that the input {@link JobNode} has completed.
	 * 
	 * @param jobNode
	 *            {@link JobNode} that has completed.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances
	 *            waiting on this {@link JobSequence} if all {@link JobNode}
	 *            instances of this {@link JobSequence} are complete.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} completing
	 *            the {@link JobNode}.
	 */
	void jobNodeComplete(JobNode jobNode, JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam);

	/**
	 * Obtains the {@link ThreadState} containing this {@link JobSequence}.
	 * 
	 * @return {@link ThreadState} containing this {@link JobSequence}.
	 */
	ThreadState getThreadState();

}