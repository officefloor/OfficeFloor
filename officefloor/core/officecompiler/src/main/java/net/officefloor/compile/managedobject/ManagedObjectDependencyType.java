package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * <code>Type definition</code> of a dependency required by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyType<D extends Enum<D>> {

	/**
	 * Obtains the name of the dependency.
	 * 
	 * @return Name of the dependency.
	 */
	String getDependencyName();

	/**
	 * <p>
	 * Obtains the index identifying the dependency.
	 * <p>
	 * Should this be a {@link ManagedObjectFunctionDependency}, then will return
	 * <code>-1</code>.
	 * 
	 * @return Index identifying the dependency.
	 */
	int getIndex();

	/**
	 * Obtains the {@link Class} that the dependent object must extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getDependencyType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying the
	 *         type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	D getKey();

}