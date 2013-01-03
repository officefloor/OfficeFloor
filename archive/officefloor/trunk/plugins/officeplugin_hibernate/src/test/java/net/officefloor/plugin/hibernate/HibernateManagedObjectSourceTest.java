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
package net.officefloor.plugin.hibernate;

import java.sql.Connection;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.hibernate.HibernateManagedObjectSource.HibernateDependenciesEnum;

import org.hibernate.Session;

/**
 * Test the {@link HibernateManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateManagedObjectSourceTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Test Hibernate connection.
	 */
	public void testHibernate() throws Throwable {

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Configure the Hibernate managed object
		ManagedObjectBuilder<?> moBuilder = this.constructManagedObject(
				"Hibernate", HibernateManagedObjectSource.class, officeName);
		moBuilder.addProperty("configuration",
				"net/officefloor/plugin/hibernate/hibernate.cfg.xml");

		// Configure the Connection for Hibernate
		final Connection mockConnection = this.createMock(Connection.class);
		this.constructManagedObject("Connection", new ManagedObject() {
			public Object getObject() throws Exception {
				return mockConnection;
			}
		}, officeName);

		// Allow specifying the session connection
		final Connection[] connectionHolder = new Connection[1];

		// Flag not in transaction so not 'aggressively released'
		this
				.recordReturn(mockConnection, mockConnection.getAutoCommit(),
						false);

		// Create the task to use the session
		AbstractSingleTask<Work, Indexed, None> task = new AbstractSingleTask<Work, Indexed, None>() {
			@Override
			public Object doTask(TaskContext<Work, Indexed, None> context)
					throws Exception {
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
		WorkBuilder<Work> workBuilder = task.registerWork("work", this
				.getOfficeBuilder());
		workBuilder.addWorkManagedObject("conn", "Connection");
		workBuilder.addWorkManagedObject("mo", "Hibernate").mapDependency(
				HibernateDependenciesEnum.CONNECTION, "conn");

		// Configure the Task
		TaskBuilder<Work, Indexed, None> taskBuilder = task.registerTask(
				"task", "team", workBuilder);
		taskBuilder.linkManagedObject(0, "mo", Session.class);

		// Configure the Team
		this.constructTeam("team", new PassiveTeam());

		// Invoke the work to obtain the Hibernate Session
		this.replayMockObjects();
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager("work");
		workManager.invokeWork(null);
		officeFloor.closeOfficeFloor();
		this.validateNoTopLevelEscalation();
		this.verifyMockObjects();

		// Ensure have Session Connection
		Connection sessionConnection;
		synchronized (connectionHolder) {
			sessionConnection = connectionHolder[0];
		}
		assertNotNull("Session missing Connection", sessionConnection);

		// Ensure using specified connection
		assertEquals("Incorrect underlying connection", mockConnection,
				sessionConnection);
	}

}