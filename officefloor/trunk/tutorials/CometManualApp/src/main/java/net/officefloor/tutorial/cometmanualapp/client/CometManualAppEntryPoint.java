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
package net.officefloor.tutorial.cometmanualapp.client;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Comet Manual App {@link EntryPoint}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class CometManualAppEntryPoint implements EntryPoint {

	/**
	 * {@link UserNameService}.
	 */
	private final UserNameServiceAsync userNameService = GWT
			.create(UserNameService.class);

	@Override
	public void onModuleLoad() {

		// Vertically align contents
		RootPanel panel = RootPanel.get("chat");
		VerticalPanel chatPanel = new VerticalPanel();
		panel.add(chatPanel);

		// Provide dialog box for user name
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setModal(true);
		HorizontalPanel userNamePanel = new HorizontalPanel();
		dialogBox.add(userNamePanel);
		userNamePanel.add(new Label("Enter user name: "));
		final TextBox userNameTextBox = new TextBox();
		userNamePanel.add(userNameTextBox);
		Button userNameSubmit = new Button("submit");
		userNamePanel.add(userNameSubmit);
		dialogBox.show();
		dialogBox.center();
		userNameSubmit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CometManualAppEntryPoint.this.userNameService.login(
						userNameTextBox.getText(), new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								dialogBox.hide();
							}

							@Override
							public void onFailure(Throwable caught) {
								Window.alert("Failed to specify user name");
							}
						});
			}
		});

		// Provide the text area to contain conversation
		final TextArea conversation = new TextArea();
		conversation.setReadOnly(true);
		conversation.setSize("100%", "300px");
		chatPanel.add(conversation);

		// Handle listening for messages
		OfficeFloorComet.subscribe(ConversationSubscription.class,
				new ConversationSubscription() {
					@Override
					public void message(ConversationMessage message) {
						conversation.setText(conversation.getText() + "\n"
								+ message.getName() + ": " + message.getText());
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
				publisher.message(new ConversationMessage(messageText));
			}
		});
	}
}
// END SNIPPET: example