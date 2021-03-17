/*-
 * #%L
 * CosmosDB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
