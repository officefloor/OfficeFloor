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

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;

import net.officefloor.nosql.cosmosdb.CosmosDbConnect;
import net.officefloor.nosql.cosmosdb.CosmosDbFactory;
import net.officefloor.nosql.cosmosdb.CosmosDbUtil;
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
public abstract class AbstractCosmosDbJunit<T extends AbstractCosmosDbJunit<T>> {

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
	 * Instantiate.
	 * 
	 * @param emulatorInstance {@link CosmosEmulatorInstance}.
	 */
	public AbstractCosmosDbJunit(CosmosEmulatorInstance emulatorInstance) {
		this.emulatorInstance = emulatorInstance;
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
	 * Obtains the {@link CosmosAsyncClient}.
	 * 
	 * @return {@link CosmosAsyncClient}.
	 */
	public CosmosAsyncClient getCosmosAsyncClient() {
		return this.emulatorInstance.getCosmosAsyncClient();
	}

	/**
	 * Start CosmosDb locally.
	 * 
	 * @param isSetupClient Indicates whether to override {@link CosmosDbConnect} to
	 *                      connect.
	 * @throws Exception If fails to start.
	 */
	protected void startCosmosDb(boolean isSetupClient) throws Exception {

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

		// Obtain the client connections
		System.out.println("Connecting to Cosmos DB Emulator");
		CosmosClient client = this.getCosmosClient();
		CosmosAsyncClient asyncClient = this.getCosmosAsyncClient();

		// Clean containers
		System.out.println("Cleaning Cosmos DB Emulator");
		for (CosmosDatabaseProperties databaseProperties : client.readAllDatabases()) {
			CosmosDatabase database = client.getDatabase(databaseProperties.getId());
			for (CosmosContainerProperties containerProperties : database.readAllContainers()) {
				CosmosDbUtil.ignoreNotFound(
						() -> CosmosDbUtil.retry(() -> database.getContainer(containerProperties.getId()).delete()));
			}
			CosmosDbUtil.ignoreNotFound(() -> CosmosDbUtil.retry(() -> database.delete()));
		}
		System.out.println("Cosmos DB Emulator clean");

		// Override to connect to local Cosmos DB
		if (isSetupClient) {
			CosmosDbFactory factory = new CosmosDbFactory() {

				@Override
				public CosmosClient createCosmosClient() throws Exception {
					return client;
				}

				@Override
				public CosmosAsyncClient createCosmosAsyncClient() throws Exception {
					return asyncClient;
				}
			};
			CosmosDbConnect.setCosmosDbFactory(factory);
		}
	}

	/**
	 * Stops locally running CosmosDb.
	 * 
	 * @throws Exception If fails to stop.
	 */
	protected void stopCosmosDb() throws Exception {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		// Ensure clear connection factory
		CosmosDbConnect.setCosmosDbFactory(null);
	}

}
