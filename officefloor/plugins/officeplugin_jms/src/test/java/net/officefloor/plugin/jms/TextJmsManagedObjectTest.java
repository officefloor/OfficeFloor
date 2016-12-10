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
package net.officefloor.plugin.jms;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.jms.activemq.VmJmsAdminObjectFactory;

/**
 * Tests sending text messages.
 *
 * @author Daniel Sagenschneider
 */
public class TextJmsManagedObjectTest extends AbstractJmsManagedObjectTest {

	/**
	 * Ensure able to publish a test message.
	 */
	public void testPublishTextMessage() throws Throwable {

		final String TEST_MSG = "test msg";

		// Create the JMS managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
				VmJmsAdminObjectFactory.class.getName());
		ManagedObjectSource<?, ?> moSource = loader
				.loadManagedObjectSource(JmsManagedObjectSource.class);

		// Create the JMS managed object
		ManagedObject mo = new ManagedObjectUserStandAlone()
				.sourceManagedObject(moSource);

		// Obtain the producer
		TextMessageProducer producer = (TextMessageProducer) mo.getObject();

		// Send the text
		producer.send(TEST_MSG);

		// Commit the message to queue
		Transaction transaction = (Transaction) mo;
		transaction.commit();

		// Source text
		String msg = this.popText(100);

		// Validate
		assertEquals("Incorrect message", TEST_MSG, msg);
	}

}
