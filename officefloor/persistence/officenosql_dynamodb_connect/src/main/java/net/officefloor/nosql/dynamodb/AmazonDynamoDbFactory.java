package net.officefloor.nosql.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * Factory for {@link AmazonDynamoDB} connection.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AmazonDynamoDbFactory {

	/**
	 * Creates the {@link AmazonDynamoDB}.
	 * 
	 * @return {@link AmazonDynamoDB}.
	 * @throws Exception If fails to create {@link AmazonDynamoDB}.
	 */
	AmazonDynamoDB createAmazonDynamoDB() throws Exception;

}