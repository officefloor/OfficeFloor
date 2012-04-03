/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.tutorial.featureapp.client;

import java.util.List;

import net.officefloor.plugin.comet.api.OfficeFloorComet;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Feature App {@link EntryPoint}.
 * 
 * @author Daniel Sagenschneider
 */
public class FeatureAppEntryPoint implements EntryPoint {

	private String userName = null;

	@Override
	public void onModuleLoad() {

		// Vertically align contents
		RootPanel panel = RootPanel.get("chat");
		VerticalPanel chatPanel = new VerticalPanel();
		panel.add(chatPanel);

		// Provide means to add message
		HorizontalPanel messagePanel = new HorizontalPanel();
		messagePanel.setSpacing(10);
		messagePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		chatPanel.add(messagePanel);
		final TextBox messageText = new TextBox();
		messageText.setStylePrimaryName("message");
		messagePanel.add(messageText);
		Button send = new Button("send");
		messagePanel.add(send);

		// List to contain chat messages
		Cell<ChatMessage> chatCell = new AbstractCell<ChatMessage>() {
			@Override
			public void render(Context context, ChatMessage value,
					SafeHtmlBuilder sb) {
				sb.appendHtmlConstant("<span><b>");
				sb.appendEscaped(value.getUserName());
				sb.appendHtmlConstant("</b>");
				sb.appendEscaped(" > ");
				sb.appendEscaped(value.getMessage());
				sb.appendHtmlConstant("</span>");
			}
		};
		CellList<ChatMessage> chatList = new CellList<ChatMessage>(chatCell);
		chatPanel.add(chatList);

		// Add the data
		final ListDataProvider<ChatMessage> chatEntries = new ListDataProvider<ChatMessage>();
		chatEntries.addDataDisplay(chatList);

		// Handle listening for messages
		OfficeFloorComet.subscribe(ConversationSubscription.class,
				new ConversationSubscription() {
					@Override
					public void sendMessage(ChatMessage message) {
						// Add the message
						List<ChatMessage> list = chatEntries.getList();
						if (list.size() == 0) {
							list.add(message);
						} else {
							list.add(0, message);
						}
					}
				}, null);

		// Handle submitting a message
		final ConversationSubscription publisher = OfficeFloorComet
				.createPublisher(ConversationSubscription.class);
		messageText.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
					FeatureAppEntryPoint.this.sendChatMessage(messageText,
							publisher);
				}
			}
		});
		send.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FeatureAppEntryPoint.this.sendChatMessage(messageText,
						publisher);
			}
		});
	}

	/**
	 * Sends the chat message.
	 * 
	 * @param messageText
	 *            {@link TextBox} containing the message.
	 * @param publisher
	 *            {@link ConversationSubscription}.
	 */
	private void sendChatMessage(final TextBox messageText,
			final ConversationSubscription publisher) {

		// Obtain the user name
		String user = FeatureAppEntryPoint.this.userName;
		if ((user == null) || (user.trim().length() == 0)) {
			// Obtain user name
			final DialogBox userDialog = new DialogBox(false, true);
			HorizontalPanel userPanel = new HorizontalPanel();
			userPanel.setSpacing(10);
			userPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			userDialog.add(userPanel);
			userPanel.add(new Label("Enter name for chat"));
			final TextBox nameText = new TextBox();
			nameText.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
						FeatureAppEntryPoint.this.enterChatName(nameText,
								userDialog, messageText, publisher);
					}
				}
			});
			userPanel.add(nameText);
			Button okButton = new Button("ok");
			userPanel.add(okButton);
			okButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					FeatureAppEntryPoint.this.enterChatName(nameText,
							userDialog, messageText, publisher);
				}
			});
			userDialog.center();
			nameText.setFocus(true);
			return; // message sent on ok
		}

		// Send the chat message
		String message = messageText.getText();
		publisher.sendMessage(new ChatMessage(user, message));
		messageText.setText(""); // clear as sent
	}

	/**
	 * Enters the chat name.
	 * 
	 * @param nameText
	 *            {@link TextBox} containing the name.
	 * @param userDialog
	 *            {@link DialogBox} to obtain the name.
	 * @param messageText
	 *            {@link TextBox} containing the message.
	 * @param publisher
	 *            {@link ConversationSubscription}.
	 */
	private void enterChatName(TextBox nameText, DialogBox userDialog,
			TextBox messageText, ConversationSubscription publisher) {

		// Ensure user name provided
		String name = nameText.getText();
		if ((name == null) || (name.trim().length() == 0)) {
			Window.alert("Must provide name!");
			return;
		}

		// Name provided
		FeatureAppEntryPoint.this.userName = name;
		userDialog.hide();

		// Send the chat message
		String message = messageText.getText();
		publisher.sendMessage(new ChatMessage(name, message));
		messageText.setText(""); // clear as sent
	}

}