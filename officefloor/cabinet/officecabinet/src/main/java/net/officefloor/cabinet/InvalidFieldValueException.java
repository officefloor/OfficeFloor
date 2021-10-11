package net.officefloor.cabinet;

import java.lang.reflect.Field;

/**
 * Indicates an invalid field value.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidFieldValueException extends OfficeCabinetException {

	/**
	 * Serial version Id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param documentType {@link Document} type.
	 * @param fieldName    Name of the {@link Field}.
	 * @param cause        Cause.
	 */
	public InvalidFieldValueException(Class<?> documentType, String fieldName, Throwable cause) {
		super("Field " + documentType.getName() + "#" + fieldName + " can not be loaded with required data", cause);
	}
}