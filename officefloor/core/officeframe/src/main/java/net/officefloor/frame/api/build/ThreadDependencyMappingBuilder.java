package net.officefloor.frame.api.build;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * <p>
 * Provides additional means to obtain the {@link ManagedObject} from
 * {@link ThreadLocal}.
 * <p>
 * This is typically used for integrating third party libraries that expect to
 * obtain objects from {@link ThreadLocal} state.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadDependencyMappingBuilder extends DependencyMappingBuilder {

	/**
	 * Obtains the {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 * 
	 * @param <T> Type of object.
	 * @return {@link OptionalThreadLocal} for the {@link ManagedObject}.
	 */
	<T> OptionalThreadLocal<T> getOptionalThreadLocal();

}