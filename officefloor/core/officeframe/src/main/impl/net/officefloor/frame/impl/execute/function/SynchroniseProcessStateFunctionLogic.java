/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionState} to synchronise the {@link ProcessState} with the
 * current {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchroniseProcessStateFunctionLogic implements FunctionLogic {

	/**
	 * Current {@link ThreadState}.
	 */
	private final ThreadState currentThreadState;

	/**
	 * Instantiate.
	 * 
	 * @param currentThreadState
	 *            Current {@link ThreadState}.
	 */
	public SynchroniseProcessStateFunctionLogic(ThreadState currentThreadState) {
		this.currentThreadState = currentThreadState;
	}

	/*
	 * ======================== Object =============================
	 */

	@Override
	public String toString() {
		return "Synchronising ThreadState " + Integer.toHexString(this.currentThreadState.hashCode())
				+ " to ThreadState "
				+ Integer.toHexString(this.currentThreadState.getProcessState().getMainThreadState().hashCode());
	}

	/*
	 * ===================== FunctionLogic ==========================
	 */

	@Override
	public boolean isRequireThreadStateSafety() {
		// Ensure have thread state safety, to synchronise process state
		return true;
	}

	@Override
	public FunctionState execute(Flow flow) {

		// Synchronise process state (always undertaken via main thread state)
		synchronized (flow.getThreadState().getProcessState().getMainThreadState()) {
		}

		// Synchronized
		return null;
	}

}