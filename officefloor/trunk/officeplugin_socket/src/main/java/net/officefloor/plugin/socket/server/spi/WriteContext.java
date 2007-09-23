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
 * Context for handling a potential write.
 * 
 * @author Daniel
 */
public interface WriteContext {

	/**
	 * Obtains the {@link WriteMessage} that is to be written to the
	 * {@link java.nio.channels.SocketChannel}.
	 * 
	 * @return {@link WriteMessage} that is to be written to the
	 *         {@link java.nio.channels.SocketChannel}.
	 */
	WriteMessage getWriteMessage();
	
	/**
	 * Flags to close the {@link Connection}.
	 * 
	 * @param isClose
	 *            <code>true</code> to close the {@link Connection}.
	 */
	void setCloseConnection(boolean isClose);

}
