package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncClient;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Asynchronous {@link CosmosAsyncClient} test.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncTest extends AbstractCosmosTestCase {

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosAsyncClientManagedObjectSource> getClientManagedObjectSourceClass() {
		return CosmosAsyncClientManagedObjectSource.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<CosmosAsyncDatabaseManagedObjectSource> getDatabaseManagedObjectSourceClass() {
		return CosmosAsyncDatabaseManagedObjectSource.class;
	}

	@Override
	protected ManagedObjectSource<?, ?> getEntitiesManagedObjectSource(Class<?>... entityTypes) {
		return new CosmosAsyncEntitiesManagedObjectSource(entityTypes);
	}

	@Override
	protected boolean isAsynchronous() {
		return true;
	}

}