package net.officefloor.cabinet.key;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.InvalidFieldValueException;

/**
 * Implementation of {@link DocumentKey} for a {@link Field}.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldDocumentKey<D> implements DocumentKey<D> {

	/**
	 * {@link Field} on {@link Document} containing the key.
	 */
	private final Field field;

	/**
	 * Instantiate.
	 * 
	 * @param field {@link Field} on {@link Document} containing the key.
	 */
	public FieldDocumentKey(Field field) {
		this.field = field;

		// Ensure the field is accessible
		this.field.setAccessible(true);
	}

	/*
	 * =================== DocumentKey =======================
	 */

	@Override
	public String getKeyName() {
		return this.field.getName();
	}

	@Override
	public String getKey(D document) throws InvalidFieldValueException {
		try {
			return (String) this.field.get(document);
		} catch (Exception ex) {
			throw new InvalidFieldValueException(document.getClass(), this.field.getName(), ex);
		}
	}

	@Override
	public void setKey(D document, String key) throws InvalidFieldValueException {
		try {
			this.field.set(document, key);
		} catch (Exception ex) {
			throw new InvalidFieldValueException(document.getClass(), this.field.getName(), ex);
		}
	}

}
