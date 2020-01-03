package net.officefloor.web.value.retrieve;

import net.officefloor.server.http.HttpException;

/**
 * {@link HttpException} from attempting to retrieve a value.
 * 
 * @author Daniel Sagenschneider
 */
public class RetrieveValueException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause of failure to retrieve value.
	 */
	public RetrieveValueException(Throwable cause) {
		super(cause);
	}

}