/*-
 * #%L
 * DynamoDB Persistence
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

package net.officefloor.nosql.dynamodb;

import java.util.HashSet;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;

/**
 * {@link SupplierSource} for {@link DynamoDBMapper}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbSupplierSource extends AbstractSupplierSource {

	/**
	 * <p>
	 * {@link Property} name for the comma separate list of
	 * {@link DynamoEntityLocator} {@link Class} names.
	 * <p>
	 * {@link DynamoEntityLocator} instances configured are instantiated by default
	 * constructors.
	 */
	public static final String PROPERTY_ENTITY_LOCATORS = "dynamo.entity.locators";

	/*
	 * ======================= SupplierSource ===========================
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
				if (DynamoEntityLocator.class.isAssignableFrom(entityLocatorClass)) {

					// Load the located entities
					DynamoEntityLocator locator = (DynamoEntityLocator) entityLocatorClass.getConstructor()
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

		// Register the DynamoDb managed object source
		context.addManagedObjectSource(null, DynamoDBMapper.class, new DynamoDbMapperManagedObjectSource(entities));
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

}
