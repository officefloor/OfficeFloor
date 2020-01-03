package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Indicates parameter type to {@link ManagedFunction} is invalid.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidParameterTypeException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param message Message.
	 */
	public InvalidParameterTypeException(String message) {
		super(message);
	}

}