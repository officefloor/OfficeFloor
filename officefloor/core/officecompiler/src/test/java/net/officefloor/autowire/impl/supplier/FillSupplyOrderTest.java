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
package net.officefloor.autowire.impl.supplier;

import java.sql.Connection;
import java.util.Properties;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeam;
import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests filling a {@link SupplyOrder}.
 * 
 * @author Daniel Sagenschneider
 */
public class FillSupplyOrderTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	@Override
	protected void setUp() throws Exception {
		MockSupplierSource.reset();
	}

	/**
	 * Ensure issue if <code>null</code> {@link AutoWire} for the
	 * {@link SupplyOrder}.
	 */
	public void testNullAutoWire() {

		// Ensure issue if no auto-wire
		this.issues.recordIssue("SupplyOrder 0 must have an AutoWire");

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		// Test
		MockSupplyOrder order = new MockSupplyOrder(null);
		this.fillSupplyOrders(order);

		// Ensure not filled
		assertNull("Order should not be filled", order.suppliedManagedObject);
	}

	/**
	 * Indicate not fill {@link SupplyOrder}.
	 */
	public void testNotFillSupplyOrder() {

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		MockSupplyOrder order = new MockSupplyOrder(new AutoWire(UnknownType.class));
		this.fillSupplyOrders(order);
		assertNull("Order should not be filled", order.suppliedManagedObject);
	}

	/**
	 * Unknown type that can not be fulfilled.
	 */
	public static class UnknownType {
	}

	/**
	 * Ensure can fill a {@link SupplyOrder}.
	 */
	public void testFillSupplyOrder() {

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		MockSupplyOrder order = new MockSupplyOrder(new AutoWire(String.class));
		this.fillSupplyOrders(order);
		validateSuppliedManagedObject(order, String.class, 0, 0);
	}

	/**
	 * Ensure can fill multiple {@link SupplyOrder} instances.
	 */
	public void testFillMultipleSupplyOrders() {

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		MockSupplyOrder one = new MockSupplyOrder(new AutoWire(String.class));
		MockSupplyOrder two = new MockSupplyOrder(new AutoWire(Property.class));
		MockSupplyOrder three = new MockSupplyOrder(new AutoWire(Long.class));
		this.fillSupplyOrders(one, two, three);
		validateSuppliedManagedObject(one, String.class, 0, 0);
		validateSuppliedManagedObject(two, Property.class, 0, 0, "MO_NAME", "MO_VALUE");
		validateSuppliedManagedObject(three, Long.class, 1000, 0);
	}

	/**
	 * Ensure can fill a qualified {@link SupplyOrder}.
	 */
	public void testFillQualifiedSupplyOrder() {

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		MockSupplyOrder defaultOrder = new MockSupplyOrder(new AutoWire(String.class));
		MockSupplyOrder qualifiedOrder = new MockSupplyOrder(new AutoWire("QUALIFIED", String.class.getName()));
		this.fillSupplyOrders(defaultOrder, qualifiedOrder);
		validateSuppliedManagedObject(defaultOrder, String.class, 0, 0);
		validateSuppliedManagedObject(qualifiedOrder, String.class, 0, 0);
		assertNotSame("Should be different supplied managed objects",
				defaultOrder.suppliedManagedObject.getManagedObjectSource(),
				qualifiedOrder.suppliedManagedObject.getManagedObjectSource());
	}

	/**
	 * Ensure can provide {@link SuppliedManagedObjectTeam} instances.
	 */
	public void testFillSupplyOrderHavingSuppliedTeams() {

		// Record obtaining the managed object types
		this.recordLoadingManagedObjects();

		MockSupplyOrder complex = new MockSupplyOrder(new AutoWire(Connection.class));
		this.fillSupplyOrders(complex);
		validateSuppliedManagedObject(complex, Connection.class, 0, 1);

		// Ensure correct details for team
		SuppliedManagedObjectTeam team = complex.suppliedManagedObject.getSuppliedTeams()[0];
		assertEquals("Incorrect team name", "SUPPLIED_TEAM", team.getTeamName());
		assertEquals("Incorrect team source class name", "PASSIVE", team.getTeamSourceClassName());
		Properties properties = team.getProperties().getProperties();
		assertEquals("Incorrect number of team properties", 1, properties.size());
		assertEquals("Incorrect team property", "TEAM_VALUE", properties.get("TEAM_NAME"));
	}

	/**
	 * Validates the {@link SuppliedManagedObject}.
	 * 
	 * @param supplyOrder
	 *            {@link MockSupplyOrder} to have its
	 *            {@link SuppliedManagedObject} validated.
	 * @param objectClass
	 *            Expected object {@link Class}.
	 * @param timeout
	 *            Expected timeout.
	 * @param numberOfSuppliedTeams
	 *            Expected number of {@link SuppliedManagedObjectTeam}
	 *            instances.
	 * @param propertyNameValuePairs
	 *            Expected properties.
	 */
	private static void validateSuppliedManagedObject(MockSupplyOrder supplyOrder, Class<?> objectClass, int timeout,
			int numberOfSuppliedTeams, String... propertyNameValuePairs) {

		// Obtain the supplied managed object
		SuppliedManagedObject<?, ?> suppliedManagedObject = supplyOrder.suppliedManagedObject;

		// Ensure have supplied managed object
		assertNotNull("Should have supplied managed object", suppliedManagedObject);

		// Validate the managed object type
		ManagedObjectType<?> managedObjectType = suppliedManagedObject.getManagedObjectType();
		assertNotNull("Should have managed object type", managedObjectType);
		assertEquals("Incorrect object class for managed object type", objectClass, managedObjectType.getObjectClass());

		// Validate the managed object source
		ManagedObjectSource<?, ?> managedObjectSource = suppliedManagedObject.getManagedObjectSource();
		assertNotNull("Should have managed object source", managedObjectSource);
		MockTypeManagedObjectSource mockSource = (MockTypeManagedObjectSource) managedObjectSource;
		assertEquals("Incorrect managed object source (by auto wire type)", objectClass.getName(),
				mockSource.getAutoWire().getType());

		// Validate the timeout
		assertEquals("Incorrect timeout", timeout, suppliedManagedObject.getTimeout());

		// Validate the properties
		Properties properties = suppliedManagedObject.getProperties().getProperties();
		assertEquals("Incorrect number of properties", (propertyNameValuePairs.length / 2), properties.size());
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			assertEquals("Incorrect property " + name, value, properties.get(name));
		}

		// Ensure correct number of supplied teams
		SuppliedManagedObjectTeam[] suppliedTeams = suppliedManagedObject.getSuppliedTeams();
		assertEquals("Incorrect number of supplied teams", numberOfSuppliedTeams, suppliedTeams.length);
	}

	/**
	 * Fills the {@link SupplyOrder} instances from the {@link MockInit}.
	 * 
	 * @param supplyOrders
	 *            {@link SupplyOrder} instances.
	 */
	private void fillSupplyOrders(SupplyOrder... supplyOrders) {
		this.fillSupplyOrders(new MockInit(), supplyOrders);
	}

	/**
	 * Fills the {@link SupplyOrder} instances.
	 * 
	 * @param init
	 *            {@link Init}.
	 * @param supplyOrders
	 *            {@link SupplyOrder} instances.
	 */
	private void fillSupplyOrders(Init init, SupplyOrder... supplyOrders) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();

		// Create the supply loader and fill supply orders
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		SupplierLoader supplierLoader = compiler.getSupplierLoader();
		MockSupplierSource.init = init;
		supplierLoader.fillSupplyOrders(MockSupplierSource.class, propertyList, supplyOrders);

		// Verify the mock objects
		this.verifyMockObjects();
	}

	/**
	 * Records loading the {@link ManagedObjectType} instances.
	 */
	private void recordLoadingManagedObjects() {
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);
	}

	/**
	 * Mock {@link Init} to supply various {@link ManagedObject} instances.
	 */
	private static class MockInit implements Init {
		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Provide various managed object instances

			// Simple managed object
			context.addManagedObject(new MockTypeManagedObjectSource(String.class), null, new AutoWire(String.class));

			// Qualified managed object
			context.addManagedObject(new MockTypeManagedObjectSource(String.class), null,
					new AutoWire("QUALIFIED", String.class.getName()));

			// Managed object with properties
			AutoWireObject property = context.addManagedObject(new MockTypeManagedObjectSource(Property.class), null,
					new AutoWire(Property.class));
			property.addProperty("MO_NAME", "MO_VALUE");

			// Managed object with timeout
			AutoWireObject timeout = context.addManagedObject(new MockTypeManagedObjectSource(Long.class), null,
					new AutoWire(Long.class));
			timeout.setTimeout(1000);

			// Managed object with team
			MockTypeManagedObjectSource teamMo = new MockTypeManagedObjectSource(Connection.class);
			teamMo.addTeam("SUPPLIED_TEAM");
			teamMo.addTeam("LINK_TEAM");
			ManagedObjectSourceWirer teamWirer = new ManagedObjectSourceWirer() {
				@Override
				public void wire(ManagedObjectSourceWirerContext context) {
					AutoWireTeam team = context.mapTeam("SUPPLIED_TEAM", "PASSIVE");
					team.addProperty("TEAM_NAME", "TEAM_VALUE");
					context.mapTeam("LINK_TEAM", new AutoWire(OnePersonTeam.class));
				}
			};
			context.addManagedObject(teamMo, teamWirer, new AutoWire(Connection.class));
		}
	}

	/**
	 * Mock {@link SupplyOrder}.
	 */
	private static class MockSupplyOrder implements SupplyOrder {

		/**
		 * {@link AutoWire}.
		 */
		private final AutoWire autoWire;

		/**
		 * {@link SuppliedManagedObject}.
		 */
		public SuppliedManagedObject<?, ?> suppliedManagedObject = null;

		/**
		 * Initiate.
		 * 
		 * @param autoWire
		 *            {@link AutoWire}.
		 */
		public MockSupplyOrder(AutoWire autoWire) {
			this.autoWire = autoWire;
		}

		/*
		 * =================== SupplyOrder ======================
		 */

		@Override
		public AutoWire getAutoWire() {
			return this.autoWire;
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> void fillOrder(
				SuppliedManagedObject<D, F> suppliedManagedObject) {
			this.suppliedManagedObject = suppliedManagedObject;
		}
	}

	/**
	 * Implement to initialise the {@link MockSupplierSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link SupplierSource}.
		 * 
		 * @param context
		 *            {@link SupplierSourceContext}.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	@TestSource
	public static class MockSupplierSource implements SupplierSource {

		/**
		 * {@link Init} to init the {@link SupplierSource}.
		 */
		public static Init init = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			init = null;
		}

		/*
		 * ================== SupplierSource ===========================
		 */

		@Override
		public SupplierSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			// Run the init if available
			if (init != null) {
				init.supply(context);
			}
		}
	}

}