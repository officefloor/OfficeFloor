package net.officefloor.frame.api.manage;

/**
 * Indicates an unknown {@link Office} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownOfficeException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link Office}.
	 */
	private final String unknownOfficeName;

	/**
	 * Initiate.
	 * 
	 * @param unknownOfficeName Name of the unknown {@link Office}.
	 */
	public UnknownOfficeException(String unknownOfficeName) {
		super("Unknown Office '" + unknownOfficeName + "'");
		this.unknownOfficeName = unknownOfficeName;
	}

	/**
	 * Obtains the name of the unknown {@link Office}.
	 * 
	 * @return Name of the unknown {@link Office}.
	 */
	public String getUnknownOfficeName() {
		return this.unknownOfficeName;
	}
}
