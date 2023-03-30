package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManagerChange;

/**
 * Loads the {@link Field} value onto the stored internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValueSetter<S, V> {

	/**
	 * Loads the {@link Field} value onto the stored internal {@link Document}.
	 * 
	 * @param document         {@link Document}.
	 * @param internalDocument Stored internal {@link Document}.
	 * @param fieldName        Name of {@link Field}.
	 * @param value            Value to be stored.
	 * @param change           {@link CabinetManagerChange}.
	 */
	void setValue(S internalDocument, String fieldName, V value, CabinetManagerChange change);

}
