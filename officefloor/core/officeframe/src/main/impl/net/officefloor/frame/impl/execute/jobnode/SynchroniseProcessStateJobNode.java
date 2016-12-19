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
package net.officefloor.frame.impl.execute.jobnode;

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link JobNode} to synchronise the {@link ProcessState} with the current
 * {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchroniseProcessStateJobNode implements JobNode {

	/**
	 * Current {@link ThreadState}.
	 */
	private ThreadState currentThreadState;

	/**
	 * Instantiate.
	 * 
	 * @param currentThreadState
	 *            Current {@link ThreadState} to have the {@link ProcessState}
	 *            synchronised into it.
	 */
	public SynchroniseProcessStateJobNode(ThreadState currentThreadState) {
		this.currentThreadState = currentThreadState;
	}

	/*
	 * ======================== JobNode =================================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.currentThreadState;
	}

	@Override
	public boolean isRequireThreadStateSafety() {
		// Ensure have thread state safety, to synchronise process state
		return true;
	}

	@Override
	public JobNode doJob() {

		// Synchronise process state (always undertaken via main thread state)
		synchronized (this.currentThreadState.getProcessState().getMainThreadState()) {
		}

		// Synchronized
		return null;
	}

}