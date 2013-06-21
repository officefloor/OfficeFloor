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
package net.officefloor.tutorial.cometmanualapp;

import junit.framework.TestCase;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometPublicationService;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.tutorial.cometmanualapp.client.ConversationMessage;
import net.officefloor.tutorial.cometmanualapp.client.ConversationSubscription;

import com.gdevelop.gwt.syncrpc.SyncProxy;

/**
 * Ensures able to publish a {@link CometEvent}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConversationTest extends TestCase {

	/**
	 * Ensure able to publish a {@link CometEvent}.
	 */
	public void testPublishEvent() throws Exception {

		// Start the application
		WoofOfficeFloorSource.start();

		// Publish a message
		CometPublicationService service = (CometPublicationService) SyncProxy
				.newProxyInstance(CometPublicationService.class,
						"http://localhost:7878/comet", "/comet-publish");
		Long sequenceNumber = service.publish(new CometEvent(
				ConversationSubscription.class.getName(),
				new ConversationMessage("TEST"), null));
		assertTrue("Invalid sequence number", sequenceNumber.longValue() > 0);

		// Stop the application
		WoofOfficeFloorSource.stop();
	}

}