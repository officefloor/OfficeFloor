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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Chat message.
 * 
 * @author Daniel Sagenschneider
 */
public class ChatMessage implements IsSerializable {

	/**
	 * User name of user sending the {@link ChatMessage}.
	 */
	private String userName;

	/**
	 * Flag indicating if the user is typing.
	 */
	private boolean isTyping;

	/**
	 * Message text sent. May be <code>null</code> if user start typing
	 * notification.
	 */
	private String message;

	/**
	 * Default constructor for {@link IsSerializable}.
	 */
	public ChatMessage() {
	}

	/**
	 * Initiate.
	 * 
	 * @param userName
	 *            User name of user sending the {@link ChatMessage}.
	 * @param isTyping
	 *            Flag indicating if the user is typing.
	 * @param message
	 *            Message text sent. May be <code>null</code> if user start
	 *            typing notification.
	 */
	public ChatMessage(String userName, boolean isTyping, String message) {
		this.userName = userName;
		this.isTyping = isTyping;
		this.message = message;
	}

	/**
	 * Obtains the user name of user sending the {@link ChatMessage}.
	 * 
	 * @return User name of user sending the {@link ChatMessage}.
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Indicates if the user is typing.
	 * 
	 * @return <code>true</code> if the user is typing.
	 */
	public boolean isTyping() {
		return this.isTyping;
	}

	/**
	 * Obtains the message text sent.
	 * 
	 * @return Message text sent. May be <code>null</code> if a user start
	 *         typing notification.
	 */
	public String getMessage() {
		return this.message;
	}

}