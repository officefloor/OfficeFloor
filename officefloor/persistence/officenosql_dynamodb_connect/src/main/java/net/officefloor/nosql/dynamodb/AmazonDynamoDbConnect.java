package net.officefloor.nosql.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link AmazonDynamoDB} connect functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class AmazonDynamoDbConnect {

	/**
	 * <p>
	 * Sets using the {@link AmazonDynamoDbFactory}.
	 * <p>
	 * This is typically used for testing to allow overriding the
	 * {@link AmazonDynamoDbFactory} being used.
	 * 
	 * @param amazonDynamoDbFactory {@link AmazonDynamoDbFactory}. May be
	 *                              <code>null</code> to not override.
	 */
	public static void setAmazonDynamoDbFactory(AmazonDynamoDbFactory amazonDynamoDbFactory) {
		if (amazonDynamoDbFactory != null) {
			// Undertake override
			threadLocalAmazonDynamoDbFactoryOverride.set(amazonDynamoDbFactory);
		} else {
			// Clear the override
			threadLocalAmazonDynamoDbFactoryOverride.remove();
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link AmazonDynamoDbFactory}.
	 */
	private static ThreadLocal<AmazonDynamoDbFactory> threadLocalAmazonDynamoDbFactoryOverride = new ThreadLocal<>();

	/**
	 * <p>
	 * Connects to {@link AmazonDynamoDB}.
	 * <p>
	 * Note that the {@link AmazonDynamoDB} instance is not managed. It will need to
	 * be manually shutdown once use is complete.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link AmazonDynamoDB}.
	 * @throws Exception If fails to connect.
	 */
	public static AmazonDynamoDB connect(SourceContext context) throws Exception {

		// Determine if thread local instance available to use
		AmazonDynamoDbFactory factory = threadLocalAmazonDynamoDbFactoryOverride.get();
		if (factory == null) {

			// No thread local, so see if configured factory
			factory = context.loadOptionalService(AmazonDynamoDbServiceFactory.class);
		}
		if (factory != null) {
			// Configured factory available, so use
			return factory.createAmazonDynamoDB();
		}

		// No factory, so provide default connection
		return AmazonDynamoDBClientBuilder.defaultClient();
	}

	/**
	 * All access via static methods.
	 */
	private AmazonDynamoDbConnect() {
	}

}