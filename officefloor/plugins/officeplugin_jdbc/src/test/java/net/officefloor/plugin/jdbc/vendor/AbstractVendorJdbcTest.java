/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.jdbc.vendor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.plugin.jdbc.ConnectionValidator;
import net.officefloor.plugin.jdbc.connection.JdbcDataSourceAccess;
import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;
import net.officefloor.plugin.jdbc.connection.JdbcTask;

/**
 * Provides the abstract functionality for testing the
 * {@link JdbcManagedObjectSource} working with a vendor database.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractVendorJdbcTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Flag indicating if the database is available.
	 */
	protected boolean isDatabaseAvailable;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the connection pool data source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		Properties properties = this.getDataSourceProperties();
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			loader.addProperty(name, value);
		}
		JdbcManagedObjectSource mos = loader
				.initManagedObjectSource(JdbcManagedObjectSource.class);

		// Attempt to obtain connection to database to ensure available
		try {
			ConnectionPoolDataSource dataSource = JdbcDataSourceAccess
					.obtainConnectionPoolDataSource(mos);
			PooledConnection connection = dataSource.getPooledConnection();
			connection.close();

			// Obtained connection, so database available
			this.isDatabaseAvailable = true;

		} catch (Throwable ex) {
			// Failed to get connection, so not available
			this.isDatabaseAvailable = false;

			// Indicate database not available
			System.err.println("============================================");
			System.err.println("  Test " + this.getName()
					+ " invalid as database not available");
			ex.printStackTrace();
			System.err.println("============================================");
		}

		// Clean up database ready for testing
		try {
			ConnectionPoolDataSource dataSource = JdbcDataSourceAccess
					.obtainConnectionPoolDataSource(mos);
			PooledConnection connection = dataSource.getPooledConnection();
			if (this.isDropTablesOnSetup()) {
				Statement statement = connection.getConnection()
						.createStatement();
				statement.execute("DROP TABLE PRODUCT");
			}
			connection.close();
		} catch (SQLException ex) {
			// IMDBs will not be setup and otherwise should pass.
			// Also first runs will fail as table not yet created.
			ex.printStackTrace();
		}
	}

	/**
	 * Obtains the properties for the {@link ConnectionPoolDataSource}.
	 * 
	 * @return Properties for the {@link ConnectionPoolDataSource}.
	 */
	private Properties getDataSourceProperties() {
		Properties properties = new Properties();

		// Add login timeout as always required
		properties.setProperty("loginTimeout", "15");

		// Load specific properties
		this.loadProperties(properties);

		// Return the properties
		return properties;
	}

	/**
	 * Overridden to populate the properties for the vendor JDBC implementation.
	 * 
	 * @param properties
	 *            Properties to populate to initialise the
	 *            {@link JdbcManagedObjectSource}.
	 */
	protected abstract void loadProperties(Properties properties);

	/**
	 * Flag indicating whether to drop the TABLEs within the database on setup.
	 * 
	 * @return <code>true</code> to drop the TABLEs.
	 */
	protected boolean isDropTablesOnSetup() {
		return true;
	}

	/**
	 * Tests setup of a database and selecting data from it.
	 */
	public void testSelect() throws Exception {

		// Only proceed if database available
		if (!this.isDatabaseAvailable) {
			return;
		}

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Configure the JDBC managed object
		ManagedObjectBuilder<None> moBuilder = this.constructManagedObject(
				"JDBC", JdbcManagedObjectSource.class, officeName);
		Properties properties = this.getDataSourceProperties();
		System.out.println("Loading "
				+ JdbcManagedObjectSource.class.getSimpleName()
				+ " with properties:");
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			System.out.println("    " + name + "=" + value);
			moBuilder.addProperty(name, value);
		}
		moBuilder.addProperty(
				JdbcManagedObjectSource.DATA_SOURCE_INITIALISE_SCRIPT, this
						.getFileLocation(AbstractVendorJdbcTest.class,
								"InitialiseDatabase.sql"));

		// Configure the task for the connection
		JdbcTask task = new JdbcTask(new ConnectionValidator() {
			@Override
			public void validateConnection(Connection connection)
					throws Throwable {
				// Obtain the product name to validate working
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement
						.executeQuery("SELECT PRODUCT_NAME FROM PRODUCT WHERE PRODUCT_ID = 1");
				resultSet.next();
				String productName = resultSet.getString("PRODUCT_NAME");
				assertEquals("Incorrect product name", "Test", productName);
				statement.close();
			}
		});
		String workName = task.construct(this.getOfficeBuilder(), null, "JDBC",
				"TEAM");

		// Configure the necessary Teams
		this.constructTeam("TEAM", new PassiveTeam());
		this.constructTeam("of-JDBC.jdbc.recycle", new PassiveTeam());

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Obtain the work manager with task to use the connection
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager(workName);

		// Invoke work to use the connection
		workManager.invokeWork(null);

		// Close the Office Floor
		officeFloor.closeOfficeFloor();
	}

}