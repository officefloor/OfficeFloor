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
package net.officefloor.jdbc.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.postgresql.ds.PGSimpleDataSource;

import com.zaxxer.hikari.HikariDataSource;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule.Configuration;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Tests the {@link PostgreSqlConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlJdbcTest extends AbstractJdbcTestCase {

	/**
	 * Port to run PostgreSql.
	 */
	private static final int PORT = 5433;

	/**
	 * Username to connect to PostgreSql.
	 */
	private static final String USERNAME = "testuser";

	/**
	 * Password to connect to PostgreSql.
	 */
	private static final String PASSWORD = "testpassword";

	/**
	 * {@link PostgreSqlRule} to run PostgreSql.
	 */
	private static PostgreSqlRule server = new PostgreSqlRule(
			new Configuration().port(PORT).username(USERNAME).password(PASSWORD));

	/**
	 * Manage PostgreSql before/after class (rather than each test) to improve
	 * performance.
	 */
	public static Test suite() {
		return new TestSetup(new TestSuite(PostgreSqlJdbcTest.class)) {

			protected void setUp() throws Exception {
				server.startPostgreSql();
			}

			protected void tearDown() throws Exception {
				server.stopPostgreSql();
			}
		};
	}

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return PostgreSqlConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return PostgreSqlReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, String.valueOf(PORT));
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_USER, USERNAME);
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PASSWORD, PASSWORD);
	}

	@Override
	protected void loadOptionalConnectionSpecification(Properties properties) {
		properties.setProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, "5433");
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		new PGSimpleDataSource();
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, HikariDataSource.class.getName());
		mos.addProperty("jdbcUrl", "jdbc:postgresql://localhost:" + String.valueOf(PORT) + "/");
		mos.addProperty("username", USERNAME);
		mos.addProperty("password", PASSWORD);
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeQuery("SELECT * FROM information_schema.tables");
			statement.executeUpdate("DROP TABLE IF EXISTS OFFICE_FLOOR_JDBC_TEST");
		}
		if (!connection.getAutoCommit()) {
			connection.commit();
		}
	}

}