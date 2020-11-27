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

package net.officefloor.frame.impl.execute.managedfunction;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Meta-data of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionMetaDataImpl<O extends Enum<O>, F extends Enum<F>>
		implements ManagedFunctionMetaData<O, F> {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * {@link ManagedFunctionFactory} to create the {@link ManagedFunction} of the
	 * {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionFactory<O, F> functionFactory;

	/**
	 * Annotations.
	 */
	private final Object[] annotations;

	/**
	 * Parameter type of this {@link ManagedFunction}.
	 */
	private final Class<?> parameterType;

	/**
	 * {@link TeamManagement} of the {@link Team} responsible for executing this
	 * {@link ManagedFunction}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * Required {@link Governance}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * Translates the {@link ManagedFunction} index to the
	 * {@link ManagedObjectIndex} to obtain the {@link ManagedObject} for the
	 * {@link ManagedFunction}.
	 */
	private final ManagedObjectIndex[] functionIndexedManagedObjects;

	/**
	 * {@link ManagedObjectMetaData} for the {@link ManagedObject} instances bound
	 * to this {@link ManagedFunction}.
	 */
	private final ManagedObjectMetaData<?>[] functionBoundManagedObjects;

	/**
	 * {@link AsynchronousFlow} timeout.
	 */
	private final long asynchronousFlowTimeout;

	/**
	 * {@link AssetManagerReference} for the instigated {@link AsynchronousFlow}
	 * instances.
	 */
	private final AssetManagerReference asynchronousFlowAssetManagerReference;

	/**
	 * {@link Logger} for {@link ManagedFunctionContext}.
	 */
	private final Logger logger;

	/**
	 * {@link Executor} for {@link ManagedFunctionContext}.
	 */
	private final Executor executor;

	/**
	 * {@link OfficeMetaData}.
	 */
	private OfficeMetaData officeMetaData;

	/**
	 * <p>
	 * Meta-data of the available {@link Flow} instances from this
	 * {@link ManagedFunction}.
	 * <p>
	 * Acts as <code>final</code> but specified after constructor.
	 */
	private FlowMetaData[] flowMetaData;

	/**
	 * {@link ManagedFunctionMetaData} of the next {@link ManagedFunction}.
	 */
	private ManagedFunctionMetaData<?, ?> nextFunctionMetaData;

	/**
	 * {@link EscalationProcedure} for exceptions of the {@link ManagedFunction} of
	 * this {@link ManagedFunctionMetaData}.
	 */
	private EscalationProcedure escalationProcedure;

	/**
	 * {@link ManagedFunctionAdministrationMetaData} specifying the
	 * {@link Administration} instances to be completed before executing the
	 * {@link ManagedFunction}.
	 */
	private ManagedFunctionAdministrationMetaData<?, ?, ?>[] preAdministration;

	/**
	 * {@link ManagedFunctionAdministrationMetaData} specifying the
	 * {@link Administration} instances to be completed after executing the
	 * {@link ManagedFunction}.
	 */
	private ManagedFunctionAdministrationMetaData<?, ?, ?>[] postAdministration;

	/**
	 * {@link ManagedObjectIndex} instances identifying the {@link ManagedObject}
	 * instances that must be loaded before the {@link ManagedFunction} may be
	 * executed.
	 */
	private ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * Initiate with details of the meta-data for the {@link ManagedFunction}.
	 * 
	 * @param functionName                           Name of the
	 *                                               {@link ManagedFunction}.
	 * @param functionFactory                        {@link ManagedFunctionFactory}
	 *                                               to create the
	 *                                               {@link ManagedFunction} of the
	 *                                               {@link ManagedFunctionMetaData}.
	 * @param annotations                            Differentiators.
	 * @param parameterType                          Parameter type of this
	 *                                               {@link ManagedFunction}.
	 * @param responsibleTeam                        {@link TeamManagement} of the
	 *                                               {@link Team} responsible for
	 *                                               executing this
	 *                                               {@link ManagedFunction}. May be
	 *                                               <code>null</code>.
	 * @param functionIndexedManagedObjects          Translates the
	 *                                               {@link ManagedFunction} index
	 *                                               to the
	 *                                               {@link ManagedObjectIndex} to
	 *                                               obtain the
	 *                                               {@link ManagedObject} for the
	 *                                               {@link ManagedFunction}.
	 * @param functionBoundManagedObjects            {@link ManagedObjectMetaData}
	 *                                               of the {@link ManagedObject}
	 *                                               instances bound to the
	 *                                               {@link ManagedFunction}.
	 * @param requiredGovernance                     Required {@link Governance}.
	 * @param asynchronousFlowTimeout                {@link AsynchronousFlow}
	 *                                               timeout.
	 * @param asynchronousFlowsAssetManagerReference {@link AssetManagerReference}
	 *                                               for the invoked
	 *                                               {@link AsynchronousFlow}
	 *                                               instances.
	 * @param logger                                 {@link Logger} for
	 *                                               {@link ManagedFunctionContext}.
	 * @param executor                               {@link Executor} for
	 *                                               {@link ManagedFunctionContext}.
	 */
	public ManagedFunctionMetaDataImpl(String functionName, ManagedFunctionFactory<O, F> functionFactory,
			Object[] annotations, Class<?> parameterType, TeamManagement responsibleTeam,
			ManagedObjectIndex[] functionIndexedManagedObjects, ManagedObjectMetaData<?>[] functionBoundManagedObjects,
			boolean[] requiredGovernance, long asynchronousFlowTimeout,
			AssetManagerReference asynchronousFlowsAssetManagerReference, Logger logger, Executor executor) {
		this.functionName = functionName;
		this.functionFactory = functionFactory;
		this.annotations = annotations;
		this.parameterType = parameterType;
		this.responsibleTeam = responsibleTeam;
		this.functionIndexedManagedObjects = functionIndexedManagedObjects;
		this.functionBoundManagedObjects = functionBoundManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.asynchronousFlowTimeout = asynchronousFlowTimeout;
		this.asynchronousFlowAssetManagerReference = asynchronousFlowsAssetManagerReference;
		this.logger = logger;
		this.executor = executor;
	}

	/**
	 * Loads the remaining state of this {@link ManagedFunctionMetaData}.
	 * 
	 * @param officeMetaData         {@link OfficeMetaData}.
	 * @param flowMetaData           Meta-data of the available {@link Flow}
	 *                               instances from this {@link ManagedFunction}.
	 * @param nextFunctionMetaData   {@link ManagedFunctionMetaData} of the next
	 *                               {@link ManagedFunction}.
	 * @param escalationProcedure    {@link EscalationProcedure} for exceptions of
	 *                               the {@link ManagedFunction} of this
	 *                               {@link ManagedFunctionMetaData}.
	 * @param preAdministration      {@link ManagedFunctionAdministrationMetaData}
	 *                               specifying the {@link Administration} instances
	 *                               to be completed before executing the
	 *                               {@link ManagedFunction}.
	 * @param postAdministration     {@link ManagedFunctionAdministrationMetaData}
	 *                               specifying the {@link Administration} instances
	 *                               to be completed after executing the
	 *                               {@link ManagedFunction}.
	 * @param requiredManagedObjects {@link ManagedObjectIndex} instances
	 *                               identifying the {@link ManagedObject} instances
	 *                               that must be loaded before the
	 *                               {@link ManagedFunction} may be executed.
	 */
	public void loadOfficeMetaData(OfficeMetaData officeMetaData, FlowMetaData[] flowMetaData,
			ManagedFunctionMetaData<?, ?> nextFunctionMetaData, EscalationProcedure escalationProcedure,
			ManagedFunctionAdministrationMetaData<?, ?, ?>[] preAdministration,
			ManagedFunctionAdministrationMetaData<?, ?, ?>[] postAdministration,
			ManagedObjectIndex[] requiredManagedObjects) {
		this.officeMetaData = officeMetaData;
		this.flowMetaData = flowMetaData;
		this.nextFunctionMetaData = nextFunctionMetaData;
		this.escalationProcedure = escalationProcedure;
		this.preAdministration = preAdministration;
		this.postAdministration = postAdministration;
		this.requiredManagedObjects = requiredManagedObjects;
	}

	/*
	 * =============== ManagedFunctionMetaData ========================
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
	public Object[] getAnnotations() {
		return this.annotations;
	}

	@Override
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public Executor getExecutor() {
		return this.executor;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public ManagedObjectIndex[] getRequiredManagedObjects() {
		return this.requiredManagedObjects;
	}

	@Override
	public boolean[] getRequiredGovernance() {
		return this.requiredGovernance;
	}

	@Override
	public ManagedObjectIndex getManagedObject(int managedObjectIndex) {
		return this.functionIndexedManagedObjects[managedObjectIndex];
	}

	@Override
	public FlowMetaData getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public long getAsynchronousFlowTimeout() {
		return this.asynchronousFlowTimeout;
	}

	@Override
	public AssetManagerReference getAsynchronousFlowManagerReference() {
		return this.asynchronousFlowAssetManagerReference;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public OfficeMetaData getOfficeMetaData() {
		return this.officeMetaData;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		return this.nextFunctionMetaData;
	}

	@Override
	public ManagedFunctionAdministrationMetaData<?, ?, ?>[] getPreAdministrationMetaData() {
		return this.preAdministration;
	}

	@Override
	public ManagedFunctionAdministrationMetaData<?, ?, ?>[] getPostAdministrationMetaData() {
		return this.postAdministration;
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.functionBoundManagedObjects;
	}

}
