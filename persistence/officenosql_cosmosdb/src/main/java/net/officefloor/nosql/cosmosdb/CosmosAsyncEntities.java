/*-
 * #%L
 * CosmosDB
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

package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.PartitionKey;

/**
 * Provides means to work with entities for Cosmos.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosAsyncEntities {

	/**
	 * Obtains the {@link CosmosAsyncContainer} for the entity type.
	 * 
	 * @param entityType Entity type.
	 * @return {@link CosmosAsyncContainer} for the entity type.
	 */
	CosmosAsyncContainer getContainer(Class<?> entityType);

	/**
	 * Creates the {@link PartitionKey} for the entity.
	 * 
	 * @param entity Entity to generate {@link PartitionKey}.
	 * @return {@link PartitionKey} for the entity.
	 */
	PartitionKey createPartitionKey(Object entity);

}
