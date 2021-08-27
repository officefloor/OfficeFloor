package net.officefloor.cabinet.dynamo;

import java.util.LinkedList;
import java.util.List;

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
import net.officefloor.cabinet.common.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Meta-data for the {@link DynamoOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDocumentMetaData<D> extends AbstractDocumentMetaData<Item, Item, D> {

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

	/**
	 * {@link DynamoDB}.
	 */
	final DynamoDB dynamoDb;

	/**
	 * Name of table for {@link Document}.
	 */
	final String tableName;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Document type.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	DynamoDocumentMetaData(Class<D> documentType, DynamoDB dynamoDb) throws Exception {
		super(documentType);
		this.dynamoDb = dynamoDb;

		// Obtain the table name
		this.tableName = CabinetUtil.getDocumentName(documentType);

		// Include the key
		List<AttributeDefinition> attributeDefinitions = new LinkedList<>();
		List<KeySchemaElement> keys = new LinkedList<>();
		keys.add(new KeySchemaElement(this.getKeyName(), KeyType.HASH));
		attributeDefinitions.add(new AttributeDefinition(this.getKeyName(), ScalarAttributeType.S.name()));

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

	@Override
	protected void initialise(Initialise<Item, Item> init) throws Exception {

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