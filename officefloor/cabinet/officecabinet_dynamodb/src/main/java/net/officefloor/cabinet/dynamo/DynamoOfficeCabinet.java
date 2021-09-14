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

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.test.UsesDockerTest;

/**
 * Dynamo DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoOfficeCabinet<D> extends AbstractOfficeCabinet<Item, Item, D, DynamoDocumentMetaData<D>> {

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link DynamoDocumentMetaData}.
	 */
	public DynamoOfficeCabinet(DynamoDocumentMetaData<D> metaData) {
		super(metaData);
	}

	/*
	 * =================== AbstractOfficeCabinet ======================
	 */

	@Override
	protected Item retrieveInternalDocument(String key) {

		// Create the table key
		TableKeysAndAttributes tableKey = new TableKeysAndAttributes(this.metaData.tableName)
				.addHashOnlyPrimaryKey(this.metaData.getKeyName(), key);

		// Retrieve the item
		BatchGetItemSpec get = new BatchGetItemSpec().withTableKeyAndAttributes(tableKey);
		List<Item> items = this.metaData.dynamoDb.batchGetItem(get).getTableItems().get(this.metaData.tableName);
		if (items.size() == 0) {
			return null;
		}

		// Return the item
		return items.get(0);
	}

	@Override
	protected void storeInternalDocument(InternalDocument<Item> internalDocument) {

		// Write item to table
		TableWriteItems items = new TableWriteItems(this.metaData.tableName);
		items.addItemToPut(internalDocument.getInternalDocument());

		// Write data
		BatchWriteItemSpec write = new BatchWriteItemSpec();
		write.withTableWriteItems(items);

		// Write the data
		this.metaData.dynamoDb.batchWriteItem(write);
	}

}
