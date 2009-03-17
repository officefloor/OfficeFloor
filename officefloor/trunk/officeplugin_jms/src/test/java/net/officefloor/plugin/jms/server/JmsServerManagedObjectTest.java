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
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.jms.AbstractJmsManagedObjectTest;
import net.officefloor.plugin.jms.JmsUtil;
import net.officefloor.plugin.jms.activemq.VmJmsAdminObjectFactory;

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

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Configure the JMS Server Managed Object
		ManagedObjectBuilder moBuilder = this.constructManagedObject(
				"JMS_SERVER", JmsServerManagedObjectSource.class, officeName);
		moBuilder.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
				VmJmsAdminObjectFactory.class.getName());
		moBuilder.addProperty(
				JmsServerManagedObjectSource.JMS_MAX_SERVER_SESSION, "2");

		// Create the process message task
		AbstractSingleTask processTask = new AbstractSingleTask() {
			public Object doTask(TaskContext context) throws Exception {
				// Set text of message
				TextMessage message = (TextMessage) context.getParameter();
				JmsServerManagedObjectTest.this.msg = message.getText();

				// Output message contents
				System.out.println("Processing msg: "
						+ JmsServerManagedObjectTest.this.msg);

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
		officeBuilder.addProcessManagedObject("JMS_SERVER", "JMS_SERVER");

		// Configure the administrator to commit
		AdministratorBuilder<TransactionDutiesEnum> adminBuilder = this
				.constructAdministrator("TRANSACTION",
						TransactionAdministratorSource.class,
						"of-JMS_SERVER.jms.server.onmessage");
		adminBuilder.administerManagedObject("JMS_SERVER");
		adminBuilder.addDuty(TransactionDutiesEnum.BEGIN);
		adminBuilder.addDuty(TransactionDutiesEnum.COMMIT);
		adminBuilder.addDuty(TransactionDutiesEnum.ROLLBACK);

		// Configure the Work
		WorkBuilder workBuilder = processTask.registerWork("work",
				officeBuilder);

		// Configure the Task
		TaskBuilder taskBuilder = processTask.registerTask("task",
				"of-JMS_SERVER.jms.server.onmessage", workBuilder);
		taskBuilder.linkPostTaskAdministration("TRANSACTION",
				TransactionDutiesEnum.COMMIT);

		// Obtain the on message task to link it to task processing result
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				// Obtain the JMS flow node
				FlowNodeBuilder<?> flowNodeBuilder = context
						.getFlowNodeBuilder("of-JMS_SERVER",
								"jms.server.onmessage", "onmessage");

				// Flag its next task
				flowNodeBuilder.linkFlow(0, "work", "task",
						FlowInstigationStrategyEnum.SEQUENTIAL);
			}
		});

		// Configure the teams
		this.constructTeam("of-JMS_SERVER.jms.server.recycle",
				OnePersonTeamSource.class);
		this.constructTeam("of-JMS_SERVER.jms.server.onmessage",
				OnePersonTeamSource.class);

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
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