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
