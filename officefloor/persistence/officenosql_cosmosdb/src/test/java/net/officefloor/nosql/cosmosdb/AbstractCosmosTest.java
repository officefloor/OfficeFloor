/*-
 * #%L
 * DynamoDB Persistence
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.nosql.cosmosdb.test.CosmosDbExtension;
import net.officefloor.test.UsesDockerTest;
import reactor.core.publisher.Mono;

/**
 * Abstract testing for {@link CosmosClient} / {@link CosmosAsyncClient}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public abstract class AbstractCosmosTest {

	public static final @RegisterExtension CosmosDbExtension cosmosDb = new CosmosDbExtension();

	private static final PartitionKey partitionKey = new PartitionKey(new TestEntity().getPartition());

	/**
	 * Obtains the {@link ManagedObjectSource} {@link Class} for client.
	 * 
	 * @return {@link ManagedObjectSource} {@link Class} for client.
	 */
	protected abstract <M extends ManagedObjectSource<?, ?>> Class<M> getClientManagedObjectSourceClass();

	/**
	 * Obtains the {@link ManagedObjectSource} {@link Class} for database.
	 * 
	 * @return {@link ManagedObjectSource} {@link Class} for database.
	 */
	protected abstract <M extends ManagedObjectSource<?, ?>> Class<M> getDatabaseManagedObjectSourceClass();

	/**
	 * {@link TestEntity}.
	 */
	public static TestEntity entity = null;

	/**
	 * Indicates if testing asynchronous.
	 * 
	 * @return Asynchronous.
	 */
	protected abstract boolean isAsynchronous();

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void clientSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(this.getClientManagedObjectSourceClass(), "url", "URL", "key",
				"Key");
	}

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void databaseSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(this.getDatabaseManagedObjectSourceClass());
	}

	/**
	 * Ensure correct meta-data.
	 */
	@Test
	public void clientMetaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(this.isAsynchronous() ? CosmosAsyncClient.class : CosmosClient.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getClientManagedObjectSourceClass());
	}

	/**
	 * Ensure correct meta-data.
	 */
	@Test
	public void databaseMetaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(this.isAsynchronous() ? CosmosAsyncDatabase.class : CosmosDatabase.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getDatabaseManagedObjectSourceClass());
	}

	/**
	 * Ensure {@link CosmosClient} working.
	 */
	@Test
	public void cosmosClient() throws Throwable {
		this.doCosmosTest("serviceClient");
	}

	/**
	 * Ensure {@link CosmosDatabase} working.
	 */
	@Test
	public void cosmosDatabase() throws Throwable {
		this.doCosmosTest("serviceDatabase");
	}

	/**
	 * Undertakes the {@link CosmosClient} / {@link CosmosDatabase} test.
	 */
	private void doCosmosTest(String serviceMethodName) throws Throwable {

		// Ensure the cosmos running (before opening OfficeFloor)
		CosmosClient client = cosmosDb.getCosmosClient();

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Register Cosmos client
			OfficeManagedObjectSource clientMos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("COSMOS_CLIENT", this.getClientManagedObjectSourceClass().getName());
			clientMos.addProperty(CosmosDbConnect.PROPERTY_URL, cosmosDb.getEndpointUrl());
			clientMos.addProperty(CosmosDbConnect.PROPERTY_KEY, "TESTKEY");
			clientMos.addOfficeManagedObject("COSMOS_CLIENT", ManagedObjectScope.THREAD);

			// Setup the database
			context.getOfficeArchitect()
					.addOfficeManagedObjectSource("COSMOS_DB", this.getDatabaseManagedObjectSourceClass().getName())
					.addOfficeManagedObject("COSMOS_DB", ManagedObjectScope.THREAD);

			// Register test logic
			context.addSection("TEST", this.isAsynchronous() ? TestAsyncSection.class : TestSyncSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Reset entity
			entity = null;

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST." + serviceMethodName, null);

			// Ensure database set up
			CosmosDatabase database = client.getDatabase(OfficeFloor.class.getSimpleName());
			assertNotNull("Should have created database");

			// Ensure container set up
			CosmosContainer container = database.getContainer(TestEntity.class.getSimpleName());
			assertNotNull("Should have container");

			// Retrieve
			List<TestEntity> entities = container.readAllItems(partitionKey, TestEntity.class).stream()
					.collect(Collectors.toList());
			assertEquals(1, entities.size(), "Should have retrieved stored entity");
		}
	}

	public static class TestSyncSection {

		public void serviceClient(CosmosClient client) {

			// Obtain the database
			CosmosDatabase database = client.getDatabase(OfficeFloor.class.getSimpleName());

			// Service database
			this.serviceDatabase(database);
		}

		public void serviceDatabase(CosmosDatabase database) {

			// Obtain the container
			CosmosContainer container = database.getContainer(TestEntity.class.getSimpleName());

			// Save
			entity = new TestEntity(UUID.randomUUID().toString(), "Test Entity");
			container.createItem(entity);

			// Ensure able to obtain entity
			TestEntity retrieved = container.readItem(entity.getId(), partitionKey, TestEntity.class).getItem();
			assertEquals("Test Entity", retrieved.getMessage(), "Should obtain entity");
		}
	}

	public static class TestAsyncSection {

		public void serviceClient(CosmosAsyncClient client) {

			// Obtain the database
			CosmosAsyncDatabase database = client.getDatabase(OfficeFloor.class.getSimpleName());

			// Service database
			this.serviceDatabase(database);
		}

		public void serviceDatabase(CosmosAsyncDatabase database) {

			// Obtain the container
			CosmosAsyncContainer container = database.getContainer(TestEntity.class.getSimpleName());

			// Save
			entity = new TestEntity(UUID.randomUUID().toString(), "Test Entity");
			Mono<TestEntity> monoCreated = container.createItem(entity).map(response -> response.getItem());

			// Ensure able to obtain entity
			Mono<TestEntity> monoRetrieved = monoCreated.flatMap(created -> container
					.readItem(entity.getId(), partitionKey, TestEntity.class).map(response -> response.getItem()));
			TestEntity retrieved = monoRetrieved.block();
			assertEquals("Test Entity", retrieved.getMessage(), "Should obtain entity");
		}
	}

}
