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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.nosql.cosmosdb.CosmosDbConnect;
import net.officefloor.nosql.cosmosdb.CosmosDbFactory;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.logger.LoggerUtil;
import net.officefloor.test.logger.LoggerUtil.LoggerReset;

/**
 * Abstract JUnit CosmosDb functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCosmosDbJunit<T extends AbstractCosmosDbJunit<T>> {

	/**
	 * Default local CosmosDb port.
	 */
	public static final int DEFAULT_LOCAL_COSMOS_PORT = 8003;

	/**
	 * Default number of partitions for emulator.
	 */
	public static final int DEFAULT_PARTITION_COUNT = 2;

	/**
	 * Default CosmosDb emulator start time.
	 */
	public static final int DEFAULT_EMULATOR_START_TIMEOUT = 120;

	/**
	 * Initiate for use.
	 */
	static {
		CosmosSelfSignedCertificate.noOpenSsl();
	}

	/**
	 * <p>
	 * Configuration of CosmosDb.
	 * <p>
	 * Follows builder pattern to allow configuring and passing to
	 * {@link AbstractCosmosDbJunit} constructor.
	 */
	public static class Configuration {

		/**
		 * Port.
		 */
		private int port = DEFAULT_LOCAL_COSMOS_PORT;

		/**
		 * Partition count.
		 */
		private int partitionCount = DEFAULT_PARTITION_COUNT;

		/**
		 * Start timeout.
		 */
		private int startTimeout = DEFAULT_EMULATOR_START_TIMEOUT;

		/**
		 * Specifies the port.
		 * 
		 * @param port Port.
		 * @return <code>this</code>.
		 */
		public Configuration port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Specifies the number of partitions.
		 * 
		 * @param partitionCount Number of partitions.
		 * @return <code>this</code>.
		 */
		public Configuration partitionCount(int partitionCount) {
			this.partitionCount = partitionCount;
			return this;
		}

		/**
		 * Specifies the start timeout.
		 * 
		 * @param startTimeout Start timeout.
		 * @return <code>this</code>.
		 */
		public Configuration startTimeout(int startTimeout) {
			this.startTimeout = startTimeout;
			return this;
		}
	}

	/**
	 * {@link Configuration}.
	 */
	protected final Configuration configuration;

	/**
	 * {@link DockerContainerInstance} for CosmosDb.
	 */
	private DockerContainerInstance cosmosDb;

	/**
	 * {@link CosmosClient}.
	 */
	protected CosmosClient cosmosClient = null;

	/**
	 * {@link CosmosAsyncClient}.
	 */
	protected CosmosAsyncClient cosmosAsyncClient = null;

	/**
	 * Flags to start CosmosDb.
	 */
	protected boolean isStartCosmosDb = true;

	/**
	 * Flags to wait for CosmosDb to be available on start.
	 */
	protected boolean isWaitForCosmosDb = false;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractCosmosDbJunit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractCosmosDbJunit(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Flags whether to start CosmosDb.
	 * 
	 * @param isStart <code>false</code> to not start CosmosDb.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public T start(boolean isStart) {
		this.isStartCosmosDb = isStart;
		return (T) this;
	}

	/**
	 * Sets up to wait on CosmosDB to be available.
	 * 
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	public T waitForCosmosDb() {
		this.isWaitForCosmosDb = true;
		return (T) this;
	}

	/**
	 * Obtains the end point URL.
	 * 
	 * @return End point URL.
	 */
	public String getEndpointUrl() {
		return "https://localhost:" + this.configuration.port;
	}

	/**
	 * Obtains the {@link CosmosClient}.
	 * 
	 * @return {@link CosmosClient}.
	 */
	public CosmosClient getCosmosClient() {
		return this.getClient(() -> this.cosmosClient, (builder) -> builder.buildClient(),
				(client) -> this.cosmosClient = client);
	}

	/**
	 * Obtains the {@link CosmosAsyncClient}.
	 * 
	 * @return {@link CosmosAsyncClient}.
	 */
	public CosmosAsyncClient getCosmosAsyncClient() {
		return this.getClient(() -> this.cosmosAsyncClient, (builder) -> builder.buildAsyncClient(),
				(client) -> this.cosmosAsyncClient = client);
	}

	/**
	 * Obtains to the client.
	 * 
	 * @param <C>     Client type.
	 * @param getter  Obtains the existing client.
	 * @param factory Creates the client from the {@link CosmosClientBuilder}.
	 * @param setter  Specifies the client.
	 * @return Lazy creates the client.
	 */
	private <C> C getClient(Supplier<C> getter, Function<CosmosClientBuilder, C> factory, Consumer<C> setter) {
		return this.cosmosDb.connectToDockerInstance(() -> {

			// Lazy create the client
			C client = getter.get();
			if (client == null) {

				// Disable logging
				LoggerReset loggerReset = LoggerUtil.disableLogging();
				try {

					// Attempt to create client (must wait for CosmosDb to start)
					try {

						// Try until time out (as may take time for ComosDb to come up)
						final int MAX_SETUP_TIME = this.configuration.startTimeout * 1000; // milliseconds
						long startTimestamp = System.currentTimeMillis();
						do {

							// Ignore stderr
							PrintStream originalStdErr = System.err;
							ByteArrayOutputStream stdErrCapture = new ByteArrayOutputStream();
							try {

								// Capture stderr to report on failure to connect
								System.setErr(new PrintStream(stdErrCapture));
								try {

									// Create builder that allows unsigned SSL certificates
									CosmosClientBuilder clientBuilder = new CosmosClientBuilder();

									// Initialise for self signed certificate
									CosmosSelfSignedCertificate.initialise(clientBuilder);

									// Provide location
									String emulatorBase64Key = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
									clientBuilder.endpoint(this.getEndpointUrl()).key(emulatorBase64Key).gatewayMode();

									// Create client
									client = factory.apply(clientBuilder);

								} finally {
									// Ensure reinstate stderr
									System.setErr(originalStdErr);
								}

							} catch (Exception ex) {

								// Failed connect, determine if try again
								long currentTimestamp = System.currentTimeMillis();
								if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {

									// Log the stderr output
									System.err.write(stdErrCapture.toByteArray());

									// Propagate failure to connect
									throw new RuntimeException("Timed out setting up CosmosDb ("
											+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

								} else {
									// Try again in a little
									Thread.sleep(100);
								}
							}
						} while (client == null);

					} catch (Exception ex) {
						return JUnitAgnosticAssert.fail(ex);
					}

					// Specify the client
					setter.accept(client);

				} finally {
					// Reset logger
					loggerReset.reset();
				}
			}

			// Return the cosmos client
			return client;
		});
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

		// Determine if start Cosmos DB emulator
		if (this.isStartCosmosDb) {

			// Generate the exposed ports
			List<Integer> ports = Arrays.asList(this.configuration.port, 10251, 10252, 10253, 10254);
			List<PortBinding> portBindings = ports.stream()
					.map(port -> new PortBinding(Binding.bindIpAndPort("0.0.0.0", port), ExposedPort.tcp(port)))
					.collect(Collectors.toList());
			List<ExposedPort> exposedPorts = ports.stream().map(port -> ExposedPort.tcp(port))
					.collect(Collectors.toList());

			// Start Cosmos DB
			final String IMAGE_NAME = "mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest";
			final String CONTAINER_NAME = "officefloor-cosmosdb";
			this.cosmosDb = OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
				final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(portBindings);
				return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig)
						.withEnv("AZURE_COSMOS_EMULATOR_PARTITION_COUNT=" + this.configuration.partitionCount,
								"AZURE_COSMOS_EMULATOR_ARGS=/port=" + this.configuration.port)
						.withExposedPorts(exposedPorts);
			});

			// Override to connect to local Cosmos DB
			if (isSetupClient) {
				CosmosDbFactory factory = () -> this.getClient(() -> null, (builder) -> builder, (setter) -> {
				});
				CosmosDbConnect.setCosmosDbFactory(factory);
			}

		} else {
			// Provide mock docker instance
			this.cosmosDb = DockerContainerInstance.mockInstance();
		}

		// Determine if wait for Cosmos DB on start
		if (this.isWaitForCosmosDb) {

			// Allow some time for start up
			Thread.sleep(100);

			// Wait for Cosmos DB to be available
			this.getCosmosClient();
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

		try {
			try {
				try {
					// Ensure clients closed
					try {
						if (this.cosmosClient != null) {
							this.cosmosClient.close();
						}

					} finally {
						if (this.cosmosAsyncClient != null) {
							this.cosmosAsyncClient.close();
						}
					}
				} finally {
					// Ensure clear connection factory
					CosmosDbConnect.setCosmosDbFactory(null);
				}

			} finally {
				// Ensure release clients
				this.cosmosClient = null;
				this.cosmosAsyncClient = null;
			}

		} finally {
			// Stop CosmosDb
			if (this.cosmosDb != null) {
				this.cosmosDb.close();
			}
		}
	}

}
