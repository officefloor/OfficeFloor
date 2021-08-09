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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;

/**
 * Abstract JUnit DynamoDb functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractDynamoDbJunit extends AbstractDynamoDbConnectJunit {

	/**
	 * <p>
	 * Starts DynamoDb.
	 * <P>
	 * This should not be called directly. It is available for OfficeFloor testing
	 * infrastructure to have consistent way to run DynamoDb.
	 * 
	 * @param port              Port to run DynamoDB on.
	 * @param dockerNetworkName Optional docker network name. May be
	 *                          <code>null</code>.
	 * @return {@link DockerContainerInstance} for running DynamoDb.
	 * @throws Exception If fails to start DynamoDb.
	 */
	public static DockerContainerInstance startDynamoDb(int port, String dockerNetworkName) throws Exception {
		final String IMAGE_NAME = "amazon/dynamodb-local:latest";
		final String CONTAINER_NAME = AmazonDynamoDbConnect.DYNAMODB_SAM_LOCAL_HOST_NAME;
		return OfficeFloorDockerUtil.ensureContainerAvailable(CONTAINER_NAME, IMAGE_NAME, (docker) -> {
			final HostConfig hostConfig = HostConfig.newHostConfig()
					.withPortBindings(new PortBinding(Binding.bindIpAndPort("0.0.0.0", port), ExposedPort.tcp(8000)));
			if (dockerNetworkName != null) {
				hostConfig.withNetworkMode(dockerNetworkName);
			}
			return docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME).withHostConfig(hostConfig);
		});
	}

	/**
	 * {@link AmazonDynamoDB} {@link DockerContainerInstance}.
	 */
	private DockerContainerInstance dynamoDb;

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractDynamoDbJunit() {
		super();
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractDynamoDbJunit(Configuration configuration) {
		super(configuration);
	}

	@Override
	public AmazonDynamoDB getAmazonDynamoDb() {
		return this.dynamoDb.connectToDockerInstance(() -> super.getAmazonDynamoDb());
	}

	@Override
	protected void extendStart() throws Exception {

		// Ensure DynamoDb running
		this.dynamoDb = startDynamoDb(this.getDynamoDbPort(), null);
	}

	@Override
	protected void extendStop() throws Exception {

		// Stop DynamoDb
		if (this.dynamoDb != null) {
			this.dynamoDb.close();
		}
	}

}
