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

import java.util.Optional;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Cosmos DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * {@link CosmosOfficeCabinetMetaData}.
	 */
	private final CosmosOfficeCabinetMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param metaData {@link CosmosOfficeCabinetMetaData}.
	 */
	public CosmosOfficeCabinet(CosmosOfficeCabinetMetaData<D> metaData) {
		this.metaData = metaData;
	}

	/*
	 * ==================== OfficeCabinet ======================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Obtain the document
		CosmosItemRequestOptions options = new CosmosItemRequestOptions().setConsistencyLevel(ConsistencyLevel.STRONG);
		CosmosItemResponse<D> response = this.metaData.container.readItem(key, new PartitionKey(key), options,
				this.metaData.documentType);

		// Return the document
		return Optional.of(response.getItem());
	}

	@Override
	public void store(D document) {

		// Obtain key and determine if new
		String key;
		boolean isNew = false;
		try {
			// Obtain the key for the document
			key = this.metaData.documentKey.getKey(document);
			if (key == null) {

				// Generate and load key
				key = CabinetUtil.newKey();
				this.metaData.documentKey.setKey(document, key);

				// Flag creating
				isNew = true;
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Save
		if (isNew) {
			this.metaData.container.createItem(document);
		} else {
			this.metaData.container.replaceItem(document, key, new PartitionKey(key), null);
		}
	}

}
