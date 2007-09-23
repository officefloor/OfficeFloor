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
package net.officefloor.plugin.hibernate;

import java.sql.Connection;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.AbstractOfficeConstructTestCase;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.hibernate.HibernateManagedObjectSource.HibernateDependenciesEnum;

import org.hibernate.Session;

/**
 * Test the
 * {@link net.officefloor.plugin.hibernate.HibernateManagedObjectSource}.
 * 
 * @author Daniel
 */
public class HibernateManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Test Hibernate connection.
	 */
	public void testHibernate() throws Exception {

		// Configure the Hibernate managed object
		ManagedObjectBuilder moBuilder = this.constructManagedObject(
				"Hibernate", HibernateManagedObjectSource.class, "TEST");
		moBuilder.addProperty("configuration",
				"net/officefloor/plugin/hibernate/hibernate.cfg.xml");

		// Configure the Connection for Hibernate
		final Connection mockConnection = this.createMock(Connection.class);
		this.constructManagedObject("Connection", new ManagedObject() {
			public Object getObject() throws Exception {
				return mockConnection;
			}
		}, "TEST");

		// Allow specifying the session connection
		final Connection[] connectionHolder = new Connection[1];

		// Create the task to use the session
		AbstractSingleTask task = new AbstractSingleTask() {
			public Object doTask(TaskContext context) throws Exception {
				// Obtain the Session
				Session session = (Session) context.getObject(0);

				// Obtain the session connection
				synchronized (connectionHolder) {
					connectionHolder[0] = session.connection();
				}

				// No further tasks
				return null;
			}
		};

		// Configure the Work
		WorkBuilder workBuilder = task.registerWork("work", this
				.getOfficeBuilder());
		workBuilder.addWorkManagedObject("conn", "Connection");
		DependencyMappingBuilder dependencyBuilder = workBuilder
				.addWorkManagedObject("mo", "Hibernate");
		dependencyBuilder.registerDependencyMapping(
				HibernateDependenciesEnum.CONNECTION, "conn");

		// Configure the Task
		TaskBuilder taskBuilder = task
				.registerTask("task", "team", workBuilder);
		taskBuilder.linkManagedObject(0, "mo");

		// Configure the Team
		Team team = new PassiveTeam();
		this.constructTeam("team", team);

		// Replay
		this.replayMockObjects();

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor("TEST");
		officeFloor.openOfficeFloor();

		// Invoke the work
		officeFloor.getOffice("TEST").getWorkManager("work").invokeWork(null);

		// Close the Office Floor
		officeFloor.closeOfficeFloor();

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure have Session Connection
		Connection sessionConnection;
		synchronized (connectionHolder) {
			sessionConnection = connectionHolder[0];
		}
		assertNotNull("Session missing Connection", sessionConnection);

		// Ensure using specified connection
		System.out.println("mock: " + mockConnection + "["
				+ mockConnection.getClass().getName() + "]");
		System.out.println("session: " + sessionConnection + "["
				+ sessionConnection.getClass().getName() + "]");
		assertEquals("Incorrect underlying connection", mockConnection,
				sessionConnection);
	}

}
