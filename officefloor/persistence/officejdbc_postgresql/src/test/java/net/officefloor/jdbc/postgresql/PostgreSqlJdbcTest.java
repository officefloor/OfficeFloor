/*-
 * #%L
 * PostgreSQL Persistence
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

package net.officefloor.jdbc.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlExtension;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link PostgreSqlDataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
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
	 * {@link PostgreSqlExtension} to run PostgreSql.
	 */
	@RegisterExtension
	public static PostgreSqlExtension server = new PostgreSqlExtension(
			new Configuration().port(PORT).username(USERNAME).password(PASSWORD));

	@Override
	protected Class<? extends DataSourceManagedObjectSource> getDataSourceManagedObjectSourceClass() {
		return PostgreSqlDataSourceManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return PostgreSqlReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		this.loadDataSourceProperties(mos);
	}

	@Override
	protected void loadOptionalConnectionSpecification(Properties properties) {
		properties.setProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PORT, "5433");
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PORT, String.valueOf(PORT));
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_USER, USERNAME);
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PASSWORD, PASSWORD);
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate("DROP TABLE IF EXISTS OFFICE_FLOOR_JDBC_TEST");
		}
		if (!connection.getAutoCommit()) {
			connection.commit();
		}
	}

}
