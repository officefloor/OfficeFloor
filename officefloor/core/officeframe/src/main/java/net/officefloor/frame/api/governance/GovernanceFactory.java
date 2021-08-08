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

package net.officefloor.frame.api.governance;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Factory for the creation of the {@link Governance}.
 * 
 * @param <E>
 *            Extension interface type for the {@link ManagedObject} instances
 *            to be under this {@link Governance}.
 * @param <F>
 *            {@link Flow} keys for the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceFactory<E, F extends Enum<F>> {

	/**
	 * Creates the {@link Governance}.
	 * 
	 * @return {@link Governance}.
	 * @throws Throwable
	 *             If fails to create the {@link Governance}.
	 */
	Governance<E, F> createGovernance() throws Throwable;

}
