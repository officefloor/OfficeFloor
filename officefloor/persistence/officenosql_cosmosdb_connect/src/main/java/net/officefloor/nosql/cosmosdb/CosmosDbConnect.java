package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link CosmosDatabase} connect functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbConnect {

	/**
	 * {@link Property} name for the URL for the {@link CosmosClient}.
	 */
	public static final String PROPERTY_URL = "url";

	/**
	 * {@link Property} name for the key.
	 */
	public static final String PROPERTY_KEY = "key";

	/**
	 * <p>
	 * Sets using the {@link CosmosDbFactory}.
	 * <p>
	 * This is typically used for testing to allow overriding the
	 * {@link CosmosDbFactory} being used.
	 * 
	 * @param cosmosDbFactory {@link CosmosDbFactory}. May be <code>null</code> to
	 *                        not override.
	 */
	public static void setCosmosDbFactory(CosmosDbFactory cosmosDbFactory) {
		if (cosmosDbFactory != null) {
			// Undertake override
			threadLocalCosmosDbFactoryOverride.set(cosmosDbFactory);
		} else {
			// Clear the override
			threadLocalCosmosDbFactoryOverride.remove();
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link CosmosDbFactory}.
	 */
	private static ThreadLocal<CosmosDbFactory> threadLocalCosmosDbFactoryOverride = new ThreadLocal<>();

	/**
	 * Creates the {@link CosmosClientBuilder} to connect to {@link CosmosDatabase}
	 * / {@link CosmosAsyncDatabase}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link CosmosClientBuilder}.
	 * @throws Exception If fails to connect.
	 */
	public static CosmosClientBuilder createCosmosClientBuilder(SourceContext context) throws Exception {

		// Determine if thread local instance available to use
		CosmosDbFactory factory = threadLocalCosmosDbFactoryOverride.get();
		if (factory == null) {

			// No thread local, so see if configured factory
			factory = context.loadOptionalService(CosmosDbServiceFactory.class);
		}
		if (factory != null) {
			// Configured factory available, so use
			return factory.createCosmosClientBuilder();
		}

		// No factory, so provide default connection
		String cosmosUrl = context.getProperty(PROPERTY_URL);
		String key = context.getProperty(PROPERTY_KEY);
		return new CosmosClientBuilder().endpoint(cosmosUrl).key(key);
	}

	/**
	 * All access via static methods.
	 */
	private CosmosDbConnect() {
	}

}