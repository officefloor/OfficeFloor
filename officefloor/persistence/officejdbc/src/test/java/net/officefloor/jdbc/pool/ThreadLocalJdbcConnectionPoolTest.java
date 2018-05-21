/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.jdbc.pool;

import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.AbstractConnectionTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link ThreadLocalJdbcConnectionPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPoolTest extends AbstractConnectionTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {

			// Connection
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("mo",
					ConnectionManagedObjectSource.class.getName());
			this.loadProperties(mos);
			mos.addOfficeFloorManagedObject("mo", ManagedObjectScope.THREAD);

			// Pool the connection
			OfficeFloorManagedObjectPool pool = context.getOfficeFloorDeployer().addManagedObjectPool("POOL",
					ThreadLocalJdbcConnectionPoolSource.class.getName());
			context.getOfficeFloorDeployer().link(mos, pool);

			// Provide different thread
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					NewThread.class.getName());
		});
		compiler.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SECTION", MockSection.class);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Reset mock section for testing
		MockSection.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection}.
	 */
	public void testNoTransaction() throws Throwable {
		MockSection.isTransaction = false;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertNotNull("Connection should still be pooled", !MockSection.serviceDelegate.isClosed());
		assertTrue("Thread connection should still be pooled", !MockSection.threadDelegate.isClosed());

		// Current thread connection should be returned to pool
		assertNull("Should not have thread local bound connection", MockSection.pool.getThreadLocalConnection());

		// Run again (to ensure pooling)
		Connection previous = MockSection.serviceDelegate;
		MockSection.reset();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertTrue(
				"Should obtain same connection from pool (either quickly from main thread completing or requiring new thread)",
				previous == MockSection.serviceDelegate || previous == MockSection.threadDelegate);
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} within a
	 * transaction.
	 */
	public void testWitinTransaction() throws Throwable {
		MockSection.isTransaction = true;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertNotNull("Connection should still be pooled", !MockSection.serviceDelegate.isClosed());
		assertSame("Should be same connection", MockSection.serviceDelegate, MockSection.threadDelegate);
		assertTrue("Thread connection should still be pooled", !MockSection.threadDelegate.isClosed());

		// Run again (to ensure transaction connection returned to pool)
		Connection previous = MockSection.threadDelegate;
		MockSection.reset();
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertTrue("Should re-use transaction connection (returned to pool on completion)",
				previous == MockSection.serviceDelegate || previous == MockSection.threadDelegate);
	}

	public static class NewThread {
	}

	public static class MockSection {

		private static boolean isTransaction = false;

		private static ThreadLocalJdbcConnectionPool pool = null;

		private static Connection serviceDelegate = null;

		private static volatile Connection threadDelegate = null;

		private static void reset() {
			isTransaction = false;
			pool = null;
			serviceDelegate = null;
			threadDelegate = null;
		}

		@FlowInterface
		public static interface Flows {
			void thread(PassedState state, FlowCallback callback);
		}

		public void service(Connection connection, Flows flows) throws SQLException {
			assertNotNull("Should have connection", connection);
			serviceDelegate = ThreadLocalJdbcConnectionPool.extractDelegateConnection(connection);
			assertNotNull("Should have delegate", serviceDelegate);
			pool = ThreadLocalJdbcConnectionPool.extractConnectionPool(connection);
			assertNotNull("Should have pool", pool);

			// Initiate transaction
			if (isTransaction) {
				connection.setAutoCommit(true);
			}

			// Undertake flow
			flows.thread(new PassedState(connection, Thread.currentThread()), (exception) -> {
				assertNull("Should be no failure in thread", exception);
				assertNotNull("Should now have thread delegate", threadDelegate);
			});
		}

		public void thread(Connection connection, @Parameter PassedState parameter, NewThread tag) throws SQLException {
			assertNotNull("Should have dependency", connection);
			assertNotSame("Should be different thread", Thread.currentThread(), parameter.thread);
			assertSame("Should be same dependency between teams", connection, parameter.proxy);
			threadDelegate = ThreadLocalJdbcConnectionPool.extractDelegateConnection(connection);
		}
	}

	private static class PassedState {

		private final Connection proxy;

		private final Thread thread;

		private PassedState(Connection proxy, Thread thread) {
			this.proxy = proxy;
			this.thread = thread;
		}
	}

}