package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Configuration for a dependent {@link Object} of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectConfiguration<O> {

	/**
	 * Indicates if this dependent {@link Object} is the argument passed to the
	 * {@link ManagedFunction}.
	 * 
	 * @return <code>true</code> if is argument passed to the
	 *         {@link ManagedFunction}. <code>false</code> indicates it is a
	 *         {@link ManagedObject} dependency.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the name of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * <p>
	 * This must return a value if not a parameter.
	 * 
	 * @return Name of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	String getScopeManagedObjectName();

	/**
	 * Obtains the type of {@link Object} required by the
	 * {@link ManagedFunction}.
	 * 
	 * @return Type of {@link Object} required by the {@link ManagedFunction}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the index identifying the dependent {@link Object}.
	 * 
	 * @return Index identifying the dependent {@link Object}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the dependent {@link Object}.
	 * 
	 * @return Key identifying the dependent {@link Object}. <code>null</code>
	 *         if indexed.
	 */
	O getKey();

}