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

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.jms.AbstractJmsManagedObjectTest;
import net.officefloor.plugin.jms.JmsUtil;
import net.officefloor.plugin.jms.activemq.VmJmsAdminObjectFactory;
import net.officefloor.plugin.jms.server.JmsServerManagedObjectSource.JmsServerFlows;
import net.officefloor.plugin.transaction.Transaction;
import net.officefloor.plugin.transaction.TransactionGovernanceSource;

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
		ManagedObjectBuilder<JmsServerFlows> moBuilder = this.constructManagedObject("JMS_SERVER",
				JmsServerManagedObjectSource.class, null);
		moBuilder.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY, VmJmsAdminObjectFactory.class.getName());
		moBuilder.addProperty(JmsServerManagedObjectSource.JMS_MAX_SERVER_SESSIONS, "10");
		ManagingOfficeBuilder<JmsServerFlows> managingOffice = moBuilder.setManagingOffice(officeName);
		managingOffice.linkFlow(JmsServerFlows.SERVICE_MESSAGE, "function");
		managingOffice.setInputManagedObjectName("JMS").mapGovernance("TRANSACTION");

		// Configure the governance to commit
		GovernanceBuilder<None> governBuilder = this.getOfficeBuilder().addGovernance("TRANSACTION", Transaction.class,
				new TransactionGovernanceSource());
		governBuilder.setResponsibleTeam("JMS_TEAM");

		// Create the process message function factory
		ManagedFunctionFactory<Indexed, None> processFunction = () -> (context) -> {

			// Set text of message
			TextMessage message = (TextMessage) context.getObject(0);
			JmsServerManagedObjectTest.this.msg = message.getText();

			// Output message contents
			System.out.println("Processing msg: " + JmsServerManagedObjectTest.this.msg);

			// Wake up test case
			synchronized (this) {
				isWait = false;
				this.notify();
			}

			// No further functions
			return null;
		};

		// Configure the process message function
		ManagedFunctionBuilder<Indexed, None> functionBuilder = this.constructFunction("function", processFunction);
		functionBuilder.linkParameter(0, Message.class);
		functionBuilder.setResponsibleTeam("PROCESS_TEAM");
		functionBuilder.addGovernance("TRANSACTION");

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
			synchronized (processFunction) {
				this.isWait = true;
			}
			String msgText = TEST_MSG_PREFIX + i;
			this.pushText(msgText);

			// Wait for message to be processed
			long startTime = System.currentTimeMillis();
			synchronized (processFunction) {
				while (this.isWait) {
					validateNoTopLevelEscalation();
					if ((System.currentTimeMillis() - startTime) > 2000) {
						fail("Likely threading issue as waiting too long");
					}
					processFunction.wait(100);
				}
			}

			// Validate message
			assertEquals("Incorrect message", msgText, this.msg);
		}

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();

		// Flag test complete
		System.out.println("Messages processed");
	}

}