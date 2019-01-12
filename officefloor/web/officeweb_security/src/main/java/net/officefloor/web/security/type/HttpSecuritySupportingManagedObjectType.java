package net.officefloor.web.security.type;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of the
 * {@link HttpSecuritySupportingManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectType {

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
	ManagedObjectSource<?, ?> getManagedObjectSource();

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

}