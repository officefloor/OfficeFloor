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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.document.spec.BatchWriteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.InternalDocumentBundle;
import net.officefloor.cabinet.common.NextDocumentBundleContext;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Range.Direction;
import net.officefloor.test.UsesDockerTest;

/**
 * Dynamo DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoOfficeCabinet<D> extends AbstractOfficeCabinet<Item, Item, D, DynamoDocumentMetaData<D>> {

	/**
	 * Undertakes the {@link Query}.
	 * 
	 * @param query                   {@link Query}.
	 * @param range                   {@link InternalDocumentBundle}.
	 * @param nextDocumentBundleToken Next {@link DocumentBundle} token.
	 * @return {@link InternalDocumentBundle}.
	 */
	private InternalDocumentBundle<Item> doQuery(Query query, InternalRange range,
			String nextDocumentBundleToken) {

		// TODO handle more than one field
		String partitionFieldName = query.getFields()[0].fieldName;
		String indexName = partitionFieldName;
		String keyCondition = partitionFieldName + " = :" + partitionFieldName;
		Map<String, Object> valueMap = new HashMap<>();
		valueMap.put(":" + partitionFieldName, query.getFields()[0].fieldValue);
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression(keyCondition).withValueMap(valueMap);

		// Handle range
		String sortFieldName = range != null ? range.getFieldName() : null;
		if (sortFieldName != null) {
			indexName += "-" + sortFieldName;
			querySpec = querySpec.withScanIndexForward(range.getDirection() == Direction.Ascending);
		}

		// Obtain the index
		Index index = this.metaData.dynamoDb.getTable(this.metaData.tableName).getIndex(indexName);

		// Query for the items
		ItemCollection<QueryOutcome> outcomes = index.query(querySpec);

		// Return the items
		return new DynamoDocumentBundle(outcomes.iterator(), query, range);
	}

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
	protected InternalDocumentBundle<Item> retrieveInternalDocuments(Query query, InternalRange range) {
		String nextDocumentBundleToken = range != null ? range.getNextDocumentBundleToken() : null;
		return doQuery(query, range, nextDocumentBundleToken);
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

	/**
	 * {@link DynamoDB} {@link InternalDocumentBundle}.
	 */
	private class DynamoDocumentBundle implements InternalDocumentBundle<Item> {

		/**
		 * {@link Iterator} over the {@link Item} instances for this
		 * {@link InternalDocumentBundle}.
		 */
		private final Iterator<Item> iterator;

		/**
		 * {@link Query}.
		 */
		private final Query query;

		/**
		 * {@link InternalRange}.
		 */
		private final InternalRange range;

		/**
		 * Instantiate.
		 * 
		 * @param iterator {@link Iterator} over the {@link Item} instances for this
		 *                 {@link InternalDocumentBundle}.
		 * @param query    {@link Query}.
		 * @param range    {@link InternalRange}.
		 */
		private DynamoDocumentBundle(Iterator<Item> iterator, Query query, InternalRange range) {
			this.iterator = iterator;
			this.query = query;
			this.range = range;
		}

		/*
		 * ======================= InternalDocumentBundle ==============================
		 */

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Item next() {
			return this.iterator.next();
		}

		@Override
		public InternalDocumentBundle<Item> nextDocumentBundle(NextDocumentBundleContext context) {
			String nextDocumentBundleToken = context.getNextDocumentBundleToken();
			return DynamoOfficeCabinet.this.doQuery(this.query, this.range, nextDocumentBundleToken);
		}

		@Override
		public String getNextDocumentBundleToken() {
			// TODO implement InternalDocumentBundle<Item>.getNextDocumentBundleToken
			throw new UnsupportedOperationException(
					"TODO implement InternalDocumentBundle<Item>.getNextDocumentBundleToken");
		}
	}

}
