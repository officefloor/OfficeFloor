package net.officefloor.web.session;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Indicates the {@link HttpSession} is currently invalidated and can not be
 * used. This can occur:
 * <ol>
 * <li>after the {@link HttpSession} has been invalidated with no further
 * {@link HttpSession} required (in other words not creating another
 * {@link HttpSession})</li>
 * <li>during {@link HttpSession} invalidation as another {@link HttpSession} is
 * being created</li>
 * <li>failure in invalidating the {@link HttpSession}</li>
 * </ol>
 *
 * @author Daniel Sagenschneider
 */
public class InvalidatedSessionHttpException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 */
	public InvalidatedSessionHttpException() {
		super(HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Initiate with cause.
	 *
	 * @param cause Cause of {@link HttpSession} being invalid.
	 */
	public InvalidatedSessionHttpException(Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, cause);
	}

}