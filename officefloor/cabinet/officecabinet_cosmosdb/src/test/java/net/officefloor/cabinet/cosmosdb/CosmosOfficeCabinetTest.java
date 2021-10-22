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

import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.Index.IndexField;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosOfficeCabinetTest extends AbstractOfficeCabinetTest {

	public static final @RegisterExtension CosmosDbExtension cosmosDb = new CosmosDbExtension();

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private CosmosDocumentAdapter adapter;

	@BeforeEach
	public void setup(TestInfo info) {

		// Create the database (if required)
		CosmosDatabase database = cosmosDb.getCosmosDatabase();

		// Create and return cabinet
		this.adapter = new CosmosDocumentAdapter(database, this.logger);
	}

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected <D> OfficeCabinetArchive<D> getOfficeCabinetArchive(Class<D> documentType) throws Exception {
		return new CosmosOfficeCabinetArchive<>(this.adapter, documentType, new Index(new IndexField("queryValue")));
	}

}
