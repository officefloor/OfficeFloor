package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClientBuilder;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link CosmosClientBuilderDecorator} for Cosmos emulator.
 * 
 * @author Daniel Sagenschneider
 */
public class EmulatorCosmosClientBuilderDecorator
		implements CosmosClientBuilderDecorator, CosmosClientBuilderDecoratorServiceFactory {

	/*
	 * ================= CosmosClientBuilderDecoratorServiceFactory ==============
	 */

	@Override
	public CosmosClientBuilderDecorator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== CosmosClientBuilderDecorator ========================
	 */

	@Override
	public CosmosClientBuilder decorate(CosmosClientBuilder builder) throws Exception {
		return builder.gatewayMode();
	}

}
