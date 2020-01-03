package net.officefloor.web.spi.security;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAuthentication;

/**
 * Factory for the creation of the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthenticationFactory<A, C> {

	/**
	 * Creates {@link HttpAuthentication} from the custom authentication.
	 * 
	 * @param authentication
	 *            Custom authentication.
	 * @return {@link HttpAuthentication} adapting the custom access control.
	 */
	HttpAuthentication<C> createHttpAuthentication(A authentication) throws HttpException;

}