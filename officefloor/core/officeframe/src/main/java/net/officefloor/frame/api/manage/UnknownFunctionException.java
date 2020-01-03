package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Indicates an unknown {@link ManagedFunction} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownFunctionException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link ManagedFunction}.
	 */
	private final String unknownFunctionName;

	/**
	 * Initiate.
	 * 
	 * @param unknownFunctionName Name of the unknown {@link ManagedFunction}.
	 */
	public UnknownFunctionException(String unknownFunctionName) {
		super("Unknown Function '" + unknownFunctionName + "'");
		this.unknownFunctionName = unknownFunctionName;
	}

	/**
	 * Obtains the name of the unknown {@link ManagedFunction}.
	 * 
	 * @return Name of the unknown {@link ManagedFunction}.
	 */
	public String getUnknownFunctionName() {
		return this.unknownFunctionName;
	}

}
