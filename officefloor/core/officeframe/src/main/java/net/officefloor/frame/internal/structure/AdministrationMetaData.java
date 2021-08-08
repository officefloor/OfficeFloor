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

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data of the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationMetaData<E, F extends Enum<F>, G extends Enum<G>> extends ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link Administration}.
	 * 
	 * @return Name of the {@link Administration}.
	 */
	String getAdministrationName();

	/**
	 * Obtains the {@link AdministrationFactory}.
	 * 
	 * @return {@link AdministrationFactory}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the extension interface to administer the {@link ManagedObject}
	 * instances.
	 * 
	 * @return Extension interface to administer the {@link ManagedObject}
	 *         instances.
	 */
	Class<E> getExtensionInterface();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractorMetaData} over the
	 * {@link ManagedObject} instances to be administered by this
	 * {@link Administration}.
	 * 
	 * @return {@link ManagedObjectExtensionExtractorMetaData} over the
	 *         {@link ManagedObject} instances to be administered by this
	 *         {@link Administration}.
	 */
	ManagedObjectExtensionExtractorMetaData<E>[] getManagedObjectExtensionExtractorMetaData();

	/**
	 * Translates the {@link Administration} {@link Governance} index to the
	 * {@link ThreadState} {@link Governance} index.
	 * 
	 * @param governanceIndex {@link Administration} {@link Governance} index.
	 * @return {@link ThreadState} {@link Governance} index.
	 */
	int translateGovernanceIndexToThreadIndex(int governanceIndex);

}
