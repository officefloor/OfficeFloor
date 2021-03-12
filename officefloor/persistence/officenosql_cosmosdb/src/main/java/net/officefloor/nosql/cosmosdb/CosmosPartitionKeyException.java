package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.PartitionKey;

/**
 * Indicates unable to extract {@link PartitionKey} for a Cosmos entity.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosPartitionKeyException extends CosmosException {

	/**
	 * Required.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param entityType Type of entity.
	 * @param cause      Cause.
	 */
	protected CosmosPartitionKeyException(Class<?> entityType, Throwable cause) {
		super(500, "Failed to extract partition key for entity " + entityType.getName(), null, cause);
	}

}
