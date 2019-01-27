package net.officefloor.web.security.type;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of the
 * {@link HttpSecuritySupportingManagedObject} dependency.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectDependencyType<O extends Enum<O>> {

	/**
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	O getKey();

	/**
	 * Obtains the {@link OfficeManagedObject} for this dependency.
	 * 
	 * @param context {@link HttpSecuritySupportingManagedObjectDependencyContext}.
	 * @return {@link OfficeManagedObject} for this dependency.
	 */
	OfficeManagedObject getOfficeManagedObject(HttpSecuritySupportingManagedObjectDependencyContext context);

}