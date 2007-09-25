/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.jdbc;

import java.sql.Connection;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.pool.PassiveManagedObjectPool;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TypeMatcher;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Tests the {@link net.officefloor.plugin.jdbc.JdbcManagedObjectSource}.
 * 
 * @author Daniel
 */
public class JdbcManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * {@link Connection}.
	 */
	protected volatile Connection connection;

	/**
	 * {@link Connection} reused.
	 */
	protected volatile Connection reusedConnection;

	/**
	 * Ensures interacts with the database.
	 */
	public void testDbInteration() throws Exception {

		// Configure the JDBC managed object
		ManagedObjectBuilder moBuilder = this.constructManagedObject("JDBC",
				JdbcManagedObjectSource.class, "TEST");

		// Bind the Mock Data Source Factory
		ConnectionPoolDataSource dataSource = this
				.createMock(ConnectionPoolDataSource.class);
		MockDataSourceFactory.bind(dataSource, moBuilder);

		// Pool the JDBC managed object instances
		moBuilder.setManagedObjectPool(new PassiveManagedObjectPool(1));

		// Create the task to use the connection
		AbstractSingleTask task = new AbstractSingleTask() {
			public Object doTask(TaskContext context) throws Exception {
				// Obtain the Connection
				Connection connection = (Connection) context.getObject(0);

				// Specify the connection
				if (JdbcManagedObjectSourceTest.this.connection == null) {
					JdbcManagedObjectSourceTest.this.connection = connection;
				} else {
					JdbcManagedObjectSourceTest.this.reusedConnection = connection;
				}

				// No further tasks
				return null;
			}
		};

		// Configure the Office
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		
		// Configure the Work
		WorkBuilder workBuilder = task.registerWork("work", officeBuilder);
		workBuilder.addWorkManagedObject("mo", "JDBC");

		// Configure the Task
		TaskBuilder taskBuilder = task
				.registerTask("task", "team", workBuilder);
		taskBuilder.linkManagedObject(0, "mo");

		// Configure the Team
		Team team = new OnePersonTeam(10);
		this.constructTeam("team", team);
		this.constructTeam("jdbc.recycle", team);

		// ---------------------
		// Record running
		// ---------------------

		// Mock objects
		PooledConnection pooledConnection = this
				.createMock(PooledConnection.class);
		Connection connection = this.createMock(Connection.class);

		// Obtain the PooledConnection
		dataSource.getPooledConnection();
		this.control(dataSource).setReturnValue(pooledConnection);

		// Listen
		pooledConnection.addConnectionEventListener(null);
		this.control(pooledConnection).setMatcher(
				new TypeMatcher(JdbcManagedObject.class));

		// Obtain the Connection
		pooledConnection.getConnection();
		this.control(pooledConnection).setReturnValue(connection);

		// Connection used, therefore close
		connection.close();

		// Obtain the next Connection
		pooledConnection.getConnection();
		this.control(pooledConnection).setReturnValue(connection);

		// Connection used, therefore close
		connection.close();

		// ---------------------
		// Run
		// ---------------------

		// Replay managed objects
		this.replayMockObjects();

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor("TEST");
		officeFloor.openOfficeFloor();

		// Invoke the work
		officeFloor.getOffice("TEST").getWorkManager("work").invokeWork(null);

		// Invoke work to reuse connection
		officeFloor.getOffice("TEST").getWorkManager("work").invokeWork(null);

		// Allow time for processing
		this.sleep(1);

		// Close the Office Floor
		officeFloor.closeOfficeFloor();

		// Ensure obtained the connections
		assertNotNull("Connection not obtained", this.connection);
		assertNotNull("Re-used connection not obtained", this.reusedConnection);

		// Verify
		this.verifyMockObjects();
	}
}
