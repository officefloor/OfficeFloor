package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Obtains the serialised {@link Field} value from the {@link InternalDocument}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValueSerialiser<V> {

	/**
	 * Obtains the serialised {@link Field} value from retrieved
	 * {@link InternalDocument}.
	 * 
	 * @param fieldName  Name of {@link Field}.
	 * @param fieldValue Value for {@link Field}.
	 * @return Serialised value for {@link Field}.
	 */
	String getSerialisedValue(String fieldName, V fieldValue);

}