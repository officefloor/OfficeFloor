package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.LogoutRequest;

/**
 * Context for authentication.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthenticationContext<AC extends Serializable, C> {

	/**
	 * Obtains the qualifier for the {@link HttpSecurity} backing this
	 * {@link AuthenticationContext}.
	 * 
	 * @return Qualifier for the {@link HttpSecurity} backing this
	 *         {@link AuthenticationContext}.
	 */
	String getQualifier();

	/**
	 * Registers an {@link AccessControlListener}.
	 * 
	 * @param accessControlListener
	 *            {@link AccessControlListener}.
	 */
	void register(AccessControlListener<? super AC> accessControlListener);

	/**
	 * Undertakes authentication.
	 * 
	 * @param credentials
	 *            Credentials (if available). May be <code>null</code>.
	 * @param authenticateRequest
	 *            Optional {@link AuthenticateRequest}. May be <code>null</code>.
	 */
	void authenticate(C credentials, AuthenticateRequest authenticateRequest);

	/**
	 * Undertakes logout.
	 * 
	 * @param logoutRequest
	 *            Optional {@link LogoutRequest}. May be <code>null</code>.
	 */
	void logout(LogoutRequest logoutRequest);

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param <R>
	 *            Return type.
	 * @param <T>
	 *            Possible {@link Exception} type.
	 * @param operation
	 *            {@link ProcessSafeOperation}.
	 * @return Return value.
	 * @throws T
	 *             Possible {@link Throwable}.
	 */
	<R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T;

}