package net.officefloor.web.escalation;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;

/**
 * Not found {@link HttpException}.
 * 
 * @author Daniel Sagenschneider
 */
public class NotFoundHttpException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param requestUri Request URI.
	 */
	public NotFoundHttpException(String requestUri) {
		super(HttpStatus.NOT_FOUND, null, "No resource found for " + requestUri);
	}

}