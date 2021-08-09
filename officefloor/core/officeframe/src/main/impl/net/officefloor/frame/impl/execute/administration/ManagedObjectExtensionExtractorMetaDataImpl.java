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

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractorMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;

/**
 * Implementation of the {@link ManagedObjectExtensionExtractorMetaData}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExtensionExtractorMetaDataImpl<I extends Object>
		implements ManagedObjectExtensionExtractorMetaData<I>, ManagedObjectExtensionExtractor<I> {

	/**
	 * {@link ManagedObjectIndex} identifying the {@link ManagedObject}
	 * implementing the extension interface.
	 */
	private final ManagedObjectIndex managedObjectIndex;

	/**
	 * {@link ExtensionFactory} to create the extension interface from the
	 * {@link ManagedObject}.
	 */
	private final ExtensionFactory<I>[] extensionInterfaceFactories;

	/**
	 * Initiate.
	 *
	 * @param managedObjectIndex
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject} implementing the extension interface.
	 * @param extensionInterfaceFactories
	 *            {@link ExtensionFactory} instances in the order of the
	 *            {@link ManagedObjectMetaData} instances corresponding to the
	 *            {@link ManagedObject} instances for the
	 *            {@link ManagedObjectIndex}.
	 */
	public ManagedObjectExtensionExtractorMetaDataImpl(ManagedObjectIndex managedObjectIndex,
			ExtensionFactory<I>[] extensionInterfaceFactories) {
		this.managedObjectIndex = managedObjectIndex;
		this.extensionInterfaceFactories = extensionInterfaceFactories;
	}

	/*
	 * ====================== ExtensionInterfaceMetaData =====================
	 */

	@Override
	public ManagedObjectIndex getManagedObjectIndex() {
		return this.managedObjectIndex;
	}

	@Override
	public ManagedObjectExtensionExtractor<I> getManagedObjectExtensionExtractor() {
		return this;
	}

	/*
	 * ====================== ExtensionInterfaceExtractor =====================
	 */

	@Override
	public I extractExtension(ManagedObject managedObject, ManagedObjectMetaData<?> managedObjectMetaData)
			throws Throwable {

		// Obtain the instance index of the Managed Object
		int instanceIndex = managedObjectMetaData.getInstanceIndex();

		// Obtain the corresponding factory for the instance
		ExtensionFactory<I> factory = this.extensionInterfaceFactories[instanceIndex];

		// Return the extension interface
		return factory.createExtension(managedObject);
	}

}
