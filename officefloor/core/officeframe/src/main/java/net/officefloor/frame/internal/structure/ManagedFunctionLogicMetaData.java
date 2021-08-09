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

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;

/**
 * Meta-data for a {@link ManagedFunctionLogic} to be executed within a
 * {@link ManagedFunctionContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link ManagedFunctionLogic}.
	 * 
	 * @return Name of the {@link ManagedFunctionLogic}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link TeamManagement} responsible for completion of the
	 * {@link FunctionState}.
	 * 
	 * @return {@link TeamManagement} responsible for completion of the
	 *         {@link FunctionState}. May be <code>null</code> to enable any
	 *         {@link Team} to execute the {@link FunctionState}.
	 */
	TeamManagement getResponsibleTeam();

	/**
	 * Obtains the time out for {@link AsynchronousFlow} instigated by the
	 * {@link ManagedFunction}.
	 * 
	 * @return Time out for {@link AsynchronousFlow} instigated by the
	 *         {@link ManagedFunction}.
	 */
	long getAsynchronousFlowTimeout();

	/**
	 * Obtains the {@link AssetManagerReference} that manages
	 * {@link AsynchronousFlow} instances instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link AssetManagerReference} that manages {@link AsynchronousFlow}
	 *         instances instigated by the {@link ManagedFunction}.
	 */
	AssetManagerReference getAsynchronousFlowManagerReference();

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData getFlow(int flowIndex);

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the next
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of next {@link ManagedFunction}.
	 */
	ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link ManagedFunctionLogic}.
	 * 
	 * @return {@link EscalationProcedure}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link OfficeMetaData}.
	 * 
	 * @return {@link OfficeMetaData}.
	 */
	OfficeMetaData getOfficeMetaData();

}
