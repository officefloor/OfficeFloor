/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.EscalationCompletion;
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
	 * Instantiate.
	 * 
	 * @param delegate Delegate {@link FunctionState}.
	 */
	public AbstractDelegateFunctionState(FunctionState delegate) {
		this.delegate = delegate;
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
	public FunctionState execute(FunctionStateContext context) throws Throwable {
		return context.executeDelegate(this.delegate);
	}

	@Override
	public FunctionState cancel() {
		return this.delegate.cancel();
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
		return this.delegate.handleEscalation(escalation, completion);
	}

}
