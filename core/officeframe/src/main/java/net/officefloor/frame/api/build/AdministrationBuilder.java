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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Enables building an {@link Administration}.
 * 
 * @param <F> {@link Flow} key type.
 * @param <G> {@link Governance} key type.
 * @author Daniel Sagenschneider
 */
public interface AdministrationBuilder<F extends Enum<F>, G extends Enum<G>> extends FunctionBuilder<F> {

	/**
	 * Flags for the {@link Administration} to administer the referenced
	 * {@link ManagedObject}. This may be called more than once to register more
	 * than one {@link ManagedObject} to be administered by this
	 * {@link Administration}.
	 * 
	 * @param scopeManagedObjectName Name of the {@link ManagedObject} within the
	 *                               scope this {@link Administration} is being
	 *                               added.
	 */
	void administerManagedObject(String scopeManagedObjectName);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param key            Key to identify the {@link Governance}.
	 * @param governanceName Name of the {@link Governance}.
	 */
	void linkGovernance(G key, String governanceName);

	/**
	 * Links a {@link Governance}.
	 * 
	 * @param governanceIndex Index to identify the {@link Governance}.
	 * @param governanceName  Name of the {@link Governance}.
	 */
	void linkGovernance(int governanceIndex, String governanceName);

	/**
	 * Specifies the timeout to for {@link AsynchronousFlow} instances for this
	 * {@link Administration}.
	 *
	 * @param timeout Timeout.
	 */
	void setAsynchronousFlowTimeout(long timeout);

}
