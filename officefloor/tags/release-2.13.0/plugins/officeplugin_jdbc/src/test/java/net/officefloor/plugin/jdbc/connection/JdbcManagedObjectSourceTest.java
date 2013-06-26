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
package net.officefloor.plugin.jdbc.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.util.LinkedList;
import java.util.List;

import javax.sql.PooledConnection;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.pool.PassiveManagedObjectPool;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.plugin.jdbc.ConnectionValidator;
import net.officefloor.plugin.jdbc.connection.JdbcManagedObject;
import net.officefloor.plugin.jdbc.connection.JdbcManagedObjectSource;

/**
 * Tests the {@link JdbcManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JdbcManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * {@link PooledConnection}.
	 */
	private PooledConnection pooledConnection = this
			.createMock(PooledConnection.class);

	/**
	 * {@link Connection}.
	 */
	private Connection connection = this.createMock(Connection.class);

	/**
	 * Ensures able to obtain the {@link Connection}.
	 */
	public void testLoadAndUseJdbc() throws Exception {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Specify the pooled connection
		MockConnectionPoolDataSource.setPooledConnection(this.pooledConnection);

		// Configure the JDBC managed object
		ManagedObjectBuilder<?> moBuilder = this.constructManagedObject("JDBC",
				JdbcManagedObjectSource.class, officeName);
		moBuilder.setManagedObjectPool(new PassiveManagedObjectPool(1));
		moBuilder
				.addProperty(
						JdbcManagedObjectSource.CONNECTION_POOL_DATA_SOURCE_CLASS_PROPERTY,
						MockConnectionPoolDataSource.class.getName());
		moBuilder.addProperty("driver", Driver.class.getName());
		moBuilder.addProperty("url", "server:10000");
		moBuilder.addProperty("serverName", "server");
		moBuilder.addProperty("port", "10000");
		moBuilder.addProperty("databaseName", "database");
		moBuilder.addProperty("username", "user");
		moBuilder.addProperty("password", "not telling");
		moBuilder.addProperty("loginTimeout", "15");

		// Construct the task to validate the connection
		final List<Connection> connections = new LinkedList<Connection>();
		JdbcTask task = new JdbcTask(new ConnectionValidator() {
			@Override
			public void validateConnection(Connection connection)
					throws Throwable {
				// Record the connection being used
				connections.add(connection);
			}
		});
		String workName = task.construct(this.getOfficeBuilder(), null, "JDBC",
				"TEAM");

		// Configure the necessary Teams
		this.constructTeam("TEAM", new PassiveTeam());
		this.constructTeam("of-JDBC.jdbc.recycle", new PassiveTeam());

		// Record actions on mocks
		this.pooledConnection.close();
		this.pooledConnection.addConnectionEventListener(null);
		this.control(this.pooledConnection).setMatcher(
				new TypeMatcher(JdbcManagedObject.class));
		this.recordReturn(this.pooledConnection, this.pooledConnection
				.getConnection(), this.connection);
		this.connection.close();
		this.recordReturn(this.pooledConnection, this.pooledConnection
				.getConnection(), this.connection);
		this.connection.close();

		// Replay mocks
		this.replayMockObjects();

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Verify properties were loaded onto connection pool data source
		MockConnectionPoolDataSource dataSource = MockConnectionPoolDataSource
				.getInstance();
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
		assertEquals("Incorrect login timeout", 15, dataSource
				.getLoginTimeout());

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