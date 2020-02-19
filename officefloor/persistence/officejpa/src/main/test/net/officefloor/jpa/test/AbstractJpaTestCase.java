/*-
 * #%L
 * JPA Persistence
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

package net.officefloor.jpa.test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.pool.ThreadLocalJdbcConnectionPoolSource;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;
import net.officefloor.jdbc.test.DataSourceRule;
import net.officefloor.jdbc.test.ValidateConnections;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.DependencyType;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Abstract functionality for the JPA testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJpaTestCase extends OfficeFrameTestCase {

	/**
	 * Loads the properties for the {@link JpaManagedObjectSource}.
	 * 
	 * @param jpa {@link PropertyConfigurable} to receive the {@link Property}
	 *            values.
	 */
	protected abstract void loadJpaProperties(PropertyConfigurable jpa);

	/**
	 * Obtains the {@link IMockEntity} implementation {@link Class}.
	 * 
	 * @return {@link IMockEntity} implementation {@link Class}.
	 */
	protected abstract Class<? extends IMockEntity> getMockEntityClass();

	/**
	 * Allows overriding the {@link JpaManagedObjectSource} {@link Class} for vendor
	 * specific implementations.
	 * 
	 * @return {@link JpaManagedObjectSource} {@link Class} to use in testing.
	 */
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return JpaManagedObjectSource.class;
	}

	/**
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected void loadDatabaseProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("url", "jdbc:h2:mem:test");
	}

	/**
	 * Cleans the database.
	 * 
	 * @param connection {@link Connection}.
	 * @throws SQLException If fails to clean the database.
	 */
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

	/**
	 * {@link IMockEntity} implementation {@link Class}.
	 */
	protected static Class<? extends IMockEntity> mockEntityClass;

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {

		// Ensure clean state (no connections from previous test)
		ValidateConnections.assertNoPreviousTestConnections();
		assertEquals("Should be no connections before test setup", 0,
				ValidateConnections.getConnectionsRegisteredCount());

		// Obtain connection
		// Must keep reference to keep potential in memory databases active
		this.connection = DataSourceRule.waitForDatabaseAvailable((context) -> {

			// Obtain the connection
			Connection conn = context.setConnection(AbstractJdbcTestCase
					.getConnection(ConnectionManagedObjectSource.class, (mos) -> this.loadDatabaseProperties(mos)));

			// Clean database for testing
			this.cleanDatabase(conn);
		});

		// Create table for testing
		try (Statement statement = this.connection.createStatement()) {
			statement.execute(
					"CREATE TABLE MOCKENTITY ( ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, NAME VARCHAR(20) NOT NULL, DESCRIPTION VARCHAR(256) )");
		}

		// Capture the mock entity class implementation
		mockEntityClass = this.getMockEntityClass();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		} finally {
			if (this.connection != null) {
				this.connection.close();
			}
		}

		// Ensure connections (and all closed)
		assertTrue("Should have at least one connection registered",
				ValidateConnections.getConnectionsRegisteredCount() >= 1);
		ValidateConnections.assertAllConnectionsClosed();
	}

	/**
	 * Validate the specification
	 */
	public void testSpecification() {

		// Determine if default JPA implementation
		Class<? extends JpaManagedObjectSource> jpaMosClass = this.getJpaManagedObjectSourceClass();

		// Create the specification
		List<String> specification = new ArrayList<>(2);
		specification.add(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT);
		specification.add("Persistence Unit");
		if (jpaMosClass == JpaManagedObjectSource.class) {
			specification.add(JpaManagedObjectSource.PROPERTY_PERSISTENCE_FACTORY);
			specification.add("Persistence Factory");
		}

		// Validate the specification
		ManagedObjectLoaderUtil.validateSpecification(jpaMosClass,
				specification.toArray(new String[specification.size()]));
	}

	/**
	 * Validate the type.
	 */
	public void testType_Connection() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addDependency("Connection", Connection.class, null, 0, null);
		type.addExtensionInterface(EntityManager.class);

		// Load the properties
		List<String> properties = new LinkedList<>();
		this.loadJpaProperties((name, value) -> {
			properties.add(name);
			properties.add(value);
		});

		// Indicate connection
		properties.add(JpaManagedObjectSource.PROPERTY_DEPENDENCY_TYPE);
		properties.add(DependencyType.connection.name());

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getJpaManagedObjectSourceClass(),
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Validate the type.
	 */
	public void testType_DataSource() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addDependency("DataSource", DataSource.class, null, 0, null);
		type.addExtensionInterface(EntityManager.class);

		// Load the properties
		List<String> properties = new LinkedList<>();
		this.loadJpaProperties((name, value) -> {
			properties.add(name);
			properties.add(value);
		});

		// Indicate connection
		properties.add(JpaManagedObjectSource.PROPERTY_DEPENDENCY_TYPE);
		properties.add(DependencyType.datasource.name());

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getJpaManagedObjectSourceClass(),
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Validate the type.
	 */
	public void testType_Managed() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addExtensionInterface(EntityManager.class);

		// Load the properties
		List<String> properties = new LinkedList<>();
		this.loadJpaProperties((name, value) -> {
			properties.add(name);
			properties.add(value);
		});

		// Indicate connection
		properties.add(JpaManagedObjectSource.PROPERTY_DEPENDENCY_TYPE);
		properties.add(DependencyType.managed.name());

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getJpaManagedObjectSourceClass(),
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Validate the type.
	 */
	public void testType_Default() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addExtensionInterface(EntityManager.class);

		// Load the properties
		List<String> properties = new LinkedList<>();
		this.loadJpaProperties((name, value) -> {
			properties.add(name);
			properties.add(value);
		});

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, this.getJpaManagedObjectSourceClass(),
				properties.toArray(new String[properties.size()]));
	}

	/**
	 * Ensure if {@link EntityManager} is managing the {@link DataSource} that it is
	 * validated on start up.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testManagedConnectivity() throws Throwable {

		// Configure without data source
		Closure<Throwable> closure = new Closure<>();
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeManagedObjectSource jpaMos = context.getOfficeArchitect().addOfficeManagedObjectSource("JPA",
					this.getJpaManagedObjectSourceClass().getName());
			this.loadJpaProperties(jpaMos);
		});
		compile.getOfficeFloorCompiler().setEscalationHandler((escalation) -> {
			closure.value = escalation;
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();
		assertNotNull("No connectivity should not prevent running (data source may be temporarily unavailable)",
				officeFloor);

		// Ensure start up failure
		assertNotNull("Should have start up failure", closure.value);
		assertEquals("Incorrect start up failure", "Failing to connect EntityManager", closure.value.getMessage());
		assertNotNull("Should indicate cause", closure.value.getCause());

		// Close
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Ensure able to read entry from database with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testConnectionReadWithCompiler() throws Throwable {
		SourceContext context = OfficeFloorCompiler.newOfficeFloorCompiler(this.getClass().getClassLoader())
				.createRootSourceContext();
		assertNotNull("Invalid test as java compiler is not available", OfficeFloorJavaCompiler.newInstance(context));
		this.doReadTest(true);
	}

	/**
	 * Ensure able to read entry from database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testConnectionReadWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doReadTest(true));
	}

	/**
	 * Ensure able to read entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testDataSourceRead() throws Throwable {
		this.doReadTest(false);
	}

	/**
	 * Ensure able to read entry from database.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @throws Throwable On test failure.
	 */
	public void doReadTest(boolean isConnection) throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock read entry')");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, false, (context) -> {
			context.addSection("READ", ReadSection.class);
		});

		// Invoke function to retrieve entity
		Result result = new Result();
		CompileOfficeFloor.invokeProcess(officeFloor, "READ.service", result);

		// Validate the result
		assertNotNull("Should have result", result.entity);
		assertEquals("Incorrect name", "test", result.entity.getName());
		assertEquals("Incorrect description", "mock read entry", result.entity.getDescription());
	}

	/**
	 * Holder for the result.
	 */
	public static class Result {
		public IMockEntity entity = null;
	}

	/**
	 * Mock section for reading entity.
	 */
	public static class ReadSection {
		public void service(@Parameter Result result, EntityManager entityManager) {
			TypedQuery<? extends IMockEntity> query = entityManager
					.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'", mockEntityClass);
			IMockEntity entity = query.getSingleResult();
			result.entity = entity;
		}
	}

	/**
	 * Ensure able to insert entry into database with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testInsertWithCompiler() throws Throwable {
		this.doInsertTest(true);
	}

	/**
	 * Ensure able to insert entry into database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testInsertWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertTest(true));
	}

	/**
	 * Ensure able to insert entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testInsertDataSource() throws Throwable {
		this.doInsertTest(false);
	}

	/**
	 * Ensure able to insert entry into database.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @throws Throwable On test failure.
	 */
	public void doInsertTest(boolean isConnection) throws Throwable {

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, false, (context) -> {
			context.addSection("INSERT", InsertSection.class);
		});

		// Invoke function to insert entity
		CompileOfficeFloor.invokeProcess(officeFloor, "INSERT.service", null);

		// Validate the inserted entity
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertTrue("Should insert row", resultSet.next());
			assertEquals("Incorrect name", "test", resultSet.getString("NAME"));
			assertEquals("Incorrect description", "mock insert entry", resultSet.getString("DESCRIPTION"));
			assertFalse("Should only be the one row", resultSet.next());
		}
	}

	/**
	 * Mock section for inserting entity.
	 */
	public static class InsertSection {
		public void service(EntityManager entityManager) throws Exception {
			IMockEntity entity = mockEntityClass.getDeclaredConstructor().newInstance();
			entity.setName("test");
			entity.setDescription("mock insert entry");
			entityManager.persist(entity);
		}
	}

	/**
	 * Ensure able to update entry into database with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testUpdateWithCompiler() throws Throwable {
		this.doUpdateTest(true);
	}

	/**
	 * Ensure able to update entry into database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testUpdateWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doUpdateTest(true));
	}

	/**
	 * Ensure able to update entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testUpdateDataSource() throws Throwable {
		this.doUpdateTest(false);
	}

	/**
	 * Ensure able to update entry into database.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @throws Throwable On test failure.
	 */
	public void doUpdateTest(boolean isConnection) throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock initial entry')");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, false, (context) -> {
			context.addSection("UPDATE", UpdateSection.class);
		});

		// Invoke function to insert entity
		CompileOfficeFloor.invokeProcess(officeFloor, "UPDATE.service", null);

		// Validate the entity updated
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertTrue("Should be update row", resultSet.next());
			assertEquals("Incorrect name", "test", resultSet.getString("NAME"));
			assertEquals("Incorrect changed description", "mock updated entry", resultSet.getString("DESCRIPTION"));
			assertFalse("Should only be the one row", resultSet.next());
		}
	}

	/**
	 * Mock section for updating entity.
	 */
	public static class UpdateSection {
		public void service(EntityManager entityManager) {
			IMockEntity entity = entityManager
					.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'", mockEntityClass).getSingleResult();
			entity.setDescription("mock updated entry");
		}
	}

	/**
	 * Ensure able to delete entry from database with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testDeleteWithCompiler() throws Throwable {
		this.doDeleteTest(true);
	}

	/**
	 * Ensure able to delete entry from database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testDeleteWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doDeleteTest(true));
	}

	/**
	 * Ensure able to delete entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	public void testDeleteDataSource() throws Throwable {
		this.doDeleteTest(false);
	}

	/**
	 * Ensure able to delete entry from database.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @throws Throwable On test failure.
	 */
	public void doDeleteTest(boolean isConnection) throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock entry')");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, false, (context) -> {
			context.addSection("DELETE", DeleteSection.class);
		});

		// Invoke function to delete entity
		CompileOfficeFloor.invokeProcess(officeFloor, "DELETE.service", null);

		// Validate the entity updated
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertFalse("Should delete row", resultSet.next());
		}
	}

	/**
	 * Mock section for deleting entity.
	 */
	public static class DeleteSection {
		public void service(EntityManager entityManager) {
			IMockEntity entity = entityManager
					.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'", mockEntityClass).getSingleResult();
			entityManager.remove(entity);
		}
	}

	/**
	 * Undertake stress insert test with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertWithCompiler() throws Throwable {
		this.doStressInsertTest(true, false);
	}

	/**
	 * Undertake stress insert test with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressInsertTest(true, false));
	}

	/**
	 * Undertake stress insert test with pooled connections with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertPooledConnectionsWithCompiler() throws Throwable {
		this.doStressInsertTest(true, true);
	}

	/**
	 * Undertake stress insert test with pooled connections with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertPooledConnectionsWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressInsertTest(true, true));
	}

	/**
	 * Undertake stress insert test with {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertDataSource() throws Throwable {
		this.doStressInsertTest(false, false);
	}

	/**
	 * Undertakes the stress insert test.
	 * 
	 * @param isConnection      Indicates if {@link Connection} otherwise
	 *                          {@link DataSource}.
	 * @param isPoolConnections Indicates whether to pool connections.
	 */
	private void doStressInsertTest(boolean isConnection, boolean isPoolConnections) throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;
		final long TIMEOUT = 10000;

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, isPoolConnections, (context) -> {
			context.addSection("StressInsert", StressInsertSection.class);
		});

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(this.getName());
		int warmupProgress = WARM_UP / 10;
		for (int i = 0; i < WARM_UP; i++) {
			if ((i % warmupProgress) == 0) {
				System.out.print("w");
				System.out.flush();
			}
			CompileOfficeFloor.invokeProcess(officeFloor, "StressInsert.run", null, TIMEOUT);
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
			CompileOfficeFloor.invokeProcess(officeFloor, "StressInsert.run", null, TIMEOUT);
		}
		System.out.println();
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * 2) / (((float) runTime) / 1000.0));
		System.out.println(requestsPerSecond + " inserts/sec " + (isPoolConnections ? " (pooled)" : ""));
		System.out.println();

		// Ensure inserted all rows
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT COUNT(*) AS ENTRY_COUNT FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			assertEquals("Should create 2 rows for each run", ((RUN_COUNT + WARM_UP) * 2),
					resultSet.getInt("ENTRY_COUNT"));
		}

		// Complete
		officeFloor.closeOfficeFloor();
	}

	public static class StressInsertSection {

		@FlowInterface
		public static interface Flows {
			void thread();
		}

		public void run(EntityManager entityManager, Flows flows) throws Exception {
			this.insertRow(entityManager, "run");
			flows.thread();
		}

		public void thread(EntityManager entityManager, NewThread thread) throws Exception {
			this.insertRow(entityManager, Thread.currentThread().getName());
		}

		private void insertRow(EntityManager entityManager, String name) throws Exception {
			IMockEntity entity = mockEntityClass.getDeclaredConstructor().newInstance();
			entity.setName(name);
			entity.setDescription(name);
			entityManager.persist(entity);
		}
	}

	/**
	 * Ensure stress select with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressSelectWithCompiler() throws Throwable {
		this.doStressSelectTest(true, false);
	}

	/**
	 * Ensure stress select with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressSelectWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressSelectTest(true, false));
	}

	/**
	 * Ensure stress select with pooled connections with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressSelectPooledConnectionsWithCompiler() throws Throwable {
		this.doStressSelectTest(true, true);
	}

	/**
	 * Ensure stress select with pooled connections with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressSelectPooledConnectionsWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressSelectTest(true, true));
	}

	/**
	 * Ensure stress select with {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressSelectDataSource() throws Throwable {
		this.doStressSelectTest(false, false);
	}

	/**
	 * Undertakes the stress select test.
	 * 
	 * @param isConnection        Indicates if {@link Connection} otherwise
	 *                            {@link DataSource}.
	 * @param isPooledConnections Indicates if pool connections.
	 */
	private void doStressSelectTest(boolean isConnection, boolean isPooledConnections) throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;

		// Create the two rows
		int rowTwoId;
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('one', 'first')");
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('two', 'second')");
			ResultSet resultTwo = statement.executeQuery("SELECT ID FROM MOCKENTITY WHERE NAME = 'two'");
			resultTwo.next();
			rowTwoId = resultTwo.getInt("ID");
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, isPooledConnections, (context) -> {
			context.addSection("StressSelect", StressSelectSection.class);
		});

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(this.getName());
		int warmupProgress = WARM_UP / 10;
		for (int i = 0; i < WARM_UP; i++) {
			if ((i % warmupProgress) == 0) {
				System.out.print("w");
				System.out.flush();
			}
			CompileOfficeFloor.invokeProcess(officeFloor, "StressSelect.run", new SelectInput(rowTwoId));
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
			StressSelectSection.isCompleted = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "StressSelect.run", new SelectInput(rowTwoId));
		}
		System.out.println();
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * StressSelectSection.THREAD_COUNT * 2)
				/ (((float) runTime) / 1000.0));
		System.out.println(requestsPerSecond + " selects/sec");
		System.out.println();
		assertTrue("Should be complete", StressSelectSection.isCompleted);

		// Complete
		officeFloor.closeOfficeFloor();

	}

	public static class SelectInput {

		private final long rowTwoId;

		private SelectInput(int rowTwoId) {
			this.rowTwoId = rowTwoId;
		}
	}

	public static class SelectParameter {

		private final SelectInput input;

		private volatile IMockEntity entity;

		private SelectParameter(SelectInput input) {
			this.input = input;
		}
	}

	public static class StressSelectSection {

		private static int THREAD_COUNT = 10;

		private static boolean isCompleted = false;

		@FlowInterface
		public static interface Flows {
			void thread(SelectParameter parameter, FlowCallback callback);
		}

		public void run(@Parameter SelectInput input, Flows flows, EntityManager entityManager) throws SQLException {
			int[] completed = new int[] { 0 };
			for (int i = 0; i < THREAD_COUNT; i++) {

				// Obtain first row
				IMockEntity entity = entityManager
						.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'one'", mockEntityClass)
						.getSingleResult();
				assertEquals("Should obtain entity one", "first", entity.getDescription());

				// Obtain second row on another thread
				SelectParameter parameter = new SelectParameter(input);
				flows.thread(parameter, (exception) -> {
					assertNull("Should be no failure in thread", exception);
					assertEquals("Should obtain entity", "two", parameter.entity.getName());
					completed[0]++;
					if (completed[0] == THREAD_COUNT) {
						isCompleted = true;
					}
				});
			}
		}

		public void thread(EntityManager entityManager, @Parameter SelectParameter parameter, NewThread tag)
				throws SQLException {
			parameter.entity = entityManager.find(mockEntityClass, parameter.input.rowTwoId);
		}
	}

	/**
	 * Compiles and opens the {@link OfficeFloor} for testing.
	 * 
	 * @param isConnection      Indicates if {@link Connection} otherwise
	 *                          {@link DataSource}.
	 * @param isPoolConnections Indicates whether to pool connections.
	 * @param extension         {@link CompileOfficeExtension} for test specific
	 *                          configuration.
	 * @return Open {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
	 */
	private OfficeFloor compileAndOpenOfficeFloor(boolean isConnection, boolean isPoolConnections,
			CompileOfficeExtension extension) throws Exception {

		// Configure the application
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {

			// Provide different thread
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					NewThread.class.getName());
		});
		compile.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);

			// Provide connection to database
			if (isConnection) {
				// Create the connection
				OfficeManagedObjectSource connectionMos = context.getOfficeArchitect()
						.addOfficeManagedObjectSource("connection", ConnectionManagedObjectSource.class.getName());
				this.loadDatabaseProperties(connectionMos);
				connectionMos.addOfficeManagedObject("connection", ManagedObjectScope.THREAD);

				// Determine if pool connections
				if (isPoolConnections) {
					OfficeManagedObjectPool pool = context.getOfficeArchitect().addManagedObjectPool("POOL",
							ThreadLocalJdbcConnectionPoolSource.class.getName());
					context.getOfficeArchitect().link(connectionMos, pool);
				}

			} else {
				// Create the data source
				OfficeManagedObjectSource dataSourceMos = context.getOfficeArchitect()
						.addOfficeManagedObjectSource("dataSource", DataSourceManagedObjectSource.class.getName());
				this.loadDatabaseProperties(dataSourceMos);
				dataSourceMos.addOfficeManagedObject("dataSource", ManagedObjectScope.THREAD);
			}

			// Add JPA
			OfficeManagedObjectSource jpaMos = context.getOfficeArchitect().addOfficeManagedObjectSource("JPA",
					this.getJpaManagedObjectSourceClass().getName());
			this.loadJpaProperties(jpaMos);
			jpaMos.addProperty(JpaManagedObjectSource.PROPERTY_DEPENDENCY_TYPE,
					isConnection ? DependencyType.connection.name() : DependencyType.datasource.name());
			jpaMos.addOfficeManagedObject("JPA", ManagedObjectScope.THREAD);

			// Configure specific test functionality
			extension.extend(context);
		});

		// Compile the OfficeFloor
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Return the OfficeFloor
		return this.officeFloor;
	}

	public static class NewThread {
	}

}
