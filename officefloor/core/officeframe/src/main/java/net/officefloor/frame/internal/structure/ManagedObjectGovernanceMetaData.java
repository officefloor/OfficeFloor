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
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Governance} of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectGovernanceMetaData<E> {

	/**
	 * Obtains the index for the {@link Governance} within the
	 * {@link ThreadState}.
	 * 
	 * @return Index for the {@link Governance} within the {@link ThreadState}.
	 */
	int getGovernanceIndex();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractor} to extract the extension
	 * interface from the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectExtensionExtractor}.
	 */
	ManagedObjectExtensionExtractor<E> getExtensionInterfaceExtractor();

}
