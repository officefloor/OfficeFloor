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
package net.officefloor.plugin.jms;

import net.officefloor.admin.transaction.Transaction;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.util.ManagedObjectSourceLoader;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests sending text messages.
 * 
 * @author Daniel
 */
public class TextJmsManagedObjectTest extends AbstractJmsManagedObjectTest {

	/**
	 * Ensure able to publish a test message.
	 */
	public void testPublishTextMessage() throws Exception {

		final String TEST_MSG = "test msg";

		// Create the JMS managed object source
		ManagedObjectSourceLoader loader = new ManagedObjectSourceLoader();
		loader.addProperty(JmsUtil.JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY,
				TestJmsAdminObjectFactory.class.getName());
		ManagedObjectSource moSource = loader
				.loadManagedObjectSource(JmsManagedObjectSource.class);

		// Create the JMS managed object
		ManagedObject mo = ManagedObjectUserStandAlone
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
