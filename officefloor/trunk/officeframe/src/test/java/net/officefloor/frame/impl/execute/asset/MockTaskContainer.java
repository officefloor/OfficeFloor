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

import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.util.OfficeFrameTestCase;

/**
 * Mock {@link net.officefloor.frame.spi.team.TaskContainer}.
 * 
 * @author Daniel
 */
class MockTaskContainer implements TaskContainer {

	/**
	 * {@link ThreadState}.
	 */
	protected final ThreadState threadState;

	/**
	 * Indicates if this {@link TaskContainer} was activated.
	 */
	protected boolean isActive = false;

	/**
	 * Initiate.
	 */
	public MockTaskContainer(OfficeFrameTestCase testCase) {
		this.threadState = testCase.createMock(ThreadState.class);
	}

	/**
	 * Indicates if this {@link TaskContainer} was activated.
	 * 
	 * @return <code>true</code> if this {@link TaskContainer} was actived.
	 */
	public boolean isActivated() {
		return this.isActive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#activeTask()
	 */
	public void activateTask() {
		this.isActive = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#doTask(net.officefloor.frame.spi.team.ExecutionContext)
	 */
	public boolean doTask(ExecutionContext executionContext) {
		throw new IllegalStateException("Should not be executing tasks");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/**
	 * Next {@link TaskContainer}.
	 */
	protected TaskContainer nextTaskContainer = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#setNextTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void setNextTask(TaskContainer task) {
		this.nextTaskContainer = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getNextTask()
	 */
	public TaskContainer getNextTask() {
		return this.nextTaskContainer;
	}

}