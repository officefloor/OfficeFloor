package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Indicates an invalid domain {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidCabinetException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	public InvalidCabinetException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCabinetException(String message) {
		super(message);
	}

	public InvalidCabinetException(Throwable cause) {
		super(cause);
	}

}