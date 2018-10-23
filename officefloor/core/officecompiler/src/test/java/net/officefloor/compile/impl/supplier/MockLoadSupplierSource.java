/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Mock {@link SupplierSource} that enables validating loading a
 * {@link SupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} to ensure valid {@link SupplierType} as must be provided.
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
		MockTypeManagedObjectSource simple = new MockTypeManagedObjectSource(Object.class);
		context.addManagedObjectSource(null, Object.class, simple);

		// Load the qualified managed object source
		MockTypeManagedObjectSource qualified = new MockTypeManagedObjectSource(Object.class);
		context.addManagedObjectSource("QUALIFIED", Object.class, qualified);

		// Load complex managed object source
		MockTypeManagedObjectSource complex = new MockTypeManagedObjectSource(Map.class);
		complex.addDependency("dependency", Connection.class, "QUALIFIER");
		complex.addDependency("overridden", Object.class, null);
		complex.addFlow("flow", Integer.class);
		complex.addTeam("team");
		complex.addTeam("provided");
		complex.addExtensionInterface(XAResource.class);
		SuppliedManagedObjectSource mos = context.addManagedObjectSource("COMPLEX", Map.class, complex);
		mos.addProperty("PROPERTY", "VALUE");
	}

}