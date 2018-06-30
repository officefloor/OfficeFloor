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
package net.officefloor.jdbc.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.pool.ThreadLocalJdbcConnectionPoolSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Abstract tests for an JDBC vendor implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJdbcTestCase extends OfficeFrameTestCase {

	/**
	 * Obtains a {@link Connection} with the input
	 * {@link ConnectionManagedObjectSource} and properties.
	 *
	 * @param connectionMosClass {@link ConnectionManagedObjectSource} {@link Class}
	 *                           used to obtain the {@link Connection}.
	 * @param propertyLoader     Loads the properties.
	 * @return {@link Connection}.
	 * @throws Exception If fails to create {@link Connection}.
	 */
	public static Connection getConnection(Class<? extends ConnectionManagedObjectSource> connectionMosClass,
			Consumer<PropertyConfigurable> propertyLoader) throws Exception {

		// Run OfficeFloor with pool to obtain connection
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", SetupSection.class);

			// Connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					connectionMosClass.getName());
			propertyLoader.accept(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Pool the connection (keeps it alive)
			OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
					ThreadLocalJdbcConnectionPoolSource.class.getName());
			context.getOfficeArchitect().link(mos, pool);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		try {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.script", null);
		} catch (Throwable ex) {
			throw fail(ex);
		} finally {
			officeFloor.closeOfficeFloor();
		}

		// Obtain connection
		return SetupSection.connection;
	}

	public static class SetupSection {

		private static Connection connection;

		public void script(Connection connection) throws SQLException {
			SetupSection.connection = connection;
		}
	}

	/**
	 * Obtains the {@link ConnectionManagedObjectSource} {@link Class} being tested.
	 * 
	 * @return {@link ConnectionManagedObjectSource} {@link Class} being tested.
	 */
	protected abstract Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass();

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected abstract void loadProperties(PropertyConfigurable mos);

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 * @throws SQLException On failure to clean up the database.
	 */
	protected abstract void cleanDatabase(Connection connection) throws SQLException;

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Ignore errors in trying to start
		PrintStream stdout = System.out;
		PrintStream stderr = System.err;
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
		try {

			// Try until time out (as may take time for database to come up)
			final int MAX_SETUP_TIME = 30000; // milliseconds
			long startTimestamp = System.currentTimeMillis();
			NEXT_TRY: for (;;) {
				try {

					// Obtain connection
					// Must keep reference to keep potential in memory databases active
					this.connection = getConnection(this.getConnectionManagedObjectSourceClass(),
							(mos) -> this.loadProperties(mos));

					// Clean database
					this.cleanDatabase(this.connection);

					// Successful setup
					return;

				} catch (Throwable ex) {

					// Failed setup, determine if try again
					long currentTimestamp = System.currentTimeMillis();
					if (currentTimestamp > (startTimestamp + MAX_SETUP_TIME)) {
						throw new RuntimeException("Timed out setting up JDBC test ("
								+ (currentTimestamp - startTimestamp) + " milliseconds)", ex);

					} else {
						// Try again in a little
						Thread.sleep(100);
						continue NEXT_TRY;
					}
				}
			}

		} finally {
			// Reinstate standard out / error
			System.setOut(stdout);
			System.setErr(stderr);
		}
	}

	/**
	 * Enables adding additional properties to specification.
	 * 
	 * @param properties {@link Properties} to be loaded with additional
	 *                   specification.
	 */
	protected void loadOptionalSpecification(Properties properties) {
	}

	/**
	 * Ensure correct specification.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testSpecification() throws Exception {

		// Load the specification
		PropertyList specification = OfficeFloorCompiler.newOfficeFloorCompiler(null).getManagedObjectLoader()
				.loadSpecification(this.getConnectionManagedObjectSourceClass());
		Properties actual = specification.getProperties();
		this.loadOptionalSpecification(actual);

		// Obtain the expected properties
		Properties expected = new Properties();
		this.loadProperties((name, value) -> expected.put(name, value));

		// Ensure the correct specification
		for (String name : expected.stringPropertyNames()) {
			assertTrue("Missing specification property " + name, actual.containsKey(name));
		}
		assertEquals("Incorrect number of properties (e: " + expected + ", a: " + actual + ")", expected.size(),
				actual.size());
	}

	/**
	 * Ensure correct type.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testType() throws Exception {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Connection.class);

		// Create the properties
		List<String> properties = new LinkedList<>();
		this.loadProperties((name, value) -> {
			properties.add(name);
			properties.add(value);
		});

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getConnectionManagedObjectSourceClass(),
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Ensure can connect to database.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testConnectivity() throws Throwable {

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			context.addSection("SECTION", ConnectivitySection.class);
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					this.getConnectionManagedObjectSourceClass().getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.checkConnectivity", null);
		officeFloor.closeOfficeFloor();

		// Ensure row in database
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT NAME FROM OFFICE_FLOOR_JDBC_TEST WHERE ID = ?")) {
			statement.setInt(1, 1);
			ResultSet resultSet = statement.executeQuery();
			assertTrue("Should have row in database", resultSet.next());
			assertEquals("Incorrect row in database", "test", resultSet.getString("NAME"));
		}

		// As no pooling, should close the connection
		assertTrue("Connection should be closed", ConnectivitySection.connection.isClosed());
	}

	public static class ConnectivitySection {

		private static Connection connection;

		public void checkConnectivity(Connection connection) throws SQLException {
			ConnectivitySection.connection = connection;

			// Create table with row
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
				statement.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			}
		}
	}

	/**
	 * Ensure can stress test against the database.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testStress() throws Throwable {
		this.doStressTest(false);
	}

	/**
	 * Ensure can stress test against the database with transactions.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testTransactionStress() throws Throwable {
		this.doStressTest(true);
	}

	/**
	 * Undertake stress test.
	 * 
	 * @param isTransaction Whether test uses transactions.
	 */
	private void doStressTest(boolean isTransaction) throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;
		StressSection.isTransaction = isTransaction;

		// Undertake stress test
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {
			// Provide different thread
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					NewThread.class.getName());
		});
		compiler.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SECTION", StressSection.class);

			// Connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					this.getConnectionManagedObjectSourceClass().getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Pool the connection
			OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
					ThreadLocalJdbcConnectionPoolSource.class.getName());
			context.getOfficeArchitect().link(mos, pool);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Setup table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
		}

		// Undertake warm up
		for (int i = 0; i < WARM_UP; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
		}

		// Run test
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
		}
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * 2) / (((float) runTime) / 1000.0));
		System.out.println(this.getClass().getSimpleName() + " " + this.getName() + ": performance "
				+ +requestsPerSecond + " inserts/sec");

		// Ensure inserted all rows
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT COUNT(*) AS ENTRY_COUNT FROM OFFICE_FLOOR_JDBC_TEST")) {
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			assertEquals("Should create 2 rows for each run", ((RUN_COUNT + WARM_UP) * 2),
					resultSet.getInt("ENTRY_COUNT"));
		}

		// Complete
		officeFloor.closeOfficeFloor();
	}

	public static class NewThread {
	}

	public static class StressSection {

		private static volatile boolean isTransaction = false;

		@FlowInterface
		public static interface Flows {
			void thread();
		}

		private final AtomicInteger id = new AtomicInteger(1);

		public void run(Connection connection, Flows flows) throws SQLException {
			if (isTransaction) {
				connection.setAutoCommit(false);
			}
			this.insertRow(connection, "run");
			flows.thread();
		}

		public void thread(Connection connection, NewThread thread) throws SQLException {
			this.insertRow(connection, Thread.currentThread().getName());
		}

		private void insertRow(Connection connection, String name) throws SQLException {
			try (PreparedStatement statement = connection
					.prepareStatement("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( ?, ? )")) {
				statement.setInt(1, this.id.getAndIncrement());
				statement.setString(2, name);
				assertEquals("Should add the row", 1, statement.executeUpdate());
			}
		}
	}

	/**
	 * Undertakes select stress.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testSelectStress() throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;

		// Undertake stress test
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {
			// Provide different thread
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					NewThread.class.getName());
		});
		compiler.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SECTION", SelectSection.class);
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);

			// Connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					this.getConnectionManagedObjectSourceClass().getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Pool the connection
			OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
					ThreadLocalJdbcConnectionPoolSource.class.getName());
			context.getOfficeArchitect().link(mos, pool);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Setup table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
			statement.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID , NAME ) VALUES ( 1, 'test' )");
		}

		// Undertake warm up
		for (int i = 0; i < WARM_UP; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
		}

		// Run test
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			SelectSection.isCompleted = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
			assertTrue("Should be complete", SelectSection.isCompleted);
		}
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * SelectSection.THREAD_COUNT) / (((float) runTime) / 1000.0));
		System.out.println(this.getClass().getSimpleName() + " " + this.getName() + ": performance "
				+ +requestsPerSecond + " selects/sec");

		// Complete
		officeFloor.closeOfficeFloor();
	}

	private static class SelectParameter {

		private volatile String name = null;
	}

	public static class SelectSection {

		private static int THREAD_COUNT = 10;

		private static boolean isCompleted = false;

		@FlowInterface
		public static interface Flows {
			void thread(SelectParameter parameter, FlowCallback callback);
		}

		public void run(Flows flows) throws SQLException {
			int[] completed = new int[] { 0 };
			for (int i = 0; i < THREAD_COUNT; i++) {
				SelectParameter parameter = new SelectParameter();
				flows.thread(parameter, (exception) -> {
					assertNull("Should be no failure in thread", exception);
					assertEquals("Should obtain name", "test", parameter.name);
					completed[0]++;
					if (completed[0] == THREAD_COUNT) {
						isCompleted = true;
					}
				});
			}
		}

		public void thread(Connection connection, @Parameter SelectParameter parameter, NewThread tag)
				throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM OFFICE_FLOOR_JDBC_TEST")) {
				ResultSet resultSet = statement.executeQuery();
				assertTrue("Should have row from database", resultSet.next());
				parameter.name = resultSet.getString("NAME");
			}
		}
	}

}