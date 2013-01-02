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

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.tutorial.cometmanualapp.client.ConversationMessage;
import net.officefloor.tutorial.cometmanualapp.client.ConversationSubscription;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	public void login(@Parameter String userName, User user,
			AsyncCallback<Void> callback) {

		user.setName(userName);
		callback.onSuccess(null);
	}

	public void message(@Parameter CometEvent event, User user,
			ConversationSubscription publisher, AsyncCallback<Long> callback) {

		ConversationMessage message = (ConversationMessage) event.getData();
		publisher.message(new ConversationMessage(user.getName(), message
				.getText()));
		callback.onSuccess(Long.valueOf(1));
	}
}
// END SNIPPET: example