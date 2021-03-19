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
