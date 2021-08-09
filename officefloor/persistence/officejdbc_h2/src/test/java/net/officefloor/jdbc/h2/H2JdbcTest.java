/*-
 * #%L
 * H2 Persistence
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

package net.officefloor.jdbc.h2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.DataSourceManagedObjectSource;
import net.officefloor.jdbc.ReadOnlyConnectionManagedObjectSource;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Tests the {@link H2DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2JdbcTest extends AbstractJdbcTestCase {

	private static final String JDBC_URL = "jdbc:h2:mem:test";

	@Override
	protected Class<? extends DataSourceManagedObjectSource> getDataSourceManagedObjectSourceClass() {
		return H2DataSourceManagedObjectSource.class;
	}

	@Override
	protected Class<? extends ReadOnlyConnectionManagedObjectSource> getReadOnlyConnectionManagedObjectSourceClass() {
		return H2ReadOnlyConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadConnectionProperties(PropertyConfigurable mos) {
		this.loadDataSourceProperties(mos);
	}

	@Override
	protected void loadDataSourceProperties(PropertyConfigurable mos) {
		mos.addProperty("url", JDBC_URL);
		mos.addProperty("user", "test");
		mos.addProperty("password", "test");
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		}
	}

}
