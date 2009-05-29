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

import java.nio.channels.SocketChannel;

/**
 * <p>
 * Handler for a {@link Connection}.
 * <p>
 * Required to be implemented by the handler provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionHandler {

	/**
	 * Handles a read from the {@link SocketChannel}.
	 * 
	 * @param context
	 *            {@link ReadContext}.
	 */
	void handleRead(ReadContext context);

	/**
	 * Handles a potential write to the {@link SocketChannel}.
	 * 
	 * @param context
	 *            {@link WriteContext}.
	 */
	void handleWrite(WriteContext context);

	/**
	 * Handles a {@link Connection} being idled.
	 * 
	 * @param context
	 *            {@link IdleContext}.
	 */
	void handleIdleConnection(IdleContext context);

}