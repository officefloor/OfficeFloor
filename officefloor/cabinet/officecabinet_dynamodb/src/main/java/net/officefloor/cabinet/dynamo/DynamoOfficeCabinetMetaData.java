package net.officefloor.cabinet.dynamo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeCabinetMetaData;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.DocumentKey;

/**
 * Meta-data for the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeCabinetMetaData<D> extends AbstractOfficeCabinetMetaData<D> {

	/**
	 * Mapping of {@link Field} type to {@link AttributeDefinition} type.
	 */
	private static final Map<Class<?>, AttributeType<?>> fieldTypeToAtributeType = new HashMap<>();

	private static <T> void addFieldTypeToAttributeType(Class<T> fieldType, String attributeType,
			AttributeGetter<T> getter, AttributeSetter<T> setter) {
		fieldTypeToAtributeType.put(fieldType, new AttributeType<>(attributeType, (item, attributeName) -> {
			return item.isNull(attributeName) ? null : getter.get(item, attributeName);
		}, (item, attributeName, value) -> {
			if (value == null) {
				item.withNull(attributeName);
			} else {
				setter.set(item, attributeName, value);
			}
		}));
	}

	private static <T> void addFieldTypeToAttributeType(Class<T> fieldType, Class<T> boxedFieldType,
			String attributeType, AttributeGetter<T> getter, AttributeSetter<T> setter) {
		addFieldTypeToAttributeType(fieldType, attributeType, getter, setter);
		addFieldTypeToAttributeType(boxedFieldType, attributeType, getter, setter);
	}

	static {
		// Numbers
		String numberType = ScalarAttributeType.N.name();
		addFieldTypeToAttributeType(boolean.class, Boolean.class, numberType, Item::getBoolean, Item::withBoolean);
		addFieldTypeToAttributeType(byte.class, Byte.class, numberType, (item, attributeName) -> {
			byte[] value = item.getBinary(attributeName);
			return value != null ? value[0] : null;
		}, (item, attributeName, value) -> {
			item.withBinary(attributeName, new byte[] { value });
		});
		addFieldTypeToAttributeType(short.class, Short.class, numberType, Item::getShort, Item::withShort);
		addFieldTypeToAttributeType(int.class, Integer.class, numberType, Item::getInt, Item::withInt);
		addFieldTypeToAttributeType(long.class, Long.class, numberType, Item::getLong, Item::withLong);
		addFieldTypeToAttributeType(float.class, Float.class, numberType, Item::getFloat, Item::withFloat);
		addFieldTypeToAttributeType(double.class, Double.class, numberType, Item::getDouble, Item::withDouble);

		// Strings
		String stringType = ScalarAttributeType.S.name();
		addFieldTypeToAttributeType(char.class, Character.class, stringType,
				(item, attributeName) -> item.getString(attributeName).charAt(0),
				(item, attributeName, value) -> item.withString(attributeName, new String(new char[] { value })));
		addFieldTypeToAttributeType(String.class, stringType, Item::getString, Item::withString);
	}

	@FunctionalInterface
	static interface AttributeGetter<T> {
		T get(Item item, String attributeName);
	}

	@FunctionalInterface
	static interface AttributeSetter<T> {
		void set(Item item, String attributeName, T value);
	}

	static class AttributeType<T> {

		final String attributeType;

		final AttributeGetter<T> getter;

		final AttributeSetter<T> setter;

		private AttributeType(String attributeType, AttributeGetter<T> getter, AttributeSetter<T> setter) {
			this.attributeType = attributeType;
			this.getter = getter;
			this.setter = setter;
		}
	}

	static class Attribute<T> {

		final Field field;

		final AttributeType<T> attributeType;

		private Attribute(Field field, AttributeType<T> attributeType) {
			this.field = field;
			this.attributeType = attributeType;
		}
	}

	/**
	 * {@link DynamoDB}.
	 */
	final DynamoDB dynamoDb;

	/**
	 * Name of table for {@link Document}.
	 */
	final String tableName;

	/**
	 * {@link Attribute} for the {@link DocumentKey}.
	 */
	final Attribute<String> keyAttribute;

	/**
	 * {@link Attribute} instances for {@link Document}.
	 */
	final Attribute<?>[] attributes;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Document type.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	@SuppressWarnings("unchecked")
	DynamoOfficeCabinetMetaData(Class<D> documentType, DynamoDB dynamoDb) throws Exception {
		super(documentType);
		this.dynamoDb = dynamoDb;

		// Obtain the table name
		this.tableName = CabinetUtil.getDocumentName(documentType);

		// Include the key
		List<AttributeDefinition> attributeDefinitions = new LinkedList<>();
		List<KeySchemaElement> keys = new LinkedList<>();
		keys.add(new KeySchemaElement(this.documentKey.getKeyName(), KeyType.HASH));
		attributeDefinitions.add(new AttributeDefinition(this.documentKey.getKeyName(), ScalarAttributeType.S.name()));

		// Load the attributes
		Attribute<String>[] keyAttribute = new Attribute[] { null };
		List<Attribute<?>> attributes = new ArrayList<>();
		CabinetUtil.processFields(this.documentType, (context) -> {
			Field field = context.getField();

			// Ensure accessible
			field.setAccessible(true);

			// Determine the attribute type
			Class<?> attributeClass = field.getType();
			AttributeType<?> attributeType = fieldTypeToAtributeType.get(attributeClass);
			if (attributeType == null) {
				// TODO load as embedded type
				throw new UnsupportedOperationException(
						"TODO implement embedded for " + field.getName() + " of type " + attributeClass.getName());
			}

			// Create the attribute
			Attribute<?> attribute = new Attribute<>(field, attributeType);

			// Load the attribute
			if (context.isKey()) {
				keyAttribute[0] = (Attribute<String>) attribute;
			} else {
				attributes.add(attribute);
			}
		});
		this.keyAttribute = keyAttribute[0];
		this.attributes = attributes.toArray(Attribute[]::new);

		try {
			// Determine if table exists
			this.dynamoDb.getTable(this.tableName).describe();

		} catch (ResourceNotFoundException ex) {
			// Table not exists

			// Load provisioned through put
			// TODO configure read/write provisioned throughput
			ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(25L, 25L);

			// Create table request
			CreateTableRequest createTable = new CreateTableRequest(attributeDefinitions, this.tableName, keys,
					provisionedThroughput);

			// Create the table
			Table table = this.dynamoDb.createTable(createTable);

			// TODO allow concurrent table creation
			table.waitForActive();
		}
	}

}