package net.officefloor.cabinet.dynamo;

import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueGetter;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;
import net.officefloor.cabinet.spi.Index;

/**
 * Dynamo DB {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentAdapter extends AbstractDocumentAdapter<Item, Item, DynamoDocumentAdapter> {

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

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * Instantiate.
	 */
	public DynamoDocumentAdapter(DynamoDB dynamoDb) {
		super(new DynamoSectionAdapter());
		this.dynamoDb = dynamoDb;
	}

	/**
	 * Creates the {@link DynamoDocumentMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param adapter      {@link DynamoDocumentAdapter}.
	 * @return {@link DynamoDocumentMetaData}.
	 * @throws Exception IF fails to create {@link DynamoDocumentMetaData}.
	 */
	private <D> DynamoDocumentMetaData<D> createDocumentMetaData(Class<D> documentType, Index[] indexes,
			DynamoDocumentAdapter adapter) throws Exception {
		return new DynamoDocumentMetaData<>(adapter, documentType, indexes, this.dynamoDb);
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

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createDocumentMetaData);

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, nullable(Item::getBoolean), translator(),
				nullable(Item::withBoolean), serialiser(), deserialiser(Boolean::valueOf));
		init.addFieldType(byte.class, Byte.class,
				nullable((item, attributeName) -> Integer.valueOf(item.getInt(attributeName)).byteValue()),
				nullable(Byte::intValue), nullable((item, attributeName, value) -> item.withInt(attributeName, value)),
				serialiser(), deserialiser(Byte::valueOf));
		init.addFieldType(short.class, Short.class, nullable(Item::getShort), translator(), nullable(Item::withShort),
				serialiser(), deserialiser(Short::valueOf));
		init.addFieldType(char.class, Character.class,
				nullable((item, attributeName) -> item.getString(attributeName).charAt(0)),
				nullable((fieldValue) -> new String(new char[] { fieldValue })),
				nullable((item, attributeName, value) -> item.withString(attributeName, value)), serialiser(),
				deserialiser((character) -> character.charAt(0)));
		init.addFieldType(int.class, Integer.class, nullable(Item::getInt), translator(), nullable(Item::withInt),
				serialiser(), deserialiser(Integer::valueOf));
		init.addFieldType(long.class, Long.class, nullable(Item::getLong), translator(), nullable(Item::withLong),
				serialiser(), deserialiser(Long::valueOf));
		init.addFieldType(float.class, Float.class, nullable(Item::getFloat), translator(), nullable(Item::withFloat),
				serialiser(), deserialiser(Float::valueOf));
		init.addFieldType(double.class, Double.class, nullable(Item::getDouble), translator(),
				nullable(Item::withDouble), serialiser(), deserialiser(Double::valueOf));

		// Open types
		init.addFieldType(String.class, nullable(Item::getString), translator(), nullable(Item::withString),
				serialiser(), deserialiser((value) -> value));

		// Section types
		init.addFieldType(Map.class, nullable(Item::getMap), translator(), nullable(Item::withMap), notSerialiseable(),
				notDeserialiseable(Map.class));
	}

}
