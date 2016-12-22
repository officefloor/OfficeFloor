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
package net.officefloor.plugin.jms.server;

import javax.jms.Message;
import javax.jms.TextMessage;

import net.officefloor.admin.transaction.TransactionAdministratorSource;
import net.officefloor.admin.transaction.TransactionDutiesEnum;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.jms.AbstractJmsManagedObjectTest;
import net.officefloor.plugin.jms.JmsUtil;
import net.officefloor.plugin.jms.activemq.VmJmsAdminObjectFactory;
import net.officefloor.plugin.jms.server.JmsServerManagedObjectSource.JmsServerFlows;
import net.officefloor.plugin.jms.server.OnMessageTask.OnMessageFlows;

/**
 * Tests sending text messages.
 * 
 * @author Daniel Sagenschneider
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
	public void testConsumeTextMessage() throws Throwable {

		// Obtain the office name and builder
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Configure the JMS Server Managed Object
		ManagedObjectBuilder<JmsServerFlows> moBuilder = this
				.constructManagedObject("JMS_SERVER",
						JmsServerManagedObjectSource.class, officeName);
		moBuilder.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
				VmJmsAdminObjectFactory.class.getName());
		moBuilder.addProperty(
				JmsServerManagedObjectSource.JMS_MAX_SERVER_SESSIONS, "10");

		// Configure the administrator to commit
		AdministratorBuilder<TransactionDutiesEnum> adminBuilder = this
				.constructAdministrator("TRANSACTION",
						TransactionAdministratorSource.class, "JMS_TEAM");
		adminBuilder.administerManagedObject("JMS_SERVER");
		adminBuilder.addDuty(TransactionDutiesEnum.COMMIT.name());

		// Create the process message task
		AbstractSingleTask<Work, Indexed, None> processTask = new AbstractSingleTask<Work, Indexed, None>() {
			@Override
			public Object execute(ManagedFunctionContext<Work, Indexed, None> context)
					throws Exception {

				// Set text of message
				TextMessage message = (TextMessage) context.getObject(0);
				JmsServerManagedObjectTest.this.msg = message.getText();

				// Output message contents
				System.out.println("Processing msg: "
						+ JmsServerManagedObjectTest.this.msg);

				// Wake up test case
				synchronized (this) {
					isWait = false;
					this.notify();
				}

				// No further tasks
				return null;
			}
		};

		// Configure the process message task
		ManagedFunctionBuilder<Work, Indexed, None> taskBuilder = processTask
				.registerTask("work", "task", "PROCESS_TEAM", officeBuilder);
		taskBuilder.linkParameter(0, Message.class);
		taskBuilder.linkPostTaskAdministration("TRANSACTION",
				TransactionDutiesEnum.COMMIT);

		// Obtain the on message task to link it to task processing result
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void enhanceOffice(OfficeEnhancerContext context) {
				// Link JMS on message flow to process message task
				FlowNodeBuilder flowNodeBuilder = context.getFlowNodeBuilder(
						"of-JMS_SERVER", "server", "onmessage");
				flowNodeBuilder.linkFlow(OnMessageFlows.ON_MESSAGE, "work",
						"task", FlowInstigationStrategyEnum.SEQUENTIAL,
						Message.class);
			}
		});

		// Configure the teams
		officeBuilder.registerTeam("of-JMS_SERVER.team", "of-JMS_TEAM");
		this.constructTeam("JMS_TEAM", OnePersonTeamSource.class);
		this.constructTeam("PROCESS_TEAM", OnePersonTeamSource.class);

		// Open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Process a number of messages
		final String TEST_MSG_PREFIX = "test msg ";
		for (int i = 0; i <= 1000; i++) {

			// Publish the message
			synchronized (processTask) {
				this.isWait = true;
			}
			String msgText = TEST_MSG_PREFIX + i;
			this.pushText(msgText);

			// Wait for message to be processed
			long startTime = System.currentTimeMillis();
			synchronized (processTask) {
				while (this.isWait) {
					validateNoTopLevelEscalation();
					if ((System.currentTimeMillis() - startTime) > 2000) {
						fail("Likely threading issue as waiting too long");
					}
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