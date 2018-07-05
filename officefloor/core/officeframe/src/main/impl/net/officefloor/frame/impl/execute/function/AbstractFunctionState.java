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
