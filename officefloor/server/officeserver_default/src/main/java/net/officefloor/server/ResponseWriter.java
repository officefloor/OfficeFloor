/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server;

import java.net.Socket;
import java.nio.ByteBuffer;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Writes the response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResponseWriter {

	/**
	 * Writes the {@link StreamBuffer} instances as the response.
	 * 
	 * @param responseBuffers
	 *            Response contained in the {@link StreamBuffer} instances. Once
	 *            the {@link StreamBuffer} is written back to the
	 *            {@link Socket}, it is released back to its {@link BufferPool}.
	 */
	void write(Iterable<StreamBuffer<ByteBuffer>> responseBuffers);

}