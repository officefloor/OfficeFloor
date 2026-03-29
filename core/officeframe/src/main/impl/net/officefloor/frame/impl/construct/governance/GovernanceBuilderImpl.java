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

package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.impl.construct.function.AbstractFunctionBuilder;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<E, F extends Enum<F>> extends AbstractFunctionBuilder<F>
		implements GovernanceBuilder<F>, GovernanceConfiguration<E, F> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Extension interface.
	 */
	private final Class<E> extensionType;

	/**
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<? super E, F> governanceFactory;

	/**
	 * {@link AsynchronousFlow} timeout.
	 */
	private long asynchronousFlowTimeout = -1;

	/**
	 * Initiate.
	 * 
	 * @param governanceName    Name of the {@link Governance}.
	 * @param extensionType     Extension interface.
	 * @param governanceFactory {@link GovernanceFactory}.
	 */
	public GovernanceBuilderImpl(String governanceName, Class<E> extensionType,
			GovernanceFactory<? super E, F> governanceFactory) {
		this.governanceName = governanceName;
		this.extensionType = extensionType;
		this.governanceFactory = governanceFactory;
	}

	/*
	 * =============== GovernanceBuilder ====================
	 */

	@Override
	public void setAsynchronousFlowTimeout(long timeout) {
		this.asynchronousFlowTimeout = timeout;
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
	public Class<E> getExtensionType() {
		return this.extensionType;
	}

	@Override
	public long getAsynchronousFlowTimeout() {
		return this.asynchronousFlowTimeout;
	}

}
