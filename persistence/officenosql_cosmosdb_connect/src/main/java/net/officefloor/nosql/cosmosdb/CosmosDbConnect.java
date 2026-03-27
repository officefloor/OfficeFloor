/*-
 * #%L
 * CosmosDB Connect
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

package net.officefloor.nosql.cosmosdb;

import java.util.Arrays;
import java.util.logging.Level;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseProperties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

/**
 * {@link CosmosDatabase} connect functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbConnect {

	static {
		workAroundFix();
	}

	/**
	 * Work around fix for using latest reactor.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void workAroundFix() {
		// Fix as per https://github.com/reactor/reactor-core/issues/2663
		Hooks.onEachOperator("workaroundGh2663", p -> {
			if ("MonoSingleMono".equals(p.getClass().getSimpleName())
					|| "MonoSingleCallable".equals(p.getClass().getSimpleName())) {
				return ((Mono) p).hide();
			}
			return p;
		});
	}

	/**
	 * {@link Property} name for the URL for the {@link CosmosClient}.
	 */
	public static final String PROPERTY_URL = "url";

	/**
	 * {@link Property} name for the key.
	 */
	public static final String PROPERTY_KEY = "key";

	/**
	 * {@link Property} name for the {@link CosmosDatabase} id.
	 */
	public static final String PROPERTY_DATABASE = "database";

	/**
	 * {@link Property} name for timeout in creating {@link CosmosDatabase}.
	 */
	public static final String PROPERTY_DATABASE_CREATE_TIMEOUT = "database.create.timeout";

	/**
	 * Default timeout in creating {@link CosmosDatabase}.
	 */
	private static final int DEFAULT_DATABASE_CREATE_TIMEOUT = 120;

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
	 * Creates the {@link CosmosDatabase}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link CosmosDatabase}.
	 * @throws Exception If fails to create the {@link CosmosDatabase}.
	 */
	public static CosmosDatabase createCosmosDatabase(SourceContext context) throws Exception {
		CosmosDbFactory factory = getCosmosDbFactory(context);
		if (factory != null) {
			return factory.createCosmosDatabase();
		} else {
			// Connect and ensure database exists
			String databaseId = getCosmosDatabaseId(context);
			int timeout = getCreateDatabaseTimeout(context);
			CosmosClient client = createCosmosClientBuilder(context).buildClient();
			CosmosDbUtil.createDatabases(client, Arrays.asList(new CosmosDatabaseProperties(databaseId)), timeout,
					context.getLogger(), Level.INFO);
			return client.getDatabase(databaseId);
		}
	}

	/**
	 * Creates the {@link CosmosAsyncDatabase}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link CosmosAsyncDatabase}.
	 * @throws Exception If fails to create the {@link CosmosAsyncDatabase}.
	 */
	public static CosmosAsyncDatabase createCosmosAsyncDatabase(SourceContext context) throws Exception {
		CosmosDbFactory factory = getCosmosDbFactory(context);
		if (factory != null) {
			return factory.createCosmosAsyncDatabase();
		} else {
			// Connect and ensure database exists
			String databaseId = getCosmosDatabaseId(context);
			int timeout = getCreateDatabaseTimeout(context);
			CosmosAsyncClient client = createCosmosClientBuilder(context).buildAsyncClient();
			CosmosDbUtil.createAsyncDatabases(client, Arrays.asList(new CosmosDatabaseProperties(databaseId)), timeout,
					context.getLogger(), Level.INFO);
			return client.getDatabase(databaseId);
		}
	}

	/**
	 * Obtains the {@link CosmosDbFactory}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link CosmosDbFactory} or <code>null</code> if not configured.
	 */
	private static CosmosDbFactory getCosmosDbFactory(SourceContext context) {

		// Determine if thread local instance available to use
		CosmosDbFactory factory = threadLocalCosmosDbFactoryOverride.get();
		if (factory == null) {

			// No thread local, so see if configured factory
			factory = context.loadOptionalService(CosmosDbServiceFactory.class);
		}
		return factory;
	}

	/**
	 * Obtains the {@link CosmosDatabase} id.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return {@link CosmosDatabase} id.
	 */
	private static String getCosmosDatabaseId(SourceContext sourceContext) {
		return getProperty(PROPERTY_DATABASE, sourceContext);
	}

	/**
	 * Obtains the timeout for creating the {@link CosmosDatabase}.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return Timeout for creating the {@link CosmosDatabase}.
	 */
	private static int getCreateDatabaseTimeout(SourceContext sourceContext) {
		return Integer.parseInt(sourceContext.getProperty(PROPERTY_DATABASE_CREATE_TIMEOUT,
				String.valueOf(DEFAULT_DATABASE_CREATE_TIMEOUT)));
	}

	/**
	 * Creates the {@link CosmosClientBuilder} to connect to {@link CosmosDatabase}
	 * / {@link CosmosAsyncDatabase}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link CosmosClientBuilder}.
	 * @throws Exception If fails to connect.
	 */
	private static CosmosClientBuilder createCosmosClientBuilder(SourceContext context) throws Exception {

		// Initiate builder
		String cosmosUrl = getProperty(PROPERTY_URL, context);
		String key = getProperty(PROPERTY_KEY, context);
		CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(cosmosUrl).key(key)
				.contentResponseOnWriteEnabled(true);

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
	private static String getProperty(String propertyName, SourceContext context) {

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
