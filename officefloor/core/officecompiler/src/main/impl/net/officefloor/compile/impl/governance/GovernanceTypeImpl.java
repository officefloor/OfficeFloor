/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * {@link GovernanceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceTypeImpl<I, F extends Enum<F>> implements
		GovernanceType<I, F> {

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? extends I, F> governanceFactory;

	/**
	 * Extension interface.
	 */
	private final Class<I> extensionInterface;

	/**
	 * {@link GovernanceFlowType} instances.
	 */
	private final GovernanceFlowType<F>[] flowTypes;

	/**
	 * {@link GovernanceEscalationType} instances.
	 */
	private final GovernanceEscalationType[] escalationTypes;

	/**
	 * Initiate.
	 * 
	 * @param governanceFactory
	 *            {@link GovernanceFactory}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param flowTypes
	 *            {@link GovernanceFlowType} instances.
	 * @param escalationTypes
	 *            {@link GovernanceEscalationType} instances.
	 */
	public GovernanceTypeImpl(
			GovernanceFactory<? extends I, F> governanceFactory,
			Class<I> extensionInterface, GovernanceFlowType<F>[] flowTypes,
			GovernanceEscalationType[] escalationTypes) {
		this.governanceFactory = governanceFactory;
		this.extensionInterface = extensionInterface;
		this.flowTypes = flowTypes;
		this.escalationTypes = escalationTypes;
	}

	/*
	 * ======================== GovernanceType ======================
	 */

	@Override
	public GovernanceFactory<? extends I, F> getGovernanceFactory() {
		return this.governanceFactory;
	}

	@Override
	public Class<I> getExtensionType() {
		return this.extensionInterface;
	}

	@Override
	public GovernanceFlowType<F>[] getFlowTypes() {
		return this.flowTypes;
	}

	@Override
	public GovernanceEscalationType[] getEscalationTypes() {
		return this.escalationTypes;
	}

}
