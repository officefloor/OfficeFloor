package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

/**
 * Obtains the scalar (non-section) value.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface ScalarFieldValueGetter<R, V> {

	/**
	 * Obtains the value.
	 * 
	 * @param internalDocument Internal document.
	 * @param fieldName        Name of {@link Field}.
	 * @return Value.
	 */
	V getValue(R internalDocument, String fieldName);

}
