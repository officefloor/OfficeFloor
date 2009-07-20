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
 * {@link Message} for reading from the client.
 * 
 * @author Daniel Sagenschneider
 */
public interface ReadMessage extends Message {

	/**
	 * Reads data from the {@link ReadMessage} into the input buffer.
	 * 
	 * @param buffer
	 *            Buffer to load the data.
	 * @return Number of bytes loaded to the buffer.
	 */
	int read(byte[] buffer);

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
	 * @return Number of bytes loaded to the buffer.
	 */
	int read(byte[] buffer, int offset, int length);

	/**
	 * Indicates if data is available to be read from this {@link ReadMessage}.
	 * 
	 * @return <code>true</code> if data is available.
	 */
	boolean isDataAvailable();

	/**
	 * Obtains the next {@link ReadMessage}.
	 * 
	 * @return Next {@link ReadMessage} or <code>null</code> if no further
	 *         {@link ReadMessage} instances.
	 */
	ReadMessage getNextReadMessage();

}
