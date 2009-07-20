/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.spi;

/**
 * Listener to a {@link net.officefloor.plugin.socket.server.spi.WriteMessage}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WriteMessageListener {

	/**
	 * Indicates the {@link WriteMessage} has been written to the client.
	 * 
	 * @param message
	 *            {@link WriteMessage} that has been written to the client.
	 */
	void messageWritten(WriteMessage message);

}
