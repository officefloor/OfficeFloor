package net.officefloor.compile.spi.managedobject;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Dependency of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependency {

	/**
	 * Obtains the name of this {@link ManagedObjectDependency}.
	 * 
	 * @return Name of this {@link ManagedObjectDependency}.
	 */
	String getManagedObjectDependencyName();

	/**
	 * Overrides the qualifier specified by the {@link ManagedObjectSource}.
	 * 
	 * @param qualifier Qualifier to use for the {@link ManagedObjectDependency}.
	 */
	void setOverrideQualifier(String qualifier);

	/**
	 * <p>
	 * Specifies a more specific type than the type specified by the
	 * {@link ManagedObjectSource}.
	 * <p>
	 * Note: the type needs to be child of the actual type, otherwise it would cause
	 * {@link ClassCastException} on attempting to the use the resulting dependency.
	 * Hence the type can not be overridden but rather is able to be made more
	 * specific (particularly for auto wiring the dependency).
	 * 
	 * @param type Type to use for the {@link ManagedObjectDependency}.
	 */
	void setSpecificType(String type);

}