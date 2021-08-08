/*-
 * #%L
 * CosmosDB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
