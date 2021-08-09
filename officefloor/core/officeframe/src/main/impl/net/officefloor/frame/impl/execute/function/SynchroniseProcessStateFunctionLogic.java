/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param currentThreadState Current {@link ThreadState}.
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
		return flow.getThreadState().getProcessState().getMainThreadState().runThreadSafeOperation(null);
	}

}
