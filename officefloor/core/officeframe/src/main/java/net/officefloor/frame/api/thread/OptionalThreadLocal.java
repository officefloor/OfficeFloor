package net.officefloor.frame.api.thread;

import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Provides {@link ThreadLocal} access to the {@link ManagedObject} object
 * instances.
 * <p>
 * For applications making use of {@link OfficeFloor} functionality this is not
 * typically required. It is provided to enable integration with third party
 * libraries requiring {@link ThreadLocal} access to objects.
 * 
 * @see ThreadDependencyMappingBuilder
 * 
 * @author Daniel Sagenschneider
 */
public interface OptionalThreadLocal<T> {

	/**
	 * <p>
	 * Obtains the object for the respective {@link ManagedObject} this represents.
	 * <p>
	 * This is <strong>optional</strong>, for if the {@link ManagedObject} is not
	 * yet instantiated then this will return <code>null</code>. As
	 * {@link ManagedObject} instances are asynchronously loaded, they can not be
	 * loaded synchronously for this method. Hence, it may return <code>null</code>
	 * if the {@link ManagedObject} has not been required as a dependency.
	 * <p>
	 * To ensure the {@link ManagedObject} is available, have the
	 * {@link ManagedFunction} or {@link ManagedObject} using this
	 * {@link OptionalThreadLocal} depend on the {@link ManagedObject} this
	 * represents.
	 * 
	 * @return Object from the {@link ManagedObject} or <code>null</code> if
	 *         {@link ManagedObject} not instantiated yet.
	 */
	T get();

}