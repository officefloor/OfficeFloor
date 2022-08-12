package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Obtains a value from the start after {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public interface StartAfterDocumentValueGetter {

	/**
	 * Obtains the {@link Field} name containing the {@link Key} for the
	 * {@link Document}.
	 * 
	 * @return {@link Field} name containing the {@link Key} for the
	 *         {@link Document}.
	 */
	String getKeyFieldName();

	/**
	 * Obtains the {@link Key} value for the {@link Document}.
	 * 
	 * @return {@link Key} value for the {@link Document}.
	 */
	String getKey();

	/**
	 * Obtains the value from the start after {@link Document}.
	 * 
	 * @param fieldName Name of the field.
	 * @return Value of the field.
	 */
	Object getValue(String fieldName);

}