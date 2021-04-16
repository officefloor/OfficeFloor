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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.cosmosdb.test.AbstractCosmosDbJunit;
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
	private static class ConnectCosmos extends AbstractCosmosDbJunit<ConnectCosmos> {

		public void start() throws Exception {
			this.waitForCosmosDb().startCosmosDb(false);
		}

		public void stop() throws Exception {
			this.stopCosmosDb();
		}
	}

	private static ConnectCosmos cosmos = new ConnectCosmos();

	@BeforeAll
	public static void setupCosmos() throws Exception {
		cosmos.start();
	}

	@AfterAll
	public static void tearDownCosmos() throws Exception {
		cosmos.stop();
	}

	/**
	 * Ensure able to connect via configured {@link Property} values.
	 */
	@Test
	public void connectViaProperties() throws Throwable {
		this.doTest(CosmosDbConnect.PROPERTY_URL, cosmos.getEndpointUrl(), CosmosDbConnect.PROPERTY_KEY, "TESTKEY");
	}

	/**
	 * Ensure able to connect via environment variables (typically from Azure
	 * configuration).
	 */
	@Test
	public void connectViaEnvironment() throws Throwable {
		ConnectEnvironment env = new ConnectEnvironment();
		Runnable reset = env.property("COSMOS_URL", cosmos.getEndpointUrl()).property("COSMOS_KEY", "TESTKEY").setup();
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

			// Register Cosmos client
			OfficeManagedObjectSource clientMos = office.addOfficeManagedObjectSource("COSMOS_CLIENT",
					CosmosClientManagedObjectSource.class.getName());
			for (int i = 0; i < propertyNameValues.length; i += 2) {
				String propertyName = propertyNameValues[i];
				String propertyValue = propertyNameValues[i + 1];
				clientMos.addProperty(propertyName, propertyValue);
			}
			clientMos.addOfficeManagedObject("COSMOS_CLIENT", ManagedObjectScope.THREAD);

			// Setup the database
			OfficeManagedObjectSource databaseMos = office.addOfficeManagedObjectSource("COSMOS_DB",
					CosmosDatabaseManagedObjectSource.class.getName());
			databaseMos.addOfficeManagedObject("COSMOS_DB", ManagedObjectScope.THREAD);
			office.startAfter(databaseMos, clientMos);

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
