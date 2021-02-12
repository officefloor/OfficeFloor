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