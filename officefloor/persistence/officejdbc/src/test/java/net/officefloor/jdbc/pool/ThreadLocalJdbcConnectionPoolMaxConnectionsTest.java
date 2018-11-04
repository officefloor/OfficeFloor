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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.jdbc.AbstractConnectionTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;

/**
 * Tests the maximum {@link Connection} of the
 * {@link ThreadLocalJdbcConnectionPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalJdbcConnectionPoolMaxConnectionsTest extends AbstractConnectionTestCase {

	/**
	 * Maximum number of {@link Connection} instances for testing.
	 */
	private static final int MAXIMUM_CONNECTIONS = 5;

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
			pool.addProperty(ThreadLocalJdbcConnectionPoolSource.PROPERTY_MAXIMUM_CONNECTIONS,
					String.valueOf(MAXIMUM_CONNECTIONS));
			context.getOfficeFloorDeployer().link(mos, pool);

			// Provide different thread
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					Connection.class.getName());
		});
		compiler.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SECTION", MockSection.class);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();

		// Clear sections
		MockSection.sections.clear();

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
			}
		}
	}

	/**
	 * <p>
	 * Ensure can block on maximum {@link Connection} to avoid creating further
	 * {@link Connection} instances.
	 * <p>
	 * {@link Thread} instances of the {@link Team} should timeout and release their
	 * {@link Connection} back to the pool.
	 * <p>
	 * Note: if {@link Team} size is kept beneath maximum {@link Connection} size
	 * then there should be limited blocking (if any). However, increasing
	 * {@link Team} size above causes extra {@link Thread} instances to the
	 * {@link Connection} causing need to block (to avoid too many
	 * {@link Connection} instances open).
	 */
	public void testMaximumConnections() throws Exception {

		// Obtain the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor();
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function");

		// Run max connection functions to exhaust pool of connections
		for (int i = 0; i < MAXIMUM_CONNECTIONS; i++) {
			function.invokeProcess(null, null);
		}

		// Ensure clean up sections
		List<MockSection> sections = new ArrayList<>();
		try {

			// Ensure all connections available
			Closure<MockSection> firstSection = new Closure<>();
			this.waitForTrue(() -> {

				// Load the sections
				MockSection runningSection;
				while ((runningSection = MockSection.sections.poll()) != null) {
					if (firstSection.value == null) {
						firstSection.value = runningSection;
					}
					sections.add(runningSection);
				}

				// Ensure have all sections
				if (sections.size() != MAXIMUM_CONNECTIONS) {
					return false;
				}

				// Ensure each has connection
				for (MockSection section : sections) {
					if (!section.hasConnection()) {
						return false;
					}
				}

				// As here, have all connections
				return true;
			});
			assertEquals("Should consume all connections", 0, MockSection.sections.size());

			// Run over maximum connections (should not obtain connection)
			function.invokeProcess(null, null);

			/**
			 * Ensure does not have connection.
			 * 
			 * Note: due to blocking, can not be sure the thread actually attempted to
			 * obtain connection (or was scheduled out before attempting). Therefore, will
			 * make multiple attempts (and running test over time should have higher
			 * percentage chance to ensure blocks on obtaining the connection).
			 */
			Closure<MockSection> overLimitSection = new Closure<>();
			this.waitForTrue(() -> {

				// Obtain the new running section
				MockSection section = MockSection.sections.poll();
				if (section != null) {
					overLimitSection.value = section;
					sections.add(section);
					return true;
				}

				// Section not yet running
				return false;
			});

			// Allow section thread to attempt to obtain connection
			Thread.sleep(1);
			assertFalse("Should be blocked waiting for connection", overLimitSection.value.hasConnection());

			// Release section (so can return connection on thread)
			// Note: team has to allow thread to exit, causing return of connection.
			firstSection.value.flagComplete();

			// Should now allow connection to be obtained
			this.waitForTrue(() -> overLimitSection.value.hasConnection());

		} finally {
			// Flag all sections complete
			for (MockSection section : sections) {
				section.flagComplete();
			}
		}
	}

	/**
	 * Mock section.
	 */
	public static class MockSection {

		private static Deque<MockSection> sections = new ConcurrentLinkedDeque<>();

		private boolean hasConnection = false;

		private boolean isComplete = false;

		public void function(Connection connection) throws Exception {

			// Add this section
			sections.add(this);

			// Use connection, forcing onto the thread
			// Note: at max connections this blocks
			connection.getSchema();

			// Flag that has connection on thread
			long endTime = System.currentTimeMillis() + 1000;
			synchronized (this) {
				this.hasConnection = true;

				// Wait to be notified (holds connection on thread)
				while (!isComplete) {

					// Determine if timed out
					if (System.currentTimeMillis() > endTime) {
						fail("Timed out waiting on completion");
					}

					// Wait some time
					this.wait(10);
				}
			}
		}

		public synchronized boolean hasConnection() {
			return this.hasConnection;
		}

		public synchronized void flagComplete() {
			this.isComplete = true;
		}
	}

}