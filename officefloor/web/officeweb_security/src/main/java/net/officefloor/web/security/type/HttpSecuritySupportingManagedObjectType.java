package net.officefloor.web.security.type;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of the
 * {@link HttpSecuritySupportingManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectType<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @return Name of the {@link HttpSecuritySupportingManagedObject}.
	 */
	String getSupportingManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<O, ?> getManagedObjectSource();

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	PropertyList getProperties();

	/**
	 * Obtains the object type for the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @return Object type.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link ManagedObjectScope}.
	 * 
	 * @return {@link ManagedObjectScope}.
	 */
	ManagedObjectScope getManagedObjectScope();

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObjectDependencyType}
	 * instances.
	 * 
	 * @return {@link HttpSecuritySupportingManagedObjectDependencyType} instances.
	 */
	HttpSecuritySupportingManagedObjectDependencyType<O>[] getDependencyTypes();

}