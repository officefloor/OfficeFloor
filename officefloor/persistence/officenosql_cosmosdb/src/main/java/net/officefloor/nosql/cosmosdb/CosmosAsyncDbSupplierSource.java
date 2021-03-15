package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;

/**
 * {@link SupplierSource} for {@link CosmosAsyncDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncDbSupplierSource extends AbstractCosmosDbSupplierSource {

	@Override
	protected void setupManagedObjectSources(SupplierSourceContext context, Class<?>[] entities) throws Exception {

		// Create the managed object sources
		CosmosAsyncClientManagedObjectSource clientMos = new CosmosAsyncClientManagedObjectSource();
		CosmosAsyncClient client = clientMos.createCosmosAsyncClient(context);
		CosmosAsyncDatabaseManagedObjectSource databaseMos = new CosmosAsyncDatabaseManagedObjectSource();
		String databaseName = databaseMos.getDatabaseName(context);
		CosmosAsyncDatabase database = databaseMos.createCosmosAsyncDatabase(client, databaseName).block();
		CosmosAsyncEntitiesManagedObjectSource entitiesMos = new CosmosAsyncEntitiesManagedObjectSource(entities);
		entitiesMos.loadEntityTypes(context);
		entitiesMos.setupEntities(database).block();

		// Register the CosmosDb managed object sources
		context.addManagedObjectSource(null, CosmosAsyncClient.class, clientMos);
		context.addManagedObjectSource(null, CosmosAsyncDatabase.class, databaseMos);
		context.addManagedObjectSource(null, CosmosAsyncEntities.class, entitiesMos);
	}

}