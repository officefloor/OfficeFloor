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
package net.officfloor.jdbc.validate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.jdbcx.JdbcDataSource;

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

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return ConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return ReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadOptionalSpecification(Properties properties) {
		this.loadProperties((name, value) -> properties.setProperty(name, value));
	}

	@Override
	protected void loadProperties(PropertyConfigurable mos) {
		mos.addProperty(DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
		mos.addProperty("uRL", "jdbc:h2:mem:test");
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

}