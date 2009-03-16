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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.internal.structure.EscalationProcedure;
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
	 * ==================== JobNode =====================================
	 */

	@Override
	public void activateJob() {
		// Do nothing
	}

	@Override
	public boolean isJobNodeComplete() {
		// Always running
		return false;
	}

	@Override
	public void clearNodes(JobActivateSet notifySet) {
		// Do nothing
	}

	@Override
	public Flow getFlow() {
		return this.flow;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return new EscalationProcedureImpl();
	}

	/**
	 * Next {@link JobNode}.
	 */
	private JobNode nextNode = null;

	@Override
	public void setNextNode(JobNode jobNode) {
		this.nextNode = jobNode;
	}

	@Override
	public JobNode getNextNode() {
		return this.nextNode;
	}

	/**
	 * Parallel {@link JobNode}.
	 */
	private JobNode parallelNode = null;

	@Override
	public void setParallelNode(JobNode jobNode) {
		this.parallelNode = jobNode;
	}

	@Override
	public JobNode getParallelNode() {
		return this.parallelNode;
	}

	/**
	 * Parallel owner.
	 */
	private JobNode parallelOwner = null;

	@Override
	public void setParallelOwner(JobNode jobNode) {
		this.parallelOwner = jobNode;
	}

	@Override
	public JobNode getParallelOwner() {
		return this.parallelOwner;
	}

}
