package net.officefloor.nosql.cosmosdb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link CosmosClient}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDatabaseManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Entity types to load.
	 */
	private final Set<Class<?>> entityTypes;

	/**
	 * {@link SourceContext}.
	 */
	private SourceContext sourceContext;

	/**
	 * {@link CosmosClient}.
	 */
	private CosmosClient client;

	/**
	 * Default constructor.
	 */
	public CosmosDatabaseManagedObjectSource() {
		this(new Class[0]);
	}

	/**
	 * Instantiate with entity types to load.
	 * 
	 * @param entityTypes Entity types to load.
	 */
	public CosmosDatabaseManagedObjectSource(Class<?>... entityTypes) {
		this.entityTypes = new HashSet<>(Arrays.asList(entityTypes));
	}

	/*
	 * ====================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Capture the source context for service start
		this.sourceContext = context.getManagedObjectSourceContext();

		// Load the entity types
		for (CosmosEntityLocator locator : this.sourceContext
				.loadOptionalServices(CosmosEntityLocatorServiceFactory.class)) {
			for (Class<?> entityClass : locator.locateEntities()) {

				// Add the entity type
				this.entityTypes.add(entityClass);
			}
		}

		// Load the meta-data
		context.setObjectClass(CosmosDatabase.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {
		context.addService(new ManagedObjectService<None>() {

			@Override
			public void startServicing(ManagedObjectServiceContext<None> serviceContext) throws Exception {

				// Easy access
				CosmosDatabaseManagedObjectSource source = CosmosDatabaseManagedObjectSource.this;

//				// Create the mapper
//				source.dynamo = AmazonDynamoDbConnect.connect(source.sourceContext);
//				source.mapper = new DynamoDBMapper(source.dynamo);
//
//				// Obtain available tables
//				Set<String> tableNames = new HashSet<>(source.dynamo.listTables().getTableNames());
//
//				// Ensure the tables are created
//				boolean isFirstCreateTable = true;
//				DynamoDB db = new DynamoDB(source.dynamo);
//				NEXT_ENTITY: for (Class<?> entityClass : source.entityTypes) {
//
//					// Obtain the table name
//					DynamoDBTable tableAnnotation = entityClass.getAnnotation(DynamoDBTable.class);
//					String tableName = (tableAnnotation != null) ? tableAnnotation.tableName()
//							: entityClass.getSimpleName();
//
//					// Determine if table already exists
//					if (tableNames.contains(tableName)) {
//						continue NEXT_ENTITY;
//					}
//
//					// Log creating the table
//					if (isFirstCreateTable) {
//						context.getLogger().info("Setting up DynamoDB tables");
//						isFirstCreateTable = false;
//					}
//					context.getLogger().info("Creating table " + tableName);
//
//					// Create the table
//					CreateTableRequest createTable = source.mapper.generateCreateTableRequest(entityClass)
//							.withProvisionedThroughput(
//									new ProvisionedThroughput(source.readCapacity, source.writeCapacity));
//					List<GlobalSecondaryIndex> secondaryIndexes = createTable.getGlobalSecondaryIndexes();
//					if (secondaryIndexes != null) {
//						for (GlobalSecondaryIndex index : secondaryIndexes) {
//							index.setProvisionedThroughput(
//									new ProvisionedThroughput(source.readCapacity, source.writeCapacity));
//						}
//					}
//					db.createTable(createTable).waitForActive();
//				}
			}

			@Override
			public void stopServicing() {

				// Easy access
				CosmosDatabaseManagedObjectSource source = CosmosDatabaseManagedObjectSource.this;

				// Stop connection
				if (source.client != null) {
					source.client.close();
				}
			}
		});
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
		return this.client;
	}

}
