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

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.InternalDocument;

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
	private static CosmosItemRequestOptions OPTIONS = new CosmosItemRequestOptions()
			.setConsistencyLevel(ConsistencyLevel.STRONG);

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
				OPTIONS, InternalObjectNode.class);
		return response.getItem();
	}

	@Override
	protected void storeInternalDocument(InternalDocument<InternalObjectNode> internalDocument) {
		InternalObjectNode internalObjectNode = internalDocument.getInternalDocument();
		if (internalDocument.isNew()) {
			this.metaData.container.createItem(internalObjectNode, OPTIONS);
		} else {
			String key = internalDocument.getKey();
			this.metaData.container.replaceItem(internalObjectNode, key, new PartitionKey(key), OPTIONS);
		}
	}

}
