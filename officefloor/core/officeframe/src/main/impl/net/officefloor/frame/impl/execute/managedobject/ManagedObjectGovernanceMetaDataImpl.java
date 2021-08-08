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

package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * {@link ManagedObjectGovernanceMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectGovernanceMetaDataImpl<I>
		implements ManagedObjectGovernanceMetaData<I>, ManagedObjectExtensionExtractor<I> {

	/**
	 * {@link Governance} index.
	 */
	private final int governanceIndex;

	/**
	 * {@link ExtensionFactory}.
	 */
	private final ExtensionFactory<I> extensionInterfaceFactory;

	/**
	 * Initiate.
	 * 
	 * @param governanceIndex
	 *            {@link Governance} index.
	 * @param extensionInterfaceFactory
	 *            {@link ExtensionFactory}.
	 */
	public ManagedObjectGovernanceMetaDataImpl(int governanceIndex,
			ExtensionFactory<I> extensionInterfaceFactory) {
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceFactory = extensionInterfaceFactory;
	}

	/*
	 * =================== ManagedObjectGovernanceMetaData ====================
	 */

	@Override
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	@Override
	public ManagedObjectExtensionExtractor<I> getExtensionInterfaceExtractor() {
		return this;
	}

	/*
	 * ========================= ExtensionInterfaceExtractor ===================
	 */

	@Override
	public I extractExtension(ManagedObject managedObject, ManagedObjectMetaData<?> managedObjectMetaData)
			throws Throwable {
		return this.extensionInterfaceFactory.createExtension(managedObject);
	}

}
