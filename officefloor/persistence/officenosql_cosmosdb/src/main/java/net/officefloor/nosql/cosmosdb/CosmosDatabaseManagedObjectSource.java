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

/**
 * {@link ManagedObjectSource} for the {@link CosmosClient}.
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
	public static final String PROPERTY_DATABASE = "database";

	/**
	 * {@link CosmosDatabase}.
	 */
	private volatile CosmosDatabase database;

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
					try {

						// Obtain the client
						CosmosClient client = (CosmosClient) mfContext.getObject(FunctionDependencyKeys.COSMOS_CLIENT);

						// Create the database
						String databaseId = client.createDatabaseIfNotExists(databaseName).getProperties().getId();
						this.database = client.getDatabase(databaseId);

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

		// Load the meta-data
		context.setObjectClass(CosmosDatabase.class);
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
