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

import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Abstract {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow>
		implements FunctionState {

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState;

	/**
	 * Instantiate.
	 * 
	 * @param threadState
	 *            {@link ThreadState}.
	 */
	public AbstractFunctionState(ThreadState threadState) {
		this.threadState = threadState;
	}

	/*
	 * ===================== FunctionState ========================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

}
