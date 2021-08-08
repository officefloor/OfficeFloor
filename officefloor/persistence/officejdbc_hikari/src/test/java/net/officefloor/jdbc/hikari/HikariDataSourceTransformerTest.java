/*-
 * #%L
 * Hikari JDBC Pooling
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

package net.officefloor.jdbc.hikari;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.jdbc.h2.H2DataSourceManagedObjectSource;

/**
 * Tests the {@link HikariDataSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HikariDataSourceTransformerTest {

	/**
	 * Ensure can transform the DataSource.
	 */
	@Test
	public void transformDataSource() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					H2DataSourceManagedObjectSource.class.getName());
			mos.addProperty("url", "jdbc:h2:mem:test");
			mos.addProperty("user", "sa");
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockDataSourceSection.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Undertake operation
			MockDataSourceSection.dataSource = null;
			MockDataSourceSection.connection = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);

			// Ensure correct data source
			assertNotNull(MockDataSourceSection.dataSource, "Should have DataSource");
			assertTrue(MockDataSourceSection.dataSource instanceof HikariDataSource,
					"Should transform to Hikari pooling");

			// Ensure can access data
			try (Connection connection = MockDataSourceSection.dataSource.getConnection()) {
				ResultSet results = connection.createStatement().executeQuery("SELECT CONTENT FROM TEST");
				assertTrue(results.next(), "Should have entry");
				assertEquals("AVAILABLE", results.getString("CONTENT"), "Incorrect data");
			}
		}
	}

	public static class MockDataSourceSection {

		private static DataSource dataSource;

		private static Connection connection;

		public void service(DataSource dataSource) throws SQLException {
			MockDataSourceSection.dataSource = dataSource;
			MockDataSourceSection.connection = dataSource.getConnection();

			// Setup data
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE TABLE TEST ( CONTENT VARCHAR(50))");
				statement.execute("INSERT INTO TEST ( CONTENT ) VALUES ( 'AVAILABLE' )");
			}
		}
	}

}
