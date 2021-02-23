package net.officefloor.nosql.cosmosdb.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("test-db");
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