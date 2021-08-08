/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.validate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.jdbcx.JdbcDataSource;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
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
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return ReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected Class<? extends DataSourceManagedObjectSource> getDataSourceManagedObjectSourceClass() {
		return DataSourceManagedObjectSource.class;
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
