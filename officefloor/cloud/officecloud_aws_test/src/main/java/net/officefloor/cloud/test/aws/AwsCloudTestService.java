package net.officefloor.cloud.test.aws;

import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import net.officefloor.cabinet.dynamo.DynamoOfficeStore;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.cloud.test.CloudTestCabinet;
import net.officefloor.cloud.test.CloudTestService;
import net.officefloor.cloud.test.CloudTestServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.nosql.dynamodb.test.AbstractDynamoDbJunit;

/**
 * {@link CloudTestService} for AWS.
 * 
 * @author Daniel Sagenschneider
 */
public class AwsCloudTestService implements CloudTestService, CloudTestServiceFactory {

	/*
	 * ================ CloudTestServiceFactory ================
	 */

	@Override
	public CloudTestService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== CloudTestService ====================
	 */

	@Override
	public String getCloudServiceName() {
		return "AWS";
	}

	@Override
	public CloudTestCabinet getCloudTestCabinet() {
		return new AwsCloudTestCabinet();
	}

	/**
	 * {@link CloudTestCabinet} for AWS.
	 */
	private static class AwsCloudTestCabinet extends AbstractDynamoDbJunit implements CloudTestCabinet {

		/*
		 * ============== CloudTestCabinet ===================
		 */

		@Override
		public void startDataStore() {
			try {
				this.startAmazonDynamoDb();
			} catch (Exception ex) {
				fail(ex);
			}
		}

		@Override
		public OfficeStore getOfficeStore() {
			AmazonDynamoDB amazonDynamoDb = this.getAmazonDynamoDb();
			return new DynamoOfficeStore(amazonDynamoDb);
		}

		@Override
		public void stopDataStore() {
			try {
				this.stopAmazonDynamoDb();
			} catch (Exception ex) {
				fail(ex);
			}
		}
	}

}
