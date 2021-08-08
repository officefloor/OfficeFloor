/*-
 * #%L
 * OfficeFloor Filing Cabinet for Dynamo DB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.cabinet.dynamo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.test.UsesDockerTest;

/**
 * Dynamo DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * Mapping of {@link Field} type to {@link AttributeDefinition} type.
	 */
	private static final Map<Class<?>, AttributeType<?>> fieldTypeToAtributeType = new HashMap<>();

	static {
		// Numbers
		String numberType = ScalarAttributeType.N.name();
		fieldTypeToAtributeType.put(boolean.class,
				new AttributeType<>(numberType, Item::getBoolean, Item::withBoolean));
		fieldTypeToAtributeType.put(byte.class,
				new AttributeType<>(numberType, (item, attributeName) -> item.getBinary(attributeName)[0],
						(item, attributeName, value) -> item.withBinary(attributeName, new byte[] { value })));
		fieldTypeToAtributeType.put(short.class, new AttributeType<>(numberType, Item::getShort, Item::withShort));
		fieldTypeToAtributeType.put(int.class, new AttributeType<>(numberType, Item::getInt, Item::withInt));
		fieldTypeToAtributeType.put(long.class, new AttributeType<>(numberType, Item::getLong, Item::withLong));
		fieldTypeToAtributeType.put(float.class, new AttributeType<>(numberType, Item::getFloat, Item::withFloat));
		fieldTypeToAtributeType.put(double.class, new AttributeType<>(numberType, Item::getDouble, Item::withDouble));

		// Strings
		String stringType = ScalarAttributeType.S.name();
		fieldTypeToAtributeType.put(char.class, new AttributeType<>(stringType,
				(item, attributeName) -> item.getString(attributeName).charAt(0),
				(item, attributeName, value) -> item.withString(attributeName, new String(new char[] { value }))));
		fieldTypeToAtributeType.put(String.class, new AttributeType<>(stringType, Item::getString, Item::withString));
	}

	@FunctionalInterface
	private static interface AttributeGetter<T> {
		T get(Item item, String attributeName);
	}

	@FunctionalInterface
	private static interface AttributeSetter<T> {
		void set(Item item, String attributeName, T value);
	}

	private static class AttributeType<T> {

		private final String attributeType;

		private final AttributeGetter<T> getter;

		private final AttributeSetter<T> setter;

		private AttributeType(String attributeType, AttributeGetter<T> getter, AttributeSetter<T> setter) {
			this.attributeType = attributeType;
			this.getter = getter;
			this.setter = setter;
		}
	}

	private static class Attribute<T> {

		private final Field field;

		private final AttributeType<T> attributeType;

		private Attribute(Field field, AttributeType<T> attributeType) {
			this.field = field;
			this.attributeType = attributeType;
		}
	}

	private static class ItemMetaData<D> {

		private final Class<D> documentType;

		private final String tableName;

		private final Attribute<String> key;

		private final List<Attribute<?>> attributes;

		private ItemMetaData(Class<D> documentType, String tableName, Attribute<String> key,
				List<Attribute<?>> attributes) {
			this.documentType = documentType;
			this.tableName = tableName;
			this.key = key;
			this.attributes = attributes;
		}
	}

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * {@link ItemMetaData}.
	 */
	private final ItemMetaData<D> itemMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Document type.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	@SuppressWarnings("unchecked")
	public DynamoOfficeCabinet(Class<D> documentType, DynamoDB dynamoDb) throws Exception {
		this.dynamoDb = dynamoDb;

		// Obtain the table name
		String tableName = CabinetUtil.getDocumentName(documentType);

		// Load the attributes and keys
		List<AttributeDefinition> attributeDefinitions = new LinkedList<>();
		List<KeySchemaElement> keys = new LinkedList<>();
		Attribute<String> keyAttribute = null;
		List<Attribute<?>> attributes = new ArrayList<>();
		Class<?> interrogate = documentType;
		do {

			// Load the attributes
			for (Field field : interrogate.getDeclaredFields()) {

				// Ensure accessible
				field.setAccessible(true);

				// Obtain the attribute name
				String attributeName = field.getName();

				// Determine the type
				Class<?> attributeClass = field.getType();
				AttributeType<?> attributeType = fieldTypeToAtributeType.get(attributeClass);
				if (attributeType == null) {
					// TODO load as embedded type
					throw new UnsupportedOperationException(
							"TODO implement embedded for " + attributeName + " of type " + attributeClass.getName());
				}

				// Create the attribute
				Attribute<?> attribute = new Attribute<>(field, attributeType);

				// Determine if key
				Key key = field.getAnnotation(Key.class);
				if (key != null) {

					// Include the key
					keys.add(new KeySchemaElement(attributeName, KeyType.HASH));
					attributeDefinitions.add(new AttributeDefinition(attributeName, attributeType.attributeType));

					// Capture the key attribute
					keyAttribute = (Attribute<String>) attribute;

				} else {
					// Include attribute
					attributes.add(attribute);
				}
			}

			// Interrogate parent
			interrogate = interrogate.getSuperclass();
		} while (interrogate != null);

		// Create the item meta-data
		this.itemMetaData = new ItemMetaData<>(documentType, tableName, keyAttribute, attributes);

		// Load provisioned through put
		// TODO configure read/write provisioned throughput
		ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(25L, 25L);

		// Create table request
		CreateTableRequest createTable = new CreateTableRequest(attributeDefinitions, tableName, keys,
				provisionedThroughput);

		// Create the table
		Table table = this.dynamoDb.createTable(createTable);

		// TODO allow concurrent table creation
		table.waitForActive();
	}

	/*
	 * =================== OfficeCabinet ======================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	public Optional<D> retrieveByKey(String key) {

		// Create the table key
		TableKeysAndAttributes tableKey = new TableKeysAndAttributes(this.itemMetaData.tableName)
				.addHashOnlyPrimaryKey(this.itemMetaData.key.field.getName(), key);

		// Retrieve the item
		BatchGetItemSpec get = new BatchGetItemSpec().withTableKeyAndAttributes(tableKey);
		List<Item> items = this.dynamoDb.batchGetItem(get).getTableItems().get(this.itemMetaData.tableName);
		if (items.size() == 0) {
			return Optional.empty();
		}

		// Obtain the item
		Item item = items.get(0);

		// Create the document
		try {
			D document = this.itemMetaData.documentType.getConstructor().newInstance();

			// Load key
			this.itemMetaData.key.field.set(document,
					this.itemMetaData.key.attributeType.getter.get(item, this.itemMetaData.key.field.getName()));

			// Load the attributes
			for (Attribute attribute : this.itemMetaData.attributes) {
				attribute.field.set(document, attribute.attributeType.getter.get(item, attribute.field.getName()));
			}

			// Return the document
			return Optional.of(document);

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to retrieve document " + this.itemMetaData.documentType.getName(),
					ex);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void store(D document) {

		// Setup the write
		BatchWriteItemSpec write = new BatchWriteItemSpec();
		try {

			// Determine if have key
			String key = (String) this.itemMetaData.key.field.get(document);
			if (key == null) {

				// Generate key
				key = CabinetUtil.newKey();

				// Load key back onto entity
				this.itemMetaData.key.field.set(document, key);
			}

			// Create the item
			Item item = new Item();
			item.withPrimaryKey(this.itemMetaData.key.field.getName(), key);

			// Load the attributes
			for (Attribute attribute : this.itemMetaData.attributes) {
				attribute.attributeType.setter.set(item, attribute.field.getName(), attribute.field.get(document));
			}

			System.out.println("ITEM: " + item);

			// Write item to table
			TableWriteItems items = new TableWriteItems(this.itemMetaData.tableName);
			items.addItemToPut(item);

			// Write data
			write.withTableWriteItems(items);

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Write the data
		this.dynamoDb.batchWriteItem(write);
	}

}
