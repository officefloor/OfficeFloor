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

import java.util.List;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.dynamo.DynamoOfficeCabinetMetaData.Attribute;
import net.officefloor.test.UsesDockerTest;

/**
 * Dynamo DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoOfficeCabinet<D> extends AbstractOfficeCabinet<D, DynamoOfficeCabinetMetaData<D>> {

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link DynamoOfficeCabinetMetaData}.
	 */
	public DynamoOfficeCabinet(DynamoOfficeCabinetMetaData<D> metaData) {
		super(metaData);
	}

	/*
	 * =================== AbstractOfficeCabinet ======================
	 */

	@Override
	@SuppressWarnings("rawtypes")
	protected D _retrieveByKey(String key) {

		// Create the table key
		TableKeysAndAttributes tableKey = new TableKeysAndAttributes(this.metaData.tableName)
				.addHashOnlyPrimaryKey(this.metaData.documentKey.getKeyName(), key);

		// Retrieve the item
		BatchGetItemSpec get = new BatchGetItemSpec().withTableKeyAndAttributes(tableKey);
		List<Item> items = this.metaData.dynamoDb.batchGetItem(get).getTableItems().get(this.metaData.tableName);
		if (items.size() == 0) {
			return null;
		}

		// Obtain the item
		Item item = items.get(0);

		// Create the document
		try {
			D document = this.createManagedDocument();

			// Load key
			String retrievedKey = this.metaData.keyAttribute.attributeType.getter.get(item,
					this.metaData.documentKey.getKeyName());
			this.metaData.documentKey.setKey(document, retrievedKey);

			// Load the attributes
			for (Attribute attribute : this.metaData.attributes) {
				attribute.field.set(document, attribute.attributeType.getter.get(item, attribute.field.getName()));
			}

			// Return the document
			return document;

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to retrieve document " + this.metaData.documentType.getName()
					+ " by " + this.metaData.documentKey.getKeyName() + " ("
					+ this.metaData.keyAttribute.attributeType.attributeType + ")", ex);
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String _store(D document) {

		// Setup the write
		String key;
		BatchWriteItemSpec write = new BatchWriteItemSpec();
		try {

			// Determine if have key
			key = (String) this.metaData.documentKey.getKey(document);
			if (key == null) {

				// Generate key
				key = CabinetUtil.newKey();
				this.metaData.documentKey.setKey(document, key);
			}

			// Create the item
			Item item = new Item();
			item.withPrimaryKey(this.metaData.documentKey.getKeyName(), key);

			// Load the attributes
			for (Attribute attribute : this.metaData.attributes) {
				attribute.attributeType.setter.set(item, attribute.field.getName(), attribute.field.get(document));
			}

			// Write item to table
			TableWriteItems items = new TableWriteItems(this.metaData.tableName);
			items.addItemToPut(item);

			// Write data
			write.withTableWriteItems(items);

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Write the data
		this.metaData.dynamoDb.batchWriteItem(write);

		// Return the key
		return key;
	}

}
