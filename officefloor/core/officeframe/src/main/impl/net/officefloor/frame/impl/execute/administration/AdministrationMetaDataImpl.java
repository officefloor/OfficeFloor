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

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractorMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link AdministrationMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements AdministrationMetaData<E, F, G> {

	/**
	 * Bound name of this {@link Administration}.
	 */
	private final String administrationName;

	/**
	 * {@link AdministrationFactory}.
	 */
	private final AdministrationFactory<E, F, G> administrationFactory;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionInterface;

	/**
	 * {@link ManagedObjectExtensionExtractorMetaData}.
	 */
	private final ManagedObjectExtensionExtractorMetaData<E>[] eiMetaData;

	/**
	 * {@link TeamManagement} of {@link Team} responsible for the
	 * {@link GovernanceActivity}.
	 */
	private final TeamManagement responsibleTeam;

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
	 * {@link FlowMetaData} instances for this {@link Administration}.
	 */
	private final FlowMetaData[] flowMetaData;

	/**
	 * Translates the index to a {@link ThreadState} {@link Governance} index.
	 */
	private final int[] governanceIndexes;

	/**
	 * {@link EscalationProcedure}.
	 */
	private final EscalationProcedure escalationProcedure;

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param administrationName                    Bound name of this
	 *                                              {@link Administration}.
	 * @param administrationFactory                 {@link AdministrationFactory}.
	 * @param extensionInterface                    Extension interface.
	 * @param eiMetaData                            {@link ManagedObjectExtensionExtractorMetaData}.
	 * @param responsibleTeam                       {@link TeamManagement} of
	 *                                              {@link Team} responsible for the
	 *                                              {@link GovernanceActivity}.
	 * @param asynchronousFlowAssetManagerReference {@link AssetManagerReference}
	 *                                              for the instigated
	 *                                              {@link AsynchronousFlow}
	 *                                              instances.
	 * @param asynchronousFlowTimeout               {@link AsynchronousFlow}
	 *                                              tiemout.
	 * @param flowMetaData                          {@link FlowMetaData} instances
	 *                                              for this {@link Administration}.
	 * @param governanceIndexes                     Translates the index to a
	 *                                              {@link ThreadState}
	 *                                              {@link Governance} index.
	 * @param escalationProcedure                   {@link EscalationProcedure}.
	 * @param officeMetaData                        {@link OfficeMetaData}.
	 */
	public AdministrationMetaDataImpl(String administrationName, AdministrationFactory<E, F, G> administrationFactory,
			Class<E> extensionInterface, ManagedObjectExtensionExtractorMetaData<E>[] eiMetaData,
			TeamManagement responsibleTeam, long asynchronousFlowTimeout,
			AssetManagerReference asynchronousFlowAssetManagerReference, FlowMetaData[] flowMetaData,
			int[] governanceIndexes, EscalationProcedure escalationProcedure, OfficeMetaData officeMetaData) {
		this.administrationName = administrationName;
		this.administrationFactory = administrationFactory;
		this.extensionInterface = extensionInterface;
		this.eiMetaData = eiMetaData;
		this.responsibleTeam = responsibleTeam;
		this.asynchronousFlowTimeout = asynchronousFlowTimeout;
		this.asynchronousFlowAssetManagerReference = asynchronousFlowAssetManagerReference;
		this.flowMetaData = flowMetaData;
		this.governanceIndexes = governanceIndexes;
		this.escalationProcedure = escalationProcedure;
		this.officeMetaData = officeMetaData;
	}

	/*
	 * ================= ManagedFunctionLogicMetaData =================
	 */

	@Override
	public String getFunctionName() {
		return Administration.class.getSimpleName() + "-" + this.administrationName;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
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
	public FlowMetaData getFlow(int flowIndex) {
		return this.flowMetaData[flowIndex];
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData() {
		return null; // no next function
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.escalationProcedure;
	}

	@Override
	public OfficeMetaData getOfficeMetaData() {
		return this.officeMetaData;
	}

	/*
	 * ================= AdministratorMetaData ============================
	 */

	@Override
	public ManagedObjectExtensionExtractorMetaData<E>[] getManagedObjectExtensionExtractorMetaData() {
		return this.eiMetaData;
	}

	@Override
	public String getAdministrationName() {
		return this.administrationName;
	}

	@Override
	public AdministrationFactory<E, F, G> getAdministrationFactory() {
		return this.administrationFactory;
	}

	@Override
	public Class<E> getExtensionInterface() {
		return this.extensionInterface;
	}

	@Override
	public int translateGovernanceIndexToThreadIndex(int governanceIndex) {
		return this.governanceIndexes[governanceIndex];
	}

}
