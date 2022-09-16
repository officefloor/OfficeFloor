package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;

/**
 * Translates the {@link Document} {@link Field} value to underlying persistent
 * store value.
 * 
 * @author Daniel Sagenschneider
 */
public interface FieldValueTranslator<V, P> {

	/**
	 * Translates the {@link Document} {@link Field} value to underlying persistent
	 * value.
	 * 
	 * @param fieldName  Name of {@link Document} {@link Field}.
	 * @param fieldValue {@link Document} {@link Field} value.
	 * @return Translated underlying persistent value.
	 */
	P translate(String fieldName, V fieldValue);

}