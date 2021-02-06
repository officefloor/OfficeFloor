/*-
 * #%L
 * DynamoDB Connect
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

package net.officefloor.nosql.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
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
	 * Environment variable to determine if running AWS SAM locally.
	 */
	public static final String AWS_SAM_LOCAL = "AWS_SAM_LOCAL";

	/**
	 * DynamoDB name for local SAM network.
	 */
	public static final String DYNAMODB_LOCAL = "officefloor-dynamodb";

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

		// Determine if running within local SAM
		if ("true".equals(System.getenv(AWS_SAM_LOCAL))) {

			// Connect to local SAM DynamoDB
			System.out.println("Connecting to local SAM DynamoDB");
			return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
					new EndpointConfiguration("http://" + DYNAMODB_LOCAL + ":8000", Regions.DEFAULT_REGION.getName()))
					.build();
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
