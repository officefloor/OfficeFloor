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
package net.officefloor.jpa.test;

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

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.Dependencies;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Abstract functionality for the JPA testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractJpaTestCase extends OfficeFrameTestCase {

	/**
	 * Loads the properties for the jpama
	 * 
	 * @param jpa
	 */
	protected abstract void loadJpaProperties(PropertyConfigurable jpa);

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
	 * @param mos
	 *            {@link PropertyConfigurable}.
	 */
	protected void loadDatabaseProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", "jdbc:h2:mem:test");
	}

	/**
	 * Cleans the database.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 */
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

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

		// Obtain connection
		// Must keep reference to keep potential in memory databases active
		this.connection = AbstractJdbcTestCase.getConnection(ConnectionManagedObjectSource.class,
				(mos) -> this.loadDatabaseProperties(mos));

		// Clean database for testing
		this.cleanDatabase(this.connection);

		// Create table for testing
		try (Statement statement = this.connection.createStatement()) {
			statement.execute(
					"CREATE TABLE MOCKENTITY ( ID IDENTITY PRIMARY KEY, NAME VARCHAR(20) NOT NULL, DESCRIPTION VARCHAR(256) )");
		}
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
	}

	/**
	 * Validate the specification.
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
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(EntityManager.class);
		type.addDependency(Dependencies.CONNECTION, Connection.class, null);
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
	 * Ensure able to read entry from database.
	 */
	public void testRead() throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock read entry')");
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
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
		public MockEntity entity = null;
	}

	/**
	 * Mock section for reading entity.
	 */
	public static class ReadSection {
		public void service(@Parameter Result result, EntityManager entityManager) {
			TypedQuery<MockEntity> query = entityManager.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'",
					MockEntity.class);
			MockEntity entity = query.getSingleResult();
			result.entity = entity;
		}
	}

	/**
	 * Ensure able to insert entry into database.
	 */
	public void testInsert() throws Throwable {

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
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
		public void service(EntityManager entityManager) {
			MockEntity entity = new MockEntity();
			entity.setName("test");
			entity.setDescription("mock insert entry");
			entityManager.persist(entity);
		}
	}

	/**
	 * Ensure able to update entry into database.
	 */
	public void testUpdate() throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock initial entry')");
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
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
			MockEntity entity = entityManager
					.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'", MockEntity.class)
					.getSingleResult();
			entity.setDescription("mock updated entry");
		}
	}

	/**
	 * Ensure able to delete entry from database.
	 */
	public void testDelete() throws Throwable {

		// Add entry to database
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('test', 'mock entry')");
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
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
			MockEntity entity = entityManager
					.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'test'", MockEntity.class)
					.getSingleResult();
			entityManager.remove(entity);
		}
	}

	/**
	 * Undertake stress insert test.
	 */
	public void testStressInsert() throws Throwable {

		final int RUN_COUNT = 10000;
		final int WARM_UP = RUN_COUNT / 10;

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
			context.addSection("StressInsert", StressInsertSection.class);
		});

		// Undertake warm up
		for (int i = 0; i < WARM_UP; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "StressInsert.run", null);
		}

		// Run test
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "StressInsert.run", null);
		}
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * 2) / (((float) runTime) / 1000.0));
		System.out.println(this.getClass().getSimpleName() + " " + this.getName() + ": performance "
				+ +requestsPerSecond + " inserts/sec");

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

		public void run(EntityManager entityManager, Flows flows) throws SQLException {
			this.insertRow(entityManager, "run");
			flows.thread();
		}

		public void thread(EntityManager entityManager, NewThread thread) throws SQLException {
			this.insertRow(entityManager, Thread.currentThread().getName());
		}

		private void insertRow(EntityManager entityManager, String name) throws SQLException {
			MockEntity entity = new MockEntity();
			entity.setName(name);
			entity.setDescription(name);
			entityManager.persist(entity);
		}
	}

	/**
	 * Ensure stress select.
	 */
	public void testStressSelect() throws Throwable {

		final int RUN_COUNT = 1000;
		final int WARM_UP = RUN_COUNT / 10;

		// Create the two rows
		int rowTwoId;
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('one', 'first')");
			statement.execute("INSERT INTO MOCKENTITY (NAME, DESCRIPTION) VALUES ('two', 'second')");
			ResultSet resultTwo = statement.executeQuery("SELECT ID FROM MOCKENTITY WHERE NAME = 'two'");
			resultTwo.next();
			rowTwoId = resultTwo.getInt("ID");
		}

		// Configure the application
		OfficeFloor officeFloor = this.compileAndOpenOfficeFloor((context) -> {
			context.addSection("StressSelect", StressSelectSection.class);
		});

		// Undertake warm up
		for (int i = 0; i < WARM_UP; i++) {
			CompileOfficeFloor.invokeProcess(officeFloor, "StressSelect.run", new SelectInput(rowTwoId));
		}

		// Run test
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < RUN_COUNT; i++) {
			StressSelectSection.isCompleted = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "StressSelect.run", new SelectInput(rowTwoId));
		}
		long runTime = System.currentTimeMillis() - startTime;
		long requestsPerSecond = (int) ((RUN_COUNT * StressSelectSection.THREAD_COUNT * 2)
				/ (((float) runTime) / 1000.0));
		System.out.println(this.getClass().getSimpleName() + " " + this.getName() + ": performance "
				+ +requestsPerSecond + " selects/sec");
		assertTrue("Should be complete", StressSelectSection.isCompleted);

		// Complete
		officeFloor.closeOfficeFloor();

	}

	private static class SelectInput {

		private final int rowTwoId;

		private SelectInput(int rowTwoId) {
			this.rowTwoId = rowTwoId;
		}
	}

	private static class SelectParameter {

		private final SelectInput input;

		private volatile MockEntity entity;

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
				MockEntity entity = entityManager
						.createQuery("SELECT M FROM MockEntity M WHERE M.name = 'one'", MockEntity.class)
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
			parameter.entity = entityManager.find(MockEntity.class, parameter.input.rowTwoId);
		}
	}

	/**
	 * Compiles and opens the {@link OfficeFloor} for testing.
	 * 
	 * @param extension
	 *            {@link CompileOfficeExtension} for test specific configuration.
	 * @return Open {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile and open the {@link OfficeFloor}.
	 */
	private OfficeFloor compileAndOpenOfficeFloor(CompileOfficeExtension extension) throws Exception {

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

			// Create the connection
			OfficeManagedObjectSource connectionMos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("connection", ConnectionManagedObjectSource.class.getName());
			this.loadDatabaseProperties(connectionMos);
			connectionMos.addOfficeManagedObject("connection", ManagedObjectScope.THREAD);

			// Add JPA
			OfficeManagedObjectSource jpaMos = context.getOfficeArchitect().addOfficeManagedObjectSource("JPA",
					this.getJpaManagedObjectSourceClass().getName());
			this.loadJpaProperties(jpaMos);
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