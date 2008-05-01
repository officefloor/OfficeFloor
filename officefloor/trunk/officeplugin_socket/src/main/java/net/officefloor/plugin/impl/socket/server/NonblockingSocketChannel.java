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
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Interface encapsulating the {@link SocketChannel} to provide the necessary
 * non-blocking {@link Socket} operations.
 * 
 * @author Daniel
 */
public interface NonblockingSocketChannel {

	/**
	 * Registers the underlying {@link SocketChannel} with the input
	 * {@link Selector}.
	 * 
	 * @param selector
	 *            {@link Selector}.
	 * @param ops
	 *            Operations as per {@link SelectionKey#interestOps()}.
	 * @param attachment
	 *            Attachment for the {@link SelectionKey}.
	 * @return {@link SelectionKey} of the resulting registration.
	 * @throws IOException
	 *             If fails to register.
	 */
	SelectionKey register(Selector selector, int ops, Object attachment)
			throws IOException;

	/**
	 * Reads data from the {@link SocketChannel}.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer} to write data.
	 * @return Number of bytes read.
	 * @throws IOException
	 *             If fails to read.
	 */
	int read(ByteBuffer buffer) throws IOException;

	/**
	 * Writes the data to the {@link SocketChannel}.
	 * 
	 * @param data
	 *            {@link ByteBuffer} with the data to write.
	 * @return Number of bytes written.
	 * @throws IOException
	 *             If fails to write.
	 */
	int write(ByteBuffer data) throws IOException;

	/**
	 * Closes the underlying {@link SocketChannel}.
	 * 
	 * @throws IOException
	 *             If fails to close.
	 */
	void close() throws IOException;

}
