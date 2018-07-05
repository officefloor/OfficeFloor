/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcDataSource;

import junit.framework.TestCase;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Abstract {@link Connection} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionTestCase extends OfficeFrameTestCase {

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
	 * Loads the properties for the {@link ConnectionManagedObjectSource}.
	 * 
	 * @param mos
	 *            {@link PropertyConfigurable}.
	 */
	protected void loadProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", "jdbc:h2:mem:test");
	}

	/**
	 * {@link Connection}.
	 */
	protected Connection connection;

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

	@Override
	protected void tearDown() throws Exception {
		if (this.connection != null) {
			this.connection.close();
		}
	}

}