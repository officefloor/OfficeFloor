package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data to extract the extension from the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionExtractorMetaData<E extends Object> {

	/**
	 * Obtains the {@link ManagedObjectIndex} to identify the
	 * {@link ManagedObject} to extract the extension interface from.
	 *
	 * @return {@link ManagedObjectIndex} to identify the {@link ManagedObject}
	 *         to extract the extension interface from.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractor} to extract the
	 * Extension Interface from the {@link ManagedObject}.
	 *
	 * @return {@link ManagedObjectExtensionExtractor} to extract the Extension
	 *         Interface from the {@link ManagedObject}.
	 */
	ManagedObjectExtensionExtractor<E> getManagedObjectExtensionExtractor();

}