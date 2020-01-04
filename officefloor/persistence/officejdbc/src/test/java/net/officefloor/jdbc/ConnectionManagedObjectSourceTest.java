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

package net.officefloor.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Tests the {@link ConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagedObjectSourceTest extends AbstractConnectionTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Reset mock section
		MockSection.connection = null;
	}

	/**
	 * Ensures the database is appropriately setup.
	 */
	public void testEnsureDatabaseSetup() throws Exception {

		// Insert row into table
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 1, 'test' )");
		}

		// Ensure can obtain row
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 1");
			assertTrue("Should have row", resultSet.next());
			assertEquals("Incorrect row", "test", resultSet.getString("NAME"));
		}
	}

	/**
	 * Validate the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(ConnectionManagedObjectSource.class);
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(Connection.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, ConnectionManagedObjectSource.class,
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
	}

	/**
	 * Ensures {@link Connection} with compiler.
	 */
	public void testConnectionWithCompiler() throws Throwable {
		this.doConnectionTest();
	}

	/**
	 * Ensures {@link Connection} with {@link Proxy}.
	 */
	public void testConnectionWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doConnectionTest());
	}

	/**
	 * Ensures {@link Connection}.
	 */
	public void doConnectionTest() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					ConnectionManagedObjectSource.class.getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", null);

		// Ensure row inserted
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
			assertTrue("Should have row", resultSet.next());
			assertEquals("Incorrect row", "OfficeFloor", resultSet.getString("NAME"));
		}

		// Ensure the connection is closed
		assertNotNull("Should have connection", MockSection.connection);
		assertTrue("Connection used should be closed", MockSection.connection.isClosed());
	}

	public static class MockSection {

		private static Connection connection = null;

		public void section(Connection connection) throws SQLException {
			try (Statement statement = connection.createStatement()) {
				statement.execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 2, 'OfficeFloor' )");
			}
			MockSection.connection = connection;
		}
	}

	/**
	 * Ensures able to configure {@link DataSourceFactory}.
	 */
	public void testDataSourceFactory() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					ConnectionManagedObjectSource.class.getName());
			mos.addProperty(ConnectionManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
					MockDataSourceFactory.class.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", null);

		// Ensure row inserted
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
			assertTrue("Should have row", resultSet.next());
			assertEquals("Incorrect row", "OfficeFloor", resultSet.getString("NAME"));
		}

		// Ensure the connection is closed
		assertNotNull("Should have connection", MockSection.connection);
		assertTrue("Connection used should be closed", MockSection.connection.isClosed());
	}

	/**
	 * Mock {@link DataSourceFactory}.
	 */
	public static class MockDataSourceFactory implements DataSourceFactory {

		@Override
		public DataSource createDataSource(SourceContext context) throws Exception {
			JdbcDataSource dataSource = new JdbcDataSource();
			dataSource.setURL("jdbc:h2:mem:test");
			return dataSource;
		}
	}

	/**
	 * Ensure appropriate management of the {@link DataSource}.
	 */
	public void testDataSourceManagementWithCompiler() throws Throwable {
		this.doDataSourceManagementTest(ConnectionManagedObjectSource.class, 0);
	}

	/**
	 * Ensure appropriate management of the {@link DataSource}.
	 */
	public void testDataSourceManagementWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler
				.runWithoutCompiler(() -> this.doDataSourceManagementTest(ConnectionManagedObjectSource.class, 0));
	}

}
