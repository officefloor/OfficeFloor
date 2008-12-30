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

import net.officefloor.frame.spi.team.Job;

/**
 * Node within the tree of {@link JobNode} instances to execute.
 * 
 * @author Daniel
 */
public interface JobNode {

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
	 * Obtains the {@link Flow} that this {@link JobNode} is bound. The returned
	 * {@link Flow} provide access to the {@link ThreadState} and subsequent
	 * {@link ProcessState} that this {@link JobNode} is involved in.
	 * 
	 * @return {@link Flow} that this {@link JobNode} is bound.
	 */
	Flow getFlow();

	/**
	 * <p>
	 * Specifies the parallel owner of this {@link JobNode}.
	 * <p>
	 * The input {@link JobNode} is executed once the current {@link Flow} that
	 * this {@link JobNode} is involved with is complete.
	 * 
	 * @param taskNode
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
	 * @param taskNode
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
	 * @param taskNode
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
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 */
	void clearNodes(JobActivateSet notifySet);

}
