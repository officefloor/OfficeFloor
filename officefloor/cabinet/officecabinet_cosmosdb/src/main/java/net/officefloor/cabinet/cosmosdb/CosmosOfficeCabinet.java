/*-
 * #%L
 * OfficeFloor Filing Cabinet for Cosmos DB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
