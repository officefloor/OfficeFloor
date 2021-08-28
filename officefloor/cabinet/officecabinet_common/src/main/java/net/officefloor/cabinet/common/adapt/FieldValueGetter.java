package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;

/**
 * Obtains the {@link Field} value from retrieved internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValueGetter<R, V> {

	/**
	 * Obtains the {@link Field} value from retrieved internal {@link Document}.
	 * 
	 * @param internalDocument Retrieved internal {@link Document}.
	 * @param fieldName        Name of {@link Field}.
	 * @return Value for {@link Field}.
	 */
	V getValue(R internalDocument, String fieldName);

}
