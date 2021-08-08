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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.PartitionKey;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * {@link ManagedObjectSource} for the {@link CosmosEntities}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosEntitiesManagedObjectSource extends AbstractManagedObjectSource<None, None>
		implements ManagedObject {

	/**
	 * Dependency keys.
	 */
	private static enum FunctionDependencyKeys {
		COSMOS_DATABASE
	}

	/**
	 * {@link CosmosEntities}.
	 */
	private volatile CosmosEntities cosmosEntities;

	/**
	 * Entity types to load.
	 */
	private final Set<Class<?>> entityTypes;

	/**
	 * Default constructor.
	 */
	public CosmosEntitiesManagedObjectSource() {
		this(new Class[0]);
	}

	/**
	 * Instantiate with entity types to load.
	 * 
	 * @param entityTypes Entity types to load.
	 */
	public CosmosEntitiesManagedObjectSource(Class<?>... entityTypes) {
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
	 * @param database {@link CosmosDatabase}.
	 */
	public void setupEntities(CosmosDatabase database) {

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
			database.createContainerIfNotExists(containerId, metaData.getPath());
		}

		// Create container id resolver
		Function<Class<?>, String> containerIdResolver = (entityType) -> entityType.getSimpleName();

		// Create the unknown partition key factory
		Function<Class<?>, Function<Object, PartitionKey>> unknownFactory = (entityType) -> (
				entity) -> PartitionKeyMetaData.getPartitionKeyMetaData(entity.getClass()).getFactory().apply(entity);

		// Provide the partition key factory
		this.cosmosEntities = new CosmosEntitiesImpl(database, containerIds, containerIdResolver, partitionKeyFactories,
				unknownFactory);
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
		context.setObjectClass(CosmosEntities.class);

		// Supplier setup
		if (this.cosmosEntities != null) {
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
						CosmosDatabase database = (CosmosDatabase) mfContext
								.getObject(FunctionDependencyKeys.COSMOS_DATABASE);

						// Set up the entities
						this.setupEntities(database);

						// Flag set up
						setupCompletion.complete();

					} catch (Throwable ex) {
						// Indicate failure to setup
						setupCompletion.failOpen(ex);
					}
				});
		setupFunction.linkObject(FunctionDependencyKeys.COSMOS_DATABASE,
				mosContext.addFunctionDependency("COSMOS_DATABASE", CosmosDatabase.class));
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
		return this.cosmosEntities;
	}

	/**
	 * {@link CosmosEntities} implementation.
	 */
	private static class CosmosEntitiesImpl implements CosmosEntities {

		/**
		 * {@link CosmosDatabase}.
		 */
		private final CosmosDatabase database;

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
		 * @param database                   {@link CosmosDatabase}.
		 * @param containerIds               Container identifiers by entity type.
		 * @param containerIdResolver        Resolves the container identifier for
		 *                                   unknown entity type.
		 * @param partitionKeyFactories      Factories for {@link PartitionKey} by
		 *                                   entity type.
		 * @param partitionKeyUnknownFactory Factory for {@link PartitionKey} of unknown
		 *                                   entities.
		 */
		private CosmosEntitiesImpl(CosmosDatabase database, Map<Class<?>, String> containerIds,
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
		 * ================== CosmosEntities =======================
		 */

		@Override
		public CosmosContainer getContainer(Class<?> entityType) {

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
