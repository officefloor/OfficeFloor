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
package net.officefloor.plugin.jms.server;

import javax.jms.TextMessage;

import net.officefloor.admin.transaction.TransactionAdministratorSource;
import net.officefloor.admin.transaction.TransactionDutiesEnum;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.FlowNodesEnhancer;
import net.officefloor.frame.api.build.FlowNodesEnhancerContext;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.jms.AbstractJmsManagedObjectTest;
import net.officefloor.plugin.jms.TestJmsAdminObjectFactory;

/**
 * Tests sending text messages.
 * 
 * @author Daniel
 */
public class JmsServerManagedObjectTest extends AbstractJmsManagedObjectTest {

	/**
	 * Message being consumed by the Server.
	 */
	private volatile String msg;

	/**
	 * Flag indicating to wait.
	 */
	private boolean isWait;

	/**
	 * Ensure able to consume a test message.
	 */
	@SuppressWarnings("unchecked")
	public void testConsumeTextMessage() throws Exception {

		// Configure the JMS Server Managed Object
		ManagedObjectBuilder moBuilder = this.constructManagedObject(
				"JMS_SERVER", JmsServerManagedObjectSource.class, "TEST");
		moBuilder
				.addProperty(
						JmsServerManagedObjectSource.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
						TestJmsAdminObjectFactory.class.getName());
		moBuilder.addProperty(
				JmsServerManagedObjectSource.JMS_MAX_SERVER_SESSION, "2");

		// Configure task to process message
		moBuilder.addProperty(JmsServerManagedObjectSource.JMS_ON_MESSAGE_WORK,
				"work");
		moBuilder.addProperty(JmsServerManagedObjectSource.JMS_ON_MESSAGE_TASK,
				"task");

		// Configure the administrator to commit
		this.constructAdministrator("TRANSACTION",
				TransactionAdministratorSource.class, OfficeScope.WORK);

		// Create the process message task
		AbstractSingleTask processTask = new AbstractSingleTask() {
			public Object doTask(TaskContext context) throws Exception {
				// Set text of message
				TextMessage message = (TextMessage) context.getParameter();
				msg = message.getText();

				// Output message contents
				System.out.println("Processing msg: " + msg);

				// Wakeup test case
				synchronized (this) {
					isWait = false;
					this.notify();
				}

				// No further tasks
				return null;
			}
		};

		// Configure the Office
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		officeBuilder.addProcessManagedObject("P_JMS", "JMS_SERVER");

		// Configure the Work
		WorkBuilder workBuilder = processTask.registerWork("work",
				officeBuilder);
		workBuilder.registerProcessManagedObject("mo", "P_JMS");
		AdministrationBuilder adminBuilder = workBuilder
				.registerAdministration("transaction", "TRANSACTION");
		adminBuilder.setManagedObjects(new String[] { "mo" });

		// Configure the Task
		TaskBuilder taskBuilder = processTask.registerTask("task",
				"jms.server.recycle", workBuilder);
		taskBuilder.linkPostTaskAdministration("transaction",
				TransactionDutiesEnum.COMMIT);

		// Obtain the on message task to link it to task processing result
		this.getOfficeBuilder().addFlowNodesEnhancer(new FlowNodesEnhancer() {
			@Override
			public void enhanceFlowNodes(FlowNodesEnhancerContext context)
					throws BuildException {
				// Obtain the JMS flow node
				FlowNodeBuilder<?> flowNodeBuilder = context
						.getFlowNodeBuilder("of-JMS_SERVER",
								"jms.server.onmessage", "onmessage");

				// Flag its next task
				flowNodeBuilder.setNextTaskInFlow("work", "task");
			}
		});

		// Configure the teams
		Team team = new OnePersonTeam(10);
		this.constructTeam("jms.server.recycle", team);
		this.constructTeam("of-JMS_SERVER.jms.server.recycle", team);
		this.constructTeam("of-JMS_SERVER.jms.server.onmessage", team);

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor("TEST");
		officeFloor.openOfficeFloor();

		// Process a number of messages
		final String TEST_MSG_PREFIX = "test msg ";
		for (int i = 0; i <= 1000; i++) {
			// Publish the message
			synchronized (processTask) {
				isWait = true;
			}
			String msgText = TEST_MSG_PREFIX + i;
			this.pushText(msgText);
			synchronized (processTask) {
				while (isWait) {
					processTask.wait(100);
				}
			}

			// Validate message
			assertEquals("Incorrect message", msgText, this.msg);
		}

		// Close the Office
		officeFloor.closeOfficeFloor();

		// Flag test complete
		System.out.println("Messages processed");
	}

}
