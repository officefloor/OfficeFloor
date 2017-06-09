/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.supplier;

import java.sql.Connection;

import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.compile.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.compile.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.compile.supplier.SuppliedManagedObjectType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;

/**
 * Mock {@link SupplierSource} that enables validating loading a
 * {@link SupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadSupplierSource extends AbstractSupplierSource {

	/**
	 * {@link Property} to ensure valid {@link SupplierType} as must be
	 * provided.
	 */
	public static final String PROPERTY_TEST = "TEST";

	/**
	 * Validates the {@link SupplierType} is correct for this
	 * {@link MockLoadSupplierSource}.
	 * 
	 * @param supplierType
	 *            {@link SupplierType}.
	 */
	public static void assertSupplierType(SupplierType supplierType) {

		// Validate correct number of managed objects
		SuppliedManagedObjectType[] moTypes = supplierType
				.getSuppliedManagedObjectTypes();
		TestCase.assertEquals("Incorrect number of managed objects", 2,
				moTypes.length);

		// Validate the complex managed object
		SuppliedManagedObjectType complexMo = moTypes[0];

		// Validate the managed object auto-wiring
		AutoWire[] complexAutoWiring = complexMo.getAutoWiring();
		TestCase.assertEquals("Should have auto-wiring", 1,
				complexAutoWiring.length);
		TestCase.assertEquals("Incorrect auto-wire", new AutoWire("COMPLEX",
				MockTypeManagedObjectSource.class.getName()),
				complexAutoWiring[0]);

		// Validate input managed object
		TestCase.assertTrue("Should be input managed object",
				complexMo.isInputManagedObject());

		// Validate dependencies
		SuppliedManagedObjectDependencyType[] dependencies = complexMo
				.getDependencyTypes();
		TestCase.assertEquals("Incorrect number of dependencies", 2,
				dependencies.length);
		SuppliedManagedObjectDependencyType dependency = dependencies[0];
		TestCase.assertEquals("Incorrect dependency name", "dependency",
				dependency.getDependencyName());
		TestCase.assertEquals("Incorrect dependency type",
				Connection.class.getName(), dependency.getDependencyType());
		TestCase.assertEquals("Incorrect dependency qualifier", "QUALIFIER",
				dependency.getTypeQualifier());
		SuppliedManagedObjectDependencyType overridden = dependencies[1];
		TestCase.assertEquals("Incorrect dependency name", "overridden",
				overridden.getDependencyName());
		TestCase.assertEquals("Incorrect dependency type",
				String.class.getName(), overridden.getDependencyType());
		TestCase.assertEquals("Incorrect dependency qualifier", "OVERRIDDEN",
				overridden.getTypeQualifier());

		// Validate flows
		SuppliedManagedObjectFlowType[] flows = complexMo.getFlowTypes();
		TestCase.assertEquals("Incorrect number of flows", 1, flows.length);
		SuppliedManagedObjectFlowType flow = flows[0];
		TestCase.assertEquals("Incorrect flow name", "flow", flow.getFlowName());
		TestCase.assertEquals("Incorrect flow section", "SECTION",
				flow.getSectionName());
		TestCase.assertEquals("Incorrect flow section input", "INPUT",
				flow.getSectionInputName());
		TestCase.assertEquals("Incorrect flow argument", Integer.class,
				flow.getArgumentType());

		// Validate teams
		SuppliedManagedObjectTeamType[] teams = complexMo.getTeamTypes();
		TestCase.assertEquals("Incorrect number of teams", 1, teams.length);
		SuppliedManagedObjectTeamType team = teams[0];
		TestCase.assertEquals("Incorrect team name", "team", team.getTeamName());
		TestCase.assertEquals("Incorrect team auto-wire", new AutoWire(
				"SPECIFIC", Integer.class.getName()), team.getTeamAutoWire());

		// Validate extension interfaces
		Class<?>[] extensionInterfaces = complexMo.getExtensionInterfaces();
		TestCase.assertEquals("Incorrect number of extension interfaces", 1,
				extensionInterfaces.length);
		Class<?> extensionInterface = extensionInterfaces[0];
		TestCase.assertEquals("Incorrect extension interface",
				XAResource.class, extensionInterface);

		// Validate the simple managed object
		SuppliedManagedObjectType simpleMo = moTypes[1];
		AutoWire[] simpleAutoWiring = simpleMo.getAutoWiring();
		TestCase.assertEquals("Should have auto-wiring", 1,
				simpleAutoWiring.length);
		TestCase.assertEquals("Incorrect auto-wire", new AutoWire("SIMPLE",
				MockTypeManagedObjectSource.class.getName()),
				simpleAutoWiring[0]);
		TestCase.assertFalse("Should not be an input managed object",
				simpleMo.isInputManagedObject());
		TestCase.assertEquals("Should be no dependencies", 0,
				simpleMo.getDependencyTypes().length);
		TestCase.assertEquals("Should be no flows", 0,
				simpleMo.getFlowTypes().length);
		TestCase.assertEquals("Should be no teams", 0,
				simpleMo.getTeamTypes().length);
		TestCase.assertEquals("Should be no extension interfaces", 0,
				simpleMo.getExtensionInterfaces().length);
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
		TestCase.assertEquals("Property should be available", PROPERTY_TEST,
				value);

		// Load the complex managed object
		MockTypeManagedObjectSource complex = new MockTypeManagedObjectSource(
				Object.class);
		complex.addDependency("dependency", Connection.class, "QUALIFIER");
		complex.addDependency("overridden", Object.class, null);
		complex.addFlow("flow", Integer.class);
		complex.addTeam("team");
		complex.addTeam("provided");
		complex.addExtensionInterface(XAResource.class);
		context.addManagedObject(complex, new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapDependency("overridden", new AutoWire("OVERRIDDEN",
						String.class.getName()));
				context.mapFlow("flow", "SECTION", "INPUT");
				context.mapTeam("team",
						new AutoWire("SPECIFIC", Integer.class.getName()));
				context.mapTeam("provided", PassiveTeamSource.class.getName());
			}
		}, new AutoWire("COMPLEX", MockTypeManagedObjectSource.class.getName()));

		// Load the simple managed object
		MockTypeManagedObjectSource simple = new MockTypeManagedObjectSource(
				Object.class);
		context.addManagedObject(simple, null, new AutoWire("SIMPLE",
				MockTypeManagedObjectSource.class.getName()));
	}

}