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

import com.azure.cosmos.CosmosClient;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Synchronous {@link CosmosClient} test.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosSyncTest extends AbstractCosmosTestCase {

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
