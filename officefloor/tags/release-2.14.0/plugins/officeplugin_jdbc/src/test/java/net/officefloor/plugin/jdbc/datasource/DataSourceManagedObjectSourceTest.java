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
package net.officefloor.plugin.jdbc.datasource;

import java.sql.Connection;
import java.sql.Driver;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.jdbc.ConnectionValidator;

/**
 * Tests the {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Mock {@link Connection}.
	 */
	private final Connection connection = this.createMock(Connection.class);

	/**
	 * Ensure able to load the {@link DataSource} and use a {@link Connection}.
	 */
	public void testLoadAndUseDataSource() throws Exception {

		// Record closing the connection (twice as task invoked twice)
		this.connection.close();
		this.connection.close();

		// Specify the connection to return
		MockDataSource.setConnection(this.connection);

		// Obtain the office name
		final String officeName = this.getOfficeName();

		// Construct the task to validate the connection
		final List<Connection> connections = new LinkedList<Connection>();
		DataSourceTask task = new DataSourceTask(new ConnectionValidator() {
			@Override
			public void validateConnection(Connection connection)
					throws Throwable {
				// Record the connection being used
				connections.add(connection);
			}
		});
		String workName = task.construct(this.getOfficeBuilder(), null,
				"DATA_SOURCE", "TEAM");

		// Configure the necessary Teams
		this.constructTeam("TEAM", new PassiveTeam());

		// Test
		this.replayMockObjects();

		// Configure the DataSource managed object
		ManagedObjectBuilder<?> moBuilder = this.constructManagedObject(
				"DATA_SOURCE", DataSourceManagedObjectSource.class, officeName);
		moBuilder.addProperty(
				DataSourceManagedObjectSource.PROPERTY_DATA_SOURCE_CLASS_NAME,
				MockDataSource.class.getName());
		moBuilder.addProperty("driver", Driver.class.getName());
		moBuilder.addProperty("url", "server:10000");
		moBuilder.addProperty("serverName", "server");
		moBuilder.addProperty("port", "10000");
		moBuilder.addProperty("databaseName", "database");
		moBuilder.addProperty("username", "user");
		moBuilder.addProperty("password", "not telling");

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify properties were loaded onto connection pool data source
		MockDataSource dataSource = MockDataSource.getInstance();
		assertEquals("Incorrect driver", Driver.class.getName(), dataSource
				.getDriver());
		assertEquals("Incorrect url", "server:10000", dataSource.getUrl());
		assertEquals("Incorrect server", "server", dataSource.getServerName());
		assertEquals("Incorrect port", 10000, dataSource.getPort());
		assertEquals("Incorrect database", "database", dataSource
				.getDatabaseName());
		assertEquals("Incorrect username", "user", dataSource.getUsername());
		assertEquals("Incorrect password", "not telling", dataSource
				.getPassword());

		// Obtain the work manager with task to use the connection
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager(workName);

		// Invoke work to use the connection
		workManager.invokeWork(null);

		// Invoke work to re-use the connection
		workManager.invokeWork(null);

		// Close the Office Floor
		officeFloor.closeOfficeFloor();

		// Verify mocks
		this.verifyMockObjects();

		// Verify task invoked twice with connection
		assertEquals("Incorrect times task invoked", 2, connections.size());
		assertEquals("Incorrect first connection", this.connection, connections
				.get(0));
		assertEquals("Incorrect second connection", this.connection,
				connections.get(1));
	}

}