package net.officefloor.web.jwt.authority;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates failure in creating an Refresh token.
 * 
 * @author Daniel Sagenschneider
 */
public class RefreshTokenException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param status {@link HttpStatus}.
	 * @param cause  Cause.
	 */
	public RefreshTokenException(HttpStatus status, Throwable cause) {
		super(status, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public RefreshTokenException(Throwable cause) {
		super(cause);
	}

}