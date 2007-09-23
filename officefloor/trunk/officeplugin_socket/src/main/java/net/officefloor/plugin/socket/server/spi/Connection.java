/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.spi;

/**
 * <p>
 * Connection with the {@link net.officefloor.plugin.socket.server.spi.Server}.
 * <p>
 * Provided by the Server Socket plugin.
 * 
 * @author Daniel
 */
public interface Connection {

	/**
	 * Creates a new {@link ReadMessage} to listen to the client.
	 * 
	 * @return New {@link ReadMessage} to listen to the client.
	 */
	ReadMessage createReadMessage();

	/**
	 * Creates a new {@link WriteMessage} to send to the client.
	 * 
	 * @param listener
	 *            {@link WriteMessageListener} for the new {@link WriteMessage}.
	 *            May be <code>null</code> if no listening required.
	 * @return New {@link WriteMessage} to send to the client.
	 */
	WriteMessage createWriteMessage(WriteMessageListener listener);

}
