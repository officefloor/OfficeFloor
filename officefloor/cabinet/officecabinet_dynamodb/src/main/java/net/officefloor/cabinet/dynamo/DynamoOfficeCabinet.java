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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.InternalDocumentBundle;
import net.officefloor.cabinet.common.NextDocumentBundleContext;
import net.officefloor.cabinet.common.NextDocumentBundleTokenContext;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.CabinetManager;
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
public class DynamoOfficeCabinet<D> extends
		AbstractOfficeCabinet<Item, Map<String, AttributeValue>, D, DynamoDocumentMetaData<D>, DynamoTransaction> {

	/**
	 * {@link DynamoDB}.
	 */
	private final DynamoDB dynamoDb;

	/**
	 * Maximum batch size for writing to {@link DynamoDB}.
	 */
	private final int maxBatchSize;

	/**
	 * Instantiate.
	 * 
	 * @param metaData       {@link DocumentMetaData}.
	 * @param cabinetManager {@link CabinetManager}.
	 * @param dynamoDb       {@link DynamoDB}.
	 * @param maxBatchSize   Maximum batch size for writing to {@link DynamoDB}.
	 */
	public DynamoOfficeCabinet(
			DocumentMetaData<Item, Map<String, AttributeValue>, D, DynamoDocumentMetaData<D>, DynamoTransaction> metaData,
			CabinetManager cabinetManager, DynamoDB dynamoDb, int maxBatchSize) {
		super(metaData, false, cabinetManager);
		this.dynamoDb = dynamoDb;
		this.maxBatchSize = maxBatchSize;
	}

	/**
	 * Undertakes the {@link Query}.
	 * 
	 * @param query            {@link Query}.
	 * @param range            {@link InternalDocumentBundle}.
	 * @param lastEvaluatedKey Last evaluated key of previous page. May be
	 *                         <code>null</code>.
	 * @return {@link InternalDocumentBundle}.
	 */
	private InternalDocumentBundle<Item> doQuery(Query query, InternalRange range,
			Map<String, AttributeValue> lastEvaluatedKey) {

		// TODO handle more than one field
		String partitionFieldName = query.getFields()[0].fieldName;
		String indexName = partitionFieldName;
		String keyCondition = partitionFieldName + " = :" + partitionFieldName;
		Map<String, Object> valueMap = new HashMap<>();
		valueMap.put(":" + partitionFieldName, query.getFields()[0].fieldValue);
		QuerySpec querySpec = new QuerySpec().withKeyConditionExpression(keyCondition).withValueMap(valueMap);

		// Handle range
		if (range != null) {

			// Handle possible sort
			String sortFieldName = range.getFieldName();
			if (sortFieldName != null) {
				indexName += "-" + sortFieldName;
				querySpec = querySpec.withScanIndexForward(range.getDirection() == Direction.Ascending);
			}

			// Handle paging
			int limit = range.getLimit();
			if (limit > 0) {
				querySpec = querySpec.withMaxPageSize(limit);
			}
		}

		// Provide starting point for possible next page
		if (lastEvaluatedKey != null) {
			PrimaryKey primaryKey = new PrimaryKey();
			for (String keyName : lastEvaluatedKey.keySet()) {
				AttributeValue keyValue = lastEvaluatedKey.get(keyName);
				Object keySimpleValue = ItemUtils.toSimpleValue(keyValue);
				primaryKey.addComponent(keyName, keySimpleValue);
			}
			querySpec = querySpec.withExclusiveStartKey(primaryKey);
		}

		// Obtain the index
		Index index = this.dynamoDb.getTable(this.metaData.extra.tableName).getIndex(indexName);

		// Query for the items
		ItemCollection<QueryOutcome> outcomes = index.query(querySpec);

		// Return the items (if items)
		Page<Item, QueryOutcome> page = outcomes.firstPage();
		boolean isDocuments = page.iterator().hasNext();
		return isDocuments ? new DynamoDocumentBundle(outcomes, query, range) : null;
	}

	/*
	 * =================== AbstractOfficeCabinet ======================
	 */

	@Override
	protected Item retrieveInternalDocument(String key) {

		// Obtain the item
		Item item = this.dynamoDb.getTable(this.metaData.extra.tableName)
				.getItem(new GetItemSpec().withPrimaryKey(this.metaData.getKeyName(), key));

		// Return the item
		return item;
	}

	@Override
	protected InternalDocumentBundle<Item> retrieveInternalDocuments(Query query, InternalRange range) {

		// Determine last evaluated key
		Map<String, AttributeValue> lastEvaluatedKey = null;
		Map<String, Object> nextTokenValues = range != null ? range.getTokenValues() : null;
		if (nextTokenValues != null) {

			// Translate to attribute values
			lastEvaluatedKey = new HashMap<>(nextTokenValues.size());
			for (Entry<String, Object> entry : nextTokenValues.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				AttributeValue attributeValue = ItemUtils.toAttributeValue(value);
				lastEvaluatedKey.put(key, attributeValue);
			}
		}

		// Retrieve the next bundle
		return doQuery(query, range, lastEvaluatedKey);
	}

	@Override
	public void storeInternalDocuments(List<InternalDocument<Map<String, AttributeValue>>> internalDocuments,
			DynamoTransaction transaction) {

		// Write items to transaction
		transaction.add(this.metaData.extra.tableName, internalDocuments);
	}

	/**
	 * {@link DynamoDB} {@link InternalDocumentBundle}.
	 */
	private class DynamoDocumentBundle implements InternalDocumentBundle<Item> {

		/**
		 * {@link ItemCollection} for this {@link InternalDocumentBundle}.
		 */
		private final ItemCollection<QueryOutcome> outcomes;

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
		 * @param outcomes {@link ItemCollection} for this
		 *                 {@link InternalDocumentBundle}.
		 * @param query    {@link Query}.
		 * @param range    {@link InternalRange}.
		 */
		private DynamoDocumentBundle(ItemCollection<QueryOutcome> outcomes, Query query, InternalRange range) {
			this.outcomes = outcomes;
			this.iterator = outcomes.iterator();
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

			// Obtain the last evaluated key
			Map<String, AttributeValue> lastEvaluatedKey = this.outcomes.getLastLowLevelResult().getQueryResult()
					.getLastEvaluatedKey();
			if (lastEvaluatedKey == null) {
				return null; // no token
			}

			// Obtain the next bundle
			return DynamoOfficeCabinet.this.doQuery(this.query, this.range, lastEvaluatedKey);
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<Item> context) {
			return context.getLastInternalDocumentToken();
		}
	}

}
