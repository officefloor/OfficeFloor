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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the integration of the {@link AutoWireGovernance}.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireGovernanceTest extends OfficeFrameTestCase {

	/**
	 * {@link Thread} instances that executed the {@link Job} instances.
	 */
	private static final List<Thread> threadForJob = new ArrayList<Thread>(2);

	/**
	 * Registers the {@link Thread} for the {@link Job}.
	 */
	private static void registerJobThread() {
		synchronized (threadForJob) {
			threadForJob.add(Thread.currentThread());
		}
	}

	/**
	 * {@link Xid} for testing.
	 */
	private static Xid xid;

	@Override
	protected void setUp() throws Exception {
		// Specify the Xid
		xid = this.createMock(Xid.class);
	}

	/**
	 * Ensure can govern object.
	 */
	public void testGovernObject() throws Exception {
		this.doGovernanceTest(false);
	}

	/**
	 * Ensure can govern {@link ManagedObject}.
	 */
	public void testGovernManagedObject() throws Exception {
		this.doGovernanceTest(true);
	}

	/**
	 * Undertakes integration testing the {@link AutoWireGovernance}.
	 * 
	 * @param isManagedObject
	 *            Indicates to use {@link ManagedObject} to wrap {@link Object}.
	 */
	private void doGovernanceTest(boolean isManagedObject) throws Exception {

		final MockObject object = this.createSynchronizedMock(MockObject.class);
		final CallableStatement statement = this
				.createSynchronizedMock(CallableStatement.class);

		// Ensure in valid state for running test
		synchronized (threadForJob) {
			threadForJob.clear();
		}

		// Record execution
		object.start(xid, 1);
		this.recordReturn(object,
				object.prepareCall("UPDATE TEST_CASE SET TEST = 'PASSED'"),
				statement);
		this.recordReturn(statement, statement.execute(), false);
		this.recordReturn(object, object.prepare(xid), 0);
		object.commit(xid, true);

		// Test
		this.replayMockObjects();

		// Configure the application
		AutoWireApplication application = new AutoWireOfficeFloorSource();
		AutoWireSection section = application
				.addSection("SECTION", ClassSectionSource.class.getName(),
						MockSection.class.getName());
		AutoWireGovernance governance = application.addGovernance("GOVERNANCE",
				ClassGovernanceSource.class.getName());
		governance.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME,
				MockGovernance.class.getName());
		governance.governSection(section);
		application.assignTeam(OnePersonTeamSource.class.getName(),
				new AutoWire(XAResource.class));
		application.assignDefaultTeam(OnePersonTeamSource.class.getName());

		// Configure the object / managed object
		if (isManagedObject) {
			// Configure the managed object
			MockManagedObjectSource.object = object;
			application.addManagedObject(
					MockManagedObjectSource.class.getName(), null,
					new AutoWire(Connection.class));

		} else {
			// Configure the object
			application.addObject(object, new AutoWire(Connection.class));
		}

		// Execute the task
		AutoWireOfficeFloor officeFloor = application.openOfficeFloor();
		officeFloor.invokeTask("SECTION.WORK", "task", null);

		// Verify functionality
		this.verifyMockObjects();

		// Validate teams
		synchronized (threadForJob) {
			// Ensure appropriate threads executing teams
			assertEquals("Incorrect number of teams", 2, threadForJob.size());
			assertTrue(
					"Should be different threads executing governance and the task",
					threadForJob.get(0) != threadForJob.get(1));
		}
	}

	/**
	 * Mock section class for {@link ClassSectionSource}.
	 */
	public static class MockSection {

		public void task(Connection connection) throws SQLException {
			registerJobThread();
			CallableStatement statement = connection
					.prepareCall("UPDATE TEST_CASE SET TEST = 'PASSED'");
			statement.execute();
		}
	}

	/**
	 * Mock object with {@link Connection} as object interface and
	 * {@link XAResource} as extension interface.
	 */
	public static interface MockObject extends Connection, XAResource {
	}

	/**
	 * Mock {@link Governance} class for {@link ClassGovernanceSource}.
	 */
	public static class MockGovernance {

		/**
		 * {@link XAResource} instances under governance.
		 */
		private final List<XAResource> resources = new LinkedList<XAResource>();

		@Govern
		public void govern(XAResource resource) throws XAException {
			registerJobThread();
			resource.start(xid, 1);
			this.resources.add(resource);
		}

		@Enforce
		public void enforce() throws XAException {

			// Prepare commit
			for (XAResource resource : this.resources) {
				resource.prepare(xid);
			}

			// Commit
			for (XAResource resource : this.resources) {
				resource.commit(xid, true);
			}
		}
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	public static class MockManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject,
			ExtensionInterfaceFactory<XAResource> {

		/**
		 * {@link MockObject}.
		 */
		public static volatile MockObject object;

		/*
		 * =================== ManagedObjectSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setObjectClass(Connection.class);
			context.addManagedObjectExtensionInterface(XAResource.class, this);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ====================== ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return object;
		}

		/*
		 * ================== ExtensionInterfaceFactory =================
		 */

		@Override
		public XAResource createExtensionInterface(ManagedObject managedObject) {
			return object;
		}
	}

}