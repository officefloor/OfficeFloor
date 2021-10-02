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

import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.Query;
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

		// Obtain the item
		Item item = this.metaData.dynamoDb.getTable(this.metaData.tableName)
				.getItem(new GetItemSpec().withPrimaryKey(this.metaData.getKeyName(), key));

		// Return the item
		return item;
	}

	@Override
	protected Iterator<Item> retrieveInternalDocuments(Query index) {

		// Query for the items
		ItemCollection<QueryOutcome> outcomes = this.metaData.dynamoDb.getTable(this.metaData.tableName)
				.query(new QuerySpec());
		
		// Return the items
		return outcomes.iterator();
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
