/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.spi;

/**
 * Context that is available to all handle methods for the
 * {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionHandlerContext {

	/**
	 * Flags to close the {@link Connection}.
	 * 
	 * @param isClose
	 *            <code>true</code> to close the {@link Connection}.
	 */
	void setCloseConnection(boolean isClose);

	/**
	 * <p>
	 * Obtains the current time in milliseconds.
	 * <p>
	 * This should return similar to {@link System#currentTimeMillis()} but is
	 * provided to cache time for multiple quick operations that require only
	 * estimates of time.
	 * <p>
	 * Note CPU operations should be in the nano-seconds.
	 * 
	 * @return Time measured in milliseconds.
	 */
	long getTime();

}
