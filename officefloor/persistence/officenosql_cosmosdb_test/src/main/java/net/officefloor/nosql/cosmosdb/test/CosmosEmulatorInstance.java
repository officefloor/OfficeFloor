package net.officefloor.nosql.cosmosdb.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.logger.LoggerUtil;
import net.officefloor.test.logger.LoggerUtil.LoggerReset;

/**
 * Instance of running CosmosDb Emulator.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosEmulatorInstance {

	/**
	 * Base64 key for the emulator.
	 */
	private static final String EMULATOR_BASE64_KEY = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";

	/**
	 * Default local CosmosDb port.
	 */
	private static final int DEFAULT_LOCAL_COSMOS_PORT = 8003;

	/**
	 * Default local CosmosDb direct port.
	 */
	private static final int DEFAULT_LOCAL_COSMOS_DIRECT_PORT_START = 8050;

	/**
	 * Default number of partitions for emulator.
	 */
	private static final int DEFAULT_PARTITION_COUNT = -1;

	/**
	 * Default {@link ConsistencyLevel}.
	 */
	private static final ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = ConsistencyLevel.EVENTUAL;

	/**
	 * Default CosmosDb emulator start time.
	 */
	private static final int DEFAULT_EMULATOR_START_TIMEOUT = 300;

	/**
	 * Provides default {@link CosmosEmulatorInstance}.
	 */
	public static final CosmosEmulatorInstance DEFAULT = new CosmosEmulatorInstance();

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
		 * Direct port start.
		 */
		private int directPortStart = DEFAULT_LOCAL_COSMOS_DIRECT_PORT_START;

		/**
		 * Partition count.
		 */
		private int partitionCount = DEFAULT_PARTITION_COUNT;

		/**
		 * Key.
		 */
		private String key = EMULATOR_BASE64_KEY;

		/**
		 * {@link ConsistencyLevel}.
		 */
		private ConsistencyLevel consistencyLevel = DEFAULT_CONSISTENCY_LEVEL;

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
		 * Specifies the direct port start.
		 * 
		 * @param directPortStart Direct port start.
		 * @return <code>this</code>.
		 */
		public Configuration directPortStart(int directPortStart) {
			this.directPortStart = directPortStart;
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
		 * Specifies the key.
		 * 
		 * @param key Key.
		 * @return <code>this</code>.
		 */
		public Configuration key(String key) {
			this.key = key;
			return this;
		}

		/**
		 * Specifies the {@link ConsistencyLevel}.
		 * 
		 * @param consistencyLevel {@link ConsistencyLevel}.
		 * @return <code>this</code>.
		 */
		public Configuration consistencyLevel(ConsistencyLevel consistencyLevel) {
			this.consistencyLevel = consistencyLevel;
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
	private CosmosClient cosmosClient = null;

	/**
	 * {@link CosmosAsyncClient}.
	 */
	private CosmosAsyncClient cosmosAsyncClient = null;

	/**
	 * Instantiate default instance.
	 */
	private CosmosEmulatorInstance() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public CosmosEmulatorInstance(Configuration configuration) {
		this.configuration = configuration;
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
	 * Obtains the key to connect.
	 * 
	 * @return Key to connect.
	 */
	public String getKey() {
		return this.configuration.key;
	}

	/**
	 * Obtains the certificate from the Cosmos DB Emulator.
	 * 
	 * @return Certificate.
	 * @throws Exception If fails to obtain the certificate.
	 */
	public String getCosmosEmulatorCertificate() throws Exception {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.setSSLContext(new SSLContextBuilder().loadTrustMaterial(new TrustSelfSignedStrategy()).build())
				.build()) {
			HttpResponse response = httpClient.execute(new HttpGet(this.getEndpointUrl() + "/_explorer/emulator.pem"));
			int responseStatus = response.getStatusLine().getStatusCode();
			String responseEntity = EntityUtils.toString(response.getEntity());
			if (responseStatus != 200) {
				throw new Exception("Failed retrieving Cosmos DB certificate with status " + responseStatus + "\n\n"
						+ responseEntity);
			}
			return responseEntity;
		}
	}

	/**
	 * Obtains the {@link CosmosClient}.
	 * 
	 * @return {@link CosmosClient}.
	 */
	public CosmosClient getCosmosClient() {

		// Lazy create the client
		if (this.cosmosClient == null) {
			try {
				Constructor<CosmosClient> constructor = CosmosClient.class
						.getDeclaredConstructor(CosmosClientBuilder.class);
				constructor.setAccessible(true);
				this.cosmosClient = constructor.newInstance(new CosmosClientBuilder() {

					@Override
					public CosmosAsyncClient buildAsyncClient() {
						return CosmosEmulatorInstance.this.getCosmosAsyncClient();
					}
				});
			} catch (Exception ex) {
				throw new IllegalStateException("Failed wrapping " + CosmosAsyncClient.class.getSimpleName() + " with "
						+ CosmosClient.class.getSimpleName(), ex);
			}
		}

		// Return the client
		return this.cosmosClient;
	}

	/**
	 * Obtains the {@link CosmosAsyncClient}.
	 * 
	 * @return {@link CosmosAsyncClient}.
	 */
	public CosmosAsyncClient getCosmosAsyncClient() {
		return this.cosmosDb.connectToDockerInstance(() -> {

			// Lazy create the client
			if (this.cosmosAsyncClient == null) {

				// Indicate connecting
				System.out.print("Connecting to Cosmos DB Emulator ...");
				System.out.flush();

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

									// Attempt to obtain the certificate
									String certificate = this.getCosmosEmulatorCertificate();

									// Create builder that allows unsigned SSL certificates
									CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
											.endpoint(this.getEndpointUrl()).key(this.configuration.key)
											.contentResponseOnWriteEnabled(true)
											.consistencyLevel(this.configuration.consistencyLevel);

									// Initialise for self signed certificate
									CosmosSelfSignedCertificate.initialise(clientBuilder, certificate);

									// Create client
									this.cosmosAsyncClient = clientBuilder.buildAsyncClient();

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
						} while (this.cosmosAsyncClient == null);

					} catch (Exception ex) {
						return JUnitAgnosticAssert.fail(ex);
					}

				} finally {
					// Reset logger
					loggerReset.reset();
				}

				// Indicate connecting
				System.out.println(" connected");
			}

			// Return the cosmos client
			return this.cosmosAsyncClient;
		});
	}

	/**
	 * Ensures the CosmosDb Emulator is started.
	 * 
	 * @throws Exception If fails to start the CosmosDb Emulator.
	 * @return Number of partitions just started.
	 */
	public synchronized int ensureEmulatorStarted() throws Exception {

		// Determine if already running
		if (this.cosmosDb != null) {
			return 0; // already running
		}

		// Obtain the directory for Cosmos data
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String username = System.getProperty("user.name");
		File cosmosDataDir = new File(tempDir, "officefloor/" + username + "/cosmos-db-emulator");
		if (!cosmosDataDir.exists()) {
			cosmosDataDir.mkdirs();
		}

		// Obtain the direct ports
		int directPortOne = this.configuration.directPortStart;
		int directPortTwo = directPortOne + 1;
		int directPortThree = directPortTwo + 1;
		int directPortFour = directPortThree + 1;

		// Generate the exposed ports
		List<Integer> ports = Arrays.asList(this.configuration.port, directPortOne, directPortTwo, directPortThree,
				directPortFour);
		List<PortBinding> portBindings = ports.stream()
				.map(port -> new PortBinding(Binding.bindIpAndPort("0.0.0.0", port), ExposedPort.tcp(port)))
				.collect(Collectors.toList());
		List<ExposedPort> exposedPorts = ports.stream().map(port -> ExposedPort.tcp(port)).collect(Collectors.toList());

		// Create the environment variables
		List<String> environment = new ArrayList<>();
		if (this.configuration.partitionCount >= 0) {
			environment.add("AZURE_COSMOS_EMULATOR_PARTITION_COUNT=" + this.configuration.partitionCount);
		}
		environment.add("AZURE_COSMOS_EMULATOR_KEY=" + this.configuration.key);
		environment.add("AZURE_COSMOS_EMULATOR_ARGS=/DisableRateLimiting /NoUI /NoExplorer " + " /Port="
				+ this.configuration.port + " /DirectPorts=" + directPortOne + "," + directPortTwo + ","
				+ directPortThree + "," + directPortFour + " /Consistency=" + this.configuration.consistencyLevel);

		// Start Cosmos DB
		final String IMAGE_NAME = "mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest";
		final String CONTAINER_NAME = "officefloor-cosmosdb";
		this.cosmosDb = OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
			final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(portBindings)
					.withBinds(Bind.parse(cosmosDataDir.getAbsolutePath() + ":/tmp/cosmos"));
			return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig)
					.withEnv(environment).withExposedPorts(exposedPorts);
		});

		// Register shutdown of emulator
		Runtime.getRuntime().addShutdownHook(new Thread(() -> CosmosEmulatorInstance.this.shutdownEmulator()));

		// Just started
		return this.configuration.partitionCount <= 0 ? 10 : this.configuration.partitionCount;
	}

	/**
	 * Shuts down the CosmosDb Emulator.
	 */
	public synchronized void shutdownEmulator() {
		if (this.cosmosDb != null) {
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
						// Shutdown
						this.cosmosDb.close();
					}

				} finally {
					// Ensure release clients
					this.cosmosClient = null;
					this.cosmosAsyncClient = null;
				}
			} finally {
				// Ensure release to allow restart
				this.cosmosDb = null;
			}
		}
	}

}
