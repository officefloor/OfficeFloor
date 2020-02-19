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

package net.officefloor.jdbc.test;

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
import java.util.function.Consumer;

import javax.sql.DataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.impl.AbstractManagedObjectPoolSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.AbstractConnectionManagedObjectSource;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ConnectionWrapper;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.pool.ThreadLocalJdbcConnectionPoolSource;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Spawn;

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

		// Avoid compiling as leaves files open (avoids running out of file handles)
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> {
			loadConnection(connectionMosClass, propertyLoader);
		});

		// Obtain connection
		return SetupSection.connection;
	}

	/**
	 * Loads the {@link Connection}.
	 * 
	 * @param connectionMosClass {@link ConnectionManagedObjectSource} {@link Class}
	 *                           used to obtain the {@link Connection}.
	 * @param propertyLoader     Loads the properties.
	 * @return {@link Connection}.
	 * @throws Exception If fails to create {@link Connection}.
	 */
	private static void loadConnection(Class<? extends ConnectionManagedObjectSource> connectionMosClass,
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

			// Obtain the connection via pool return
			OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
					new GetConnectionManagedObjectPoolSource());
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
	}

	/**
	 * {@link ManagedObjectPoolSource} to obtain a {@link Connection}.
	 */
	private static class GetConnectionManagedObjectPoolSource extends AbstractManagedObjectPoolSource {

		/*
		 * ==================== ManagedObjectPoolSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext context) throws Exception {
			context.setPooledObjectType(Connection.class);
			context.setManagedObjectPoolFactory((poolContext) -> new GetConnectionManagedObjectPool(poolContext));
		}
	}

	/**
	 * {@link ManagedObjectPool} to obtain a {@link Connection}.
	 */
	private static class GetConnectionManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectPoolContext}.
		 */
		private final ManagedObjectPoolContext context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ManagedObjectPoolContext}.
		 */
		private GetConnectionManagedObjectPool(ManagedObjectPoolContext context) {
			this.context = context;
		}

		/*
		 * ===================== ManagedObjectPool ==========================
		 */

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.context.getManagedObjectSource().sourceManagedObject(user);
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			// Allow connection to live beyond close of OfficeFloor
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			try {
				SetupSection.connection.close();
			} catch (SQLException ex) {
				// Ignore close failure
			} finally {
				// No connection
				SetupSection.connection = null;
			}
		}

		@Override
		public void empty() {
			// nothing to clean up
		}
	}

	public static class SetupSection {

		private static Connection connection;

		public void script(Connection connection) throws SQLException {
			SetupSection.connection = ConnectionWrapper.getRealConnection(connection);
		}

	}

	/**
	 * Obtains the {@link ConnectionManagedObjectSource} {@link Class} being tested.
	 * 
	 * @return {@link ConnectionManagedObjectSource} {@link Class} being tested.
	 */
	protected abstract Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass();

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
	protected Class<? extends DataSourceManagedObjectSource> getDataSourceManagedObjectSourceClass() {
		return DataSourceManagedObjectSource.class;
	}

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource} and
	 * {@link ReadOnlyConnectionManagedObjectSource}.
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

	@Override
	protected void setUp() throws Exception {

		// Ensure clean state (no connections from previous test)
		ValidateConnections.assertNoPreviousTestConnections();

		// Setup test
		synchronized (AbstractJdbcTestCase.class) {
			super.setUp();

			// Obtain connection
			this.connection = DataSourceRule.waitForDatabaseAvailable(AbstractJdbcTestCase.class, (context) -> {

				// Obtain the connection
				Connection connection = context.setConnection(getConnection(
						this.getConnectionManagedObjectSourceClass(), (mos) -> this.loadConnectionProperties(mos)));

				// Clean database
				this.cleanDatabase(connection);
			});
		}
	}

	@Override
	protected void tearDown() throws Exception {
		synchronized (AbstractJdbcTestCase.class) {
			super.tearDown();

			// Close connection
			this.connection.close();
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
	 * Ensure correct specification for writable {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testWritableConnectionSpecification() throws Exception {
		this.doSpecification(this.getConnectionManagedObjectSourceClass());
	}

	/**
	 * Ensure correct specification for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testReadOnlyConnectionSpecification() throws Exception {
		this.doSpecification(this.getReadOnlyConnectionManagedObjectSourceClass());
	}

	/**
	 * Ensure correct specification for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
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
			assertTrue("Missing specification property " + name, actual.containsKey(name));
		}
		assertEquals("Incorrect number of properties (e: " + expected + ", a: " + actual + ")", expected.size(),
				actual.size());
	}

	/**
	 * Ensure correct type for writable {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testWritableType() throws Exception {
		this.doTypeTest(this.getConnectionManagedObjectSourceClass(), Connection.class);
	}

	/**
	 * Ensure correct type for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testReadOnlyType() throws Exception {
		this.doTypeTest(this.getReadOnlyConnectionManagedObjectSourceClass(), Connection.class);
	}

	/**
	 * Ensure correct type for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
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
	 * Ensure can connect to database.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testWritableConnectivity() throws Throwable {
		this.doWritableConnectivityTest(false);
	}

	/**
	 * Ensure can connect to database.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testPooledWritableConnectivity() throws Throwable {
		this.doWritableConnectivityTest(true);
	}

	/**
	 * Ensure can connect to database.
	 * 
	 * @throws Throwable On test failure.
	 */
	private void doWritableConnectivityTest(boolean isPooled) throws Throwable {

		// Pooled connections not within transaction (by default)
		ConnectivitySection.isAutoCommit = isPooled;

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Add connection
			context.addSection("SECTION", ConnectivitySection.class);
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("mo",
					this.getConnectionManagedObjectSourceClass().getName());
			this.loadConnectionProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide pooling
			if (isPooled) {
				architect.link(mos,
						architect.addManagedObjectPool("POOL", ThreadLocalJdbcConnectionPoolSource.class.getName()));
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
			assertTrue("Should have row in database", resultSet.next());
			assertEquals("Incorrect row in database", "test", resultSet.getString("NAME"));
		}

		// As no pooling, should close the connection
		Connection connection = ConnectionWrapper.getRealConnection(ConnectivitySection.connection);
		assertTrue("Connection should be closed", (connection == null) || (connection.isClosed()));
	}

	public static class ConnectivitySection {

		private static Connection connection;

		private static boolean isAutoCommit;

		public void checkConnectivity(Connection connection) throws SQLException {
			ConnectivitySection.connection = connection;

			// Ensure connection within transaction
			assertEquals("Incorrect transaction state", isAutoCommit, connection.getAutoCommit());

			// Create table with row
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE TABLE OFFICE_FLOOR_JDBC_TEST ( ID INT, NAME VARCHAR(255) )");
				statement.execute("INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			}
		}
	}

	/**
	 * Ensure connection management for writable {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testWritableConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getConnectionManagedObjectSourceClass(), false, 2);
	}

	/**
	 * Ensure connection management for writable {@link Connection} using fallback
	 * {@link Proxy}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testWritableConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getConnectionManagedObjectSourceClass(), false, 2));
	}

	/**
	 * Ensure connection management for writable {@link Connection} being pooled.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testPooledWritableConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getConnectionManagedObjectSourceClass(), true, 2);
	}

	/**
	 * Ensure connection management for writable {@link Connection} being pooled
	 * using fallback {@link Proxy}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testPooledWritableConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getConnectionManagedObjectSourceClass(), true, 2));
	}

	/**
	 * Ensure connection management for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testReadOnlyConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, 2);
	}

	/**
	 * Ensure connection management for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testReadOnlyConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false, 2));
	}

	/**
	 * Ensure connection management for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testDataSourceConnectionManagement() throws Throwable {
		this.doConnectionManagementTest(this.getDataSourceManagedObjectSourceClass(), false, 2);
	}

	/**
	 * Ensure connection management for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testDataSourceConnectionManagementViaProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(
				() -> this.doConnectionManagementTest(this.getDataSourceManagedObjectSourceClass(), false, 2));
	}

	/**
	 * Ensures the {@link ValidateConnections} is registered for
	 * tracking open connections and that there is appropriate connection management
	 * in place to close connections.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doConnectionManagementTest(
			Class<MS> managedObjectSourceClass, boolean isPooled, int connectionIncreaseCount) throws Throwable {

		// Obtain the number of registered connections
		ConnectionDecoratorSection.expectedConnectionCount = ValidateConnections
				.getConnectionsRegisteredCount() + connectionIncreaseCount;

		// Run connectivity to create table and add row
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect architect = context.getOfficeArchitect();

			// Add connection
			context.addSection("SECTION", ConnectionDecoratorSection.class);
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("mo",
					this.getConnectionManagedObjectSourceClass().getName());
			this.loadConnectionProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide pooling
			if (isPooled) {
				architect.link(mos,
						architect.addManagedObjectPool("POOL", ThreadLocalJdbcConnectionPoolSource.class.getName()));
			}
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.checkConnectionDecoration", null);
		officeFloor.closeOfficeFloor();

		// Ensure is closed with OfficeFloor
		Connection connection = ConnectionWrapper.getRealConnection(ConnectionDecoratorSection.connection);
		assertTrue("Should close connection with closing OfficeFloor", (connection == null) || (connection.isClosed()));
	}

	public static class ConnectionDecoratorSection {

		private static Connection connection;

		private static int expectedConnectionCount;

		public void checkConnectionDecoration(Connection connection) throws SQLException {
			ConnectionDecoratorSection.connection = connection;

			// Ensure connection retrieved (may be pooled)
			connection.createStatement();

			// Ensure connection is registered
			assertEquals("Incorrect number of connections registered",
					ValidateConnections.getConnectionsRegisteredCount(), expectedConnectionCount);
		}
	}

	/**
	 * Ensure correct type for writable {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testWritableValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getConnectionManagedObjectSourceClass(), false);
	}

	/**
	 * Ensure correct type for writable {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testPooledWritableValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getConnectionManagedObjectSourceClass(), true);
	}

	/**
	 * Ensure correct type for read-only {@link Connection}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testReadOnlyValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false);
	}

	/**
	 * Ensure correct type for {@link DataSource}.
	 * 
	 * @throws Exception On test failure.
	 */
	public void testDataSourceValidateConnectivity() throws Exception {
		this.doValidateConnectivityTest(this.getDataSourceManagedObjectSourceClass(), false);
	}

	/**
	 * Ensure can validate the connectivity on start up.
	 * 
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param isPooled                 Indicates if should pool.
	 * @throws Throwable On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doValidateConnectivityTest(
			Class<MS> managedObjectSourceClass, boolean isPooled) throws Exception {

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
			mos.addProperty(AbstractConnectionManagedObjectSource.PROPERTY_DATA_SOURCE_VALIDATE_SQL,
					"INSERT INTO OFFICE_FLOOR_JDBC_TEST ( ID, NAME ) VALUES ( 1, 'test' )");
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide pooling
			if (isPooled) {
				architect.link(mos,
						architect.addManagedObjectPool("POOL", ThreadLocalJdbcConnectionPoolSource.class.getName()));
			}
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
		assertEquals("Should be no errors\n\n" + errors, 0, errors.length());

		// Ensure row in database
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT NAME FROM OFFICE_FLOOR_JDBC_TEST WHERE ID = ?")) {
			statement.setInt(1, 1);
			ResultSet resultSet = statement.executeQuery();
			assertTrue("Should have row in database", resultSet.next());
			assertEquals("Incorrect row in database", "test", resultSet.getString("NAME"));
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
	public void testInsertConnectionStressWithCompiler() throws Throwable {
		this.doInsertStressTest(false, this.getConnectionManagedObjectSourceClass(), true,
				InsertConnectionSection.class);
	}

	/**
	 * Ensure can stress test against the database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionStressWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertStressTest(false,
				this.getConnectionManagedObjectSourceClass(), true, InsertConnectionSection.class));
	}

	/**
	 * Ensure can stress test against the database with transactions with compiled
	 * wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionTransactionStressWithCompiler() throws Throwable {
		this.doInsertStressTest(true, this.getConnectionManagedObjectSourceClass(), true,
				InsertConnectionSection.class);
	}

	/**
	 * Ensure can stress test against the database with transactions.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertConnectionTransactionStressWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertStressTest(true,
				this.getConnectionManagedObjectSourceClass(), true, InsertConnectionSection.class));
	}

	/**
	 * Ensure can stress test against {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testInsertDataSourceStress() throws Throwable {
		this.doInsertStressTest(false, this.getDataSourceManagedObjectSourceClass(), false,
				InsertDataSourceSection.class);
	}

	/**
	 * Undertake insert stress test.
	 * 
	 * @param isTransaction            Whether test uses transactions.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param isPool                   Indicates whether to pool {@link Connection}
	 *                                 instances.
	 * @param sectionLogicClass        Section logic {@link Class}.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doInsertStressTest(
			boolean isTransaction, Class<MS> managedObjectSourceClass, boolean isPool, Class<?> sectionLogicClass)
			throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;
		InsertConnectionSection.isTransaction = isTransaction;

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
			context.addSection("SECTION", sectionLogicClass);

			// Connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					managedObjectSourceClass.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Load the properties
			if (sectionLogicClass == InsertConnectionSection.class) {
				// Configure connection
				this.loadConnectionProperties(mos);
			} else if (sectionLogicClass == InsertDataSourceSection.class) {
				// Configure data source
				this.loadDataSourceProperties(mos);
			} else {
				fail("Unknown logic section");
			}

			// Pool the connection
			if (isPool) {
				OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
						ThreadLocalJdbcConnectionPoolSource.class.getName());
				context.getOfficeArchitect().link(mos, pool);

			}
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
		System.out.println(this.getName());
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
			assertEquals("Should create 2 rows for each run", ((RUN_COUNT + WARM_UP) * 2),
					resultSet.getInt("ENTRY_COUNT"));
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

	public static class InsertConnectionSection {

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
				assertEquals("Should add the row", 1, statement.executeUpdate());
			}
		}
	}

	public static class InsertDataSourceSection {

		public void run(@Parameter AtomicInteger id, DataSource dataSource, Flows flows) throws SQLException {
			try (Connection connection = dataSource.getConnection()) {
				InsertConnectionSection.insertRow(connection, id, "run");
			}
			flows.thread(id);
		}

		public void thread(@Parameter AtomicInteger id, DataSource dataSource, NewThread thread) throws SQLException {
			try (Connection connection = dataSource.getConnection()) {
				InsertConnectionSection.insertRow(connection, id, Thread.currentThread().getName());
			}
		}
	}

	/**
	 * Undertakes select stress for writable {@link Connection} with compiled
	 * wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testWritableSelectStressWithCompiler() throws Throwable {
		this.doSelectStressTest(this.getConnectionManagedObjectSourceClass(), true, SelectConnectionSection.class);
	}

	/**
	 * Undertakes select stress for writable {@link Connection} with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testWritableSelectStressWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this
				.doSelectStressTest(this.getConnectionManagedObjectSourceClass(), true, SelectConnectionSection.class));
	}

	/**
	 * Undertakes select stress for read-only {@link Connection} with compiled
	 * wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testReadOnlySelectStressWithCompiler() throws Throwable {
		this.doSelectStressTest(this.getReadOnlyConnectionManagedObjectSourceClass(), false,
				SelectConnectionSection.class);
	}

	/**
	 * Undertakes select stress for read-only {@link Connection} with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testReadOnlySelectStressWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler
				.runWithoutCompiler(() -> this.doSelectStressTest(this.getReadOnlyConnectionManagedObjectSourceClass(),
						false, SelectConnectionSection.class));
	}

	/**
	 * Undertakes select stress for {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testDataSourceSelectStress() throws Throwable {
		this.doSelectStressTest(this.getDataSourceManagedObjectSourceClass(), false, SelectDataSourceSection.class);
	}

	/**
	 * Undertakes select stress.
	 * 
	 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
	 * @param isPool                   Indicates whether to pool {@link Connection}
	 *                                 instances.
	 * @param sectionLogicClass        Section logic {@link Class}.
	 * @throws Throwable On test failure.
	 */
	private <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> void doSelectStressTest(
			Class<?> managedObjectSourceClass, boolean isPool, Class<?> sectionLogicClass) throws Throwable {

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
			context.addSection("SECTION", sectionLogicClass);
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);

			// Connection
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					managedObjectSourceClass.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Load the properties
			if (sectionLogicClass == SelectConnectionSection.class) {
				this.loadConnectionProperties(mos);
			} else if (sectionLogicClass == SelectDataSourceSection.class) {
				this.loadDataSourceProperties(mos);
			} else {
				fail("Unknown logic section");
			}

			// Pool the connection
			if (isPool) {
				OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
						ThreadLocalJdbcConnectionPoolSource.class.getName());
				context.getOfficeArchitect().link(mos, pool);
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
		System.out.println(this.getName());
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
			AbstractSelectSection.isCompleted = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.run", null);
			assertTrue("Should be complete", AbstractSelectSection.isCompleted);
		}
		System.out.println();
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * AbstractSelectSection.THREAD_COUNT)
				/ (((float) runTime) / 1000.0));
		System.out.println(requestsPerSecond + " selects/sec");
		System.out.println();

		// Complete
		officeFloor.closeOfficeFloor();
	}

	public static class SelectParameter {

		private volatile String name = null;
	}

	public static class AbstractSelectSection {

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
					assertNull("Should be no failure in thread (" + exception + ")", exception);
					assertEquals("Should obtain name", "test", parameter.name);
					completed[0]++;
					if (completed[0] == THREAD_COUNT) {
						isCompleted = true;
					}
				});
			}
		}

		protected static void runSelect(Connection connection, @Parameter SelectParameter parameter, NewThread tag)
				throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM OFFICE_FLOOR_JDBC_TEST")) {
				ResultSet resultSet = statement.executeQuery();
				assertTrue("Should have row from database", resultSet.next());
				parameter.name = resultSet.getString("NAME");
			}
		}
	}

	public static class SelectConnectionSection extends AbstractSelectSection {

		public void thread(Connection connection, @Parameter SelectParameter parameter, NewThread tag)
				throws SQLException {
			runSelect(connection, parameter, tag);
		}
	}

	public static class SelectDataSourceSection extends AbstractSelectSection {

		public void thread(DataSource dataSource, @Parameter SelectParameter parameter, NewThread tag)
				throws SQLException {
			try (Connection connection = dataSource.getConnection()) {
				runSelect(connection, parameter, tag);
			}
		}
	}

}
