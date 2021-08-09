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

package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.function.FunctionFlowContext;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context in which the {@link Administration} executes.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationContext<E extends Object, F extends Enum<F>, G extends Enum<G>>
		extends FunctionFlowContext<F> {

	/**
	 * Obtains the particular extensions.
	 * 
	 * @return Extension for the {@link ManagedObject} instances to be administered.
	 */
	E[] getExtensions();

	/**
	 * Obtains the {@link GovernanceManager} for the particular key.
	 * 
	 * @param key Key identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(G key);

	/**
	 * Obtains the {@link GovernanceManager} for the index.
	 * 
	 * @param governanceIndex Index identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(int governanceIndex);

}
