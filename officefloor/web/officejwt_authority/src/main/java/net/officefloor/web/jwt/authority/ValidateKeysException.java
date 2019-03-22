package net.officefloor.web.jwt.authority;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.jwt.validate.JwtValidateKey;

/**
 * Indicates failure in obtaining the {@link JwtValidateKey} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateKeysException extends HttpException {

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
	public ValidateKeysException(HttpStatus status, Throwable cause) {
		super(status, cause);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public ValidateKeysException(Throwable cause) {
		super(cause);
	}

}