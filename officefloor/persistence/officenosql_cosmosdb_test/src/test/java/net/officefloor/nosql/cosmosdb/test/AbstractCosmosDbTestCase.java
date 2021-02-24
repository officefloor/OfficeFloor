package net.officefloor.nosql.cosmosdb.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Tests the JUnit CosmosDb functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractCosmosDbTestCase {

	/**
	 * Undertakes a synchronous test.
	 * 
	 * @param client {@link CosmosClient} to test.
	 */
	public void doSynchronousTest(CosmosClient client) {

		// Create the database
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("sync-db");
		CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId());

		// Create the container
		database.createContainerIfNotExists(TestEntity.class.getSimpleName(), "/id");
		CosmosContainer container = database.getContainer(TestEntity.class.getSimpleName());

		// Store in container
		TestEntity entity = new TestEntity(UUID.randomUUID().toString(), "Test message");
		container.createItem(entity);

		// Retrieve item from container
		CosmosItemResponse<TestEntity> itemResponse = container.readItem(entity.getId(),
				new PartitionKey(entity.getId()), TestEntity.class);
		TestEntity retrieved = itemResponse.getItem();
		assertEquals("Test message", retrieved.getMessage(), "Incorrect retrieved");

		// Update item
		entity.setMessage("Updated message");
		container.replaceItem(entity, entity.getId(), new PartitionKey(entity.getId()), null);
		TestEntity updated = container.readItem(entity.getId(), new PartitionKey(entity.getId()), TestEntity.class)
				.getItem();
		assertEquals("Updated message", updated.getMessage(), "Incorrect updated");
	}

	/**
	 * Undertakes an asynchronous test.
	 * 
	 * @param client {@link CosmosAsyncClient}.
	 */
	public void doAsynchronousTest(CosmosAsyncClient client) {

		// Create the database
		Mono<CosmosAsyncDatabase> monoDatabase = client.createDatabaseIfNotExists("async-db")
				.map(response -> client.getDatabase(response.getProperties().getId()));

		// Create the container
		Mono<CosmosAsyncContainer> monoContainer = monoDatabase
				.flatMap(database -> database.createContainerIfNotExists(TestEntity.class.getSimpleName(), "/id")
						.map(response -> database.getContainer(TestEntity.class.getSimpleName())));

		// Store in container
		Mono<TestEntity> monoEntity = monoContainer.flatMap(
				container -> container.createItem(new TestEntity(UUID.randomUUID().toString(), "Test async message")))
				.map(response -> response.getItem());

		// Retrieve item from container
		Mono<TestEntity> monoRetrieved = monoEntity.flatMap(entity -> monoContainer.flatMap(
				container -> container.readItem(entity.getId(), new PartitionKey(entity.getId()), TestEntity.class)))
				.map(response -> response.getItem());

		// Update item
		Mono<TestEntity> monoUpdated = monoRetrieved.flatMap(retrieved -> monoEntity.flatMap(entity -> {
			entity.setMessage("Updated async message");
			return monoContainer.flatMap(
					container -> container.replaceItem(entity, entity.getId(), new PartitionKey(entity.getId())));
		})).map(response -> response.getItem());

		// Retrieve updated item
		Mono<TestEntity> monoRetrievedUpdate = monoUpdated.flatMap(updated -> monoContainer.flatMap(
				container -> container.readItem(updated.getId(), new PartitionKey(updated.getId()), TestEntity.class)))
				.map(response -> response.getItem());

		// Obtain all results
		Mono<TestEntity[]> monoResults = monoRetrievedUpdate.flatMap(
				retrievedUpdate -> monoRetrieved.map(retrieved -> new TestEntity[] { retrieved, retrievedUpdate }));

		// Obtain and verify
		TestEntity[] results = monoResults.block();
		TestEntity retrieved = results[0];
		TestEntity retrievedUpdate = results[1];
		assertEquals("Test async message", retrieved.getMessage(), "Incorrect retrieved");
		assertEquals("Updated async message", retrievedUpdate.getMessage(), "Incorrect updated");
	}

	/**
	 * Test entity.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TestEntity {

		private String id;

		private String message;

	}

}