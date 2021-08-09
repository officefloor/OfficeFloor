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

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;

/**
 * Meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceMetaData<E, F extends Enum<F>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? super E, F> getGovernanceFactory();

	/**
	 * Creates the {@link GovernanceContainer}.
	 * 
	 * @param threadState     {@link ThreadState}.
	 * @param governanceIndex Index of the {@link Governance} within the
	 *                        {@link ThreadState}.
	 * @return {@link GovernanceContainer}.
	 */
	GovernanceContainer<E> createGovernanceContainer(ThreadState threadState, int governanceIndex);

	/**
	 * Creates the {@link ManagedFunctionContainer} for the
	 * {@link GovernanceActivity}.
	 * 
	 * @param activity {@link GovernanceActivity}.
	 * @return {@link ManagedFunctionLogic} for the {@link GovernanceActivity}.
	 */
	ManagedFunctionLogic createGovernanceFunctionLogic(GovernanceActivity<F> activity);

}
