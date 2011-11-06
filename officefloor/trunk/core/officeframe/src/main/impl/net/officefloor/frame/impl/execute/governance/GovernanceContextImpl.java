/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.governance.GovernanceContext;

/**
 * {@link GovernanceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContextImpl<F extends Enum<F>> implements
		GovernanceContext<F> {

	/**
	 * {@link TaskContext}.
	 */
	private final TaskContext<?, ?, F> taskContext;

	/**
	 * Initiate.
	 * 
	 * @param taskContext
	 *            {@link TaskContext}.
	 */
	public GovernanceContextImpl(TaskContext<?, ?, F> taskContext) {
		this.taskContext = taskContext;
	}

	/*
	 * ================== GovernanceContext =======================
	 */

	@Override
	public void doFlow(F key, Object parameter) {
		this.taskContext.doFlow(key, parameter);
	}

	@Override
	public void doFlow(int flowIndex, Object parameter) {
		// TODO implement GovernanceContext<F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement GovernanceContext<F>.doFlow");
	}

}