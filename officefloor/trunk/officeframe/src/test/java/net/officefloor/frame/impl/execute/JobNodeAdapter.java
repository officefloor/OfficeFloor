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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;

/**
 * Provides default methods for the {@link JobNode}.
 * 
 * @author Daniel
 */
public class JobNodeAdapter implements JobNode {

	/**
	 * {@link Flow}.
	 */
	private final Flow flow;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} is always required.
	 */
	public JobNodeAdapter(Flow flow) {
		this.flow = flow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#activateJob()
	 */
	@Override
	public void activateJob() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#isJobNodeComplete()
	 */
	@Override
	public boolean isJobNodeComplete() {
		// Always running
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#clearNodes(net.officefloor
	 * .frame.internal.structure.JobActivateSet)
	 */
	@Override
	public void clearNodes(JobActivateSet notifySet) {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getFlow()
	 */
	@Override
	public Flow getFlow() {
		return this.flow;
	}

	/**
	 * Next {@link JobNode}.
	 */
	private JobNode nextNode = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setNextNode(net.officefloor
	 * .frame.internal.structure.JobNode)
	 */
	@Override
	public void setNextNode(JobNode jobNode) {
		this.nextNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getNextNode()
	 */
	@Override
	public JobNode getNextNode() {
		return this.nextNode;
	}

	/**
	 * Parallel {@link JobNode}.
	 */
	private JobNode parallelNode = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setParallelNode(net.
	 * officefloor.frame.internal.structure.JobNode)
	 */
	@Override
	public void setParallelNode(JobNode jobNode) {
		this.parallelNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getParallelNode()
	 */
	@Override
	public JobNode getParallelNode() {
		return this.parallelNode;
	}

	/**
	 * Parallel owner.
	 */
	private JobNode parallelOwner = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setParallelOwner(net
	 * .officefloor.frame.internal.structure.JobNode)
	 */
	@Override
	public void setParallelOwner(JobNode jobNode) {
		this.parallelOwner = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getParallelOwner()
	 */
	@Override
	public JobNode getParallelOwner() {
		return this.parallelOwner;
	}

}
