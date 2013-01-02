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
package net.officefloor.plugin.socket.server.impl;

import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;

/**
 * {@link ByteBuffer} {@link WriteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferWriteBuffer implements WriteBuffer {

	/**
	 * {@link ByteBuffer} to write.
	 */
	private final ByteBuffer buffer;

	/**
	 * Initiate.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer} to write.
	 */
	public BufferWriteBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	/*
	 * ===================== WriteBuffer ============================
	 */

	@Override
	public WriteBufferEnum getType() {
		return WriteBufferEnum.BYTE_BUFFER;
	}

	@Override
	public byte[] getData() {
		return null;
	}

	@Override
	public int length() {
		return -1;
	}

	@Override
	public ByteBuffer getDataBuffer() {
		return this.buffer;
	}

}