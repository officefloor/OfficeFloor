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
import net.officefloor.frame.internal.structure.FunctionStateContext;
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
	 * Cache the delegate {@link ThreadState} to avoid traversing delegate chain
	 * for the {@link ThreadState}.
	 */
	private final ThreadState delegateThreadState;

	/**
	 * Cache the delegate {@link TeamManagement} to avoid traversing delegate
	 * chain for the responsible {@link TeamManagement}.
	 */
	private final TeamManagement delegateResponsibleTeam;

	/**
	 * Cache the delegate {@link ThreadState} safety to avoid traversing
	 * delegate chain for the value.
	 */
	private final boolean delegateIsRequireThreadStateSafety;

	/**
	 * Instantiate.
	 * 
	 * @param delegate
	 *            Delegate {@link FunctionState}.
	 */
	public AbstractDelegateFunctionState(FunctionState delegate) {
		this.delegate = delegate;

		// Cache the delegate values
		this.delegateThreadState = delegate.getThreadState();
		this.delegateResponsibleTeam = delegate.getResponsibleTeam();
		this.delegateIsRequireThreadStateSafety = delegate.isRequireThreadStateSafety();
	}

	/*
	 * ======================= Object ==========================
	 */

	@Override
	public String toString() {
		return this.delegate.toString();
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
		return this.delegateResponsibleTeam;
	}

	@Override
	public ThreadState getThreadState() {
		return this.delegateThreadState;
	}

	@Override
	public boolean isRequireThreadStateSafety() {
		return this.delegateIsRequireThreadStateSafety;
	}

	@Override
	public FunctionState execute(FunctionStateContext context) throws Throwable {
		return context.executeDelegate(this.delegate);
	}

	@Override
	public FunctionState cancel() {
		return this.delegate.cancel();
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation) {
		return this.delegate.handleEscalation(escalation);
	}

}