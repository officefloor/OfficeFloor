/*-
 * #%L
 * OfficeFloor Filing Cabinet for Cosmos DB
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

package net.officefloor.cabinet.cosmosdb;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.OfficeCabinetArchive;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosOfficeCabinet}.
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
	protected OfficeCabinetArchive<AttributeTypesDocument> getAttributeTypesOfficeCabinetArchive() throws Exception {

		// Create the database (if required)
		CosmosClient client = cosmosDb.getCosmosClient();
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("test");
		CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());

		// Create and return cabinet
		return new CosmosOfficeCabinetArchive<>(AttributeTypesDocument.class, database);
	}

}
