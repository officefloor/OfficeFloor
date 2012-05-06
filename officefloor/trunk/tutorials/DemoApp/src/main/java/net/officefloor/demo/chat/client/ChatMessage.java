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
package net.officefloor.demo.chat.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Chat message.
 * 
 * @author Daniel Sagenschneider
 */
public class ChatMessage implements IsSerializable {

	private String userName;

	private String message;

	public ChatMessage() {
	}

	public ChatMessage(String userName, String message) {
		this.userName = userName;
		this.message = message;
	}

	public String getUserName() {
		return this.userName;
	}

	public String getMessage() {
		return this.message;
	}

}