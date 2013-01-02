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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Message as part of a conversation.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class ConversationMessage implements IsSerializable {

	private String name;

	private String text;

	public ConversationMessage(String name, String text) {
		this.name = name;
		this.text = text;
	}

	public ConversationMessage(String text) {
		this(null, text);
	}
	
	public ConversationMessage() {
	}

	public String getName() {
		return this.name;
	}

	public String getText() {
		return this.text;
	}

}
// END SNIPPET: example