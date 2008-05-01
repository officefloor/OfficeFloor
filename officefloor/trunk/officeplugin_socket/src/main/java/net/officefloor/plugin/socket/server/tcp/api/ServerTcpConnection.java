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
package net.officefloor.plugin.socket.server.tcp.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.sun.jmx.snmp.tasks.Task;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * TCP connection to be handled by the {@link OfficeFloor}.
 * <p>
 * An {@link OutputStream} is provided to write out the data. An
 * {@link InputStream} however is <b>NOT</b> provided due to the blocking
 * method {@link InputStream#read()}.
 * 
 * @author Daniel
 */
public interface ServerTcpConnection {

	/**
	 * Obtains the lock that may be <code>synchronized</code> on to reduce
	 * locking overhead of making multiple calls on this
	 * {@link ServerTcpConnection} (ie do course locking).
	 * 
	 * @return Lock that governs this {@link ServerTcpConnection}.
	 */
	Object getLock();

	/**
	 * Reads data from the connection into the input buffer.
	 * 
	 * @param buffer
	 *            Buffer to load the data.
	 * @return Number of bytes loaded to the buffer. <code>-1</code> if
	 *         connection is closed.
	 * @throws IOException
	 *             If fails to read data.
	 */
	int read(byte[] buffer) throws IOException;

	/**
	 * Reads data from the connection into the input buffer, starting at the
	 * offset for length bytes.
	 * 
	 * @param buffer
	 *            Buffer to load the data.
	 * @param offset
	 *            Offset into the buffer to start loading the data.
	 * @param length
	 *            Number of bytes to load into the buffer.
	 * @return Number of bytes loaded to the buffer. <code>-1</code> if
	 *         connection is closed.
	 * @throws IOException
	 *             If fails to read data.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException;

	/**
	 * <p>
	 * Flags for the {@link ManagedObject} to stop execution of the {@link Task}
	 * again until further data is received from the client.
	 * <p>
	 * On calling this, the next time a {@link Task} is invoked using this
	 * {@link ManagedObject} data will be available from a <code>read</code>
	 * method.
	 * 
	 * @throws IOException
	 *             If fails to initiate waiting on client.
	 */
	void waitOnClientData() throws IOException;

	/**
	 * <p>
	 * Obtains the {@link OutputStream} to write data back to the client.
	 * <p>
	 * Much like {@link SocketChannel}, calling {@link OutputStream#close()}
	 * will close this {@link ServerTcpConnection}.
	 * <p>
	 * Further to this, {@link OutputStream#flush()} is non-blocking and only
	 * indicates to start writing out the content so far (which is being
	 * buffered until it is called).
	 * 
	 * @return {@link OutputStream} to write data back to the client.
	 * @throws IOException
	 *             If fails to obtain, or this {@link ServerTcpConnection} is
	 *             closed.
	 */
	OutputStream getOutputStream() throws IOException;

	/**
	 * <p>
	 * Writes the {@link ByteBuffer} to the client.
	 * <p>
	 * This allows for {@link ByteBuffer} optimisations.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @throws IOException
	 *             If fails to write {@link ByteBuffer}.
	 */
	void write(ByteBuffer buffer) throws IOException;

	/**
	 * Indicates if connection is closed.
	 * 
	 * @return <code>true</code> if the connection is closed.
	 */
	boolean isClosed();

	/**
	 * <p>
	 * Closes the connection.
	 * <p>
	 * Connection is closed after all writes are complete. Further more this
	 * flags for the {@link ManagedObject} to stop execution of the {@link Task}
	 * again until all writes are complete.
	 * 
	 * 
	 * @throws IOException
	 *             If fails to close the connection.
	 */
	void close() throws IOException;

}
