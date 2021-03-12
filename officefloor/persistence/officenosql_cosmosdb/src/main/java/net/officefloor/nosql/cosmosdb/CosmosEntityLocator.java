package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosDatabase;

/**
 * Locates {@link CosmosDatabase} entity types for registering.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosEntityLocator {

	/**
	 * Locates the {@link CosmosDatabase} entity types.
	 * 
	 * @return {@link CosmosDatabase} entity types.
	 * @throws Exception If fails to locate the {@link CosmosDatabase} entity types.
	 */
	Class<?>[] locateEntities() throws Exception;

}