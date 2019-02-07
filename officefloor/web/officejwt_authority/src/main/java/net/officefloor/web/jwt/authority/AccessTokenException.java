package net.officefloor.web.jwt.authority;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates failure in creating an Access token.
 * 
 * @author Daniel Sagenschneider
 */
public class AccessTokenException extends HttpException {

	/**
	 * Instantiate.
	 * 
	 * @param status {@link HttpStatus}.
	 * @param cause  Cause.
	 */
	public AccessTokenException(HttpStatus status, Throwable cause) {
		super(status, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public AccessTokenException(Throwable cause) {
		super(cause);
	}

}