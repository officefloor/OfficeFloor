package net.officefloor.web.security.type;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * Context for extracting the {@link OfficeManagedObject} for the
 * {@link HttpSecuritySupportingManagedObjectDependencyType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySupportingManagedObjectDependencyContext {

	/**
	 * Obtains the custom authentication.
	 * 
	 * @return Custom authentication.
	 */
	OfficeManagedObject getAuthentication();

	/**
	 * Obtains the {@link HttpAuthentication}.
	 * 
	 * @return {@link HttpAuthentication}.
	 */
	OfficeManagedObject getHttpAuthentication();

	/**
	 * Obtains the custom access control.
	 * 
	 * @return Custom access control.
	 */
	OfficeManagedObject getAccessControl();

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl}.
	 */
	OfficeManagedObject getHttpAccessControl();

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObject}.
	 * 
	 * @param supportingManagedObject {@link HttpSecuritySupportingManagedObject}.
	 * @return {@link OfficeManagedObject} for the
	 *         {@link HttpSecuritySupportingManagedObject}.
	 */
	OfficeManagedObject getSupportingManagedObject(HttpSecuritySupportingManagedObject<?> supportingManagedObject);

}