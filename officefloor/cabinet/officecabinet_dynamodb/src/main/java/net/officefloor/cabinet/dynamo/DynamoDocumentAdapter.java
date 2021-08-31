package net.officefloor.cabinet.dynamo;

import com.amazonaws.services.dynamodbv2.document.Item;

import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueGetter;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;

/**
 * Dynamo DB {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentAdapter extends AbstractDocumentAdapter<Item, Item> {

	/**
	 * Wraps {@link FieldValueGetter} with <code>null</code> handling.
	 * 
	 * @param <V>    Type of value.
	 * @param getter {@link FieldValueGetter}.
	 * @return {@link FieldValueGetter} handling <code>null</code>.
	 */
	private static <V> FieldValueGetter<Item, V> nullable(FieldValueGetter<Item, V> getter) {
		return (item, fieldName) -> {
			if (item.isNull(fieldName)) {
				return null;
			} else {
				return getter.getValue(item, fieldName);
			}
		};
	}

	/**
	 * Wraps {@link FieldValueSetter} with <code>null</code> handling.
	 * 
	 * @param <V>    Type of value.
	 * @param setter {@link FieldValueSetter}.
	 * @return {@link FieldValueSetter} handling <code>null</code>.
	 */
	private static <V> FieldValueSetter<Item, V> nullable(FieldValueSetter<Item, V> setter) {
		return (item, fieldName, fieldValue) -> {
			if (fieldValue == null) {
				item.withNull(fieldName);
			} else {
				setter.setValue(item, fieldName, fieldValue);
			}
		};
	}

	/*
	 * =================== AbstractOfficeCabinetAdapter ===================
	 */

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new Item());

		// Keys
		init.setKeyGetter((item, keyName) -> item.getString(keyName));
		init.setKeySetter((item, keyName, keyValue) -> item.withString(keyName, keyValue));

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, nullable(Item::getBoolean), nullable(Item::withBoolean));
		init.addFieldType(byte.class, Byte.class, nullable((item, attributeName) -> {
			byte[] value = item.getBinary(attributeName);
			return value != null ? value[0] : null;
		}), nullable((item, attributeName, value) -> {
			item.withBinary(attributeName, new byte[] { value });
		}));
		init.addFieldType(short.class, Short.class, nullable(Item::getShort), nullable(Item::withShort));
		init.addFieldType(char.class, Character.class,
				nullable((item, attributeName) -> item.getString(attributeName).charAt(0)), nullable((item,
						attributeName, value) -> item.withString(attributeName, new String(new char[] { value }))));
		init.addFieldType(int.class, Integer.class, nullable(Item::getInt), nullable(Item::withInt));
		init.addFieldType(long.class, Long.class, nullable(Item::getLong), nullable(Item::withLong));
		init.addFieldType(float.class, Float.class, nullable(Item::getFloat), nullable(Item::withFloat));
		init.addFieldType(double.class, Double.class, nullable(Item::getDouble), nullable(Item::withDouble));

		// Open types
		init.addFieldType(String.class, nullable(Item::getString), nullable(Item::withString));
	}

}
