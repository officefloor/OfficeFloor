package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClient;

/**
 * Synchronous {@link CosmosClient} test.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosSyncTest extends AbstractCosmosTest {

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosClientManagedObjectSource> getClientManagedObjectSourceClass() {
		return CosmosClientManagedObjectSource.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosDatabaseManagedObjectSource> getDatabaseManagedObjectSourceClass() {
		return CosmosDatabaseManagedObjectSource.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosEntitiesManagedObjectSource> getEntitiesManagedObjectSourceClass() {
		return CosmosEntitiesManagedObjectSource.class;
	}

	@Override
	protected boolean isAsynchronous() {
		return false;
	}

}