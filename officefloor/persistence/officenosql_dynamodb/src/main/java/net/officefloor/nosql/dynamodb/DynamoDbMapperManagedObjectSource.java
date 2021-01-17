package net.officefloor.nosql.dynamodb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link DynamoDBMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbMapperManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * {@link Property} for read capacity unit.
	 */
	public static final String PROPERTY_READ_CAPACITY = "read.capacity";

	/**
	 * {@link Property} for write capacity unit.
	 */
	public static final String PROPERTY_WRITE_CAPACITY = "write.capacity";

	/**
	 * Entity types to load.
	 */
	private final Set<Class<?>> entityTypes;

	/**
	 * {@link SourceContext}.
	 */
	private SourceContext sourceContext;

	/**
	 * Read capacity.
	 */
	private long readCapacity;

	/**
	 * Write capacity.
	 */
	private long writeCapacity;

	/**
	 * {@link AmazonDynamoDB}.
	 */
	private AmazonDynamoDB dynamo;

	/**
	 * {@link DynamoDBMapper}.
	 */
	private DynamoDBMapper mapper;

	/**
	 * Default constructor.
	 */
	public DynamoDbMapperManagedObjectSource() {
		this(new Class[0]);
	}

	/**
	 * Instantiate with entity types to load.
	 * 
	 * @param entityTypes Entity types to load.
	 */
	public DynamoDbMapperManagedObjectSource(Class<?>... entityTypes) {
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

		// Obtain the capacity
		this.readCapacity = Long.parseLong(this.sourceContext.getProperty(PROPERTY_READ_CAPACITY, "25"));
		this.writeCapacity = Long.parseLong(this.sourceContext.getProperty(PROPERTY_WRITE_CAPACITY, "25"));

		// Load the entity types
		for (DynamoEntityLocator locator : this.sourceContext
				.loadOptionalServices(DynamoEntityLocatorServiceFactory.class)) {
			for (Class<?> entityClass : locator.locateEntities()) {

				// Add the entity type
				this.entityTypes.add(entityClass);
			}
		}

		// Load the meta-data
		context.setObjectClass(DynamoDBMapper.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {
		context.addService(new ManagedObjectService<None>() {

			@Override
			public void startServicing(ManagedObjectServiceContext<None> serviceContext) throws Exception {

				// Easy access
				DynamoDbMapperManagedObjectSource source = DynamoDbMapperManagedObjectSource.this;

				// Create the mapper
				source.dynamo = AmazonDynamoDbConnect.connect(source.sourceContext);
				source.mapper = new DynamoDBMapper(source.dynamo);

				// Obtain available tables
				Set<String> tableNames = new HashSet<>(source.dynamo.listTables().getTableNames());

				// Ensure the tables are created
				DynamoDB db = new DynamoDB(source.dynamo);
				NEXT_ENTITY: for (Class<?> entityClass : source.entityTypes) {

					// Obtain the table name
					DynamoDBTable tableAnnotation = entityClass.getAnnotation(DynamoDBTable.class);
					String tableName = (tableAnnotation != null) ? tableAnnotation.tableName()
							: entityClass.getSimpleName();

					// Determine if table already exists
					if (tableNames.contains(tableName)) {
						continue NEXT_ENTITY;
					}

					// Create the table
					CreateTableRequest createTable = source.mapper.generateCreateTableRequest(entityClass)
							.withProvisionedThroughput(
									new ProvisionedThroughput(source.readCapacity, source.writeCapacity));
					List<GlobalSecondaryIndex> secondaryIndexes = createTable.getGlobalSecondaryIndexes();
					if (secondaryIndexes != null) {
						for (GlobalSecondaryIndex index : secondaryIndexes) {
							index.setProvisionedThroughput(
									new ProvisionedThroughput(source.readCapacity, source.writeCapacity));
						}
					}
					db.createTable(createTable).waitForActive();
				}
			}

			@Override
			public void stopServicing() {
				// Stop connection
				DynamoDbMapperManagedObjectSource.this.dynamo.shutdown();
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
		return this.mapper;
	}

}