package net.officefloor.spring.extension;

import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * Context for a {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringSupplierExtensionContext {

	/**
	 * Obtains the object source from a {@link ManagedObject}.
	 * 
	 * @param            <O> Object type.
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type required.
	 * @return Object from the {@link ManagedObject}.
	 * @throws Exception If fails to source the {@link ManagedObject}.
	 */
	<O> O getManagedObject(String qualifier, Class<? extends O> objectType) throws Exception;

	/**
	 * Registers a {@link ThreadSynchroniserFactory}.
	 * 
	 * @param threadSynchroniserFactory {@link ThreadSynchroniserFactory}.
	 */
	void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory);

}