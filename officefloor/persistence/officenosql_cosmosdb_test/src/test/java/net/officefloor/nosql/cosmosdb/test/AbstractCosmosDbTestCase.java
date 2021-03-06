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
		database.createContainerIfNotExists(TestEntity.class.getSimpleName(), "/partition");
		CosmosContainer container = database.getContainer(TestEntity.class.getSimpleName());

		// Store in container
		TestEntity entity = new TestEntity(UUID.randomUUID().toString(), "Test message");
		container.createItem(entity);

		// Retrieve item from container
		CosmosItemResponse<TestEntity> itemResponse = container.readItem(entity.getId(),
				new PartitionKey(entity.getPartition()), TestEntity.class);
		TestEntity retrieved = itemResponse.getItem();
		assertEquals("Test message", retrieved.getMessage(), "Incorrect retrieved");

		// Retrieve all items from container
		long itemCount = container.readAllItems(new PartitionKey(new TestEntity().getPartition()), TestEntity.class)
				.stream().count();
		assertEquals(1, itemCount, "Should have the stored item");

		// Update item
		entity.setMessage("Updated message");
		container.replaceItem(entity, entity.getId(), new PartitionKey(entity.getPartition()), null);
		TestEntity updated = container
				.readItem(entity.getId(), new PartitionKey(entity.getPartition()), TestEntity.class).getItem();
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
				.flatMap(database -> database.createContainerIfNotExists(TestEntity.class.getSimpleName(), "/partition")
						.map(response -> database.getContainer(TestEntity.class.getSimpleName())));

		// Store in container
		Mono<TestEntity> monoEntity = monoContainer.flatMap(
				container -> container.createItem(new TestEntity(UUID.randomUUID().toString(), "Test async message")))
				.map(response -> response.getItem());

		// Retrieve item from container
		Mono<TestEntity> monoRetrieved = monoEntity
				.flatMap(entity -> monoContainer.flatMap(container -> container.readItem(entity.getId(),
						new PartitionKey(entity.getPartition()), TestEntity.class)))
				.map(response -> response.getItem());

		// Retrieve all items from container
		Mono<TestEntity> monoAllRetrieved = monoRetrieved.flatMap(retrieved -> monoContainer.flatMap(
				container -> container.readAllItems(new PartitionKey(new TestEntity().getPartition()), TestEntity.class)
						.collectList()))
				.map(list -> list.get(0));

		// Update item
		Mono<TestEntity> monoUpdated = monoAllRetrieved.flatMap(allRetrieved -> monoEntity.flatMap(entity -> {
			entity.setMessage("Updated async message");
			return monoContainer.flatMap(container -> container.replaceItem(entity, entity.getId(),
					new PartitionKey(entity.getPartition())));
		})).map(response -> response.getItem());

		// Retrieve updated item
		Mono<TestEntity> monoRetrievedUpdate = monoUpdated
				.flatMap(updated -> monoContainer.flatMap(container -> container.readItem(updated.getId(),
						new PartitionKey(updated.getPartition()), TestEntity.class)))
				.map(response -> response.getItem());

		// Obtain all results
		Mono<TestEntity[]> monoResults = monoRetrievedUpdate
				.flatMap(retrievedUpdate -> monoRetrieved.flatMap(retrieved -> monoAllRetrieved
						.map(allRetrievedCount -> new TestEntity[] { retrieved, allRetrievedCount, retrievedUpdate })));

		// Obtain and verify
		TestEntity[] results = monoResults.block();
		TestEntity retrieved = results[0];
		TestEntity allRetrieved = results[1];
		TestEntity retrievedUpdate = results[2];
		assertEquals("Test async message", retrieved.getMessage(), "Incorrect retrieved");
		assertEquals("Test async message", allRetrieved.getMessage(), "Incorrect all retrieved count");
		assertEquals("Updated async message", retrievedUpdate.getMessage(), "Incorrect updated");
	}

	/**
	 * Test entity.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TestEntity {

		private final String partition = "SINGLE";

		private String id;

		private String message;

	}

}