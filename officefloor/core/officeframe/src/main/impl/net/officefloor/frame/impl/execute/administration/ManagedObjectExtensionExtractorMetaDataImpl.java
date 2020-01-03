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