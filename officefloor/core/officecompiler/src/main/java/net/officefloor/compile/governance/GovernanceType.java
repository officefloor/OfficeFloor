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

package net.officefloor.compile.governance;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceType<E, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceFactory}.
	 * 
	 * @return {@link GovernanceFactory}.
	 */
	GovernanceFactory<? extends E, F> getGovernanceFactory();

	/**
	 * Obtains the extension type that the {@link ManagedObject} instances are to
	 * provide to be enable {@link Governance} over them.
	 * 
	 * @return Extension type that the {@link ManagedObject} instances are to
	 *         provide to be enable {@link Governance} over them.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link GovernanceFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link GovernanceActivity}.
	 */
	GovernanceFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link GovernanceEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link GovernanceActivity}.
	 */
	GovernanceEscalationType[] getEscalationTypes();

}
