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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.Job;

/**
 * <p>
 * Node within the graph of {@link JobNode} instances to execute.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link JobNode}
 * instances for a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobNode extends LinkedListSetEntry<JobNode, Flow> {

	/**
	 * Activates the {@link Job} for this {@link JobNode}.
	 */
	void activateJob();

	/**
	 * Indicates if this {@link JobNode} is completed.
	 * 
	 * @return <code>true</code> if this {@link JobNode} is completed.
	 */
	boolean isJobNodeComplete();

	/**
	 * Obtains the {@link Flow} containing this {@link JobNode}. The returned
	 * {@link Flow} provides access to the {@link ThreadState} and subsequent
	 * {@link ProcessState} that this {@link JobNode} is involved in.
	 * 
	 * @return {@link Flow} containing this {@link JobNode}.
	 */
	Flow getFlow();

	/**
	 * Obtains the {@link EscalationProcedure} for this {@link JobNode}.
	 * 
	 * @return {@link EscalationProcedure} for this {@link JobNode}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * <p>
	 * Specifies the parallel owner of this {@link JobNode}.
	 * <p>
	 * The input {@link JobNode} is executed once the current {@link Flow} that
	 * this {@link JobNode} is involved with is complete.
	 * 
	 * @param jobNode
	 *            Parallel owner of this {@link JobNode}.
	 */
	void setParallelOwner(JobNode jobNode);

	/**
	 * Obtains the parallel owner of this {@link JobNode}.
	 * 
	 * @return Parallel owner of this {@link JobNode}.
	 */
	JobNode getParallelOwner();

	/**
	 * Specifies the parallel {@link JobNode} to the current {@link JobNode}.
	 * The current {@link JobNode} will not complete until the input parallel
	 * {@link JobNode} is complete.
	 * 
	 * @param jobNode
	 *            Parallel {@link JobNode}.
	 */
	void setParallelNode(JobNode jobNode);

	/**
	 * Obtains the parallel {@link JobNode} to the current {@link JobNode}.
	 * 
	 * @return Parallel {@link JobNode} to the current {@link JobNode}.
	 */
	JobNode getParallelNode();

	/**
	 * Specifies the next {@link JobNode} in the {@link Flow} to execute after
	 * the current {@link JobNode} is completed.
	 * 
	 * @param jobNode
	 *            Next {@link JobNode}.
	 */
	void setNextNode(JobNode jobNode);

	/**
	 * Obtains the next {@link JobNode} in the {@link Flow} to execute after the
	 * current {@link JobNode} has completed.
	 * 
	 * @return Next {@link JobNode}.
	 */
	JobNode getNextNode();

	/**
	 * Clears the {@link JobNode} instances linked to this {@link JobNode}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 */
	void clearNodes(JobNodeActivateSet activateSet);

}