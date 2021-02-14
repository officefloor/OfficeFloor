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

import java.util.function.BiConsumer;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.endpointdiscovery.Constants;
import com.amazonaws.regions.RegionMetadata;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;
import net.officefloor.test.system.AbstractEnvironmentOverride;

/**
 * Local environment for AWS.
 * 
 * @author Daniel Sagenschneider
 */
public class AwsLocalEnvironment extends AbstractEnvironmentOverride<AwsLocalEnvironment> {

	/**
	 * Loads the AWS environment settings.
	 * 
	 * @param loadEnvironmentSetting Receives the AWS environment settings.
	 */
	public static void loadAwsEnvironmentSettings(BiConsumer<String, String> loadEnvironmentSetting) {

		// Configure connection for standard
		loadEnvironmentSetting.accept(Constants.ENDPOINT_DISCOVERY_ENVIRONMENT_VARIABLE, "true");
		loadEnvironmentSetting.accept(SDKGlobalConfiguration.AWS_REGION_ENV_VAR, AmazonDynamoDbConnect.LOCAL_REGION);

		// Credentials
		final String AWS_ACCESS_KEY = "OFFICEFLOOR_SAM_LOCAL_TEST_ACCESS_KEY";
		final String AWS_SECRET_KEY = "OFFICEFLOOR_SAM_LOCAL_TEST_SECRET_KEY";
		loadEnvironmentSetting.accept(SDKGlobalConfiguration.ACCESS_KEY_ENV_VAR, AWS_ACCESS_KEY);
		loadEnvironmentSetting.accept(SDKGlobalConfiguration.ALTERNATE_ACCESS_KEY_ENV_VAR, AWS_ACCESS_KEY);
		loadEnvironmentSetting.accept(SDKGlobalConfiguration.SECRET_KEY_ENV_VAR, AWS_SECRET_KEY);
		loadEnvironmentSetting.accept(SDKGlobalConfiguration.ALTERNATE_SECRET_KEY_ENV_VAR, AWS_SECRET_KEY);
	}

	/**
	 * Cleans up the {@link RegionMetadata}.
	 */
	private Runnable cleanUpMetaData;

	/**
	 * {@link OverrideReset}.
	 */
	private OverrideReset reset;

	/**
	 * Instantiate with environment for local {@link AmazonDynamoDB}.
	 */
	public AwsLocalEnvironment() {
		loadAwsEnvironmentSettings((name, value) -> this.property(name, value));
	}

	/**
	 * Sets up the environment.
	 * 
	 * @param port Port for DynamoDB.
	 */
	public void setupEnvironment(int port) {

		// Capture clean up of local region meta-data setup
		this.cleanUpMetaData = AmazonDynamoDbConnect.setupLocalDynamoMetaData(port);

		// Override the environment
		this.reset = this.override();
	}

	/**
	 * Tears down the environment.
	 */
	public void tearDownEnvironment() {
		try {
			// Reinstate region meta-data
			if (this.cleanUpMetaData != null) {
				this.cleanUpMetaData.run();
			}

		} finally {
			// Reset the environment
			if (this.reset != null) {
				this.reset.resetOverrides();
			}
		}
	}

}
