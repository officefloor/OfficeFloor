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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Cosmos DB {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * {@link CosmosContainer}.
	 */
	private final CosmosContainer container;

	/**
	 * {@link Document} type.
	 */
	private final Class<D> documentType;

	/**
	 * {@link Field} containing the {@link Key}.
	 */
	private final Field keyField;

	/**
	 * Instantiate.
	 * 
	 * @param documentType   Document type.
	 * @param cosmosDatabase {@link CosmosDatabase}.
	 * @throws Exception If fails to instantiate {@link OfficeCabinet}.
	 */
	public CosmosOfficeCabinet(Class<D> documentType, CosmosDatabase cosmosDatabase) throws Exception {
		this.documentType = documentType;

		// Obtain the container id
		String containerId = CabinetUtil.getDocumentName(documentType);

		// Search out the key
		Field keyField = null;
		Class<?> interrogate = documentType;
		do {

			// Load the attributes
			for (Field field : interrogate.getDeclaredFields()) {

				// Determine if key
				Key key = field.getAnnotation(Key.class);
				if (key != null) {

					// Ensure only one key
					if (keyField != null) {
						throw new IllegalStateException("More than one " + Key.class.getSimpleName() + " ("
								+ keyField.getName() + ", " + field.getName() + ") on class " + documentType.getName());
					}

					// Capture the key
					keyField = field;
				}
			}

			// Interrogate parent
			interrogate = interrogate.getSuperclass();
		} while (interrogate != null);

		// Setup key for use
		this.keyField = keyField;
		this.keyField.setAccessible(true);

		// Create the container
		CosmosContainerProperties createContainer = new CosmosContainerProperties(containerId,
				new PartitionKeyDefinition().setPaths(Arrays.asList("/" + keyField.getName())));
		createContainer.setIndexingPolicy(
				new IndexingPolicy().setIncludedPaths(Arrays.asList(new IncludedPath("/booleanPrimitive"))));
		cosmosDatabase.createContainer(createContainer);

		// Obtain the container
		this.container = cosmosDatabase.getContainer(documentType.getSimpleName());
	}

	/*
	 * ==================== OfficeCabinet ======================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Obtain the document
		CosmosItemRequestOptions options = new CosmosItemRequestOptions().setConsistencyLevel(ConsistencyLevel.STRONG);
		CosmosItemResponse<D> response = this.container.readItem(key, new PartitionKey(key), options,
				this.documentType);

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
			key = (String) this.keyField.get(document);
			if (key == null) {

				// Generate and load key
				key = CabinetUtil.newKey();
				this.keyField.set(document, key);

				// Flag creating
				isNew = true;
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Save
		if (isNew) {
			this.container.createItem(document);
		} else {
			this.container.replaceItem(document, key, new PartitionKey(key), null);
		}
	}

}
