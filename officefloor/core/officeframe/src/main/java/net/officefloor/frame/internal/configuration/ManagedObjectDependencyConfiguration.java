package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Configuration of {@link ManagedObject} dependency to another
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyConfiguration<O extends Enum<O>> {

	/**
	 * Obtains the dependency key of the {@link ManagedObject} for which this
	 * maps the dependent {@link ManagedObject}.
	 * 
	 * @return Dependency key or <code>null</code> if dependencies are
	 *         {@link Indexed}.
	 */
	O getDependencyKey();

	/**
	 * Obtains the name of the {@link ManagedObject} providing the dependency
	 * within the scope the {@link ManagedObject} is bound.
	 * 
	 * @return Name of the {@link ManagedObject} providing the dependency within
	 *         the scope the {@link ManagedObject} is bound.
	 */
	String getScopeManagedObjectName();

}