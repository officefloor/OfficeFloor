package net.officefloor.web.session;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates the {@link HttpSession} is currently being stored and can not be
 * altered until storage is complete.
 *
 * @author Daniel Sagenschneider
 */
public class StoringSessionHttpException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 */
	public StoringSessionHttpException() {
		super(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Instantiate.
	 * 
	 * @param cause {@link Throwable} cause.
	 */
	public StoringSessionHttpException(Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

}