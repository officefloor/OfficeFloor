/*-
 * #%L
 * JPA Persistence
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

package net.officefloor.jpa.test;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.compile.state.autowire.AutoWireStateManagerFactory;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.jdbc.test.ValidateConnections;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.DependencyType;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.StressTest;

/**
 * Abstract functionality for the JPA testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJpaTestCase {

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
	 * Obtains the {@link Class} of {@link Exception} when no connection available.
	 * 
	 * @return {@link Class} of {@link Exception} when no connection available.
	 */
	protected abstract Class<?> getNoConnectionFactoryExceptionClass();

	/**
	 * Obtains the message of {@link Exception} when no connection available.
	 * 
	 * @return Message of {@link Exception} when no connection available.
	 */
	protected abstract String getNoConnectionFactoryExceptionMessage();

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
	 * Loads the properties for the {@link DataSourceManagedObjectSource}.
	 * 
	 * @param mos {@link PropertyConfigurable}.
	 */
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
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
	 * {@link StateManager}.
	 */
	protected AutoWireStateManager stateManager;

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@BeforeEach
	public void setUp() throws Exception {

		// Ensure clean state (no connections from previous test)
		ValidateConnections.assertNoPreviousTestConnections();
		assertEquals(0, ValidateConnections.getConnectionsRegisteredCount(),
				"Should be no connections before test setup");

		// Obtain connection
		// Must keep reference to keep potential in memory databases active
		this.connection = DatabaseTestUtil.waitForAvailableConnection((cleanups) -> {

			// Run OfficeFloor to obtain connection
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			Closure<AutoWireStateManagerFactory> factory = new Closure<>();
			compiler.getOfficeFloorCompiler()
					.addAutoWireStateManagerVisitor((officeName, stateFactory) -> factory.value = stateFactory);
			compiler.office((office) -> {

				// Connection
				OfficeManagedObjectSource mos = office.getOfficeArchitect().addOfficeManagedObjectSource("mo",
						DataSourceManagedObjectSource.class.getName());
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

			// Create table for testing
			try (Statement statement = connection.createStatement()) {
				statement.execute(
						"CREATE TABLE MOCKENTITY ( ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, NAME VARCHAR(20) NOT NULL, DESCRIPTION VARCHAR(256) )");
			}
		});

		// Capture the mock entity class implementation
		mockEntityClass = this.getMockEntityClass();
	}

	@AfterEach
	public void tearDown() throws Exception {
		try {
			if (this.stateManager != null) {
				this.stateManager.close();
			}
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		} finally {
			if (this.connection != null) {
				this.connection.close();
			}
		}

		// Ensure connections (and all closed)
		assertTrue(ValidateConnections.getConnectionsRegisteredCount() >= 1,
				"Should have at least one connection registered");
		ValidateConnections.assertAllConnectionsClosed();
	}

	/**
	 * Validate the specification
	 */
	@Test
	public void specification() {

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
	@Test
	public void type_Connection() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addDependency("Connection", Connection.class, null, 0, null);
		type.addFunctionDependency("CONNECTION", Connection.class, null);
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
	@Test
	public void type_DataSource() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addDependency("DataSource", DataSource.class, null, 0, null);
		type.addFunctionDependency("DATA_SOURCE", DataSource.class, null);
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
	@Test
	public void type_Managed() {

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
	@Test
	public void type_Default() {

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
	@Test
	public void managedConnectivity() throws Throwable {

		// Configure without data source
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {
			OfficeManagedObjectSource jpaMos = context.getOfficeArchitect().addOfficeManagedObjectSource("JPA",
					this.getJpaManagedObjectSourceClass().getName());
			this.loadJpaProperties(jpaMos);
			jpaMos.addOfficeManagedObject("JPA", ManagedObjectScope.THREAD);
		});
		OfficeFloor officeFloor = compile.compileOfficeFloor();
		try {
			// Attempt to open OfficeFloor
			officeFloor.openOfficeFloor();
			fail("Should not successfully open");

		} catch (Exception ex) {
			assertEquals(this.getNoConnectionFactoryExceptionClass(), ex.getClass(), "Incorrect exception class");
			assertTrue(ex.getMessage().startsWith(this.getNoConnectionFactoryExceptionMessage()),
					"Message: " + ex.getMessage());
		}
	}

	/**
	 * Ensure able to read entry from database with compiled wrappers.
	 * 
	 * @ throws Throwable On test failure.
	 */
	@Test
	public void connectionReadWithCompiler() throws Throwable {
		SourceContext context = OfficeFloorCompiler.newOfficeFloorCompiler(this.getClass().getClassLoader())
				.createRootSourceContext();
		assertNotNull(OfficeFloorJavaCompiler.newInstance(context), "Invalid test as java compiler is not available");
		this.doReadTest(true);
	}

	/**
	 * Ensure able to read entry from database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void connectionReadWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doReadTest(true));
	}

	/**
	 * Ensure able to read entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void dataSourceRead() throws Throwable {
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
		try (OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("READ", ReadSection.class);
		})) {

			// Invoke function to retrieve entity
			Result result = new Result();
			CompileOfficeFloor.invokeProcess(officeFloor, "READ.service", result);

			// Validate the result
			assertNotNull(result.entity, "Should have result");
			assertEquals("test", result.entity.getName(), "Incorrect name");
			assertEquals("mock read entry", result.entity.getDescription(), "Incorrect description");
		}
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
	@Test
	public void insertWithCompiler() throws Throwable {
		this.doInsertTest(true);
	}

	/**
	 * Ensure able to insert entry into database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void insertWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInsertTest(true));
	}

	/**
	 * Ensure able to insert entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void insertDataSource() throws Throwable {
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
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("INSERT", InsertSection.class);
		});

		// Invoke function to insert entity
		CompileOfficeFloor.invokeProcess(officeFloor, "INSERT.service", null);

		// Validate the inserted entity
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertTrue(resultSet.next(), "Should insert row");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect name");
			assertEquals("mock insert entry", resultSet.getString("DESCRIPTION"), "Incorrect description");
			assertFalse(resultSet.next(), "Should only be the one row");
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
	@Test
	public void updateWithCompiler() throws Throwable {
		this.doUpdateTest(true);
	}

	/**
	 * Ensure able to update entry into database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void updateWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doUpdateTest(true));
	}

	/**
	 * Ensure able to update entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void updateDataSource() throws Throwable {
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
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("UPDATE", UpdateSection.class);
		});

		// Invoke function to insert entity
		CompileOfficeFloor.invokeProcess(officeFloor, "UPDATE.service", null);

		// Validate the entity updated
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertTrue(resultSet.next(), "Should be update row");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect name");
			assertEquals("mock updated entry", resultSet.getString("DESCRIPTION"), "Incorrect changed description");
			assertFalse(resultSet.next(), "Should only be the one row");
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
	@Test
	public void deleteWithCompiler() throws Throwable {
		this.doDeleteTest(true);
	}

	/**
	 * Ensure able to delete entry from database with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void deleteWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doDeleteTest(true));
	}

	/**
	 * Ensure able to delete entry via {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void deleteDataSource() throws Throwable {
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
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("DELETE", DeleteSection.class);
		});

		// Invoke function to delete entity
		CompileOfficeFloor.invokeProcess(officeFloor, "DELETE.service", null);

		// Validate the entity updated
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			assertFalse(resultSet.next(), "Should delete row");
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
	 * Undertake forcing commit with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void forceCommitWithCompiler() throws Throwable {
		this.doForceCommitTest();
	}

	/**
	 * Undertake forcing commit with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@Test
	public void forceCommitWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doForceCommitTest());
	}

	/**
	 * Indicates if JPA is transactional.
	 * 
	 * @return <code>true</code> if JPA is transactional.
	 */
	protected boolean isTransactional() {
		return true;
	}

	/**
	 * Undertakes the force commit test.
	 * 
	 * @throws Throwable On test failure.
	 */
	private void doForceCommitTest() throws Throwable {

		// Open OfficeFloor
		this.compileAndOpenOfficeFloor(false, null);

		// Obtain the entity manager
		EntityManager entityManager = this.stateManager.getObject(null, EntityManager.class, 0);

		// Initiate transaction (if required)
		if (!this.isTransactional()) {
			JpaManagedObjectSource.beginTransaction(entityManager);
		}

		// Persist the data
		IMockEntity entity = mockEntityClass.getDeclaredConstructor().newInstance();
		entity.setName("test");
		entity.setDescription("mock insert entry");
		entityManager.persist(entity);

		// Commit the transaction
		JpaManagedObjectSource.commitTransaction(entityManager);

		// Ensure able to obtain from another connection (to ensure persisted)
		DataSource dataSource = this.stateManager.getObject(null, DataSource.class, 0);
		try (Connection connection = dataSource.getConnection()) {
			ResultSet resultSet = connection.createStatement().executeQuery("SELECT NAME FROM MOCKENTITY");
			assertTrue(resultSet.next(), "Should persist row");
			assertEquals("test", resultSet.getString("NAME"), "Incorrect row");
		}
	}

	/**
	 * Undertake stress insert test with compiled wrappers.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void stressInsertWithCompiler(TestInfo testInfo) throws Throwable {
		this.doStressInsertTest(true, testInfo);
	}

	/**
	 * Undertake stress insert test with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void stressInsertWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressInsertTest(true, testInfo));
	}

	/**
	 * Undertake stress insert test with {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void testStressInsertDataSource(TestInfo testInfo) throws Throwable {
		this.doStressInsertTest(false, testInfo);
	}

	/**
	 * Undertakes the stress insert test.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @param testInfo     {@link TestInfo}.
	 */
	private void doStressInsertTest(boolean isConnection, TestInfo testInfo) throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;
		final long TIMEOUT = 10000;

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("StressInsert", StressInsertSection.class);
		});

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(testInfo.getDisplayName());
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
		System.out.println(requestsPerSecond + " inserts/sec");
		System.out.println();

		// Ensure inserted all rows
		try (PreparedStatement statement = this.connection
				.prepareStatement("SELECT COUNT(*) AS ENTRY_COUNT FROM MOCKENTITY")) {
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			assertEquals(((RUN_COUNT + WARM_UP) * 2), resultSet.getInt("ENTRY_COUNT"),
					"Should create 2 rows for each run");
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
	public void stressSelectWithCompiler(TestInfo testInfo) throws Throwable {
		this.doStressSelectTest(true, testInfo);
	}

	/**
	 * Ensure stress select with {@link Proxy}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void stressSelectWithDynamicProxy(TestInfo testInfo) throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doStressSelectTest(true, testInfo));
	}

	/**
	 * Ensure stress select with {@link DataSource}.
	 * 
	 * @throws Throwable On test failure.
	 */
	@StressTest
	public void stressSelectDataSource(TestInfo testInfo) throws Throwable {
		this.doStressSelectTest(false, testInfo);
	}

	/**
	 * Undertakes the stress select test.
	 * 
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @param testInfo     {@link TestInfo}.
	 */
	private void doStressSelectTest(boolean isConnection, TestInfo testInfo) throws Throwable {

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
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor(isConnection, (context) -> {
			context.addSection("StressSelect", StressSelectSection.class);
		});

		// Undertake warm up
		System.out.println("==== " + this.getClass().getSimpleName() + " ====");
		System.out.println(testInfo.getDisplayName());
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
		assertTrue(StressSelectSection.isCompleted, "Should be complete");

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
				assertEquals("first", entity.getDescription(), "Should obtain entity one");

				// Obtain second row on another thread
				SelectParameter parameter = new SelectParameter(input);
				flows.thread(parameter, (exception) -> {
					assertNull(exception, "Should be no failure in thread");
					assertEquals("two", parameter.entity.getName(), "Should obtain entity");
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
	 * @param isConnection Indicates if {@link Connection} otherwise
	 *                     {@link DataSource}.
	 * @param extension    {@link CompileOfficeExtension} for test specific
	 *                     configuration.
	 * @return Open {@link OfficeFloor}.
	 * @throws Exception If fails to compile and open the {@link OfficeFloor}.
	 */
	private OfficeFloor compileAndOpenOfficeFloor(boolean isConnection, CompileOfficeExtension extension)
			throws Exception {

		// Configure the application
		CompileOfficeFloor compile = new CompileOfficeFloor();
		Closure<AutoWireStateManagerFactory> factory = new Closure<>();
		compile.getOfficeFloorCompiler()
				.addAutoWireStateManagerVisitor((officeName, stateFactory) -> factory.value = stateFactory);
		compile.officeFloor((context) -> {

			// Provide different thread
			context.getOfficeFloorDeployer().addTeam("TEAM", new ExecutorCachedTeamSource()).addTypeQualification(null,
					NewThread.class.getName());
		});
		compile.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addManagedObject("tag", NewThread.class, ManagedObjectScope.THREAD);

			// Create the data source
			OfficeManagedObjectSource dataSourceMos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("dataSource", DataSourceManagedObjectSource.class.getName());
			this.loadDataSourceProperties(dataSourceMos);
			dataSourceMos.addOfficeManagedObject("dataSource", ManagedObjectScope.THREAD);

			// Provide connection to database
			if (isConnection) {
				context.getOfficeArchitect()
						.addOfficeManagedObjectSource("connection", ConnectionManagedObjectSource.class.getName())
						.addOfficeManagedObject("connection", ManagedObjectScope.THREAD);
			}

			// Add JPA
			OfficeManagedObjectSource jpaMos = context.getOfficeArchitect().addOfficeManagedObjectSource("JPA",
					this.getJpaManagedObjectSourceClass().getName());
			this.loadJpaProperties(jpaMos);
			jpaMos.addProperty(JpaManagedObjectSource.PROPERTY_DEPENDENCY_TYPE,
					isConnection ? DependencyType.connection.name() : DependencyType.datasource.name());
			jpaMos.addOfficeManagedObject("JPA", ManagedObjectScope.THREAD);

			// Configure specific test functionality
			if (extension != null) {
				extension.extend(context);
			}
		});

		// Compile the OfficeFloor
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Provide state manager
		this.stateManager = factory.value.createAutoWireStateManager();

		// Return the OfficeFloor
		return this.officeFloor;
	}

	public static class NewThread {
	}

}
