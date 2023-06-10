package net.officefloor.cabinet.dynamo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueGetter;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * Dynamo DB {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentAdapter extends AbstractDocumentAdapter<Item, Map<String, AttributeValue>> {

	/**
	 * Wraps {@link Function} to {@link FieldValueTranslator} that handles
	 * <code>null</code>.
	 * 
	 * @param <V>       Value type.
	 * @param <P>       Persistent type.
	 * @param transform Transforms the non-null value.
	 * @return {@link FieldValueTranslator}.
	 */
	private static <V, P> FieldValueTranslator<V, P> nullable(Function<V, P> transform) {
		return (fieldName, fieldValue) -> {
			if (fieldValue == null) {
				return null;
			} else {
				return transform.apply(fieldValue);
			}
		};
	}

	/**
	 * Wraps {@link FieldValueGetter} with <code>null</code> handling.
	 * 
	 * @param <V>    Type of value.
	 * @param getter {@link FieldValueGetter}.
	 * @return {@link FieldValueGetter} handling <code>null</code>.
	 */
	private static <V> ScalarFieldValueGetter<Item, V> nullable(ScalarFieldValueGetter<Item, V> getter) {
		return (item, fieldName) -> {
			if (item.isNull(fieldName)) {
				return null;
			} else {
				return getter.getValue(item, fieldName);
			}
		};
	}

	/**
	 * {@link FieldValueSetter} for {@link AttributeValue};
	 * 
	 * @param <V>    Type of value.
	 * @param setter {@link FieldValueSetter}.
	 * @return {@link FieldValueSetter} handling <code>null</code>.
	 */
	private static <V> FieldValueSetter<Map<String, AttributeValue>, V> attributeValueSetter() {
		return (item, fieldName, fieldValue, change) -> {
			AttributeValue value = ItemUtils.toAttributeValue(fieldValue);
			item.put(fieldName, value);
		};
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public DynamoDocumentAdapter(DynamoDB dynamoDb,
			AbstractOfficeStore<DynamoDocumentMetaData<?>, DynamoTransaction> officeStore) {
		super(officeStore);
	}

	/*
	 * =================== AbstractOfficeCabinetAdapter ===================
	 */

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new HashMap<>());

		// Keys
		init.setKeyGetter((item, keyName) -> item.getString(keyName));
		init.setKeySetter((item, keyName, keyValue) -> item.put(keyName, new AttributeValue().withS(keyValue)));

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, nullable(Item::getBoolean), translator(),
				attributeValueSetter(), serialiser(), deserialiser(Boolean::valueOf));
		init.addFieldType(byte.class, Byte.class,
				nullable((item, attributeName) -> Integer.valueOf(item.getInt(attributeName)).byteValue()),
				nullable(Byte::intValue), attributeValueSetter(), serialiser(), deserialiser(Byte::valueOf));
		init.addFieldType(short.class, Short.class, nullable(Item::getShort), translator(), attributeValueSetter(),
				serialiser(), deserialiser(Short::valueOf));
		init.addFieldType(char.class, Character.class,
				nullable((item, attributeName) -> item.getString(attributeName).charAt(0)),
				nullable((fieldValue) -> new String(new char[] { fieldValue })), attributeValueSetter(), serialiser(),
				deserialiser((character) -> character.charAt(0)));
		init.addFieldType(int.class, Integer.class, nullable(Item::getInt), translator(), attributeValueSetter(),
				serialiser(), deserialiser(Integer::valueOf));
		init.addFieldType(long.class, Long.class, nullable(Item::getLong), translator(), attributeValueSetter(),
				serialiser(), deserialiser(Long::valueOf));
		init.addFieldType(float.class, Float.class, nullable(Item::getFloat), translator(), attributeValueSetter(),
				serialiser(), deserialiser(Float::valueOf));
		init.addFieldType(double.class, Double.class, nullable(Item::getDouble), translator(), attributeValueSetter(),
				serialiser(), deserialiser(Double::valueOf));

		// Open types
		init.addFieldType(String.class, nullable(Item::getString), translator(), attributeValueSetter(), serialiser(),
				deserialiser((value) -> value));

		// Section types
		init.addFieldType(Map.class, nullable(Item::getMap), translator(), attributeValueSetter(), notSerialiseable(),
				notDeserialiseable(Map.class));
	}

}
