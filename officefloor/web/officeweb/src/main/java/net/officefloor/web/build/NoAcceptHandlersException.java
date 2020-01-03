package net.officefloor.web.build;

import net.officefloor.web.accept.AcceptNegotiator;

/**
 * Indicates there were no <code>Accept</code> handlers configured for
 * {@link AcceptNegotiator}.
 * 
 * @author Daniel Sagenschneider
 */
public class NoAcceptHandlersException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public NoAcceptHandlersException(String message) {
		super(message);
	}

}