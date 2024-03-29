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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.AsynchronousFlow;
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
public abstract class AbstractCosmosTestCase {

	/**
	 * Cosmos DB emulator.
	 */
	public final @RegisterExtension CosmosDbExtension cosmosDb = new CosmosDbExtension();

	/**
	 * Obtains the {@link ManagedObjectSource} {@link Class} for database.
	 * 
	 * @return {@link ManagedObjectSource} {@link Class} for database.
	 */
	protected abstract <M extends ManagedObjectSource<?, ?>> Class<M> getDatabaseManagedObjectSourceClass();

	/**
	 * Obtains the {@link ManagedObjectSource} for the entities.
	 * 
	 * @return {@link ManagedObjectSource} for the entities.
	 */
	protected abstract ManagedObjectSource<?, ?> getEntitiesManagedObjectSource(Class<?>... entityTypes);

	/**
	 * Factory for creation of the {@link PartitionKey}.
	 */
	private static Function<Object, PartitionKey> partitionKeyFactory = null;

	/**
	 * Test entities.
	 */
	public static TestEntity[] entities = null;

	/**
	 * Indicates if testing asynchronous.
	 * 
	 * @return Asynchronous.
	 */
	protected abstract boolean isAsynchronous();

	/**
	 * Ensure correct specification.
	 */
	@UsesDockerTest
	public void databaseSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(this.getDatabaseManagedObjectSourceClass());
	}

	/**
	 * Ensure correct specification.
	 */
	@UsesDockerTest
	@SuppressWarnings("unchecked")
	public void entitiesSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(this.getEntitiesManagedObjectSource().getClass());
	}

	/**
	 * Ensure correct meta-data.
	 */
	@UsesDockerTest
	public void databaseMetaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(this.isAsynchronous() ? CosmosAsyncDatabase.class : CosmosDatabase.class);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getDatabaseManagedObjectSourceClass());
	}

	/**
	 * Ensure correct meta-data.
	 */
	@UsesDockerTest
	@SuppressWarnings("unchecked")
	public void enitiesMetaData() {
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(this.isAsynchronous() ? CosmosAsyncEntities.class : CosmosEntities.class);
		type.addFunctionDependency("COSMOS_DATABASE",
				this.isAsynchronous() ? CosmosAsyncDatabase.class : CosmosDatabase.class, null);
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getEntitiesManagedObjectSource().getClass());
	}

	/**
	 * Ensure {@link CosmosDatabase} working.
	 */
	@UsesDockerTest
	public void cosmosDatabase() throws Throwable {

		// Compile
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Setup the database
			OfficeManagedObjectSource databaseMos = office.addOfficeManagedObjectSource("COSMOS_DB",
					this.getDatabaseManagedObjectSourceClass().getName());
			databaseMos.addProperty(CosmosDbConnect.PROPERTY_URL, cosmosDb.getEndpointUrl());
			databaseMos.addProperty(CosmosDbConnect.PROPERTY_KEY, "TESTKEY");
			databaseMos.addProperty(CosmosDbConnect.PROPERTY_DATABASE, this.cosmosDb.getCosmosDatabase().getId());
			databaseMos.addOfficeManagedObject("COSMOS_DB", ManagedObjectScope.THREAD);

			// Setup partition key factory
			OfficeManagedObjectSource partitionKeyMos = office.addOfficeManagedObjectSource("PARTITION_KEY",
					this.getEntitiesManagedObjectSource(TestDefaultEntity.class, TestAnnotatedEntity.class));
			partitionKeyMos.addOfficeManagedObject("PARTITION_KEY", ManagedObjectScope.THREAD);
			office.startAfter(partitionKeyMos, databaseMos);

			// Register test logic
			context.addSection("TEST", this.isAsynchronous() ? TestAsyncSection.class : TestSyncSection.class);
		});
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

			// Setup entities
			entities = new TestEntity[] { new TestDefaultEntity(UUID.randomUUID().toString(), "Test Default Entity"),
					new TestAnnotatedEntity(UUID.randomUUID().toString(), "Test Annotated Entity") };
			String[] containerIds = new String[] { TestDefaultEntity.class.getSimpleName(), "TEST_ANNOTATED_ENTITY" };
			PartitionKey[] partitionKeys = new PartitionKey[] { new PartitionKey(entities[0].getId()),
					new PartitionKey("ANNOTATED_PARTITION") };

			// Invoke functionality
			CompileOfficeFloor.invokeProcess(officeFloor, "TEST.service", null);

			// Ensure database set up
			CosmosDatabase database = cosmosDb.getCosmosClient().getDatabase(this.cosmosDb.getCosmosDatabase().getId());
			assertNotNull("Should have created database");

			// Ensure available
			for (int i = 0; i < entities.length; i++) {
				TestEntity expectedEntity = entities[i];
				String containerId = containerIds[i];
				PartitionKey expectedKey = partitionKeys[i];
				Class<? extends TestEntity> entityType = expectedEntity.getClass();

				// Ensure container set up
				CosmosContainer container = database.getContainer(containerId);
				assertNotNull("Should have container");

				// Ensure correct partition key
				PartitionKey partitionKey = partitionKeyFactory.apply(expectedEntity);
				assertEquals(expectedKey, partitionKey,
						"Incorrect partition key for entity " + entityType.getSimpleName());

				// Ensure available
				TestEntity retrieved = CosmosDbUtil
						.retry(() -> container.readItem(expectedEntity.getId(), partitionKey, entityType).getItem());
				assertNotNull(retrieved, "Should have retrieved stored entity " + entityType.getSimpleName());
				assertEquals(expectedEntity.getMessage(), retrieved.getMessage(),
						"Incorrect entity " + entityType.getSimpleName());
			}
		}
	}

	public static class TestSyncSection {

		public void service(CosmosDatabase database, CosmosEntities cosmosEntities) {

			// Provide means to create partition key
			partitionKeyFactory = (entity) -> cosmosEntities.createPartitionKey(entity);

			// Service entities
			for (TestEntity entity : entities) {

				// Obtain the container
				CosmosContainer container = cosmosEntities.getContainer(entity.getClass());

				// Save
				PartitionKey partitionKey = cosmosEntities.createPartitionKey(entity);
				CosmosDbUtil.retry(() -> container.createItem(entity, partitionKey, null));

				// Ensure able to obtain entity
				TestEntity retrieved = CosmosDbUtil.retry(() -> database.getContainer(container.getId())
						.readItem(entity.getId(), partitionKey, entity.getClass()).getItem());
				assertEquals(entity.getMessage(), retrieved.getMessage(), "Should obtain entity");
			}
		}
	}

	public static class TestAsyncSection {

		public void service(CosmosAsyncDatabase database, AsynchronousFlow async, CosmosAsyncEntities cosmosEntities) {

			// Provide means to create partition key
			partitionKeyFactory = (entity) -> cosmosEntities.createPartitionKey(entity);

			// Allow stringing logic together
			Mono<TestEntity> logic = Mono.just(new TestDefaultEntity());

			// Service entities
			for (TestEntity entity : entities) {

				// Obtain the container
				CosmosAsyncContainer container = cosmosEntities.getContainer(entity.getClass());

				// Save
				PartitionKey partitionKey = cosmosEntities.createPartitionKey(entity);
				Mono<TestEntity> monoCreated = container.createItem(entity, partitionKey, null).map(response -> entity);

				// Ensure able to obtain entity
				Mono<TestEntity> monoRetrieved = monoCreated.flatMap(created -> database.getContainer(container.getId())
						.readItem(entity.getId(), partitionKey, entity.getClass()).map(response -> response.getItem()))
						.map((retrieved) -> {
							assertEquals(entity.getMessage(), retrieved.getMessage(), "Should obtain entity");
							return retrieved;
						});

				// String the logic together
				logic = logic.flatMap((stringTogether) -> monoRetrieved);
			}

			// Undertake functionality
			logic.subscribe((retrieved) -> async.complete(null), (error) -> async.complete(() -> {
				throw error;
			}));

		}
	}

}
