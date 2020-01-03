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