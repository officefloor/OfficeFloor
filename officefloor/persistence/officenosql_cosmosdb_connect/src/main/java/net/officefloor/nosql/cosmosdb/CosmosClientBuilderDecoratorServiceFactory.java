package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} for the {@link CosmosClientBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CosmosClientBuilderDecoratorServiceFactory extends ServiceFactory<CosmosClientBuilderDecorator> {
}