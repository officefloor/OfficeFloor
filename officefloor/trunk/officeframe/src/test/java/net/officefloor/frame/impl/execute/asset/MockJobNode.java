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

import junit.framework.TestCase;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Mock {@link JobNode}.
 * 
 * @author Daniel
 */
class MockJobNode implements JobNode {

	/**
	 * {@link ThreadState}.
	 */
	protected final ThreadState threadState;

	/**
	 * Indicates if this {@link Job} was activated.
	 */
	protected boolean isActive = false;

	/**
	 * Initiate.
	 */
	public MockJobNode(OfficeFrameTestCase testCase) {
		this.threadState = testCase.createMock(ThreadState.class);
	}

	/**
	 * Indicates if this {@link Job} was activated.
	 * 
	 * @return <code>true</code> if this {@link Job} was activated.
	 */
	public boolean isActivated() {
		return this.isActive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#activeTask()
	 */
	@Override
	public void activateJob() {
		this.isActive = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#isJobNodeComplete()
	 */
	@Override
	public boolean isJobNodeComplete() {
		TestCase.fail("Should not be invoked");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getThreadState()
	 */
	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getNextNode()
	 */
	@Override
	public JobNode getNextNode() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getParallelNode()
	 */
	@Override
	public JobNode getParallelNode() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getParallelOwner()
	 */
	@Override
	public JobNode getParallelOwner() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setNextNode(net.officefloor
	 * .frame.internal.structure.JobNode)
	 */
	@Override
	public void setNextNode(JobNode taskNode) {
		TestCase.fail("Should not be invoked");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setParallelNode(net.
	 * officefloor.frame.internal.structure.JobNode)
	 */
	@Override
	public void setParallelNode(JobNode taskNode) {
		TestCase.fail("Should not be invoked");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.JobNode#setParallelOwner(net
	 * .officefloor.frame.internal.structure.JobNode)
	 */
	@Override
	public void setParallelOwner(JobNode taskNode) {
		TestCase.fail("Should not be invoked");
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
		TestCase.fail("Should not be invoked");
	}

}