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

package net.officefloor.jdbc.validate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.jdbcx.JdbcDataSource;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Validates the {@link AbstractJdbcTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateJdbcTest extends AbstractJdbcTestCase {

	private static final String JDBC_URL = "jdbc:h2:mem:test";

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return ConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return ReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadOptionalConnectionSpecification(Properties properties) {
		this.loadConnectionProperties((name, value) -> properties.setProperty(name, value));
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", JDBC_URL);
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, HikariDataSource.class.getName());
		mos.addProperty("jdbcUrl", JDBC_URL);
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

}
