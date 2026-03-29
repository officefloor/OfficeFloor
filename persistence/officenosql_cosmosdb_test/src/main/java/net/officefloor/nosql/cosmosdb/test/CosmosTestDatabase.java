package net.officefloor.nosql.cosmosdb.test;

import java.util.concurrent.atomic.AtomicInteger;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Reference to a test {@link CosmosDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosTestDatabase {

	/**
	 * Test {@link CosmosDatabase} prefix.
	 */
	private static final String TEST_DATABASE_PREFIX = "Test" + OfficeFloor.class.getSimpleName();

	/**
	 * Next unique index for a test {@link CosmosDatabase}.
	 */
	private static final AtomicInteger nextUniqueId = new AtomicInteger(0);

	/**
	 * Generates the next test {@link CosmosDatabase} Id.
	 * 
	 * @return Next test {@link CosmosDatabase} Id.
	 */
	private static String generateNextTestDatabaseId() {
		return TEST_DATABASE_PREFIX + nextUniqueId.incrementAndGet() + "Time" + System.currentTimeMillis();
	}

	/**
	 * Id of the test {@link CosmosDatabase}.
	 */
	private final String databaseId;

	/**
	 * Instantiate.
	 * 
	 * @param databaseId Allow specifying the {@link CosmosDatabase} Id.
	 */
	public CosmosTestDatabase(String databaseId) {
		this.databaseId = databaseId;
	}

	/**
	 * Instantiate for new {@link CosmosDatabase} Id.
	 */
	public CosmosTestDatabase() {
		this(generateNextTestDatabaseId());
	}

	/**
	 * Obtains the test {@link CosmosDatabase} Id.
	 * 
	 * @return Test {@link CosmosDatabase} Id.
	 */
	public String getTestDatabaseId() {
		return this.databaseId;
	}

}