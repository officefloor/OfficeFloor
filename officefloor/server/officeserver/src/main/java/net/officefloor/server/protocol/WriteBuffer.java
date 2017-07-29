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
package net.officefloor.server.protocol;

import java.nio.ByteBuffer;

/**
 * Buffer with data to write.
 * 
 * @author Daniel Sagenschneider
 */
public interface WriteBuffer {

	/**
	 * Obtains the type of this {@link WriteBuffer}.
	 * 
	 * @return Type of this {@link WriteBuffer}.
	 */
	WriteBufferEnum getType();

	/**
	 * Obtains the data to write.
	 * 
	 * @return Data to write. <code>null</code> if containing data in
	 *         {@link ByteBuffer}.
	 */
	byte[] getData();

	/**
	 * Obtains the number of bytes in the data to write.
	 * 
	 * @return Number of bytes in the data to write. <code>-1</code> if
	 *         providing data through {@link ByteBuffer}.
	 */
	int length();

	/**
	 * <p>
	 * Obtains the buffered data to write.
	 * <p>
	 * This allows the {@link ConnectionHandler} to cache content in direct
	 * {@link ByteBuffer} instances to write to the client.
	 * 
	 * @return {@link ByteBuffer} containing data to write.
	 */
	ByteBuffer getDataBuffer();

}