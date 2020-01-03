package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;

/**
 * Meta-data regarding an extension interface implemented by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionMetaData<E> {

	/**
	 * Obtains the type of extension.
	 * 
	 * @return {@link Class} representing the type of extension.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link ExtensionFactory} to create the extension for the
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ExtensionFactory} to create the extension for the
	 *         {@link ManagedObject}.
	 */
	ExtensionFactory<E> getExtensionFactory();

}