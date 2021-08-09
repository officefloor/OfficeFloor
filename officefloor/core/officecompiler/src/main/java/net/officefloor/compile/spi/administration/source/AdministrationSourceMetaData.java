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

package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of the {@link AdministrationSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSourceMetaData<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link AdministrationFactory} to create the
	 * {@link Administration} for this {@link AdministrationMetaData}.
	 * 
	 * @return {@link AdministrationFactory}
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the list of {@link AdministrationFlowMetaData} instances should
	 * this {@link Administration} require instigating a {@link Flow}.
	 * 
	 * @return Meta-data of {@link Flow} instances instigated by this
	 *         {@link Administration}.
	 */
	AdministrationFlowMetaData<F>[] getFlowMetaData();

	/**
	 * Obtains the list of {@link AdministrationEscalationMetaData} instances
	 * from this {@link Administration}.
	 * 
	 * @return Meta-data of {@link Escalation} instances instigated by this
	 *         {@link Administration}.
	 */
	AdministrationEscalationMetaData[] getEscalationMetaData();

	/**
	 * Obtains the list of {@link AdministrationGovernanceMetaData} instances
	 * should this {@link Administration} manually managed {@link Governance}.
	 * 
	 * @return Meta-data of {@link Governance} used by this
	 *         {@link Administration}.
	 */
	AdministrationGovernanceMetaData<G>[] getGovernanceMetaData();

}
