/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.jdbc.AbstractJdbcManagedObjectSource;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ConnectionWrapper;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Spawn;
import net.officefloor.test.StressTest;

/**
 * Abstract tests for an JDBC vendor implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJdbcTestCase {

	/**
	 * Obtains the {@link ReadOnlyConnectionManagedObjectSource} {@link Class} being
	 * tested.
	 * 
	 * @return {@link ReadOnlyConnectionManagedObjectSource} {@link Class} being
	 *         tested.
	 */
	protected abstract Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass();

	/**
	 * Obtains the {@link DataSourceManagedObjectSource} {@link Class} being tested.
	 * 
	 * @return {@link DataSourceManagedObjectSource} {@link Class} being tested.
	 */
	protected abstract Class<? extends DataSourceManagedObjectSource> getDataSourceManagedObjectSourceClass();

	/**
	 * Loads the properties for the {@link ReadOnlyConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected abstract void loadConnectionProperties(PropertyConfigurable mos);

	/**
	 * Loads the properties for the {@link DataSourceManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected abstract void loadDataSourceProperties(PropertyConfigurable mos);

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 * @throws SQLException On failure to clean up the database.
	 */
	protected abstract void cleanDatabase(Connection connection) throws SQLException;

	/**
	 * {@link Connection}. Keep {@link Connection} to database for in memory
	 * databases to stay alive.
	 */
	protected Connection connection;

	@BeforeEach
	protected void setUp() throws Exception {

		// Ensure clean state (no connections from previous test)
		ValidateConnections.assertNoPreviousTestConnections();

		// Setup test
		synchronized (AbstractJdbcTestCase.class) {

			// Obtain connection
			this.connection = DatabaseTestUtil.waitForAvailableConnection((cleanups) -> {

				// Run OfficeFloor to obtain connection
				CompileOfficeFloor compiler = new CompileOfficeFloor();
				Closure<AutoWireStateManagerFactory> factory = new Closure<>();
				compiler.getOfficeFloorCompiler()
						.addAutoWireStateManagerVisitor((officeName, stateFactory) -> factory.value = stateFactory);
				compiler.office((office) -> {

					// Connection
					OfficeManagedObjectSource mos = office.getOfficeArchitect().addOfficeManagedObjectSource("mo",
							this.getDataSourceManagedObjectSourceClass().getName());
					this.loadDataSourceProperties(mos);
					mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);
				});
				OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
				cleanups.addCleanup(() -> officeFloor.close());

				// Create the state manager
				AutoWireStateManager stateManager = factory.value.createAutoWireStateManager();
				cleanups.addCleanup(() -> stateManager.close());

				// Obtain the data source
				try {
					return stateManager.getObject(null, DataSource.class, 3000);
				} catch (Throwable ex) {
					throw new Exception(ex);
				}

			}, (connection) -> {
				// Clean database
				this.cleanDatabase(connection);
			});
		}
	}

	@AfterEach
	protected void tearDown() throws Exception {
		synchronized (AbstractJdbcTestCase.class) {

			// Close
			if (this.connection != null) {
				this.connection.close();
			}
		}

		// Ensure no open connections
		ValidateConnections.assertAllConnectionsClosed();
	}

	/**
	 * Enables adding additional {@link Connection} properties to specification.
	 * 
	 * @param properties {@link Properties} to be loaded with additional
	 *                   specification.
	 */
	protected void loadOptionalConnectionSpecification(Properties properties) {
	}

	/**
	 * Enables adding additional {@link DataSource} properties to specification.
	 * 
	 * @param properties {@link Properties} to be loaded with additional
	 *                   specification.
	 */
	protected void loadOptionalDataSourceSpecification(Properties properties) {
		this.loadDataSourceProperties((name, value) -> properties.setProperty(name, value));
	}

	/**
	 * Ensure correct specification for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testReadOnlyConnectionSpecification() throws Exception {
		this.doSpecification(this.getReadOnlyConnectionManagedObjectSourceClass());
	}

	/**
	 * Ensure correct specification for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testDataSourceSpecification() throws Exception {
		this.doSpecification(this.getDataSourceManagedObjectSourceClass());
	}

	/**
	 * Ensure correct specification.
	 *
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @throws Exception On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doSpecification(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Load the specification
		PropertyList specification = OfficeFloorCompiler.newOfficeFloorCompiler(null).getManagedObjectLoader()
				.loadSpecification(managedObjectSourceClass);
		Properties actual = specification.getProperties();
		if (managedObjectSourceClass == this.getDataSourceManagedObjectSourceClass()) {
			this.loadOptionalDataSourceSpecification(actual);
		} else {
			this.loadOptionalConnectionSpecification(actual);
		}

		// Obtain the expected properties
		Properties expected = new Properties();
		if (managedObjectSourceClass == this.getDataSourceManagedObjectSourceClass()) {
			this.loadDataSourceProperties((name, value) -> expected.put(name, value));
		} else {
			this.loadConnectionProperties((name, value) -> expected.put(name, value));
		}

		// Ensure the correct specification
		for (String name : expected.stringPropertyNames()) {
			assertTrue(actual.containsKey(name), "Missing specification property " + name);
		}
		assertEquals(expected.size(), actual.size(),
				"Incorrect number of properties (e: " + expected + ", a: " + actual + ")");
	}

	/**
	 * Ensure correct type for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testReadOnlyType() throws Exception {
		this.doTypeTest(this.getReadOnlyConnectionManagedObjectSourceClass(), Connection.class);
	}

	/**
	 * Ensure correct type for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testDataSourceType() throws Exception {
		this.doTypeTest(this.getDataSourceManagedObjectSourceClass(), DataSource.class);
	}

	/**
	 * Ensure correct type.
	 *
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param expectedType             Expected type.
	 * @throws Exception On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doTypeTest(
			Class<MS> managedObjectSourceClass, Class<?> expectedType) throws Exception {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(expectedType);

		// Create the properties
		List<String> properties = new LinkedList<>();
		if (managedObjectSourceClass == this.getDataSourceManagedObjectSourceClass()) {
			// Load data source properties
			this.loadDataSourceProperties((name, value) -> {
				properties.add(name);
				properties.add(value);
			});
		} else {
			// Load connection properties
			this.loadConnectionProperties((name, value) -> {
				properties.add(name);
				properties.add(value);
			});
		}

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, managedObjectSourceClass,
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Ensure read-only connectivity.
	 */
	@Test
	public void testReadOnlyConnectivity() throws Throwable {
		this.doConnectivityTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false);
	}

	/**
	 * Ensure {@link DataSource} connectivity.
	 */
	@Test
	public void testDataSourceConnectivity() throws Throwable {
		this.doConnectivityTest(this.getDataSourceManagedObjectSourceClass(), true);
	}

	/**
	 * Ensure can connect to database.
	 * 
	 * @throws Throwable On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doConnectivityTest(
			Class<MS> managedObjectSourceClass, boolean isRequireConnection) throws Throwable {

		// Indicate if auto commit
		ConnectivitySection.isAutoCommit = !isRequireConnection;

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Add managed object source
			context.addSection("SECTION", ConnectivitySection.class);
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("mo",
					managedObjectSourceClass.getName());
			this.loadConnectionProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide connection if required
			if (isRequireConnection) {
				architect.addOfficeManagedObjectSource("conn", ConnectionManagedObjectSource.class.getName())
						.addOfficeManagedObject("conn", ManagedObjectScope.THREAD);
			}
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.checkConnectivity", null);
		officeFloor.closeOfficeFloor();

		// Ensure row in database
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT NAME FROM OFFICE_FLOOR_JDBC_TEST WHERE ID = ?")) {
			statement.setInt(1, 1);
			ResultSet resultSet = statement.executeQuery();
			assertTrue(resultSet.next(), "Should have row in database");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect row in database");
		}

		// As no pooling, should close the connection
		Connection connection = ConnectionWrapper.getRealConnection(ConnectivitySection.connection);
		assertTrue((connection == null) || (connection.isClosed()), "Connection should be closed");
	}

	public static class ConnectivitySection {

		private static boolean isAutoCommit = false;

		private static Connection connection;

		public void checkConnectivity(Connection connection) throws SQLException {
			ConnectivitySection.connection = connection;

			// Ensure appropriate transaction
			assertEquals(isAutoCommit, connection.getAutoCommit(), "Incorrect transaction state");

			// Create table with row
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
				statement.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			}
		}
	}

	/**
	 * Ensure connection management for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testReadOnlyConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, 2);
	}

	/**
	 * Ensure connection management for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testReadOnlyConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, 2));
	}

	/**
	 * Ensure connection management for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testDataSourceConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getDataSourceManagedObjectSourceClass(), true, 2);
	}

	/**
	 * Ensure connection management for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testDataSourceConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getDataSourceManagedObjectSourceClass(), true, 2));
	}

	/**
	 * Ensures the {@link ValidateConnections} is registered for tracking open
	 * connections and that there is appropriate connection management in place to
	 * close connections.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doConnectionManagementTest(
			Class<MS> managedObjectSourceClass, boolean isRequireConnection, int connectionIncreaseCount)
			throws Throwable {

		// Obtain the number of registered connections
		ConnectionDecoratorSection.expectedConnectionCount = ValidateConnections.getConnectionsRegisteredCount()
				+ connectionIncreaseCount;

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Add connection
			context.addSection("SECTION", ConnectionDecoratorSection.class);
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("mo",
					managedObjectSourceClass.getName());
			this.loadConnectionProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide connection if required
			if (isRequireConnection) {
				architect.addOfficeManagedObjectSource("conn", ConnectionManagedObjectSource.class.getName())
						.addOfficeManagedObject("conn", ManagedObjectScope.THREAD);
			}
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.checkConnectionDecoration", null);
		officeFloor.closeOfficeFloor();

		// Ensure is closed with OfficeFloor
		Connection connection = ConnectionWrapper.getRealConnection(ConnectionDecoratorSection.connection);
		assertTrue((connection == null) || (connection.isClosed()), "Should close connection with closing OfficeFloor");
	}

	public static class ConnectionDecoratorSection {

		private static Connection connection;

		private static int expectedConnectionCount;

		public void checkConnectionDecoration(Connection connection) throws SQLException {
			ConnectionDecoratorSection.connection = connection;

			// Ensure connection retrieved (may be pooled)
			connection.createStatement();

			// Ensure connection is registered
			assertEquals(ValidateConnections.getConnectionsRegisteredCount(), expectedConnectionCount,
					"Incorrect number of connections registered");
		}
	}

	/**
	 * Ensure correct type for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testReadOnlyValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getReadOnlyConnectionManagedObjectSourceClass());
	}

	/**
	 * Ensure correct type for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	@Test
	public void testDataSourceValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getDataSourceManagedObjectSourceClass());
	}

	/**
	 * Ensure can validate the connectivity on start up.
	 * 
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param isPooled                 Indicates if should pool.
	 * @throws Throwable On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doValidateConnectivityTest(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Setup table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Add the connection
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("mo",
					managedObjectSourceClass.getName());
			if (managedObjectSourceClass == this.getDataSourceManagedObjectSourceClass()) {
				this.loadDataSourceProperties(mos);
			} else {
				this.loadConnectionProperties(mos);
			}
			mos.addProperty(AbstractJdbcManagedObjectSource.PROPERTY_DATA_SOURCE_VALIDATE_SQL,
					"INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);
		});

		OfficeFloor officeFloor;

		// Ensure no error on start up
		final PrintStream original = System.err;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(buffer);
		System.setErr(stream);
		try {

			// Open the OfficeFloor
			officeFloor = compiler.compileAndOpenOfficeFloor();

		} finally {
			System.setErr(original);
		}

		// Ensure no errors
		stream.flush();
		String errors = buffer.toString();
		assertEquals(0, errors.length(), "Should be no errors\n\n" + errors);

		// Ensure row in database
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT NAME FROM OFFICE_FLOOR_JDBC_TEST WHERE ID = ?")) {
			statement.setInt(1, 1);
			ResultSet resultSet = statement.executeQuery();
			assertTrue(resultSet.next(), "Should have row in database");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect row in database");
		}

		// Close
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Ensure can stress test against the database with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionStressWithCompiler(TestInfo testInfo) throws Throwable {
		this.doInsertStressTest(false, testInfo);
	}

	/**
	 * Ensure can stress test against the database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionStressWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertStressTest(false, testInfo));
	}

	/**
	 * Ensure can stress test against the database with transactions with compiled
	 * wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionTransactionStressWithCompiler(TestInfo testInfo) throws Throwable {
		this.doInsertStressTest(true, testInfo);
	}

	/**
	 * Ensure can stress test against the database with transactions.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionTransactionStressWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertStressTest(true, testInfo));
	}

	/**
	 * Undertake insert stress test.
	 * 
	 * @param isTransaction Whether test uses transactions.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doInsertStressTest(
			boolean isTransaction, TestInfo testInfo) throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;
		InsertSection.isTransaction = isTransaction;

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
			context.addSection("SECTION", InsertSection.class);

			// Data Source
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					this.getDataSourceManagedObjectSourceClass().getName());
			this.loadDataSourceProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Connection
			context.getOfficeArchitect()
					.addOfficeManagedObjectSource("conn", ConnectionManagedObjectSource.class.getName())
					.addOfficeManagedObject("conn", ManagedObjectScope.THREAD);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Setup table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Identifier
		AtomicInteger id = new AtomicInteger(1);

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(testInfo.getDisplayName());
		int warmupProgress = WARM_UP / 10;
		for (int i = 0; i < WARM_UP; i++) {
			if ((i % warmupProgress) == 0) {
				System.out.print("w");
				System.out.flush();
			}
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", id);
		}
		System.out.println();

		// Run test
		int runProgress = RUN_COUNT / 10;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			if ((i % runProgress) == 0) {
				System.out.print(".");
				System.out.flush();
			}
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", id);
		}
		System.out.println();
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * 2) / (((float) runTime) / 1000.0));
		System.out.println(requestsPerSecond + " inserts/sec");
		System.out.println();

		// Ensure inserted all rows
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT COUNT(*) AS ENTRY_COUNT FROM OFFICE_FLOOR_JDBC_TEST")) {
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			assertEquals(((RUN_COUNT + WARM_UP) * 2), resultSet.getInt("ENTRY_COUNT"),
					"Should create 2 rows for each run");
		}

		// Complete
		officeFloor.closeOfficeFloor();
	}

	public static class NewThread {
	}

	@FlowInterface
	public static interface Flows {
		void thread(AtomicInteger id);
	}

	public static class InsertSection {

		private static volatile boolean isTransaction = false;

		public void run(@Parameter AtomicInteger id, Connection connection, Flows flows) throws SQLException {
			if (isTransaction) {
				connection.setAutoCommit(false);
			}
			insertRow(connection, id, "run");
			flows.thread(id);
		}

		public void thread(@Parameter AtomicInteger id, Connection connection, NewThread thread) throws SQLException {
			insertRow(connection, id, Thread.currentThread().getName());
		}

		private static void insertRow(Connection connection, AtomicInteger id, String name) throws SQLException {
			try (PreparedStatement statement = connection
					.prepareStatement("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( ?, ? )")) {
				statement.setInt(1, id.getAndIncrement());
				statement.setString(2, name);
				assertEquals(1, statement.executeUpdate(), "Should add the row");
			}
		}
	}

	/**
	 * Undertakes select stress for read-only {@link Connection} with compiled
	 * wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testReadOnlySelectStressWithCompiler(TestInfo testInfo) throws Throwable {
		this.doSelectStressTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, testInfo);
	}

	/**
	 * Undertakes select stress for read-only {@link Connection} with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testReadOnlySelectStressWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doSelectStressTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, testInfo));
	}

	/**
	 * Undertakes select stress for {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testDataSourceSelectStress(TestInfo testInfo) throws Throwable {
		this.doSelectStressTest(this.getDataSourceManagedObjectSourceClass(), true, testInfo);
	}

	/**
	 * Undertakes select stress for writable {@link Connection} with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testDataSourceSelectStressWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doSelectStressTest(this.getDataSourceManagedObjectSourceClass(), true, testInfo));
	}

	/**
	 * Undertakes select stress.
	 * 
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param isRequireConnection      Indicates if requires
	 *                                 {@link ConnectionManagedObjectSource}.
	 * @param testInfo                 {@link TestInfo}.
	 * @throws Throwable On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doSelectStressTest(
			Class<?> managedObjectSourceClass, boolean isRequireConnection, TestInfo testInfo) throws Throwable {

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
					managedObjectSourceClass.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Handle if provide connection
			if (!isRequireConnection) {
				this.loadConnectionProperties(mos);

			} else {
				this.loadDataSourceProperties(mos);

				// Provide connection
				context.getOfficeArchitect()
						.addOfficeManagedObjectSource("conn", ConnectionManagedObjectSource.class.getName())
						.addOfficeManagedObject("conn", ManagedObjectScope.THREAD);
			}
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Setup table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
			statement.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID , NAME ) VALUES ( 1, 'test' )");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(testInfo.getDisplayName());
		int warmupProgress = WARM_UP / 10;
		for (int i = 0; i < WARM_UP; i++) {
			if ((i % warmupProgress) == 0) {
				System.out.print("w");
				System.out.flush();
			}
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
		}
		System.out.println();

		// Run test
		int runProgress = RUN_COUNT / 10;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			if ((i % runProgress) == 0) {
				System.out.print(".");
				System.out.flush();
			}
			SelectSection.isCompleted = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
			assertTrue(SelectSection.isCompleted, "Should be complete");
		}
		System.out.println();
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * SelectSection.THREAD_COUNT) / (((float) runTime) / 1000.0));
		System.out.println(requestsPerSecond + " selects/sec");
		System.out.println();

		// Complete
		officeFloor.closeOfficeFloor();
	}

	public static class SelectParameter {

		private volatile String name = null;
	}

	public static class SelectSection {

		private static int THREAD_COUNT = 10;

		private static boolean isCompleted = false;

		@FlowInterface
		public static interface Flows {

			@Spawn
			void thread(SelectParameter parameter, FlowCallback callback);
		}

		public void run(Flows flows) throws SQLException {
			int[] completed = new int[] { 0 };
			for (int i = 0; i < THREAD_COUNT; i++) {
				SelectParameter parameter = new SelectParameter();
				flows.thread(parameter, (exception) -> {
					assertNull(exception, "Should be no failure in thread (" + exception + ")");
					assertEquals("test", parameter.name, "Should obtain name");
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
				assertTrue(resultSet.next(), "Should have row from database");
				parameter.name = resultSet.getString("NAME");
			}
		}
	}

}
