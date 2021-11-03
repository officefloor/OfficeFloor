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

import java.util.Iterator;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;
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
		super(metaData);
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
	protected Iterator<InternalObjectNode> retrieveInternalDocuments(Query index, Range<D> range) {

		// Create the query for the index
		QueryField field = index.getFields()[0];
		String queryText = "SELECT * FROM " + this.metaData.container.getId() + " c WHERE c." + field.fieldName + " = @"
				+ field.fieldName;
		if (range != null) {
			queryText += " ORDER BY c." + range.getFieldName() + " "
					+ (range.getDirection() == Direction.Ascending ? "ASC" : "DESC");
		}
		SqlQuerySpec query = new SqlQuerySpec(queryText, new SqlParameter("@" + field.fieldName, field.fieldValue));

		// Query for items
		CosmosPagedIterable<InternalObjectNode> items = this.metaData.container.queryItems(query, QUERY_OPTIONS,
				InternalObjectNode.class);

		// Return iteration over the items
		return items.iterator();
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

}
