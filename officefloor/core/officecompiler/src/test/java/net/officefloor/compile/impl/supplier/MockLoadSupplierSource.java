/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.compile.impl.supplier;

import java.sql.Connection;
import java.util.Map;

import javax.transaction.xa.XAResource;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Mock {@link SupplierSource} that enables validating loading a
 * {@link InitialSupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} to ensure valid {@link InitialSupplierType} as must be
	 * provided.
	 */
	public static final String PROPERTY_TEST = "TEST";

	/**
	 * Validates the {@link SupplierType} is correct for this
	 * {@link MockLoadSupplierSource}.
	 * 
	 * @param supplierType {@link SupplierType}.
	 */
	public static void assertSupplierType(SupplierType supplierType) {

		// Validate correct number of managed objects
		SuppliedManagedObjectSourceType[] moTypes = supplierType.getSuppliedManagedObjectTypes();
		TestCase.assertEquals("Incorrect number of managed objects", 3, moTypes.length);

		// Validate the types
		assertSuppliedManagedObjectType(moTypes[0], null, Object.class, 0);
		assertSuppliedManagedObjectType(moTypes[1], "QUALIFIED", Object.class, 0);
		assertSuppliedManagedObjectType(moTypes[2], "COMPLEX", Map.class, 10, "PROPERTY", "VALUE");
	}

	/**
	 * Asserts {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param moType             {@link SuppliedManagedObjectSourceType}.
	 * @param qualifier          Expected qualifier.
	 * @param objectType         Expected object type.
	 * @param timeout            Expected timeout.
	 * @param propertyNameValues Expected {@link Property} name/value pairs.
	 */
	private static void assertSuppliedManagedObjectType(SuppliedManagedObjectSourceType moType, String qualifier,
			Class<?> objectType, long timeout, String... propertyNameValues) {
		Assert.assertEquals("Incorrect qualifier", qualifier, moType.getQualifier());
		Assert.assertEquals("Incorrect object type", objectType, moType.getObjectType());
		ManagedObjectSource<?, ?> simpleMos = moType.getManagedObjectSource();
		Assert.assertEquals("Incorrect managed object source", MockTypeManagedObjectSource.class, simpleMos.getClass());
		MockTypeManagedObjectSource simpleTypeMos = (MockTypeManagedObjectSource) simpleMos;
		Assert.assertEquals("Incorrect source object type", objectType, simpleTypeMos.getObjectType());
		PropertyList properties = moType.getPropertyList();
		Assert.assertEquals("Incorrect number of properties", propertyNameValues.length / 2,
				properties.getProperties().size());
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			Assert.assertEquals("Incorrect property " + name, value, properties.getProperty(name).getValue());
		}
	}

	/*
	 * ================ SupplierSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEST);
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Ensure property available
		String value = context.getProperty(PROPERTY_TEST);
		TestCase.assertEquals("Property should be available", PROPERTY_TEST, value);

		// Load the managed object source
		MockTypeManagedObjectSource simple = new MockTypeManagedObjectSource(Object.class, "TODO LOGGER NAME");
		context.addManagedObjectSource(null, Object.class, simple);

		// Load the qualified managed object source
		MockTypeManagedObjectSource qualified = new MockTypeManagedObjectSource(Object.class, "TODO LOGGER NAME");
		context.addManagedObjectSource("QUALIFIED", Object.class, qualified);

		// Load complex managed object source
		MockTypeManagedObjectSource complex = new MockTypeManagedObjectSource(Map.class, "TODO LOGGER NAME");
		complex.addDependency("dependency", Connection.class, "QUALIFIER");
		complex.addDependency("overridden", Object.class, null);
		complex.addFlow("flow", Integer.class);
		complex.addTeam("team");
		complex.addTeam("provided");
		complex.addExtensionInterface(XAResource.class);
		SuppliedManagedObjectSource mos = context.addManagedObjectSource("COMPLEX", Map.class, complex);
		mos.addProperty("PROPERTY", "VALUE");
	}

	@Override
	public void terminate() {
		// nothing to clean up
	}

}
