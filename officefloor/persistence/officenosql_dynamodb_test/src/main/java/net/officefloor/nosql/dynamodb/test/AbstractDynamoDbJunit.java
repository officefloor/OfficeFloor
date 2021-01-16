package net.officefloor.nosql.dynamodb.test;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import net.officefloor.docker.test.DockerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.system.AbstractEnvironmentOverride;

/**
 * Abstract JUnit DynamoDb functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractDynamoDbJunit {

	/**
	 * <p>
	 * Configuration of DynamoDb.
	 * <p>
	 * Follows builder pattern to allow configuring and passing to
	 * {@link AbstractDynamoDbJunit} constructor.
	 */
	public static class Configuration {

		/**
		 * Port.
		 */
		private int port = 8001;

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
	private final Configuration configuration;

	/**
	 * Sets up the AWS credentials.
	 */
	private class AwsCredentials extends AbstractEnvironmentOverride<AwsCredentials> {

		/**
		 * {@link OverrideReset}.
		 */
		private OverrideReset reset;

		/**
		 * Instantiate with credentials for local {@link AmazonDynamoDB}.
		 */
		private AwsCredentials() {
			super("AWS_ACCESS_KEY", "local", "AWS_SECRET_KEY", "local");
		}

		/**
		 * Sets up the credentials.
		 */
		private void setupCredentials() {
			this.reset = this.override();
		}

		/**
		 * Tears down the credentials.
		 */
		private void tearDownCredentials() {
			this.reset.resetOverrides();
		}
	}

	private final AwsCredentials credentials = new AwsCredentials();

	/**
	 * {@link AmazonDynamoDB} {@link DockerInstance}.
	 */
	private DockerInstance dynamoDb;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractDynamoDbJunit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractDynamoDbJunit(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Obtains the {@link AmazonDynamoDB}.
	 * 
	 * @return {@link AmazonDynamoDB}.
	 * @throws Exception If fails to obtain {@link AmazonDynamoDB}.
	 */
	public AmazonDynamoDB getDynamoDb() throws Exception {

		// Create dynamo
		AmazonDynamoDB dynamo = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
				new AwsClientBuilder.EndpointConfiguration("http://localhost:" + this.configuration.port, "us-west-2"))
				.build();

		// Try until time out (as may take time for DynamoDb to come up)
		final int MAX_SETUP_TIME = 30000; // milliseconds
		long startTimestamp = System.currentTimeMillis();
		for (;;) {
			try {
				// Attempt to connect
				dynamo.listTables();

				// Successful, so running
				return dynamo;

			} catch (Exception ex) {

				// Failed connect, determine if try again
				long currentTimestamp = System.currentTimeMillis();
				if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {
					throw new RuntimeException(
							"Timed out setting up DynamoDb (" + (currentTimestamp - startTimestamp) + " milliseconds)",
							ex);

				} else {
					// Try again in a little
					Thread.sleep(10);
				}
			}
		}
	}

	/**
	 * Starts the {@link AmazonDynamoDB} locally.
	 * 
	 * @throws Exception If fails to start.
	 */
	public void startAmazonDynamoDb() throws Exception {

		// Avoid starting up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			System.out.println("Docker not available. Unable to start DynamoDb.");
			return;
		}

		// Setup credentials
		this.credentials.setupCredentials();

		final String IMAGE_NAME = "amazon/dynamodb-local:latest";
		final String CONTAINER_NAME = "officefloor_dynamodb";

		// Ensure DynamoDb running
		this.dynamoDb = OfficeFloorDockerUtil.ensureAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
			final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(
					new PortBinding(Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(8000)));
			return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig);
		});
	}

	/**
	 * Stops {@link AmazonDynamoDB}.
	 * 
	 * @throws Exception If fails to stop.
	 */
	public void stopAmazonDynamoDb() throws Exception {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		try {
			// Stop DynamoDb
			this.dynamoDb.shutdown();

		} finally {
			// Ensure remove credentials
			this.credentials.tearDownCredentials();
		}
	}

}