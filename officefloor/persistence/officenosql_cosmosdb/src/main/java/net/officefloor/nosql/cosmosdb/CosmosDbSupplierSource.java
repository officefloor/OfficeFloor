package net.officefloor.nosql.cosmosdb;

import java.util.HashSet;
import java.util.Set;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;

/**
 * {@link SupplierSource} for {@link CosmosDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} name for the {@link CosmosDatabase} name.
	 */
	public static final String PROPERTY_DATABASE = CosmosDatabaseManagedObjectSource.PROPERTY_DATABASE;

	/**
	 * <p>
	 * {@link Property} name for the comma separate list of
	 * {@link CosmosEntityLocator} {@link Class} names.
	 * <p>
	 * {@link CosmosEntityLocator} instances configured are instantiated by default
	 * constructors.
	 */
	public static final String PROPERTY_ENTITY_LOCATORS = "cosmos.entity.locators";

	/*
	 * ================== SupplierSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Load the entity types
		Set<Class<?>> entityTypes = new HashSet<>();

		// Load from property configurations
		String propertyEntityLocators = context.getProperty(PROPERTY_ENTITY_LOCATORS, null);
		if (propertyEntityLocators != null) {
			NEXT_LOCATOR: for (String entityLocatorClassName : propertyEntityLocators.split(",")) {

				// Ignore if no class name
				if (CompileUtil.isBlank(entityLocatorClassName)) {
					continue NEXT_LOCATOR;
				}

				// Include the entity class
				Class<?> entityLocatorClass = context.loadClass(entityLocatorClassName.trim());

				// Determine if entity locator
				if (CosmosEntityLocator.class.isAssignableFrom(entityLocatorClass)) {

					// Load the located entities
					CosmosEntityLocator locator = (CosmosEntityLocator) entityLocatorClass.getConstructor()
							.newInstance();
					for (Class<?> entity : locator.locateEntities()) {
						entityTypes.add(entity);
					}

				} else {
					// Not locator, so assume the entity
					entityTypes.add(entityLocatorClass);
				}
			}
		}

		// Capture the entity types
		Class<?>[] entities = entityTypes.toArray(new Class[entityTypes.size()]);

		// Create the managed object sources
		CosmosClientManagedObjectSource clientMos = new CosmosClientManagedObjectSource();
		CosmosClient client = clientMos.createCosmosClient(context);
		CosmosDatabaseManagedObjectSource databaseMos = new CosmosDatabaseManagedObjectSource();
		String databaseName = databaseMos.getDatabaseName(context);
		CosmosDatabase database = databaseMos.createCosmosDatabase(client, databaseName);
		CosmosEntitiesManagedObjectSource entitiesMos = new CosmosEntitiesManagedObjectSource(entities);
		entitiesMos.loadEntityTypes(context);
		entitiesMos.setupEntities(database);

		// Register the CosmosDb managed object sources
		context.addManagedObjectSource(null, CosmosClient.class, clientMos);
		context.addManagedObjectSource(null, CosmosDatabase.class, databaseMos);
		context.addManagedObjectSource(null, CosmosEntities.class, entitiesMos);
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

}