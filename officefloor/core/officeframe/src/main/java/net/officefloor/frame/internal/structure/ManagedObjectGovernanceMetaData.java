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