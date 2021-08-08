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

package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of an {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationType<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link AdministrationFactory} to create the
	 * {@link Administration}.
	 * 
	 * @return {@link AdministrationFactory} to create the
	 *         {@link Administration}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link Administration}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link AdministrationFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Administration}.
	 * 
	 * @return {@link AdministrationFlowType} definitions for the possible
	 *         {@link Flow} instances instigated by the {@link Administration}.
	 */
	AdministrationFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link AdministrationEscalationType} definitions for the
	 * possible {@link EscalationFlow} instances by the {@link Administration}.
	 * 
	 * @return {@link AdministrationEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Administration}.
	 */
	AdministrationEscalationType[] getEscalationTypes();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Governance}
	 * instances used by this {@link Administration}.
	 * 
	 * @return {@link Enum} providing {@link Governance} keys or
	 *         <code>null</code> if {@link Indexed} or no {@link Governance}
	 *         used.s
	 */
	Class<G> getGovernanceKeyClass();

	/**
	 * Obtains the {@link AdministrationGovernanceType} instances for the
	 * {@link Governance} used by this {@link Administration}.
	 * 
	 * @return {@link AdministrationGovernanceType} instances for the
	 *         {@link Governance} used by this {@link Administration}.
	 */
	AdministrationGovernanceType<G>[] getGovernanceTypes();

}
