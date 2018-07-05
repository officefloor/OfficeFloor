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
package net.officefloor.frame.impl.construct.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.FunctionBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.FunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Abstract {@link FunctionBuilder}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFunctionBuilder<F extends Enum<F>>
		implements FunctionBuilder<F>, FunctionConfiguration<F> {

	/**
	 * Name of the {@link Team} responsible for this {@link ManagedFunctionLogic}.
	 * May be <code>null</code> for any {@link Team}.
	 */
	private String responsibleTeamName;

	/**
	 * {@link Flow} instances to be linked to this {@link ManagedFunctionLogic}.
	 */
	private final Map<Integer, FlowConfigurationImpl<F>> flows = new HashMap<Integer, FlowConfigurationImpl<F>>();

	/**
	 * Listing of {@link EscalationConfiguration} instances to form the
	 * {@link EscalationProcedure} for the resulting {@link ManagedFunction} of this
	 * {@link ManagedFunctionBuilder}.
	 */
	private final List<EscalationConfiguration> escalations = new LinkedList<EscalationConfiguration>();

	/*
	 * ================== FunctionBuilder =====================
	 */

	@Override
	public void setResponsibleTeam(String officeTeamName) {
		this.responsibleTeamName = officeTeamName;
	}

	@Override
	public void linkFlow(F key, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
		this.linkFlow(key.ordinal(), key, functionName, argumentType, isSpawnThreadState);
	}

	@Override
	public void linkFlow(int flowIndex, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
		this.linkFlow(flowIndex, null, functionName, argumentType, isSpawnThreadState);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
		this.escalations.add(new EscalationConfigurationImpl(typeOfCause,
				new ManagedFunctionReferenceImpl(functionName, typeOfCause)));
	}

	/**
	 * Links in a {@link Flow}.
	 * 
	 * @param flowIndex          Index of the {@link Flow}.
	 * @param flowKey            Key of the {@link Flow}. Should be
	 *                           <code>null</code> if indexed.
	 * @param functionName       Name of the {@link ManagedFunction}.
	 * @param argumentType       Type of argument passed to the instigated
	 *                           {@link Flow}.
	 * @param isSpawnThreadState Indicates whether to spawn a {@link ThreadState}.
	 */
	private void linkFlow(int flowIndex, F flowKey, String functionName, Class<?> argumentType,
			boolean isSpawnThreadState) {

		// Determine the flow name
		String flowName = (flowKey != null ? flowKey.name() : String.valueOf(flowIndex));

		// Create the function reference
		ManagedFunctionReference functionReference = new ManagedFunctionReferenceImpl(functionName, argumentType);

		// Create the flow configuration
		FlowConfigurationImpl<F> flow = new FlowConfigurationImpl<F>(flowName, functionReference, isSpawnThreadState,
				flowIndex, flowKey);

		// Register the flow
		this.flows.put(Integer.valueOf(flowIndex), flow);
	}

	/*
	 * ===================== FunctionConfiguration ==================
	 */

	@Override
	public String getResponsibleTeamName() {
		return this.responsibleTeamName;
	}

	@Override
	public FlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new FlowConfiguration[0]);
	}

	@Override
	public EscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new EscalationConfiguration[0]);
	}

}