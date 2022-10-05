/*-
 * #%L
 * CosmosDB Persistence Testing
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

package net.officefloor.nosql.cosmosdb.test;

import java.util.Arrays;
import java.util.function.BiFunction;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosDatabaseProperties;

import net.officefloor.nosql.cosmosdb.CosmosDbConnect;
import net.officefloor.nosql.cosmosdb.CosmosDbFactory;
import net.officefloor.nosql.cosmosdb.CosmosDbUtil;
import net.officefloor.nosql.cosmosdb.test.CosmosEmulatorInstance.Configuration;
import net.officefloor.nosql.cosmosdb.test.CosmosEmulatorInstance.FailureFactory;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;

/**
 * <p>
 * Abstract JUnit CosmosDb functionality.
 * <p>
 * Given the slow nature of CosmosDb emulator, keeps running for all tests and
 * only shuts down on JVM exit. However, will wipe databases between tests to
 * have consistent starting state.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCosmosDbJunit<T extends AbstractCosmosDbJunit<T>> implements FailureFactory {

	/**
	 * Indicates whether skipping failures.
	 * 
	 * @return <code>true</code> if skipping failures.
	 */
	public static boolean isSkipFailure() {
		String skipFailedCosmos = System.getProperty(PROPERTY_SKIP_FAILED_COSMOS, null);
		if (skipFailedCosmos == null) {
			skipFailedCosmos = System.getenv(PROPERTY_SKIP_FAILED_COSMOS.toUpperCase().replace('.', '_'));
		}
		return skipFailedCosmos == null || (Boolean.parseBoolean(skipFailedCosmos));
	}

	/**
	 * Property to flag skipping failed Cosmos DB tests. This is useful, as the
	 * Cosmos DB Emulator is found to not be stable on linux. Happy to be provided
	 * improvements, however this is currently necessary for clean builds.
	 */
	public static final String PROPERTY_SKIP_FAILED_COSMOS = "officefloor.skip.failed.cosmos.tests";

	/**
	 * Skip message.
	 */
	protected static final String SKIP_MESSAGE = "Skipping Cosmos DB test failure";

	/**
	 * Initiate for use.
	 */
	static {
		CosmosSelfSignedCertificate.noOpenSsl();
	}

	/**
	 * {@link CosmosEmulatorInstance}.
	 */
	private final CosmosEmulatorInstance emulatorInstance;

	/**
	 * {@link CosmosTestDatabase}.
	 */
	private final CosmosTestDatabase testDatabase;

	/**
	 * Indicates whether to setup {@link CosmosDbFactory}.
	 */
	private boolean isSetupCosmosDbFactory = true;

	/**
	 * {@link CosmosDatabase} for testing.
	 */
	private CosmosDatabase database = null;

	/**
	 * {@link CosmosAsyncDatabase} for testing.
	 */
	private CosmosAsyncDatabase asyncDatabase = null;

	/**
	 * Instantiate.
	 * 
	 * @param testDatabse {@link CosmosTestDatabase}. May be <code>null</code> for
	 *                    new {@link CosmosTestDatabase}.
	 */
	public AbstractCosmosDbJunit(CosmosTestDatabase testDatabse) {
		this(null, testDatabse);
	}

	/**
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}. May be
	 *                         <code>null</code> for
	 *                         {@link CosmosEmulatorInstance#DEFAULT}.
	 * @param testDatabse      {@link CosmosTestDatabase}. May be <code>null</code>
	 *                         for new {@link CosmosTestDatabase}.
	 */
	public AbstractCosmosDbJunit(CosmosEmulatorInstance emulatorInstance, CosmosTestDatabase testDatabse) {
		this.emulatorInstance = emulatorInstance != null ? emulatorInstance
				: new CosmosEmulatorInstance(new Configuration(), this);
		this.testDatabase = testDatabse;
	}

	/**
	 * Flags whether to setup the {@link CosmosDbFactory} for connecting.
	 * 
	 * @param isSetupCosmosDbFactory <code>true</code> to setup the
	 *                               {@link CosmosDbFactory} for connecting.
	 * @return <code>this</code> for builder pattern.
	 */
	@SuppressWarnings("unchecked")
	public T setupCosmosDbFactory(boolean isSetupCosmosDbFactory) {
		this.isSetupCosmosDbFactory = isSetupCosmosDbFactory;
		return (T) this;
	}

	/**
	 * Obtains the end point URL.
	 * 
	 * @return End point URL.
	 */
	public String getEndpointUrl() {
		return this.emulatorInstance.getEndpointUrl();
	}

	/**
	 * Obtains the Key for connecting.
	 * 
	 * @return Key for connecting.
	 */
	public String getKey() {
		return this.emulatorInstance.getKey();
	}

	/**
	 * Obtains the {@link CosmosClient}.
	 * 
	 * @return {@link CosmosClient}.
	 */
	public CosmosClient getCosmosClient() {
		return this.emulatorInstance.getCosmosClient();
	}

	/**
	 * Obtains the {@link CosmosDatabase} for testing.
	 * 
	 * @return {@link CosmosDatabase} for testing.
	 */
	public CosmosDatabase getCosmosDatabase() {
		JUnitAgnosticAssert.assertNotNull(this.database,
				"Must start before obtaining " + CosmosDatabase.class.getSimpleName());
		return this.database;
	}

	/**
	 * Obtains the {@link CosmosAsyncClient}.
	 * 
	 * @return {@link CosmosAsyncClient}.
	 */
	public CosmosAsyncClient getCosmosAsyncClient() {
		return this.emulatorInstance.getCosmosAsyncClient();
	}

	/**
	 * Obtains the {@link CosmosAsyncDatabase} for testing.
	 * 
	 * @return {@link CosmosAsyncDatabase} for testing.
	 */
	public CosmosAsyncDatabase getCosmosAsyncDatabase() {
		JUnitAgnosticAssert.assertNotNull(this.asyncDatabase,
				"Must start before obtaining " + CosmosAsyncDatabase.class.getSimpleName());
		return this.asyncDatabase;
	}

	/**
	 * Start CosmosDb locally.
	 * 
	 * @throws Exception If fails to start.
	 */
	protected void startCosmosDb() throws Exception {

		// Avoid starting up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			System.out.println("Docker not available. Unable to start CosmosDb.");
			return;
		}

		// Ensure the emulator is running
		int startedPartitions = this.emulatorInstance.ensureEmulatorStarted();
		int startWaitSeconds = startedPartitions * 3;
		if (startWaitSeconds > 0) {
			Thread.sleep(startWaitSeconds * 1000);
		}

		// Obtain the client connection
		CosmosClient client = this.getCosmosClient();

		// Ensure the test database is available
		CosmosTestDatabase testDatabase = this.testDatabase != null ? this.testDatabase : new CosmosTestDatabase();
		String testDatabaseId = testDatabase.getTestDatabaseId();
		CosmosDbUtil.createDatabases(client, Arrays.asList(new CosmosDatabaseProperties(testDatabaseId)),
				startWaitSeconds, null, null);

		// Create the database objects
		this.database = client.getDatabase(testDatabaseId);
		this.asyncDatabase = this.getCosmosAsyncClient().getDatabase(testDatabaseId);

		// Override to connect to local Cosmos DB
		if (this.isSetupCosmosDbFactory) {
			CosmosDbFactory factory = new CosmosDbFactory() {

				@Override
				public CosmosDatabase createCosmosDatabase() throws Exception {
					return AbstractCosmosDbJunit.this.database;
				}

				@Override
				public CosmosAsyncDatabase createCosmosAsyncDatabase() throws Exception {
					return AbstractCosmosDbJunit.this.asyncDatabase;
				}
			};
			CosmosDbConnect.setCosmosDbFactory(factory);
		}
	}

	/**
	 * Determine if ignore {@link CosmosException}.
	 * 
	 * @param failure Failure of test.
	 * @param skip    Handles the skip.
	 * @throws Throwable Propagation of failure.
	 */
	protected void handleTestFailure(Throwable failure, BiFunction<String, Throwable, Throwable> skip)
			throws Throwable {

		// Determine if skip tests
		if (isSkipFailure()) {

			// Skip the failed test
			throw skip.apply(SKIP_MESSAGE, failure);
		}

		// As here, not skip so propagate
		throw failure;
	}

	/**
	 * Stops locally running CosmosDb.
	 * 
	 * @throws Throwable If fails to stop.
	 */
	protected void stopCosmosDb(BiFunction<String, Throwable, Throwable> skip) throws Throwable {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		// Delete the database
		if (this.database != null) {
			try {
				this.database.delete();
			} catch (Exception ex) {
				this.handleTestFailure(ex, skip);
			}
		}

		// Clear references to databases
		this.database = null;
		this.asyncDatabase = null;

		// Ensure clear connection factory
		CosmosDbConnect.setCosmosDbFactory(null);
	}

}
