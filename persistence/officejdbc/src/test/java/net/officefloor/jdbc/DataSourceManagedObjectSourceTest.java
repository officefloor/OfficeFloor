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

package net.officefloor.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.jdbc.datasource.DataSourceFactory;
import net.officefloor.jdbc.datasource.DataSourceTransformer;
import net.officefloor.jdbc.datasource.DataSourceTransformerContext;
import net.officefloor.jdbc.datasource.DataSourceTransformerServiceFactory;
import net.officefloor.jdbc.datasource.DefaultDataSourceFactory;

/**
 * Tests the {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class DataSourceManagedObjectSourceTest implements DataSourceTransformer, DataSourceTransformerServiceFactory {

	/**
	 * {@link OfficeFloorJdbcExtension}.
	 */
	@RegisterExtension
	public final OfficeFloorJdbcExtension jdbc = new OfficeFloorJdbcExtension();

	/**
	 * {@link MockTestSupport}.
	 */
	public final MockTestSupport mockTestSupport = new MockTestSupport();

	/**
	 * {@link Connection}.
	 */
	private Connection connection;

	@BeforeEach
	protected void setUp() throws Exception {
		this.connection = this.jdbc.beforeEach();

		// Reset mock sections
		MockSection.connection = null;
		MockDataSourceSection.dataSource = null;
	}

	/**
	 * Validate the specification.
	 */
	@Test
	public void specification() {
		ManagedObjectLoaderUtil.validateSpecification(DataSourceManagedObjectSource.class);
	}

	/**
	 * Validate the type.
	 */
	@Test
	public void type() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();
		type.setObjectClass(DataSource.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type, DataSourceManagedObjectSource.class,
				DefaultDataSourceFactory.PROPERTY_DATA_SOURCE_CLASS_NAME, JdbcDataSource.class.getName());
	}

	/**
	 * Ensures {@link Connection}.
	 */
	@Test
	public void connection() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					DataSourceManagedObjectSource.class.getName());
			this.jdbc.loadProperties(mos);
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Undertake operation
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", null);

			// Ensure row inserted
			try (Statement statement = this.connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
				assertTrue(resultSet.next(), "Should have row");
				assertEquals("OfficeFloor", resultSet.getString("NAME"), "Incorrect row");
			}

			// Ensure the connection is closed
			assertNotNull(MockSection.connection, "Should have connection");
			assertTrue(MockSection.connection.isClosed(), "Connection used should be closed");
		}
	}

	public static class MockSection {

		private static Connection connection = null;

		public void section(DataSource dataSource) throws SQLException {
			try (Connection connection = dataSource.getConnection()) {
				try (Statement statement = connection.createStatement()) {
					statement.execute("INSERT INTO TEST ( ID, NAME ) VALUES ( 2, 'OfficeFloor' )");
				}
				MockSection.connection = connection;
			}
		}
	}

	/**
	 * Ensures able to configure {@link DataSourceFactory}.
	 */
	@Test
	public void dataSourceFactory() throws Throwable {

		// Open the OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Create the managed object
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
					DataSourceManagedObjectSource.class.getName());
			mos.addProperty(DataSourceManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
					MockDataSourceFactory.class.getName());
			mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

			// Provide section
			context.addSection("SECTION", MockSection.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Undertake operation
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.section", null);

			// Ensure row inserted
			try (Statement statement = this.connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery("SELECT NAME FROM TEST WHERE ID = 2");
				assertTrue(resultSet.next(), "Should have row");
				assertEquals("OfficeFloor", resultSet.getString("NAME"), "Incorrect row");
			}

			// Ensure the connection is closed
			assertNotNull(MockSection.connection, "Should have connection");
			assertTrue(MockSection.connection.isClosed(), "Connection used should be closed");
		}
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
	@Test
	public void dataSourceManagement() throws Throwable {
		this.jdbc.doDataSourceManagementTest(DataSourceManagedObjectSource.class, 0);
	}

	/**
	 * Ensure can transform the DataSource.
	 */
	@Test
	public void transformDataSource() throws Throwable {

		// Create transformed Data Source
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(this.jdbc.getJdbcUrl());
		dataSource = new HikariDataSource(config);
		try {

			// Open the OfficeFloor
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.office((context) -> {

				// Create the managed object
				OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
						DataSourceManagedObjectSource.class.getName());
				this.jdbc.loadProperties(mos);
				mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

				// Provide section
				context.addSection("SECTION", MockDataSourceSection.class);
			});
			try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

				// Undertake operation
				CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", null);

				// Ensure correct data source
				assertNotNull(MockDataSourceSection.dataSource, "Should have DataSource");
				DataSourceWrapper wrapper = (DataSourceWrapper) MockDataSourceSection.dataSource;
				DataSource actual = wrapper.getRealDataSource();
				assertSame(dataSource, actual, "Incorrect transformed DataSource");
			}

		} finally {
			// Ensure clean up
			dataSource = null;
		}
	}

	public static class MockDataSourceSection {

		private static DataSource dataSource;

		public void service(DataSource dataSource) {
			MockDataSourceSection.dataSource = dataSource;
		}
	}

	/**
	 * Ensure issue if transform database to null.
	 */
	@Test
	public void transformDataSourceToNull() throws Throwable {

		// Create transformed Data Source
		isTransformDataSource = true;
		try {

			// Record issue
			MockCompilerIssues issues = new MockCompilerIssues(this.mockTestSupport);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class,
					"Failed to initialise " + DataSourceManagedObjectSource.class.getName(), new IllegalStateException(
							"No DataSource provided from DataSourceTransformer " + this.getClass().getName()));

			// Test
			this.mockTestSupport.replayMockObjects();

			// Open the OfficeFloor
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.getOfficeFloorCompiler().setCompilerIssues(issues);
			compiler.office((context) -> {

				// Create the managed object
				OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("mo",
						DataSourceManagedObjectSource.class.getName());
				this.jdbc.loadProperties(mos);
				mos.addOfficeManagedObject("mo", ManagedObjectScope.THREAD);

				// Provide section
				context.addSection("SECTION", MockDataSourceSection.class);
			});
			try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
				this.mockTestSupport.verifyMockObjects();
			}

		} finally {
			// Ensure clean up
			isTransformDataSource = false;
		}
	}

	/*
	 * ===================== DataSourceTransformer ========================
	 */

	private static DataSource dataSource = null;

	private static boolean isTransformDataSource = false;

	@Override
	public DataSourceTransformer createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public DataSource transformDataSource(DataSourceTransformerContext context) throws Exception {
		return (dataSource != null || isTransformDataSource) ? dataSource : context.getDataSource();
	}

}
