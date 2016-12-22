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
package net.officefloor.autowire.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Integration test for a {@link SuppliedManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateSuppliedManagedObjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can use {@link SuppliedManagedObject}.
	 */
	public void testIntegrationOfSuppliedManagedObject() throws Exception {

		final MockConnection connection = this.createMock(MockConnection.class);
		final MockQueue queue = this.createMock(MockQueue.class);

		final PreparedStatement statement = this
				.createMock(PreparedStatement.class);
		final Xid xid = this.createMock(Xid.class);

		// Setup the supplier
		MockInputManagedObjectSource inputManagedObject = new MockInputManagedObjectSource(
				queue);
		ManagedObjectSourceWirer inputWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow(InputFlows.INPUT.name(), "SECTION", "task");
			}
		};
		MockSupplierSource.reset(connection, inputManagedObject, inputWirer);

		// Setup the governance
		MockGovernance.reset(xid);

		// Record the functionality
		connection.start(xid, 0);
		queue.start(xid, 0);
		this.recordReturn(queue, queue.peek(), "QUEUED_VALUE");
		this.recordReturn(connection,
				connection.prepareStatement("UPDATE TEST WHERE PASS = 'YES'"),
				statement);
		this.recordReturn(statement, statement.execute(), false);
		this.recordReturn(connection, connection.prepare(xid), XAResource.XA_OK);
		this.recordReturn(queue, queue.prepare(xid), XAResource.XA_OK);
		connection.commit(xid, false);
		queue.commit(xid, false);

		// Create the application
		AutoWireApplication application = new AutoWireOfficeFloorSource();
		AutoWireSection section = application
				.addSection("SECTION", ClassSectionSource.class.getName(),
						MockSection.class.getName());
		AutoWireSupplier supplier = application
				.addSupplier(MockSupplierSource.class.getName());
		supplier.addProperty(MockSupplierSource.PROPERTY_TEST, "supplier.value");
		AutoWireGovernance governance = application.addGovernance(
				"TRANSACTION", ClassGovernanceSource.class.getName());
		governance.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME,
				MockGovernance.class.getName());
		governance.governSection(section);

		// Test
		this.replayMockObjects();
		AutoWireOfficeFloor officeFloor = application.openOfficeFloor();
		inputManagedObject.invokeProcess("TEST");
		officeFloor.closeOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * {@link Class} for {@link ClassSectionSource}.
	 */
	public static class MockSection {

		/**
		 * {@link ManagedFunction} to be invoked by the
		 * {@link MockInputManagedObjectSource}.
		 * 
		 * @param parameter
		 *            Parameter.
		 * @param queue
		 *            {@link MockQueue}.
		 * @param connection
		 *            {@link MockConnection}.
		 */
		public void task(@Parameter String parameter, Queue<?> queue,
				Connection connection) throws SQLException {

			// Ensure correct input parameter
			assertEquals("Incorrect parameter", "TEST", parameter);

			// Obtain value from queue
			assertEquals("Incorrect queued value", "QUEUED_VALUE", queue.peek());

			// Ensure can interact with connection
			connection.prepareStatement("UPDATE TEST WHERE PASS = 'YES'")
					.execute();
		}
	}

	/**
	 * {@link Connection} for mocking.
	 */
	public static interface MockConnection extends Connection, XAResource {
	}

	/**
	 * {@link Queue} for mocking.
	 */
	public static interface MockQueue extends Queue<Object>, XAResource {
	}

	/**
	 * {@link Class} for {@link ClassGovernanceSource}.
	 */
	public static class MockGovernance {

		/**
		 * {@link Xid}.
		 */
		private static Xid xid;

		/**
		 * Reset for the next test.
		 * 
		 * @param xid
		 *            {@link Xid}.
		 */
		public static void reset(Xid xid) {
			MockGovernance.xid = xid;
		}

		/**
		 * {@link XAResource} instances to provide {@link Governance} over.
		 */
		private final List<XAResource> resources = new LinkedList<XAResource>();

		/**
		 * Governs the {@link XAResource}.
		 * 
		 * @param resource
		 *            {@link XAResource}.
		 */
		@Govern
		public void govern(XAResource resource) throws XAException {

			// Start the transaction
			resource.start(xid, 0);

			// Add the resource for later enforcing
			this.resources.add(resource);
		}

		@Enforce
		public void enforce() throws XAException {

			// Prepare transaction for committing
			for (XAResource resource : this.resources) {
				resource.prepare(xid);
			}

			// Commit the transaction
			for (XAResource resource : this.resources) {
				resource.commit(xid, false);
			}

			// Clear (as committed)
			this.resources.clear();
		}
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	@TestSource
	public static class MockSupplierSource extends AbstractSupplierSource {

		/**
		 * {@link Property} to test is available.
		 */
		public static final String PROPERTY_TEST = "supplier.property";

		/**
		 * {@link Connection}.
		 */
		private static Connection connection;

		/**
		 * {@link MockInputManagedObjectSource}.
		 */
		private static MockInputManagedObjectSource inputManagedObject;

		/**
		 * Input {@link ManagedObjectSourceWirer}.
		 */
		private static ManagedObjectSourceWirer inputWirer;

		/**
		 * Resets for the next test.
		 * 
		 * @param connection
		 *            {@link Connection}.
		 * @param inputManagedObject
		 *            {@link MockInputManagedObjectSource}.
		 * @param inputWirer
		 *            Input {@link ManagedObjectSourceWirer}.
		 */
		public static void reset(Connection connection,
				MockInputManagedObjectSource inputManagedObject,
				ManagedObjectSourceWirer inputWirer) {
			MockSupplierSource.connection = connection;
			MockSupplierSource.inputManagedObject = inputManagedObject;
			MockSupplierSource.inputWirer = inputWirer;
		}

		/*
		 * ======================== SupplierSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_TEST);
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Ensure have property
			assertEquals("Incorrect property value", "supplier.value",
					context.getProperty(PROPERTY_TEST));

			// Supply the managed objects
			context.addManagedObject(new SingletonManagedObjectSource(
					connection), null, new AutoWire(Connection.class));
			AutoWireObject object = context.addManagedObject(
					inputManagedObject, inputWirer, new AutoWire(Queue.class));
			object.addProperty(MockInputManagedObjectSource.PROPERTY_TEST,
					"mo.value");
		}
	}

	/**
	 * Flows for the {@link MockInputManagedObjectSource}.
	 */
	public static enum InputFlows {
		INPUT
	}

	/**
	 * Mock Input {@link ManagedObject}.
	 */
	@TestSource
	public static class MockInputManagedObjectSource extends
			AbstractManagedObjectSource<None, InputFlows> implements
			ManagedObject, ExtensionInterfaceFactory<XAResource> {

		/**
		 * {@link Property} to test provided in supplying.
		 */
		public static final String PROPERTY_TEST = "mo.property";

		/**
		 * {@link Queue}.
		 */
		private final Queue<?> queue;

		/**
		 * {@link ManagedObjectExecuteContext}.
		 */
		private ManagedObjectExecuteContext<InputFlows> executeContext;

		/**
		 * Initiate.
		 * 
		 * @param queue
		 *            {@link Queue}.
		 */
		public MockInputManagedObjectSource(Queue<?> queue) {
			this.queue = queue;
		}

		/**
		 * Invokes the {@link ProcessState}.
		 * 
		 * @param parameter
		 *            Parameter.
		 */
		public void invokeProcess(String parameter) {
			this.executeContext.invokeProcess(InputFlows.INPUT, parameter,
					this, 0);
		}

		/*
		 * ===================== ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_TEST);
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, InputFlows> context)
				throws Exception {
			ManagedObjectSourceContext<InputFlows> mosContext = context
					.getManagedObjectSourceContext();

			// Ensure have property
			assertEquals("Incorrect property value", "mo.value",
					mosContext.getProperty(PROPERTY_TEST));

			// Provide the meta-data
			context.setObjectClass(Queue.class);
			context.addFlow(InputFlows.INPUT, String.class);
			context.addManagedObjectExtensionInterface(XAResource.class, this);
		}

		@Override
		public void start(ManagedObjectExecuteContext<InputFlows> context)
				throws Exception {
			this.executeContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not source from InputManagedObject");
			return null;
		}

		/*
		 * ======================== ManagedObject ==========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.queue;
		}

		/*
		 * =================== ExtensionInterfaceFactory ====================
		 */

		@Override
		public XAResource createExtensionInterface(ManagedObject managedObject) {
			MockInputManagedObjectSource input = (MockInputManagedObjectSource) managedObject;
			MockQueue mockQueue = (MockQueue) input.queue;
			return mockQueue;
		}
	}

}