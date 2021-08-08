/*-
 * #%L
 * CosmosDB
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

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link CosmosDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDatabaseManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Dependency keys.
	 */
	private static enum FunctionDependencyKeys {
		COSMOS_CLIENT
	}

	/**
	 * {@link Property} name for the {@link CosmosDatabase} name.
	 */
	public static final String PROPERTY_DATABASE = AbstractCosmosDbSupplierSource.PROPERTY_DATABASE;

	/**
	 * {@link CosmosDatabase}.
	 */
	private volatile CosmosDatabase database;

	/**
	 * Obtains the name of the {@link CosmosDatabase}.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 * @return Name of the {@link CosmosDatabase}.
	 */
	public String getDatabaseName(SourceContext sourceContext) {
		return sourceContext.getProperty(PROPERTY_DATABASE, OfficeFloor.class.getSimpleName());
	}

	/**
	 * Creates the {@link CosmosDatabase}.
	 * 
	 * @param client       {@link CosmosClient}.
	 * @param databaseName Name of the {@link CosmosDatabase}.
	 * @return {@link CosmosDatabase}.
	 */
	public CosmosDatabase createCosmosDatabase(CosmosClient client, String databaseName) {
		String databaseId = client.createDatabaseIfNotExists(databaseName).getProperties().getId();
		this.database = client.getDatabase(databaseId);
		return this.database;
	}

	/*
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the Cosmos database name
		String databaseName = this.getDatabaseName(mosContext);

		// Load the meta-data
		context.setObjectClass(CosmosDatabase.class);

		// Supplier setup
		if (this.database != null) {
			return;
		}

		// Delay start up until database setup
		ManagedObjectStartupCompletion setupCompletion = mosContext.createStartupCompletion();

		// Register start up function to setup database
		final String SETUP_FUNCTION_NAME = "SETUP_DATABASE";
		ManagedObjectFunctionBuilder<FunctionDependencyKeys, None> setupFunction = mosContext
				.addManagedFunction(SETUP_FUNCTION_NAME, () -> (mfContext) -> {
					try {

						// Obtain the client
						CosmosClient client = (CosmosClient) mfContext.getObject(FunctionDependencyKeys.COSMOS_CLIENT);

						// Create the database
						this.createCosmosDatabase(client, databaseName);

						// Flag set up
						setupCompletion.complete();

					} catch (Throwable ex) {
						// Indicate failure to setup
						setupCompletion.failOpen(ex);
					}
				});
		setupFunction.linkObject(FunctionDependencyKeys.COSMOS_CLIENT,
				mosContext.addFunctionDependency("COSMOS_CLIENT", CosmosClient.class));
		mosContext.addStartupFunction(SETUP_FUNCTION_NAME, null);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ========================= ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.database;
	}

}
