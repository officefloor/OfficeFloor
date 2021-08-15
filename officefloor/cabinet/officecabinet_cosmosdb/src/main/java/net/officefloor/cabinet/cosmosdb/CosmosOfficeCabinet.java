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
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.cosmosdb.CosmosOfficeCabinetMetaData.Property;

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
	@SuppressWarnings("rawtypes")
	public Optional<D> retrieveByKey(String key) {

		// Obtain the document
		CosmosItemRequestOptions options = new CosmosItemRequestOptions().setConsistencyLevel(ConsistencyLevel.STRONG);
		CosmosItemResponse<InternalObjectNode> response = this.metaData.container.readItem(key, new PartitionKey(key), options,
				InternalObjectNode.class);
		InternalObjectNode document = response.getItem();
		
		// Transform to typed document
		D doc;
		try {

			// Create the typed document
			doc = this.metaData.documentType.getConstructor().newInstance();

			// Load the typed values
			for (Property property : this.metaData.properties) {
				String propertyName = property.field.getName();
				Object value = property.propertyType.getter.get(document, propertyName);
				property.field.set(doc, value);
			}

		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to hydrate into typed document " + this.metaData.documentType.getName(), ex);
		}

		// Return the typed document
		return Optional.of(doc);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

		// Create the document to store
		InternalObjectNode cosmosDocument = new InternalObjectNode();
		cosmosDocument.setId(key);

		// Load the properties
		try {
			for (Property property : this.metaData.properties) {
				String propertyName = property.field.getName();
				Object propertyValue = property.field.get(document);
				property.propertyType.setter.set(cosmosDocument, propertyName, propertyValue);
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failure transforming document data for storage " + document.getClass().getName(), ex);
		}

		// Save
		if (isNew) {
			this.metaData.container.createItem(cosmosDocument);
		} else {
			this.metaData.container.replaceItem(cosmosDocument, key, new PartitionKey(key), null);
		}
	}

}
