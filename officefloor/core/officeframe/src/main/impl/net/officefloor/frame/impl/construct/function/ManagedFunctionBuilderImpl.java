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
package net.officefloor.frame.impl.construct.function;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.administration.AdministrationBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.AdministrationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionBuilderImpl<O extends Enum<O>, F extends Enum<F>>
		implements ManagedFunctionBuilder<O, F>, ManagedFunctionConfiguration<O, F> {

	/**
	 * Name of this {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<O, F> functionFactory;

	/**
	 * {@link Object} instances to be linked to this {@link ManagedFunction}.
	 */
	private final Map<Integer, ManagedFunctionObjectConfigurationImpl<O>> objects = new HashMap<Integer, ManagedFunctionObjectConfigurationImpl<O>>();

	/**
	 * {@link Flow} instances to be linked to this {@link ManagedFunction}.
	 */
	private final Map<Integer, ManagedFunctionFlowConfigurationImpl<F>> flows = new HashMap<Integer, ManagedFunctionFlowConfigurationImpl<F>>();

	/**
	 * {@link Governance} instances to be active for this
	 * {@link ManagedFunction}.
	 */
	private final List<ManagedFunctionGovernanceConfiguration> governances = new LinkedList<ManagedFunctionGovernanceConfiguration>();

	/**
	 * Differentiator.
	 */
	private Object differentiator = null;

	/**
	 * {@link Team}.
	 */
	private String teamName;

	/**
	 * Next {@link FunctionState}.
	 */
	private ManagedFunctionReference nextFunction;

	/**
	 * Listing of {@link Administration} to do before executing the
	 * {@link ManagedFunction}.
	 */
	private final List<AdministrationConfiguration<?, ?, ?>> preAdministration = new LinkedList<AdministrationConfiguration<?, ?, ?>>();

	/**
	 * Listing of {@link Administration} to do after executing the
	 * {@link ManagedFunction}.
	 */
	private final List<AdministrationConfiguration<?, ?, ?>> postAdministration = new LinkedList<AdministrationConfiguration<?, ?, ?>>();

	/**
	 * Listing of {@link ManagedFunction} bound {@link ManagedObject}
	 * configuration.
	 */
	private final List<ManagedObjectConfiguration<?>> functionManagedObjects = new LinkedList<ManagedObjectConfiguration<?>>();

	/**
	 * Listing of {@link ManagedFunctionEscalationConfiguration} instances to
	 * form the {@link EscalationProcedure} for the resulting
	 * {@link ManagedFunction} of this {@link ManagedFunctionBuilder}.
	 */
	private final List<ManagedFunctionEscalationConfiguration> escalations = new LinkedList<ManagedFunctionEscalationConfiguration>();

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of this {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory}.
	 */
	public ManagedFunctionBuilderImpl(String functionName, ManagedFunctionFactory<O, F> functionFactory) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
	}

	/*
	 * ======================= ManagedFunctionBuilder =======================
	 */

	@Override
	public void setDifferentiator(Object differentiator) {
		this.differentiator = differentiator;
	}

	@Override
	public void setTeam(String officeTeamName) {
		this.teamName = officeTeamName;
	}

	@Override
	public void setNextFunction(String functionName, Class<?> argumentType) {
		this.nextFunction = new ManagedFunctionReferenceImpl(functionName, argumentType);
	}

	@Override
	public void linkParameter(O key, Class<?> parameterType) {
		this.linkObject(key.ordinal(), key, true, null, parameterType);
	}

	@Override
	public void linkParameter(int index, Class<?> parameterType) {
		this.linkObject(index, null, true, null, parameterType);
	}

	@Override
	public void linkManagedObject(O key, String scopeManagedObjectName, Class<?> objectType) {
		this.linkObject(key.ordinal(), key, false, scopeManagedObjectName, objectType);
	}

	@Override
	public void linkManagedObject(int index, String scopeManagedObjectName, Class<?> objectType) {
		this.linkObject(index, null, false, scopeManagedObjectName, objectType);
	}

	/**
	 * Links in a dependent {@link Object}.
	 * 
	 * @param objectIndex
	 *            Index of the {@link Object}.
	 * @param objectKey
	 *            Key of the {@link Object}. Should be <code>null</code> if
	 *            indexed.
	 * @param isParameter
	 *            <code>true</code> if the {@link Object} is a parameter.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the
	 *            {@link ManagedObjectSource}. Should be <code>null</code> if a
	 *            parameter.
	 * @param objectType
	 *            Type of {@link Object} required.
	 */
	private void linkObject(int objectIndex, O objectKey, boolean isParameter, String scopeManagedObjectName,
			Class<?> objectType) {
		this.objects.put(new Integer(objectIndex), new ManagedFunctionObjectConfigurationImpl<O>(isParameter,
				scopeManagedObjectName, objectType, objectIndex, objectKey));
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
	 *            Indicates whether to spawn a {@link ThreadState}.
	 */
	private void linkFlow(int flowIndex, F flowKey, String functionName, Class<?> argumentType,
			boolean isSpawnThreadState) {

		// Determine the flow name
		String flowName = (flowKey != null ? flowKey.name() : String.valueOf(flowIndex));

		// Create the function reference
		ManagedFunctionReference functionReference = new ManagedFunctionReferenceImpl(functionName, argumentType);

		// Create the flow configuration
		ManagedFunctionFlowConfigurationImpl<F> flow = new ManagedFunctionFlowConfigurationImpl<F>(flowName,
				functionReference, isSpawnThreadState, flowIndex, flowKey);

		// Register the flow
		this.flows.put(new Integer(flowIndex), flow);
	}

	@Override
	public void addEscalation(Class<? extends Throwable> typeOfCause, String functionName) {
		this.escalations.add(new ManagedFunctionEscalationConfigurationImpl(typeOfCause,
				new ManagedFunctionReferenceImpl(functionName, typeOfCause)));
	}

	@Override
	public DependencyMappingBuilder addManagedObject(String functionManagedObjectName, String officeManagedObjectName) {
		DependencyMappingBuilderImpl<?> builder = new DependencyMappingBuilderImpl<>(functionManagedObjectName,
				officeManagedObjectName);
		this.functionManagedObjects.add(builder);
		return builder;
	}

	@Override
	public <E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> preAdminister(
			String administrationName, Class<E> extension, AdministrationFactory<E, f, G> administrationFactory) {
		AdministrationBuilderImpl<E, f, G> builder = new AdministrationBuilderImpl<>(administrationName, extension,
				administrationFactory);
		this.preAdministration.add(builder);
		return builder;
	}

	@Override
	public <E, f extends Enum<f>, G extends Enum<G>> AdministrationBuilder<f, G> postAdminister(
			String administrationName, Class<E> extension, AdministrationFactory<E, f, G> administrationFactory) {
		AdministrationBuilderImpl<E, f, G> builder = new AdministrationBuilderImpl<>(administrationName, extension,
				administrationFactory);
		this.postAdministration.add(builder);
		return builder;
	}

	@Override
	public void addGovernance(String governanceName) {
		this.governances.add(new ManagedFunctionGovernanceConfigurationImpl(governanceName));
	}

	/*
	 * ============ TaskConfiguration =====================================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public ManagedFunctionFactory<O, F> getManagedFunctionFactory() {
		return this.functionFactory;
	}

	@Override
	public Object getDifferentiator() {
		return this.differentiator;
	}

	@Override
	public String getOfficeTeamName() {
		return this.teamName;
	}

	@Override
	public ManagedFunctionObjectConfiguration<O>[] getObjectConfiguration() {
		return ConstructUtil.toArray(this.objects, new ManagedFunctionObjectConfiguration[0]);
	}

	@Override
	public ManagedFunctionGovernanceConfiguration[] getGovernanceConfiguration() {
		return this.governances.toArray(new ManagedFunctionGovernanceConfiguration[this.governances.size()]);
	}

	@Override
	public ManagedFunctionReference getNextFunction() {
		return this.nextFunction;
	}

	@Override
	public ManagedFunctionFlowConfiguration<F>[] getFlowConfiguration() {
		return ConstructUtil.toArray(this.flows, new ManagedFunctionFlowConfiguration[0]);
	}

	@Override
	public AdministrationConfiguration<?, ?, ?>[] getPreAdministration() {
		return ConstructUtil.toArray(this.preAdministration, new AdministrationConfiguration[0]);
	}

	@Override
	public AdministrationConfiguration<?, ?, ?>[] getPostAdministration() {
		return ConstructUtil.toArray(this.postAdministration, new AdministrationConfiguration[0]);
	}

	@Override
	public ManagedObjectConfiguration<?>[] getManagedObjectConfiguration() {
		return this.functionManagedObjects.toArray(new ManagedObjectConfiguration[0]);
	}

	@Override
	public ManagedFunctionEscalationConfiguration[] getEscalations() {
		return this.escalations.toArray(new ManagedFunctionEscalationConfiguration[0]);
	}

}