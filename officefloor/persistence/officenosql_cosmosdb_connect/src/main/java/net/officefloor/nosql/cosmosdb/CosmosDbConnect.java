/*-
 * #%L
 * CosmosDB Connect
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

package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.impl.util.CompileUtil;
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
		String cosmosUrl = getProperty(PROPERTY_URL, context);
		String key = getProperty(PROPERTY_KEY, context);
		CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(cosmosUrl).key(key);

		// Allow decorating the builder
		for (CosmosClientBuilderDecorator decorator : context
				.loadOptionalServices(CosmosClientBuilderDecoratorServiceFactory.class)) {
			builder = decorator.decorate(builder);
		}

		// Return the configured builder ready to build client
		return builder;
	}

	/**
	 * Obtains the property value.
	 * 
	 * @param propertyName Property name.
	 * @param context      {@link SourceContext}.
	 * @return Property value.
	 */
	public static String getProperty(String propertyName, SourceContext context) {

		// Obtain the property value (configured overrides environment)
		String environmentName = "COSMOS_" + propertyName.toUpperCase().replace('-', '_');
		String propertyValue = context.getProperty(propertyName, System.getenv(environmentName));

		// Check configured
		if (CompileUtil.isBlank(propertyValue)) {
			throw new IllegalArgumentException("Must configure property '" + propertyName
					+ "' or make available in environment as " + environmentName);
		}

		// Return the property value
		return propertyValue;
	}

	/**
	 * All access via static methods.
	 */
	private CosmosDbConnect() {
	}

}
