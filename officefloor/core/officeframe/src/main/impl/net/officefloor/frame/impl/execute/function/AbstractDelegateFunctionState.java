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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Abstract {@link FunctionState} that delegates functionality to a delegate
 * {@link FunctionState}.
 *
 * @author Daniel Sagenschneider
 */
public class AbstractDelegateFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow>
		implements FunctionState {

	/**
	 * Delegate {@link FunctionState}.
	 */
	protected final FunctionState delegate;

	/**
	 * Instantiate.
	 * 
	 * @param delegate
	 *            Delegate {@link FunctionState}.
	 */
	public AbstractDelegateFunctionState(FunctionState delegate) {
		this.delegate = delegate;
	}

	/*
	 * ======================= FunctionState ==========================
	 */

	@Override
	public Flow getLinkedListSetOwner() {
		return this.delegate.getLinkedListSetOwner();
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.delegate.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.delegate.getThreadState();
	}

	@Override
	public boolean isRequireThreadStateSafety() {
		return this.delegate.isRequireThreadStateSafety();
	}

	@Override
	public FunctionState execute() throws Throwable {
		return this.delegate.execute();
	}

	@Override
	public FunctionState cancel(Throwable cause) {
		return this.delegate.cancel(cause);
	}

}