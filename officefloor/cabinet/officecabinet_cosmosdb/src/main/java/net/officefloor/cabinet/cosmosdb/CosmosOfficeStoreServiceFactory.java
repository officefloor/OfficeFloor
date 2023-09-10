package net.officefloor.cabinet.cosmosdb;

import java.util.Collections;
import java.util.logging.Logger;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.source.OfficeStoreServiceFactory;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link CosmosDatabase} {@link OfficeStoreServiceFactory}.
 */
public class CosmosOfficeStoreServiceFactory implements OfficeStoreServiceFactory {

	private static final Logger logger = Logger.getLogger(CosmosOfficeStore.class.getName());

	/*
	 * ================== OfficeStoreServiceFactory ==================
	 */

	@Override
	public OfficeStore createService(ServiceContext context) throws Throwable {

		// Build the CosmosDB connection
		// TODO configure connection
		CosmosClientBuilder clientBuilder = new CosmosClientBuilder().endpoint("https://localhost:" + 8003)
				.key("C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==")
				.preferredRegions(Collections.singletonList("Emulator")).contentResponseOnWriteEnabled(true)
				.consistencyLevel(ConsistencyLevel.EVENTUAL);
		CosmosClient client = clientBuilder.buildClient();
		CosmosDatabase database = client.getDatabase("");

		// Return the OfficeStore
		return new CosmosOfficeStore(database, logger);
	}

}
