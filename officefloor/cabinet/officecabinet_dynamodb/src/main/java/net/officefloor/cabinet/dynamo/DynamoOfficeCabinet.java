/*-
 * #%L
 * OfficeFloor Filing Cabinet for Dynamo DB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.DocumentKey;
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

	private static <T> void addFielfdTypeToAttributeType(Class<T> fieldType, String attributeType,
			AttributeGetter<T> getter, AttributeSetter<T> setter) {
		fieldTypeToAtributeType.put(fieldType, new AttributeType<>(attributeType, getter, setter));
	}

	static {
		// Numbers
		String numberType = ScalarAttributeType.N.name();
		addFielfdTypeToAttributeType(boolean.class, numberType, Item::getBoolean, Item::withBoolean);
		addFielfdTypeToAttributeType(byte.class, numberType, (item, attributeName) -> item.getBinary(attributeName)[0],
				(item, attributeName, value) -> item.withBinary(attributeName, new byte[] { value }));
		addFielfdTypeToAttributeType(short.class, numberType, Item::getShort, Item::withShort);
		addFielfdTypeToAttributeType(int.class, numberType, Item::getInt, Item::withInt);
		addFielfdTypeToAttributeType(long.class, numberType, Item::getLong, Item::withLong);
		addFielfdTypeToAttributeType(float.class, numberType, Item::getFloat, Item::withFloat);
		addFielfdTypeToAttributeType(double.class, numberType, Item::getDouble, Item::withDouble);

		// Strings
		String stringType = ScalarAttributeType.S.name();
		addFielfdTypeToAttributeType(char.class, stringType,
				(item, attributeName) -> item.getString(attributeName).charAt(0),
				(item, attributeName, value) -> item.withString(attributeName, new String(new char[] { value })));
		addFielfdTypeToAttributeType(String.class, stringType, Item::getString, Item::withString);
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

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * {@link Document} type.
	 */
	private final Class<D> documentType;

	/**
	 * Name of table for {@link Document}.
	 */
	private final String tableName;

	/**
	 * {@link DocumentKey}.
	 */
	private final DocumentKey<D> documentKey;

	/**
	 * {@link Attribute} for the {@link DocumentKey}.
	 */
	private final Attribute<String> keyAttribute;

	/**
	 * {@link Attribute} instances for {@link Document}.
	 */
	private final Attribute<?>[] attributes;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Document type.
	 * @param dynamoDb     {@link DynamoDB}.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	@SuppressWarnings("unchecked")
	public DynamoOfficeCabinet(Class<D> documentType, DynamoDB dynamoDb) throws Exception {
		this.documentType = documentType;
		this.dynamoDb = dynamoDb;

		// Obtain the table name
		this.tableName = CabinetUtil.getDocumentName(documentType);

		// Obtain the document key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);

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
		TableKeysAndAttributes tableKey = new TableKeysAndAttributes(this.tableName)
				.addHashOnlyPrimaryKey(this.documentKey.getKeyName(), key);

		// Retrieve the item
		BatchGetItemSpec get = new BatchGetItemSpec().withTableKeyAndAttributes(tableKey);
		List<Item> items = this.dynamoDb.batchGetItem(get).getTableItems().get(this.tableName);
		if (items.size() == 0) {
			return Optional.empty();
		}

		// Obtain the item
		Item item = items.get(0);

		// Create the document
		try {
			D document = this.documentType.getConstructor().newInstance();

			// Load key
			String retrievedKey = this.keyAttribute.attributeType.getter.get(item, this.documentKey.getKeyName());
			this.documentKey.setKey(document, retrievedKey);

			// Load the attributes
			for (Attribute attribute : this.attributes) {
				attribute.field.set(document, attribute.attributeType.getter.get(item, attribute.field.getName()));
			}

			// Return the document
			return Optional.of(document);

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to retrieve document " + this.documentType.getName() + " by "
					+ this.documentKey.getKeyName() + " (" + this.keyAttribute.attributeType.attributeType + ")", ex);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void store(D document) {

		// Setup the write
		BatchWriteItemSpec write = new BatchWriteItemSpec();
		try {

			// Determine if have key
			String key = (String) this.documentKey.getKey(document);
			if (key == null) {

				// Generate key
				key = CabinetUtil.newKey();
				this.documentKey.setKey(document, key);
			}

			// Create the item
			Item item = new Item();
			item.withPrimaryKey(this.documentKey.getKeyName(), key);

			// Load the attributes
			for (Attribute attribute : this.attributes) {
				attribute.attributeType.setter.set(item, attribute.field.getName(), attribute.field.get(document));
			}

			// Write item to table
			TableWriteItems items = new TableWriteItems(this.tableName);
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
