/*-
 * #%L
 * DynamoDB Persistence Testing
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

package net.officefloor.nosql.dynamodb.test;

import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbFactory;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;

/**
 * Abstract JUnit DynamoDb connect functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractDynamoDbConnectJunit {

	/**
	 * Default local DynamoDb port.
	 */
	public static final int DEFAULT_LOCAL_DYNAMO_PORT = 8001;

	/**
	 * Default timeout for starting the emulator.
	 */
	public static final int DEFAULT_EMULATOR_START_TIMEOUT = 30;

	/**
	 * <p>
	 * Configuration of DynamoDb.
	 * <p>
	 * Follows builder pattern to allow configuring and passing to
	 * {@link AbstractDynamoDbConnectJunit} constructor.
	 */
	public static class Configuration {

		/**
		 * Port.
		 */
		private int port = DEFAULT_LOCAL_DYNAMO_PORT;

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
	 * {@link AwsLocalEnvironment}.
	 */
	private final AwsLocalEnvironment environment = new AwsLocalEnvironment();

	/**
	 * {@link AmazonDynamoDbFactory} to {@link AmazonDynamoDB}
	 * {@link DockerContainerInstance}.
	 */
	protected final AmazonDynamoDbFactory dynamoFactory;

	/**
	 * Avoid recreating {@link AmazonDynamoDB}.
	 */
	private AmazonDynamoDB amazonDynamoDb = null;

	/**
	 * Avoid recreating {@link DynamoDBMapper}.
	 */
	private DynamoDBMapper dynamoDbMapper = null;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractDynamoDbConnectJunit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractDynamoDbConnectJunit(Configuration configuration) {
		this.configuration = configuration;

		// Create the DynamoDb factory
		this.dynamoFactory = () -> AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(new EndpointConfiguration("http://localhost:" + this.configuration.port,
						AmazonDynamoDbConnect.LOCAL_REGION))
				.build();
	}

	/**
	 * Obtains the {@link AmazonDynamoDB}.
	 * 
	 * @return {@link AmazonDynamoDB}.
	 */
	public AmazonDynamoDB getAmazonDynamoDb() {

		// Lazy create
		if (this.amazonDynamoDb == null) {
			AmazonDynamoDB dynamoDb;
			try {

				// Create connection to dynamo
				dynamoDb = this.dynamoFactory.createAmazonDynamoDB();

				// Try until time out (as may take time for DynamoDb to come up)
				final int MAX_SETUP_TIME = this.configuration.startTimeout * 1000; // milliseconds
				long startTimestamp = System.currentTimeMillis();
				boolean isRunning = false;
				while (!isRunning) {
					try {
						// Attempt to connect
						dynamoDb.listTables();

						// Flag now running
						isRunning = true;

					} catch (Exception ex) {

						// Ensure stop if non-repeatable error
						if (ex instanceof AmazonDynamoDBException) {
							AmazonDynamoDBException dynamoException = (AmazonDynamoDBException) ex;
							if (dynamoException.getErrorType() == ErrorType.Client) {
								// Client error, so propagate
								throw dynamoException;
							}
						}

						// Failed connect, determine if try again
						long currentTimestamp = System.currentTimeMillis();
						if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {
							throw new RuntimeException("Timed out setting up DynamoDb ("
									+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

						} else {
							// Try again in a little
							Thread.sleep(10);
						}
					}
				}
			} catch (Exception ex) {
				return JUnitAgnosticAssert.fail(ex);
			}

			// Specify for return
			this.amazonDynamoDb = dynamoDb;
		}

		// Return the client
		return this.amazonDynamoDb;
	}

	/**
	 * Obtains the {@link DynamoDBMapper}.
	 * 
	 * @return {@link DynamoDBMapper}.
	 */
	public DynamoDBMapper getDynamoDbMapper() {

		// Lazy create
		if (this.dynamoDbMapper == null) {
			this.dynamoDbMapper = new DynamoDBMapper(this.getAmazonDynamoDb());
		}

		// Return the mapper
		return this.dynamoDbMapper;
	}

	/**
	 * Obtains the DynamoDb port.
	 * 
	 * @return DynamoDb port.
	 */
	protected int getDynamoDbPort() {
		return this.configuration.port;
	}

	/**
	 * Starts the {@link AmazonDynamoDB} locally.
	 * 
	 * @throws Exception If fails to start.
	 */
	protected void startAmazonDynamoDb() throws Exception {

		// Avoid starting up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			System.out.println("Docker not available. Unable to start DynamoDb.");
			return;
		}

		// Setup credentials
		this.environment.setupEnvironment(this.configuration.port);

		// Override to connect to local DynamoDb
		AmazonDynamoDbConnect.setAmazonDynamoDbFactory(this.dynamoFactory);

		// Extend the start
		this.extendStart();
	}

	/**
	 * Allows overriding to extend starting.
	 * 
	 * @throws Exception Possible start failure.
	 */
	protected void extendStart() throws Exception {
		// Does nothing by default
	}

	/**
	 * Stops {@link AmazonDynamoDB}.
	 * 
	 * @throws Exception If fails to stop.
	 */
	protected void stopAmazonDynamoDb() throws Exception {

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		try {
			try {
				try {
					// Stop the client
					if (this.amazonDynamoDb != null) {
						this.amazonDynamoDb.shutdown();
					}
				} finally {

					// Clear the client instances
					this.amazonDynamoDb = null;
					this.dynamoDbMapper = null;

					// Extend the stop
					this.extendStop();
				}
			} finally {
				// Ensure remove environment
				this.environment.tearDownEnvironment();
			}
		} finally {
			// Clear connection factory
			AmazonDynamoDbConnect.setAmazonDynamoDbFactory(null);
		}
	}

	/**
	 * Allows overriding to extend stop.
	 * 
	 * @throws Exception Possible start failure.
	 */
	protected void extendStop() throws Exception {
		// Does nothing by default
	}

}
