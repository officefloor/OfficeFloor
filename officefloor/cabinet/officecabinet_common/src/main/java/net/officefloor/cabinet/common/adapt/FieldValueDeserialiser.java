package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

/**
 * Obtains the deserialised {@link Field} value from the serialised value.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValueDeserialiser<V> {

	/**
	 * Obtains the deserialised {@link Field} value from serialised value.
	 * 
	 * @param fieldName       Name of the {@link Field}.
	 * @param serialisedValue Serialised {@link Field} value.
	 * @return Deserialised value for {@link Field}.
	 */
	V getDeserialisedValue(String fieldName, String serialisedValue);

}