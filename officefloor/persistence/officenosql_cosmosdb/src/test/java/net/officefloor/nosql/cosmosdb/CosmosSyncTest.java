package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosClient;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

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
	protected ManagedObjectSource<?, ?> getEntitiesManagedObjectSource(Class<?>... entityTypes) {
		return new CosmosEntitiesManagedObjectSource(entityTypes);
	}

	@Override
	protected boolean isAsynchronous() {
		return false;
	}

}