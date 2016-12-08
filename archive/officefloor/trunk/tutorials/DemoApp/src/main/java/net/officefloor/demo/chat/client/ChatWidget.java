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
package net.officefloor.demo.chat.client;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * {@link Widget} providing Chat.
 * 
 * @author Daniel Sagenschneider
 */
public class ChatWidget extends VerticalPanel {

	/**
	 * {@link ConversationSubscription} publisher.
	 */
	private final ConversationSubscription publisher = OfficeFloorComet
			.createPublisher(ConversationSubscription.class);

	/**
	 * Message {@link TextBox}.
	 */
	private final TextBox messageText = new TextBox();

	/**
	 * Listing of users that are currently typing.
	 */
	private final List<String> typingUserNames = new LinkedList<String>();

	/**
	 * Flag indicating if the user is typing.
	 */
	private boolean isTyping = false;

	/**
	 * User name of the user.
	 */
	private String userName = null;

	/**
	 * Allows specifying the user name.
	 * 
	 * @param userName
	 *            User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Initiate.
	 */
	public ChatWidget() {

		// Provide means to send a message
		HorizontalPanel messagePanel = new HorizontalPanel();
		// messagePanel.setSpacing(10);
		messagePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.add(messagePanel);

		// Provide message text box
		this.messageText.setStylePrimaryName("message");
		messagePanel.add(this.messageText);
		this.messageText.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				ChatWidget.this.ensureHaveUserName();
			}
		});

		// Provide send button for message
		Button sendButton = new Button("send");
		sendButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ChatWidget.this.sendChatMessage();
			}
		});
		messagePanel.add(sendButton);

		// Provide messages in scroll area
		VerticalPanel messagesPanel = new VerticalPanel();
		messagesPanel.setSize("100%", "100%");
		ScrollPanel scrollMessagesPanel = new ScrollPanel(messagesPanel);
		scrollMessagesPanel.setStylePrimaryName("messages");
		this.add(scrollMessagesPanel);

		// Provide label to display typing users
		final Label typingLabel = new Label();
		typingLabel.setStylePrimaryName("typing");
		messagesPanel.add(typingLabel);
		typingLabel.setVisible(false); // initially hidden as no typing users

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
		messagesPanel.add(chatList);

		// Add the data
		final ListDataProvider<ChatMessage> chatEntries = new ListDataProvider<ChatMessage>();
		chatEntries.addDataDisplay(chatList);

		// Handle listening for messages
		OfficeFloorComet.subscribe(ConversationSubscription.class,
				new ConversationSubscription() {
					@Override
					public void message(ChatMessage message) {

						// Determine if typing notification
						String userName = message.getUserName();
						if ((ChatWidget.this.userName == null)
								|| (!(ChatWidget.this.userName.equals(userName)))) {
							// Not user so provide notification of typing
							if (message.isTyping()) {
								// Include user as typing
								if (!(ChatWidget.this.typingUserNames
										.contains(userName))) {
									ChatWidget.this.typingUserNames
											.add(userName);
								}
							} else {
								// User has stopped typing
								ChatWidget.this.typingUserNames
										.remove(userName);
							}
						}

						// Display typing notification
						if (ChatWidget.this.typingUserNames.size() == 0) {
							// No typing users
							typingLabel.setVisible(false);

						} else {
							// Display the typing users
							StringBuilder typingUsers = new StringBuilder();
							boolean isFirst = true;
							for (String typingUserName : ChatWidget.this.typingUserNames) {
								if (!isFirst) {
									typingUsers.append(", ");
								}
								isFirst = false;
								typingUsers.append(typingUserName);
							}
							typingUsers.append(" ... (typing)");
							typingLabel.setText(typingUsers.toString());
							typingLabel.setVisible(true);
						}

						// Add the message (if one provided)
						String text = message.getMessage();
						if (text != null) {
							List<ChatMessage> list = chatEntries.getList();
							if (list.size() == 0) {
								list.add(message);
							} else {
								list.add(0, message);
							}
						}
					}
				}, null);

		// Handle submitting a message
		this.messageText.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {

				// Obtain the message text and cursor position
				String message = ChatWidget.this.messageText.getText();
				int cursorPosition = ChatWidget.this.messageText.getCursorPos();

				// Handle various scenarios of keys for sending/notification
				switch (event.getNativeKeyCode()) {
				case KeyCodes.KEY_ENTER:
					ChatWidget.this.sendChatMessage();
					break;

				case KeyCodes.KEY_BACKSPACE:
					if (ChatWidget.this.isTyping && (message.length() == 1)
							&& (cursorPosition == 1)) {
						// No longer typing as deleting last character
						ChatWidget.this.sendChatMessage(false, null);
					}
					break;

				case KeyCodes.KEY_DELETE:
					if (ChatWidget.this.isTyping && (message.length() == 1)
							&& (cursorPosition == 0)) {
						// No longer typing as deleting last character
						ChatWidget.this.sendChatMessage(false, null);
					}
					break;

				case KeyCodes.KEY_ALT:
				case KeyCodes.KEY_CTRL:
				case KeyCodes.KEY_DOWN:
				case KeyCodes.KEY_END:
				case KeyCodes.KEY_ESCAPE:
				case KeyCodes.KEY_HOME:
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_PAGEDOWN:
				case KeyCodes.KEY_PAGEUP:
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_SHIFT:
				case KeyCodes.KEY_UP:
					// Do nothing
					break;

				default:
					if ((!ChatWidget.this.isTyping) && (message.length() == 0)) {
						// Started typing a message
						ChatWidget.this.sendChatMessage(true, null);
					}
					break;
				}
			}
		});
	}

	/**
	 * Sends the {@link ChatMessage}.
	 * 
	 * @param isTyping
	 *            Indicates if user is typing.
	 * @param message
	 *            Message text.
	 */
	private void sendChatMessage(boolean isTyping, String message) {

		// Ensure have the user name
		this.ensureHaveUserName();

		// Sends the chat message
		this.publisher
				.message(new ChatMessage(this.userName, isTyping, message));

		// Flag whether typing
		this.isTyping = isTyping;
	}

	/**
	 * Sends the {@link ChatMessage} with message text.
	 */
	private void sendChatMessage() {

		// Ensure have message to send
		String message = this.messageText.getText();
		if ((message == null) || (message.trim().length() == 0)) {
			return;
		}

		// Send the chat message (user has stopped typing)
		this.sendChatMessage(false, message);
		this.messageText.setText(""); // clear as sent
	}

	/**
	 * Ensures that have user name.
	 */
	private void ensureHaveUserName() {

		// Determine if user name already specified
		if ((this.userName != null) && (this.userName.trim().length() > 0)) {
			return; // user name specified
		}

		// No user name so obtain the user name
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
					ChatWidget.this.enterChatName(nameText, userDialog);
				}
			}
		});
		userPanel.add(nameText);
		Button okButton = new Button("ok");
		userPanel.add(okButton);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ChatWidget.this.enterChatName(nameText, userDialog);
			}
		});
		userDialog.center();
		nameText.setFocus(true);
	}

	/**
	 * Enters the chat name.
	 * 
	 * @param nameText
	 *            {@link TextBox} containing the name.
	 * @param userDialog
	 *            {@link DialogBox} to obtain the name.
	 */
	private void enterChatName(TextBox nameText, DialogBox userDialog) {

		// Ensure user name provided
		String name = nameText.getText();
		if ((name == null) || (name.trim().length() == 0)) {
			Window.alert("Must provide name!");
			return;
		}

		// Name provided
		this.userName = name;
		userDialog.hide();

		// Provide focus for writing message
		this.messageText.setFocus(true);
	}

}