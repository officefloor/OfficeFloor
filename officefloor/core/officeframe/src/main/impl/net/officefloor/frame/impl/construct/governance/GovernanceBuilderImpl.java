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
package net.officefloor.frame.impl.construct.governance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.construct.function.ManagedFunctionReferenceImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<E, F extends Enum<F>>
		implements GovernanceBuilder<F>, GovernanceConfiguration<E, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super E, F> governanceFactory;

	/**
	 * {@link Team} name responsible to undertake the {@link Governance}
	 * {@link ManagedFunction} instances.
	 */
	private String teamName;

	/**
	 * {@link Flow} instances to be linked to this {@link Governance}.
	 */
	private final Map<Integer, GovernanceFlowConfigurationImpl<F>> flows = new HashMap<Integer, GovernanceFlowConfigurationImpl<F>>();

	/**
	 * {@link GovernanceEscalationConfiguration} instances.
	 */
	private final List<GovernanceEscalationConfiguration> escalations = new LinkedList<GovernanceEscalationConfiguration>();

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 */
	public GovernanceBuilderImpl(String governanceName, Class<E> extensionInterface,
			GovernanceFactory<? super E, F> governanceFactory) {
		this.governanceName = governanceName;
		this.extensionInterface = extensionInterface;
		this.governanceFactory = governanceFactory;
	}

	/*
	 * ================= GovernanceBuilder =======================
	 */

	@Override
	public void setTeam(String teamName) {
		this.teamName = teamName;
	}

	@Override
	public void linkFlow(F key, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
		this.linkFlow(key.ordinal(), key, functionName, argumentType, isSpawnThreadState);
	}

	@Override
	public void linkFlow(int flowIndex, String functionName, Class<?> argumentType, boolean isSpawnThreadState) {
		this.linkFlow(flowIndex, null, functionName, argumentType, isSpawnThreadState);
	}

	/**
	 * Links in a {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @param flowKey
	 *            Key of the {@link Flow}. Should be <code>null</code> if
	 *            indexed.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
	 * @param isSpawnThreadState
	 *            Indicates to spawn the {@link ThreadState}.
	 */
	private void linkFlow(int flowIndex, F flowKey, String functionName, Class<?> argumentType,
			boolean isSpawnThreadState) {

		// Determine the flow name
		String flowName = (flowKey != null ? flowKey.name() : String.valueOf(flowIndex));

		// Create the function reference
		ManagedFunctionReference functionReference = new ManagedFunctionReferenceImpl(functionName, argumentType);

		// Create the flow configuration
		GovernanceFlowConfigurationImpl<F> flow = new GovernanceFlowConfigurationImpl<F>(flowName, isSpawnThreadState,
				functionReference, flowIndex, flowKey);

		// Register the flow
		this.flows.put(new Integer(flowIndex), flow);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
		this.escalations.add(new GovernanceEscalationConfigurationImpl(typeOfCause,
				new ManagedFunctionReferenceImpl(functionName, typeOfCause)));
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GovernanceFactory<? super E, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<E> getExtensionInterface() {
		return this.extensionInterface;
	}

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public GovernanceFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new GovernanceFlowConfiguration[0]);
	}

	@Override
	public GovernanceEscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new GovernanceEscalationConfiguration[this.escalations.size()]);
	}

}