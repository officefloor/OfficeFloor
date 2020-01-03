package net.officefloor.gef.configurer.internal;

/**
 * Message only exception.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageOnlyException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param message Message.
	 */
	public MessageOnlyException(String message) {
		super(message);
	}

}