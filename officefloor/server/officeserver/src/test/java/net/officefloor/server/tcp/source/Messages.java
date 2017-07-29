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
package net.officefloor.server.tcp.source;

/**
 * Provides translations of messages.
 *
 * @author Daniel Sagenschneider
 */
public class Messages {

	/**
	 * Listing of messages.
	 */
	private static final String[] messages = new String[] { "Hello World",
			"Test", "OfficeFloor" };

	/**
	 * Obtains the number of messages.
	 *
	 * @return Number of messages.
	 */
	public static int getSize() {
		return messages.length;
	}

	/**
	 * Obtains the particular message.
	 *
	 * @param index
	 *            Index for the message.
	 * @return Message.
	 */
	public static String getMessage(int index) {
		return messages[index];
	}

}