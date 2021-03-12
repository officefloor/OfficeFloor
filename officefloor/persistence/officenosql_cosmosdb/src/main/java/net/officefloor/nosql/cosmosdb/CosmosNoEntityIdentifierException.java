package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosException;

/**
 * Indicates unable to determine the identifier for a Cosmos entity.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosNoEntityIdentifierException extends CosmosException {

	/**
	 * Required.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param entityType Type of entity.
	 */
	protected CosmosNoEntityIdentifierException(Class<?> entityType) {
		super(404, "No identifier method available on entity " + entityType.getName());
	}

}
