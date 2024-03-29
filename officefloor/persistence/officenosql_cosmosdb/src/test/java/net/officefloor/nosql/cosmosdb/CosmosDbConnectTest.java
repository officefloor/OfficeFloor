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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.test.system.AbstractEnvironmentOverride;

/**
 * Ensures able to connect to {@link CosmosDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class CosmosDbConnectTest {

	/**
	 * Enable starting {@link CosmosDatabase} without overriding for client for
	 * tests. Hence, requires manual setup up of client.
	 */
	public @RegisterExtension static final CosmosDbExtension cosmos = new CosmosDbExtension()
			.setupCosmosDbFactory(false);;

	/**
	 * Ensure able to connect via configured {@link Property} values.
	 */
	@Test
	public void connectViaProperties() throws Throwable {
		this.doTest(CosmosDbConnect.PROPERTY_URL, cosmos.getEndpointUrl(), CosmosDbConnect.PROPERTY_KEY,
				cosmos.getKey(), CosmosDbConnect.PROPERTY_DATABASE, cosmos.getCosmosDatabase().getId());
	}

	/**
	 * Ensure able to connect via environment variables (typically from Azure
	 * configuration).
	 */
	@Test
	public void connectViaEnvironment() throws Throwable {
		ConnectEnvironment env = new ConnectEnvironment();
		Runnable reset = env.property("COSMOS_URL", cosmos.getEndpointUrl()).property("COSMOS_KEY", cosmos.getKey())
				.property("COSMOS_DATABASE", cosmos.getCosmosDatabase().getId()).setup();
		try {
			this.doTest();
		} finally {
			reset.run();
		}
	}

	/**
	 * Enable overriding the environment.
	 */
	private static class ConnectEnvironment extends AbstractEnvironmentOverride<ConnectEnvironment> {

		public Runnable setup() {
			OverrideReset reset = this.override();
			return () -> reset.resetOverrides();
		}
	}

	/**
	 * Undertakes the tests.
	 * 
	 * @param propertyNameValues {@link Property} name/value pairs for
	 *                           {@link CosmosClientManagedObjectSource}.
	 */
	private void doTest(String... propertyNameValues) throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Setup the database
			OfficeManagedObjectSource databaseMos = office.addOfficeManagedObjectSource("COSMOS_DB",
					CosmosDatabaseManagedObjectSource.class.getName());
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String propertyName = propertyNameValues[i];
				String propertyValue = propertyNameValues[i + 1];
				databaseMos.addProperty(propertyName, propertyValue);
			}
			databaseMos.addOfficeManagedObject("COSMOS_DB", ManagedObjectScope.THREAD);

			// Setup partition key factory
			OfficeManagedObjectSource partitionKeyMos = office.addOfficeManagedObjectSource("PARTITION_KEY",
					new CosmosEntitiesManagedObjectSource(TestDefaultEntity.class));
			partitionKeyMos.addOfficeManagedObject("PARTITION_KEY", ManagedObjectScope.THREAD);
			office.startAfter(partitionKeyMos, databaseMos);

			// Register test logic
			context.addSection("TEST", TestSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Create the entity
			TestDefaultEntity entity = new TestDefaultEntity(UUID.randomUUID().toString(), "Test");

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", entity);

			// Ensure able to obtain entity
			PartitionKey partitionKey = TestSection.entities.createPartitionKey(entity);
			TestEntity retrieved = TestSection.entities.getContainer(TestDefaultEntity.class)
					.readItem(entity.getId(), partitionKey, entity.getClass()).getItem();
			assertEquals(entity.getMessage(), retrieved.getMessage(), "Should obtain entity");
		}
	}

	public static class TestSection {

		private static CosmosEntities entities;

		public void service(CosmosEntities cosmosEntities, @Parameter TestDefaultEntity entity) {
			entities = cosmosEntities;

			// Obtain the container
			CosmosContainer container = cosmosEntities.getContainer(TestDefaultEntity.class);

			// Save
			PartitionKey partitionKey = cosmosEntities.createPartitionKey(entity);
			container.createItem(entity, partitionKey, null);

			// Ensure able to obtain entity
			TestEntity retrieved = container.readItem(entity.getId(), partitionKey, entity.getClass()).getItem();
			assertEquals(entity.getMessage(), retrieved.getMessage(), "Should obtain entity");
		}
	}

}
