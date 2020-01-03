/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jdbc.pool;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.AbstractConnectionTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ConnectionWrapper;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the transaction scope of the
 * {@link ThreadLocalJdbcConnectionPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPoolTransactionTest extends AbstractConnectionTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Compiles {@link OfficeFloor}.
	 */
	private OfficeFloor compileOfficeFloor() throws Exception {

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

		// Return the OfficeFloor
		return this.officeFloor;
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			super.tearDown();
		} finally {
			if (this.officeFloor != null) {

				// Close the OfficeFloor
				this.officeFloor.closeOfficeFloor();

				// Should clean up and close connections
				assertTrue("Should close service delegate connection", MockSection.serviceDelegate.isClosed());
				assertTrue("Should close thread delegate connection", MockSection.threadDelegate.isClosed());
			}
		}
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} with compiler.
	 */
	public void testNoTransactionWithCompiler() throws Throwable {
		this.doNoTransactionTest();
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} with {@link Proxy}.
	 */
	public void testNoTransactionWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doNoTransactionTest());
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection}.
	 */
	@SuppressWarnings("resource")
	public void doNoTransactionTest() throws Throwable {

		// Compile OfficeFloor
		this.compileOfficeFloor();

		// Invoke process
		MockSection.isTransaction = false;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertTrue("Connection should still be pooled", !MockSection.serviceDelegate.isClosed());
		assertTrue("Thread connection should still be pooled", !MockSection.threadDelegate.isClosed());

		// Current thread connection should be returned to pool
		assertNull("Should not have thread local bound connection", MockSection.pool.getThreadLocalConnection());

		// Close OfficeFloor (should close the connections)
		this.officeFloor.closeOfficeFloor();

		// Ensure connection is closed (removed from thread)
		Connection connection = MockSection.pool.getThreadLocalConnection();
		long startTime = System.currentTimeMillis();
		while (connection != null) {
			this.timeout(startTime);
			Thread.sleep(100);
			connection = MockSection.pool.getThreadLocalConnection();
		}
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} within a
	 * transaction with compiler.
	 */
	public void testWithinTransactionWithCompiler() throws Throwable {
		this.doWithinTransactionTest();
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} within a
	 * transaction with {@link Proxy}.
	 */
	public void testWithinTransactionWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doWithinTransactionTest());
	}

	/**
	 * Ensure able to use {@link ThreadLocal} {@link Connection} within a
	 * transaction.
	 */
	public void doWithinTransactionTest() throws Throwable {

		// Compile OfficeFloor
		this.compileOfficeFloor();

		// Invoke process
		MockSection.isTransaction = true;
		CompileOfficeFloor.invokeProcess(this.officeFloor, "SECTION.service", null);
		assertNotNull("Connection should still be pooled", !MockSection.serviceDelegate.isClosed());
		assertTrue("Thread connection should still be pooled", !MockSection.threadDelegate.isClosed());
		assertSame("Should be same connection", MockSection.serviceDelegate, MockSection.threadDelegate);
	}

	public static class NewThread {
	}

	public static class MockSection {

		private static volatile boolean isTransaction = false;

		private static ThreadLocalJdbcConnectionPool pool = null;

		private static volatile Connection serviceDelegate = null;

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
			assertTrue("Connection should indicate if real to recycle", connection instanceof ConnectionWrapper);
			assertNull("No real connection to recycle", ConnectionWrapper.getRealConnection(connection));
			serviceDelegate = ThreadLocalJdbcConnectionPool.extractDelegateConnection(connection);
			assertNotNull("Should have delegate", serviceDelegate);
			pool = ThreadLocalJdbcConnectionPool.extractConnectionPool(connection);
			assertNotNull("Should have pool", pool);

			// Initiate transaction
			if (isTransaction) {
				connection.setAutoCommit(false);
				assertNotNull("Should now be real connection", ConnectionWrapper.getRealConnection(connection));
			}

			// Undertake flow
			flows.thread(new PassedState(connection, Thread.currentThread()), (exception) -> {
				assertNull("Should be no failure in thread", exception);
				assertNotNull("Should now have thread delegate", threadDelegate);
			});
		}

		public void thread(Connection connection, @Parameter PassedState parameter, NewThread tag) throws SQLException {
			assertNotNull("Should have dependency", connection);
			assertTrue("Thread connection should indicate if real to recycle", connection instanceof ConnectionWrapper);
			assertEquals("Only real connection if within transaction", isTransaction,
					ConnectionWrapper.getRealConnection(connection) != null);
			assertNotSame("Should be different thread", Thread.currentThread(), parameter.thread);
			assertSame("Should be same dependency between teams", connection, parameter.proxy);
			threadDelegate = ThreadLocalJdbcConnectionPool.extractDelegateConnection(connection);
		}
	}

	public static class PassedState {

		private final Connection proxy;

		private final Thread thread;

		private PassedState(Connection proxy, Thread thread) {
			this.proxy = proxy;
			this.thread = thread;
		}
	}

}
