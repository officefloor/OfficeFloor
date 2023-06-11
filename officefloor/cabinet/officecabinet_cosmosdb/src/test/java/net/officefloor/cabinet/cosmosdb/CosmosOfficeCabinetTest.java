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

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.DomainCabinetManufacturerImpl;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link CosmosOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosOfficeCabinetTest extends AbstractOfficeCabinetTestCase {

	public static final @RegisterExtension CosmosDbExtension cosmosDb = new CosmosDbExtension();

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeStore getOfficeStore() {

		// Create the database (if required)
		CosmosDatabase database = cosmosDb.getCosmosDatabase();

		// Create and return store
		return new CosmosOfficeStore(database, this.logger);
	}

	@Override
	protected DomainCabinetManufacturer getDomainSpecificCabinetManufacturer() {
		return new DomainCabinetManufacturerImpl(this.getClass().getClassLoader());
	}

}
