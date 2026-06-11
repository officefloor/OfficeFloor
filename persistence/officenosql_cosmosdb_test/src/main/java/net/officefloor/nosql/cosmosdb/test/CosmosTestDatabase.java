/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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
