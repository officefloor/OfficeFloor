package net.officefloor.nosql.cosmosdb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import reactor.core.publisher.Mono;

/**
 * {@link ManagedObjectSource} for the {@link CosmosAsyncEntities}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosAsyncEntitiesManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Dependency keys.
	 */
	private static enum FunctionDependencyKeys {
		COSMOS_DATABASE
	}

	/**
	 * {@link CosmosAsyncEntities}.
	 */
	private volatile CosmosAsyncEntities cosmosAsyncEntities;

	/**
	 * Entity types to load.
	 */
	private final Set<Class<?>> entityTypes;

	/**
	 * Default constructor.
	 */
	public CosmosAsyncEntitiesManagedObjectSource() {
		this(new Class[0]);
	}

	/**
	 * Instantiate with entity types to load.
	 * 
	 * @param entityTypes Entity types to load.
	 */
	public CosmosAsyncEntitiesManagedObjectSource(Class<?>... entityTypes) {
		this.entityTypes = new HashSet<>(Arrays.asList(entityTypes));
	}

	/**
	 * Loads the entity types.
	 * 
	 * @throws Exception If fails to load the entity types.
	 */
	public void loadEntityTypes(SourceContext sourceContext) throws Exception {
		for (CosmosEntityLocator locator : sourceContext
				.loadOptionalServices(CosmosEntityLocatorServiceFactory.class)) {
			for (Class<?> entityClass : locator.locateEntities()) {

				// Add the entity type
				this.entityTypes.add(entityClass);
			}
		}
	}

	/**
	 * Sets up the entities.
	 * 
	 * @param database {@link CosmosAsyncDatabase}.
	 * @return {@link Mono} to create {@link CosmosAsyncEntities}.
	 */
	public Mono<CosmosAsyncEntities> setupEntities(CosmosAsyncDatabase database) {

		// Allow creation of containers
		Mono<Object> createContainers = Mono.just("init");

		// Set up the entities (loading entity details)
		Map<Class<?>, String> containerIds = new ConcurrentHashMap<>();
		Map<Class<?>, Function<Object, PartitionKey>> partitionKeyFactories = new ConcurrentHashMap<>();
		for (Class<?> entityType : this.entityTypes) {

			// Obtain the container identifier
			CosmosEntity cosmosEntity = entityType.getAnnotation(CosmosEntity.class);
			String containerId = cosmosEntity != null ? cosmosEntity.containerId() : entityType.getSimpleName();

			// Register container identifier
			containerIds.put(entityType, containerId);

			// Obtain the partition key meta-data
			PartitionKeyMetaData metaData = PartitionKeyMetaData.getPartitionKeyMetaData(entityType);

			// Register the partition key factory
			partitionKeyFactories.put(entityType, metaData.getFactory());

			// Create the container
			createContainers = createContainers
					.flatMap(container -> database.createContainerIfNotExists(containerId, metaData.getPath()));
		}

		// Create container id resolver
		Function<Class<?>, String> containerIdResolver = (entityType) -> entityType.getSimpleName();

		// Create the unknown partition key factory
		Function<Class<?>, Function<Object, PartitionKey>> unknownFactory = (entityType) -> (
				entity) -> PartitionKeyMetaData.getPartitionKeyMetaData(entity.getClass()).getFactory().apply(entity);

		// Provide the cosmos async entities
		return createContainers.map(result -> {
			this.cosmosAsyncEntities = new CosmosAsyncEntitiesImpl(database, containerIds, containerIdResolver,
					partitionKeyFactories, unknownFactory);
			return this.cosmosAsyncEntities;
		});
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide meta-data
		context.setObjectClass(CosmosAsyncEntities.class);

		// Supplier setup
		if (this.cosmosAsyncEntities != null) {
			return;
		}

		// Load the entity types
		this.loadEntityTypes(mosContext);

		// Delay start up until entities setup
		ManagedObjectStartupCompletion setupCompletion = mosContext.createStartupCompletion();

		// Register start up function to setup entities
		final String SETUP_FUNCTION_NAME = "SETUP_ENTITIES";
		ManagedObjectFunctionBuilder<FunctionDependencyKeys, None> setupFunction = mosContext
				.addManagedFunction(SETUP_FUNCTION_NAME, () -> (mfContext) -> {
					try {

						// Obtain the database
						CosmosAsyncDatabase database = (CosmosAsyncDatabase) mfContext
								.getObject(FunctionDependencyKeys.COSMOS_DATABASE);

						// Undertake creating containers
						this.setupEntities(database).subscribe((result) -> {

							// Flag set up
							setupCompletion.complete();

						}, (error) -> {
							// Indicate failure to create containers
							setupCompletion.failOpen(error);
						});

					} catch (Throwable ex) {
						// Indicate failure to setup
						setupCompletion.failOpen(ex);
					}
				});
		setupFunction.linkObject(FunctionDependencyKeys.COSMOS_DATABASE,
				mosContext.addFunctionDependency("COSMOS_DATABASE", CosmosAsyncDatabase.class));
		mosContext.addStartupFunction(SETUP_FUNCTION_NAME, null);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedObject ===============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this.cosmosAsyncEntities;
	}

	/**
	 * {@link CosmosAsyncEntities} implementation.
	 */
	private static class CosmosAsyncEntitiesImpl implements CosmosAsyncEntities {

		/**
		 * {@link CosmosAsyncDatabase}.
		 */
		private final CosmosAsyncDatabase database;

		/**
		 * Container identifiers by entity type.
		 */
		private final Map<Class<?>, String> containerIds;

		/**
		 * Resolves the container identifier for unknown entity type.
		 */
		public final Function<Class<?>, String> containerIdResolver;

		/**
		 * Factories for {@link PartitionKey} by entity type.
		 */
		private final Map<Class<?>, Function<Object, PartitionKey>> partitionKeyFactories;

		/**
		 * Factory for unknown entities.
		 */
		private final Function<Class<?>, Function<Object, PartitionKey>> partitionKeyUnknownFactory;

		/**
		 * Instantiate.
		 * 
		 * @param database                   {@link CosmosAsyncDatabase}.
		 * @param containerIds               Container identifiers by entity type.
		 * @param containerIdResolver        Resolves the container identifier for
		 *                                   unknown entity type.
		 * @param partitionKeyFactories      Factories for {@link PartitionKey} by
		 *                                   entity type.
		 * @param partitionKeyUnknownFactory Factory for {@link PartitionKey} of unknown
		 *                                   entities.
		 */
		private CosmosAsyncEntitiesImpl(CosmosAsyncDatabase database, Map<Class<?>, String> containerIds,
				Function<Class<?>, String> containerIdResolver,
				Map<Class<?>, Function<Object, PartitionKey>> partitionKeyFactories,
				Function<Class<?>, Function<Object, PartitionKey>> partitionKeyUnknownFactory) {
			this.database = database;
			this.containerIds = containerIds;
			this.containerIdResolver = containerIdResolver;
			this.partitionKeyFactories = partitionKeyFactories;
			this.partitionKeyUnknownFactory = partitionKeyUnknownFactory;
		}

		/*
		 * ================== CosmosAsyncEntities =======================
		 */

		@Override
		public CosmosAsyncContainer getContainer(Class<?> entityType) {

			// Obtain the container identifier
			String containerId = this.containerIds.get(entityType);
			if (containerId == null) {

				// Resolve the container identifier
				containerId = this.containerIdResolver.apply(entityType);

				// Cache for further potential use
				this.containerIds.put(entityType, containerId);
			}

			// Return the container
			return this.database.getContainer(containerId);
		}

		@Override
		public PartitionKey createPartitionKey(Object entity) {

			// Obtain the factory
			Class<?> entityType = entity.getClass();
			Function<Object, PartitionKey> factory = this.partitionKeyFactories.get(entityType);
			if (factory == null) {

				// Unknown entity type, so provide factory
				factory = partitionKeyUnknownFactory.apply(entityType);

				// Cache for further potential use
				this.partitionKeyFactories.put(entityType, factory);
			}

			// Create the partition key
			return factory.apply(entity);
		}
	}

}