/*-
 * #%L
 * DynamoDB Persistence Testing
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

package net.officefloor.nosql.dynamodb.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.endpointdiscovery.Constants;
import com.amazonaws.regions.InMemoryRegionImpl;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionMetadata;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import net.officefloor.docker.test.DockerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbFactory;
import net.officefloor.test.SkipUtil;
import net.officefloor.test.system.AbstractEnvironmentOverride;

/**
 * Abstract JUnit DynamoDb functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractDynamoDbJunit {

	/**
	 * Test {@link Region}.
	 */
	private static final String TEST_REGION = "test-region";

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
	 * Sets up the AWS environment.
	 */
	private class AwsEnvironment extends AbstractEnvironmentOverride<AwsEnvironment> {

		/**
		 * {@link OverrideReset}.
		 */
		private OverrideReset reset;

		/**
		 * Instantiate with environment for local {@link AmazonDynamoDB}.
		 */
		private AwsEnvironment() {

			// Configure connection for standard
			this.property(Constants.ENDPOINT_DISCOVERY_ENVIRONMENT_VARIABLE, "true");
			this.property(SDKGlobalConfiguration.AWS_REGION_ENV_VAR, TEST_REGION);

			// Credentials
			this.property(SDKGlobalConfiguration.ACCESS_KEY_ENV_VAR, "local");
			this.property(SDKGlobalConfiguration.SECRET_KEY_ENV_VAR, "local");
		}

		/**
		 * Sets up the environment.
		 */
		private void setupEnvironment() {
			this.reset = this.override();
		}

		/**
		 * Tears down the environment.
		 */
		private void tearDownEnvironment() {
			if (this.reset != null) {
				this.reset.resetOverrides();
			}
		}
	}

	/**
	 * {@link AwsEnvironment}.
	 */
	private final AwsEnvironment environment = new AwsEnvironment();

	/**
	 * {@link AmazonDynamoDbFactory} to {@link AmazonDynamoDB}
	 * {@link DockerInstance}.
	 */
	private final AmazonDynamoDbFactory dynamoFactory;

	/**
	 * {@link AmazonDynamoDB} instances.
	 */
	private final List<AmazonDynamoDB> dynamos = new LinkedList<>();

	/**
	 * Capture the {@link RegionMetadata}.
	 */
	private RegionMetadata originalMetaData;

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

		// Create the DynamoDb factory
		this.dynamoFactory = () -> AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
				new EndpointConfiguration("http://localhost:" + this.configuration.port, TEST_REGION)).build();
	}

	/**
	 * Obtains the {@link AmazonDynamoDB}.
	 * 
	 * @return {@link AmazonDynamoDB}.
	 * @throws Exception If fails to obtain {@link AmazonDynamoDB}.
	 */
	public AmazonDynamoDB getAmazonDynamoDb() throws Exception {

		// Create connection to dynamo
		AmazonDynamoDB dynamo = this.dynamoFactory.createAmazonDynamoDB();
		this.dynamos.add(dynamo);

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
	 * Obtains the {@link DynamoDBMapper}.
	 * 
	 * @return {@link DynamoDBMapper}.
	 * @throws Exception If fails to obtain {@link DynamoDBMapper}.
	 */
	public DynamoDBMapper getDynamoDbMapper() throws Exception {
		return new DynamoDBMapper(this.getAmazonDynamoDb());
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

		// Capture the region meta data
		this.originalMetaData = RegionUtils.getRegionMetadata();

		// Override meta data with test region (avoid connecting to AWS for unit tests)
		InMemoryRegionImpl testRegion = new InMemoryRegionImpl(TEST_REGION, "test.officefloor.net");
		testRegion.addHttp(AmazonDynamoDB.ENDPOINT_PREFIX);
		testRegion.addEndpoint(AmazonDynamoDB.ENDPOINT_PREFIX, "http://localhost:" + this.configuration.port);
		RegionUtils.initializeWithMetadata(new RegionMetadata(Arrays.asList(new Region(testRegion))));

		// Setup credentials
		this.environment.setupEnvironment();

		// Ensure DynamoDb running
		final String IMAGE_NAME = "amazon/dynamodb-local:latest";
		final String CONTAINER_NAME = "officefloor_dynamodb";
		this.dynamoDb = OfficeFloorDockerUtil.ensureAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
			final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(
					new PortBinding(Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(8000)));
			return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig);
		});

		// Override to connect to local DynamoDb
		AmazonDynamoDbConnect.setAmazonDynamoDbFactory(this.dynamoFactory);
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
			try {
				try {
					try {
						// Stop the clients
						for (AmazonDynamoDB client : this.dynamos) {
							client.shutdown();
						}
					} finally {
						// Stop DynamoDb
						if (this.dynamoDb != null) {
							this.dynamoDb.shutdown();
						}
					}
				} finally {
					// Ensure remove environment
					this.environment.tearDownEnvironment();
				}
			} finally {
				// Reinstate region meta-data
				if (this.originalMetaData == null) {
					RegionUtils.initializeWithMetadata(this.originalMetaData);
				}
			}
		} finally {
			// Clear connection factory
			AmazonDynamoDbConnect.setAmazonDynamoDbFactory(null);
		}
	}

}
