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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Provides default methods for the {@link JobNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class JobNodeAdapter extends
		AbstractLinkedListSetEntry<JobNode, JobSequence> implements JobNode {

	/**
	 * {@link JobSequence}.
	 */
	private final JobSequence flow;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link JobSequence} is always required.
	 */
	public JobNodeAdapter(JobSequence flow) {
		this.flow = flow;
	}

	/*
	 * ==================== LinkedListSetEntry =========================
	 */

	@Override
	public JobSequence getLinkedListSetOwner() {
		return this.flow;
	}

	/*
	 * ==================== JobNode =====================================
	 */

	@Override
	public void activateJob(TeamIdentifier currentTeam) {
		// Do nothing
	}

	@Override
	public boolean isJobNodeComplete() {
		// Always running
		return false;
	}

	@Override
	public void clearNodes(JobNodeActivateSet notifySet,
			TeamIdentifier currentTeam) {
		// Do nothing
	}

	@Override
	public JobSequence getJobSequence() {
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