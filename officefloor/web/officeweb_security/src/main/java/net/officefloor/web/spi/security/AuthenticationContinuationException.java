package net.officefloor.web.spi.security;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.security.HttpAuthentication;

/**
 * <p>
 * Indicates {@link HttpUrlContinuation} failure in {@link HttpAuthentication}.
 * <p>
 * Typically this occurs because the original request
 * {@link HttpUrlContinuation} state could not be obtained to continue
 * processing after authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationContinuationException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param entity Entity.
	 */
	public AuthenticationContinuationException(String entity) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, null, entity);
	}

}