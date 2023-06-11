package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;

/**
 * Validates the {@link Field}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValidator {

	/**
	 * Validates the {@link Field}.
	 * 
	 * @param documentType Type of {@link Document}.
	 * @param field        {@link Field} to validate.
	 * @return {@link FieldValidationResult}.
	 */
	FieldValidationResult validate(Class<?> documentType, Field field);

}