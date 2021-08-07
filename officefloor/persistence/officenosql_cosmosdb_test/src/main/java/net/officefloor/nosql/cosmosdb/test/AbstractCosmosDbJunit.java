/*-
 * #%L
 * CosmosDB Persistence Testing
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.nosql.cosmosdb.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
		 * Specifies the port.
		 * 
		 * @param port Port.
		 * @return <code>this</code>.
		 */
		public Configuration port(int port) {
			this.port = port;
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
						final int MAX_SETUP_TIME = 10_000; // milliseconds
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
									String base64Key = Base64.getEncoder()
											.encodeToString("COSMOS_DB_LOCAL".getBytes(Charset.forName("UTF-8")));
									clientBuilder.endpoint(this.getEndpointUrl()).key(base64Key).gatewayMode();

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

		// Ensure have Cosmos DB emulator image
		final String IMAGE_NAME = "officefloor-cosmosdb:emulator";
		OfficeFloorDockerUtil.ensureImageAvailable(IMAGE_NAME, () -> {

			// Ensure files are available
			File targetDir = new File(".", "target/cosmosDb");
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			this.ensureFileInTargetDirectory("Dockerfile", targetDir);
			this.ensureFileInTargetDirectory("package.json", targetDir);
			this.ensureFileInTargetDirectory("index.js", targetDir);
			this.ensureFileInTargetDirectory("cosmos.sh", targetDir);

			// Build from target directory
			return targetDir;
		});

		// Start Cosmos DB
		final String CONTAINER_NAME = "officefloor-cosmosdb";
		this.cosmosDb = OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
			final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(
					new PortBinding(Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(8080)));
			return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig);
		});

		// Override to connect to local Cosmos DB
		if (isSetupClient) {
			CosmosDbFactory factory = () -> this.getClient(() -> null, (builder) -> builder, (setter) -> {
			});
			CosmosDbConnect.setCosmosDbFactory(factory);
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
	 * Ensures the file is in the target directory.
	 * 
	 * @param fileName  Name of file to copy into target directory.
	 * @param targetDir Target directory.
	 */
	private void ensureFileInTargetDirectory(String fileName, File targetDir) throws IOException {

		// Obtain contents of file
		String contents;
		String packageFolderPath = this.getClass().getPackage().getName().replace('.', '/');
		try (InputStream fileInput = this.getClass().getClassLoader()
				.getResourceAsStream(packageFolderPath + "/" + fileName)) {
			JUnitAgnosticAssert.assertNotNull(fileInput, "Unable to find file " + fileName);
			contents = this.readContents(fileInput);
		}

		// Determine if file already exists
		File targetFile = new File(targetDir, fileName);
		if (targetFile.exists()) {

			// Determine if have to overwrite contents (as not as expected)
			String targetContents = this.readContents(new FileInputStream(targetFile));
			if (targetContents.equals(contents)) {
				// File exists as required
				return;
			}
		}

		// Write file to target directory
		try (Writer output = new FileWriter(targetFile)) {
			output.write(contents);
		}
	}

	/**
	 * Reads the contents.
	 * 
	 * @param input {@link InputStream}.
	 * @return Contents.
	 * @throws IOException If fails to read {@link InputStream}.
	 */
	private String readContents(InputStream input) throws IOException {

		// Obtain the contents
		StringWriter contents = new StringWriter();
		Reader fileReader = new InputStreamReader(input);
		for (int character = fileReader.read(); character != -1; character = fileReader.read()) {
			contents.write(character);
		}

		// Return the contents
		return contents.toString();
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
