/*-
 * #%L
 * OfficeFloor Filing Cabinet for Cosmos DB
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

package net.officefloor.cabinet.cosmosdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.InternalDocumentBundle;
import net.officefloor.cabinet.common.NextDocumentBundleContext;
import net.officefloor.cabinet.common.NextDocumentBundleTokenContext;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Cosmos DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinet<D>
		extends AbstractOfficeCabinet<InternalObjectNode, InternalObjectNode, D, CosmosDocumentMetaData<D>> {

	/**
	 * {@link CosmosItemRequestOptions} to retrieve/store {@link Document}
	 * instances.
	 */
	private static CosmosItemRequestOptions ITEM_OPTIONS = new CosmosItemRequestOptions();

	/**
	 * {@link CosmosQueryRequestOptions} to retrieve {@link Document} instances.
	 */
	private static CosmosQueryRequestOptions QUERY_OPTIONS = new CosmosQueryRequestOptions()
			.setFeedRange(FeedRange.forFullRange());

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link CosmosDocumentMetaData}.
	 */
	public CosmosOfficeCabinet(CosmosDocumentMetaData<D> metaData) {
		super(metaData, false);
	}

	/**
	 * Undertakes the {@link Query}.
	 * 
	 * @param query                   {@link Query}.
	 * @param range                   {@link InternalDocumentBundle}.
	 * @param nextDocumentBundleToken Next {@link DocumentBundle} token.
	 * @return {@link InternalDocumentBundle}.
	 */
	private InternalDocumentBundle<InternalObjectNode> doQuery(Query query, InternalRange range,
			String nextDocumentBundleToken) {

		// TODO handle more than one field
		QueryField field = query.getFields()[0];

		// Create the query for the index
		List<SqlParameter> parameters = new ArrayList<>();
		StringBuffer queryText = new StringBuffer();
		queryText.append("SELECT * FROM " + this.metaData.container.getId() + " c WHERE");

		// Add the field
		queryText.append(" c." + field.fieldName + " = @" + field.fieldName);
		parameters.add(new SqlParameter("@" + field.fieldName, field.fieldValue));

		// Provide possible ordering
		if (range != null) {
			queryText.append(" ORDER BY c." + range.getFieldName() + " "
					+ (range.getDirection() == Direction.Ascending ? "ASC" : "DESC"));
		}

		// Create the query
		SqlQuerySpec querySpec = new SqlQuerySpec(queryText.toString(), parameters);

		// Query for items
		CosmosPagedIterable<InternalObjectNode> items = this.metaData.container.queryItems(querySpec, QUERY_OPTIONS,
				InternalObjectNode.class);

		// Obtain results
		String continuationToken;
		int limit = range != null ? range.getLimit() : -1;
		Iterator<InternalObjectNode> results;
		if (limit > 0) {
			// Provide page limit and possible token for next offset
			Iterator<FeedResponse<InternalObjectNode>> pages = nextDocumentBundleToken != null
					? items.iterableByPage(nextDocumentBundleToken, limit).iterator()
					: items.iterableByPage(limit).iterator();
			if (pages.hasNext()) {

				// Obtain the page details
				FeedResponse<InternalObjectNode> feedResponse = pages.next();
				results = feedResponse.getResults().iterator();

				// Obtain the continuation token
				continuationToken = feedResponse.getContinuationToken();

				// Ensure have token
//				if (continuationToken != null) {
//					try {
//						JsonNode continuationTokenJson = MAPPER.readTree(continuationToken);
//						String compositeTokenText = continuationTokenJson.get("compositeToken").asText();
//						boolean isToken = !(MAPPER.readTree(compositeTokenText).get("token").isNull());
//						if (!isToken) {
//							// No continuation token
//							continuationToken = null;
//						}
//
//					} catch (Exception ex) {
//						// Ignore, as only extra check
//					}
//				}

			} else {
				// No results
				results = Collections.emptyIterator();
				continuationToken = null;
			}
		} else {
			// All results
			results = items.iterator();
			continuationToken = null;
		}

		// Ensure have results
		if (!results.hasNext()) {
			return null; // no further results
		}

		// Return bundle over the items
		return new CosmosDocumentBundle(results, query, range, continuationToken);
	}

	/*
	 * ==================== AbstractOfficeCabinet ======================
	 */

	@Override
	protected InternalObjectNode retrieveInternalDocument(String key) {
		CosmosItemResponse<InternalObjectNode> response = this.metaData.container.readItem(key, new PartitionKey(key),
				ITEM_OPTIONS, InternalObjectNode.class);
		return response.getItem();
	}

	@Override
	protected InternalDocumentBundle<InternalObjectNode> retrieveInternalDocuments(Query query, InternalRange range) {
		String nextDocumentBundleToken = range != null ? range.getNextDocumentBundleToken() : null;
		return this.doQuery(query, range, nextDocumentBundleToken);
	}

	@Override
	protected void storeInternalDocument(InternalDocument<InternalObjectNode> internalDocument) {
		InternalObjectNode internalObjectNode = internalDocument.getInternalDocument();
		if (internalDocument.isNew()) {
			this.metaData.container.createItem(internalObjectNode, new PartitionKey(internalObjectNode.getId()),
					ITEM_OPTIONS);
		} else {
			String key = internalDocument.getKey();
			this.metaData.container.replaceItem(internalObjectNode, key, new PartitionKey(key), ITEM_OPTIONS);
		}
	}

	/**
	 * Cosmos {@link InternalDocumentBundle}.
	 */
	private class CosmosDocumentBundle implements InternalDocumentBundle<InternalObjectNode> {

		/**
		 * {@link Iterator} over {@link InternalObjectNode} instances for this
		 * {@link InternalDocumentBundle}.
		 */
		private final Iterator<InternalObjectNode> iterator;

		/**
		 * {@link Query}.
		 */
		private final Query query;

		/**
		 * {@link InternalRange}.
		 */
		private final InternalRange range;

		/**
		 * Next {@link DocumentBundle} token. May be <code>null</code>.
		 */
		private final String nextDocumentBundleToken;

		/**
		 * Instantiate.
		 * 
		 * @param iterator                {@link Iterator} over
		 *                                {@link InternalObjectNode} instances for this
		 *                                {@link InternalDocumentBundle}.
		 * @param query                   {@link Query}.
		 * @param range                   {@link InternalRange}.
		 * @param nextDocumentBundleToken Next {@link DocumentBundle} token.
		 */
		private CosmosDocumentBundle(Iterator<InternalObjectNode> iterator, Query query, InternalRange range,
				String nextDocumentBundleToken) {
			this.iterator = iterator;
			this.query = query;
			this.range = range;
			this.nextDocumentBundleToken = nextDocumentBundleToken;
		}

		/*
		 * ======================= InternalDocumentBundle ==============================
		 */

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public InternalObjectNode next() {
			return this.iterator.next();
		}

		@Override
		public InternalDocumentBundle<InternalObjectNode> nextDocumentBundle(NextDocumentBundleContext context) {

			// Ensure have next document bundle token
			String nextDocumentBundleToken = context.getNextDocumentBundleToken();
			if (nextDocumentBundleToken == null) {
				return null; // no further pages
			}

			// Query for further pages
			return CosmosOfficeCabinet.this.doQuery(this.query, this.range, nextDocumentBundleToken);
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<InternalObjectNode> context) {
			return this.nextDocumentBundleToken;
		}
	}

}
