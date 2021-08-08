/*-
 * #%L
 * OfficeFloor Filing Cabinet for Cosmos DB
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

package net.officefloor.cabinet.cosmosdb;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesEntity;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosOfficeCabinetTest extends AbstractOfficeCabinetTest {

	public @RegisterExtension static final CosmosDbExtension cosmosDb = new CosmosDbExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet() throws Exception {

		// Create the database (if required)
		CosmosClient client = cosmosDb.getCosmosClient();
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("test");
		CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());

		// Create and return cabinet
		return new CosmosOfficeCabinet<>(AttributeTypesEntity.class, database);
	}

}
