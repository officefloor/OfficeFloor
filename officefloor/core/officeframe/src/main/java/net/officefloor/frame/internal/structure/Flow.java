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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;

/**
 * Represents a sub-graph of the {@link ManagedFunctionContainer} graph making
 * up the {@link ThreadState}. This enables knowing when to undertake the
 * {@link FlowCallback} on completion of all {@link ManagedFunctionContainer}
 * instances of the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Flow extends LinkedListSetEntry<Flow, ThreadState> {

	/**
	 * Creates a {@link FunctionState} within this {@link Flow} for the
	 * {@link FunctionLogic}.
	 * 
	 * @param logic {@link FunctionLogic}.
	 * @return {@link FunctionState} for the {@link FunctionLogic}.
	 */
	FunctionState createFunction(FunctionLogic logic);

	/**
	 * Creates a new managed {@link ManagedFunctionContainer} contained in this
	 * {@link Flow} for the {@link ManagedFunction}.
	 * 
	 * @param <O>                     Dependency key type.
	 * @param <F>                     {@link Flow} key type.
	 * @param parameter               Parameter for the {@link ManagedFunction}.
	 * @param managedFunctionMetaData {@link ManagedFunctionMetaData} for the new
	 *                                {@link ManagedFunction}.
	 * @param isEnforceGovernance     <code>true</code> to enforce
	 *                                {@link Governance} on deactivation.
	 * @param parallelFunctionOwner   {@link BlockState} that is the parallel owner
	 *                                of the new {@link ManagedFunction}.
	 * @return New {@link ManagedFunctionContainer}.
	 */
	<O extends Enum<O>, F extends Enum<F>> ManagedFunctionContainer createManagedFunction(Object parameter,
			ManagedFunctionMetaData<O, F> managedFunctionMetaData, boolean isEnforceGovernance,
			BlockState parallelFunctionOwner);

	/**
	 * Creates a new {@link ManagedFunctionContainer} contained in this {@link Flow}
	 * for the {@link GovernanceActivity}.
	 * 
	 * @param <F>                {@link Flow} key type.
	 * @param governanceActivity {@link GovernanceActivity}.
	 * @param governanceMetaData {@link GovernanceMetaData}.
	 * @return New {@link ManagedFunctionContainer}.
	 */
	<F extends Enum<F>> ManagedFunctionContainer createGovernanceFunction(GovernanceActivity<F> governanceActivity,
			GovernanceMetaData<?, F> governanceMetaData);

	/**
	 * Creates a new {@link ManagedFunctionContainer} contained in this {@link Flow}
	 * for the {@link ManagedObjectAdministrationMetaData}.
	 *
	 * @param <E>                   Extension type.
	 * @param <F>                   {@link Flow} key type.
	 * @param <G>                   {@link Governance} key type.
	 * @param adminMetaData         {@link ManagedObjectAdministrationMetaData}.
	 * @param parallelFunctionOwner {@link ManagedFunctionContainer} that is the
	 *                              parallel owner of the new
	 *                              {@link ManagedFunction}.
	 * @return New {@link ManagedFunctionContainer}.
	 */
	<E, F extends Enum<F>, G extends Enum<G>> ManagedFunctionContainer createAdministrationFunction(
			ManagedObjectAdministrationMetaData<E, F, G> adminMetaData, ManagedFunctionContainer parallelFunctionOwner);

	/**
	 * Flags that the input {@link FunctionState} has completed.
	 * 
	 * @param function             {@link FunctionState} that has completed.
	 * @param functionEscalation   Possible {@link Escalation} from the
	 *                             {@link FunctionState}. May be <code>null</code>.
	 * @param escalationCompletion Possible {@link EscalationCompletion}. May be
	 *                             <code>null</code>.
	 * @return Optional {@link FunctionState} to handle completion of the
	 *         {@link FunctionState}.
	 */
	FunctionState managedFunctionComplete(FunctionState function, Throwable functionEscalation,
			EscalationCompletion escalationCompletion);

	/**
	 * Cancels this {@link Flow}.
	 * 
	 * @return {@link FunctionState} to clean up this {@link Flow}.
	 */
	FunctionState cancel();

	/**
	 * Obtains the {@link ThreadState} containing this {@link Flow}.
	 * 
	 * @return {@link ThreadState} containing this {@link Flow}.
	 */
	ThreadState getThreadState();

}
