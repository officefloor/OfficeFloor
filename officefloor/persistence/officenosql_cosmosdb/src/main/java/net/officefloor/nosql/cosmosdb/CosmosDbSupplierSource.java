package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;

/**
 * {@link SupplierSource} for {@link CosmosDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbSupplierSource extends AbstractCosmosDbSupplierSource {

	@Override
	protected void setupManagedObjectSources(SupplierSourceContext context, Class<?>[] entities) throws Exception {

		// Create the managed object sources
		CosmosClientManagedObjectSource clientMos = new CosmosClientManagedObjectSource();
		CosmosClient client = clientMos.createCosmosClient(context);
		CosmosDatabaseManagedObjectSource databaseMos = new CosmosDatabaseManagedObjectSource();
		String databaseName = databaseMos.getDatabaseName(context);
		CosmosDatabase database = databaseMos.createCosmosDatabase(client, databaseName);
		CosmosEntitiesManagedObjectSource entitiesMos = new CosmosEntitiesManagedObjectSource(entities);
		entitiesMos.loadEntityTypes(context);
		entitiesMos.setupEntities(database);

		// Register the CosmosDb managed object sources
		context.addManagedObjectSource(null, CosmosClient.class, clientMos);
		context.addManagedObjectSource(null, CosmosDatabase.class, databaseMos);
		context.addManagedObjectSource(null, CosmosEntities.class, entitiesMos);
	}

}