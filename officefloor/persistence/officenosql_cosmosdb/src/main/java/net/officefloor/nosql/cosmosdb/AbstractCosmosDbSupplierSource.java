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

import java.util.HashSet;
import java.util.Set;

import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosDatabase;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Abstract {@link SupplierSource} for {@link CosmosDatabase} /
 * {@link CosmosAsyncDatabase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractCosmosDbSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} name for the {@link CosmosDatabase} name.
	 */
	public static final String PROPERTY_DATABASE = "database";

	/**
	 * <p>
	 * {@link Property} name for the comma separate list of
	 * {@link CosmosEntityLocator} {@link Class} names.
	 * <p>
	 * {@link CosmosEntityLocator} instances configured are instantiated by default
	 * constructors.
	 */
	public static final String PROPERTY_ENTITY_LOCATORS = "cosmos.entity.locators";

	/**
	 * Sets up the {@link ManagedObjectSource} instances.
	 * 
	 * @param context  {@link SupplierSourceContext}.
	 * @param entities Loaded entity {@link Class} instances.
	 * @throws Exception If fails to setup the {@link ManagedObjectSource}
	 *                   instances.
	 */
	protected abstract void setupManagedObjectSources(SupplierSourceContext context, Class<?>[] entities)
			throws Exception;

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

		// Setup the managed object sources
		this.setupManagedObjectSources(context, entities);
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

}
