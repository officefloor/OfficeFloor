package net.officefloor.cabinet.common.adapt;

/**
 * Result of {@link FieldValidator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldValidationResult {

	/**
	 * Invalid reasons.
	 */
	private final String[] invalidReasons;

	/**
	 * Initiate.
	 * 
	 * @param invalidReasons Invalid reasons.
	 */
	public FieldValidationResult(String... invalidReasons) {
		this.invalidReasons = invalidReasons;
	}

	/**
	 * Indicates if valid.
	 * 
	 * @return <code>true</code> if valid.
	 */
	public boolean isValid() {
		return (this.invalidReasons == null) || (this.invalidReasons.length == 0);
	}

	/**
	 * Obtains the invalid reasons.
	 * 
	 * @return Invalid reasons.
	 */
	public String[] getInvalidReasons() {
		return this.invalidReasons;
	}
}