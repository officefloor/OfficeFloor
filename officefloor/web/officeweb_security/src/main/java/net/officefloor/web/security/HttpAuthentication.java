package net.officefloor.web.security;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;

/**
 * Dependency interface allowing the application to check if the HTTP client is
 * authenticated.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthentication<C> {

	/**
	 * Indicates if authenticated.
	 * 
	 * @return <code>true</code> if authenticated.
	 * @throws HttpException
	 *             If authentication has been attempted but there were failures
	 *             in undertaking authentication.
	 */
	boolean isAuthenticated() throws HttpException;

	/**
	 * Obtains the type of credentials.
	 * 
	 * @return Type of credentials.
	 */
	Class<C> getCredentialsType();

	/**
	 * Triggers to undertake authentication.
	 *
	 * @param credentials
	 *            Credentials. May be <code>null</code> if no credentials are
	 *            required, or they are pulled from the {@link HttpRequest}.
	 * @param authenticateRequest
	 *            {@link AuthenticateRequest}.
	 */
	void authenticate(C credentials, AuthenticateRequest authenticateRequest);

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl}.
	 * @throws AuthenticationRequiredException
	 *             If not authenticated.
	 * @throws HttpException
	 *             If failure occurred in authentication.
	 */
	HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException;

	/**
	 * Undertakes logging out.
	 * 
	 * @param logoutRequest
	 *            {@link LogoutRequest}.
	 */
	void logout(LogoutRequest logoutRequest);

}