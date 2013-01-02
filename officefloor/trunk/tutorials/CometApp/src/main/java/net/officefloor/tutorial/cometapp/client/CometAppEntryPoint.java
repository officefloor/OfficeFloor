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
package net.officefloor.tutorial.cometapp.client;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Comet App {@link EntryPoint}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class CometAppEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {

		// Vertically align contents
		RootPanel panel = RootPanel.get("chat");
		VerticalPanel chatPanel = new VerticalPanel();
		panel.add(chatPanel);

		// Provide the text area to contain conversation
		final TextArea conversation = new TextArea();
		conversation.setReadOnly(true);
		conversation.setSize("100%", "300px");
		chatPanel.add(conversation);

		// Handle listening for messages
		OfficeFloorComet.subscribe(ConversationSubscription.class,
				new ConversationSubscription() {
					@Override
					public void sendMessage(String message) {
						conversation.setText(conversation.getText() + "\n"
								+ message);
					}
				}, null);

		// Provide means to add message
		HorizontalPanel messagePanel = new HorizontalPanel();
		chatPanel.add(messagePanel);
		final TextBox message = new TextBox();
		message.setWidth("80%");
		messagePanel.add(message);
		Button send = new Button("send");
		messagePanel.add(send);

		// Handle submitting a message
		final ConversationSubscription publisher = OfficeFloorComet
				.createPublisher(ConversationSubscription.class);
		send.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String messageText = message.getText();
				publisher.sendMessage(messageText);
			}
		});
	}

}
// END SNIPPET: example