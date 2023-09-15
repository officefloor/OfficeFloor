package net.officefloor.cabinet.dynamo;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import net.officefloor.cabinet.source.OfficeStoreServiceFactory;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;

public class DynamoOfficeStoreServiceFactory implements OfficeStoreServiceFactory {

	/*
	 * ================== OfficeStoreServiceFactory ==================
	 */

	@Override
	public OfficeStore createService(ServiceContext context) throws Throwable {

		// Build the Dynamo DB connection
		// TODO configure connection
		AmazonDynamoDB amazonDynamoDb = AmazonDynamoDBClientBuilder.standard()
				.withEndpointConfiguration(
						new EndpointConfiguration("http://localhost:" + 8001, AmazonDynamoDbConnect.LOCAL_REGION))
				.build();

		// Return the OfficeStore
		return new DynamoOfficeStore(amazonDynamoDb);
	}

}
