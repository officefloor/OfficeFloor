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
package net.officefloor.jdbc.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.datasource.DataSourceManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Tests the {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:mem:test");
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
	 * Loads the properties for the {@link DataSourceManagedObjectSource}.
	 * 
	 * @param mos
	 *            {@link OfficeManagedObjectSource}.
	 */
	protected void loadProperties(OfficeManagedObjectSource mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", "jdbc:h2:mem:test");
	}

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	@Override
	protected void setUp() throws Exception {

		// Create the connection
		this.connection = this.getConnection();

		// Clean database for testing
		this.cleanDatabase(this.connection);

		// Create table for testing
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("CREATE TABLE TEST ( ID INT, NAME VARCHAR(255) )");
		}
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
	 * Ensures {@link Connection}.
	 */
	public void testConnection() throws Exception {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					DataSourceManagedObjectSource.class.getName());
			this.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

		// Undertake operation
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.section");
		function.invokeProcess(null, null);

		// Ensure row inserted
		try (Statement statement = this.connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
			assertTrue("Should have row", resultSet.next());
			assertEquals("Incorrect row", "OfficeFloor", resultSet.getString("NAME"));
		}
	}

	public static class MockSection {
		public void section(DataSource dataSource) throws SQLException {
			try (Connection connection = dataSource.getConnection()) {
				connection.createStatement().execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 2, 'OfficeFloor' )");
			}
		}
	}

}