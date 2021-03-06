package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link CosmosAsyncDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncDatabaseManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Dependency keys.
	 */
	private static enum FunctionDependencyKeys {
		COSMOS_CLIENT
	}

	/**
	 * {@link Property} name for the {@link CosmosAsyncDatabase} name.
	 */
	public static final String PROPERTY_DATABASE = "database";

	/**
	 * {@link CosmosAsyncDatabase}.
	 */
	private volatile CosmosAsyncDatabase database;

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
		String databaseName = mosContext.getProperty(PROPERTY_DATABASE, OfficeFloor.class.getSimpleName());

		// Delay start up until database setup
		ManagedObjectStartupCompletion setupCompletion = mosContext.createStartupCompletion();

		// Register start up function to setup database
		final String SETUP_FUNCTION_NAME = "SETUP_DATABASE";
		ManagedObjectFunctionBuilder<FunctionDependencyKeys, None> setupFunction = mosContext
				.addManagedFunction(SETUP_FUNCTION_NAME, () -> (mfContext) -> {

					// Obtain the client
					CosmosAsyncClient client = (CosmosAsyncClient) mfContext
							.getObject(FunctionDependencyKeys.COSMOS_CLIENT);

					// Create the database
					client.createDatabaseIfNotExists(databaseName).map(response -> response.getProperties().getId())
							.subscribe((databaseId) -> {

								// Successful, so obtain the database
								this.database = client.getDatabase(databaseId);

								// Flag set up
								setupCompletion.complete();

							}, (error) -> {
								// Indicate failure to create database
								setupCompletion.failOpen(error);
							});
				});
		setupFunction.linkObject(FunctionDependencyKeys.COSMOS_CLIENT,
				mosContext.addFunctionDependency("COSMOS_CLIENT", CosmosAsyncClient.class));
		mosContext.addStartupFunction(SETUP_FUNCTION_NAME, null);

		// Load the meta-data
		context.setObjectClass(CosmosAsyncDatabase.class);
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
