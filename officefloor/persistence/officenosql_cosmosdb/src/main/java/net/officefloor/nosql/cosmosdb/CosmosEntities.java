package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.PartitionKey;

/**
 * Provides means to work with entities for Cosmos.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosEntities {

	/**
	 * Obtains the {@link CosmosContainer} for the entity type.
	 * 
	 * @param entityType Entity type.
	 * @return {@link CosmosContainer} for the entity type.
	 */
	CosmosContainer getContainer(Class<?> entityType);

	/**
	 * Creates the {@link PartitionKey} for the entity.
	 * 
	 * @param entity Entity to generate {@link PartitionKey}.
	 * @return {@link PartitionKey} for the entity.
	 */
	PartitionKey createPartitionKey(Object entity);

}