package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClientBuilder;

/**
 * Decorates the {@link CosmosClientBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosClientBuilderDecorator {

	/**
	 * Decorates the {@link CosmosClientBuilder}.
	 * 
	 * @param builder {@link CosmosClientBuilder} to decorate.
	 * @return Decorated {@link CosmosClientBuilder}.
	 * @throws Exception If fails to decorate the {@link CosmosClientBuilder}.
	 */
	CosmosClientBuilder decorate(CosmosClientBuilder builder) throws Exception;

}