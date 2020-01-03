package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * <code>Type definition</code> of a {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityType<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the type for authentication.
	 * 
	 * @return Type for authentication.
	 */
	Class<A> getAuthenticationType();

	/**
	 * Should the custom authentication not implement {@link HttpAuthentication},
	 * then this factory provides a wrapping {@link HttpAuthentication}
	 * implementation.
	 * 
	 * @return {@link HttpAuthenticationFactory} to create wrapping
	 *         {@link HttpAuthentication}, or <code>null</code> if custom already
	 *         implements {@link HttpAuthentication}.
	 */
	HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory();

	/**
	 * Obtains the type for access control.
	 * 
	 * @return Type for access control.
	 */
	Class<AC> getAccessControlType();

	/**
	 * Should the custom access control not implement {@link HttpAccessControl},
	 * then this factory provides a wrapping {@link HttpAccessControl}
	 * implementation.
	 * 
	 * @return {@link HttpAccessControlFactory} to create wrapping
	 *         {@link HttpAccessControl}, or <code>null</code> if custom already
	 *         implements {@link HttpAccessControl}.
	 */
	HttpAccessControlFactory<AC> getHttpAccessControlFactory();

	/**
	 * Obtains the type for credentials.
	 * 
	 * @return Type for credentials. May be <code>null</code> if no application
	 *         specific behaviour is required to provide credentials.
	 */
	Class<C> getCredentialsType();

	/**
	 * Obtains the {@link HttpSecurityDependencyType} definitions of the required
	 * dependencies for the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecurityDependencyType} definitions of the required
	 *         dependencies for the {@link HttpSecuritySource}.
	 */
	HttpSecurityDependencyType<O>[] getDependencyTypes();

	/**
	 * Obtains the {@link HttpSecurityFlowType} definitions of the {@link Flow}
	 * instances required to be linked for the {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecurityFlowType} definitions of the {@link Flow}
	 *         instances required to be linked for the {@link HttpSecuritySource}.
	 */
	HttpSecurityFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link HttpSecuritySupportingManagedObjectType} definitions of
	 * the {@link HttpSecuritySupportingManagedObject} instances provided by the
	 * {@link HttpSecuritySource}.
	 * 
	 * @return {@link HttpSecuritySupportingManagedObjectType} definitions of the
	 *         {@link HttpSecuritySupportingManagedObject} instances provided by the
	 *         {@link HttpSecuritySource}.
	 */
	HttpSecuritySupportingManagedObjectType<?>[] getSupportingManagedObjectTypes();

}