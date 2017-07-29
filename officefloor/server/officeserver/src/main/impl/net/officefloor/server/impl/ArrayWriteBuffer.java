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
package net.officefloor.server.impl;

import java.nio.ByteBuffer;

import net.officefloor.server.protocol.WriteBuffer;
import net.officefloor.server.protocol.WriteBufferEnum;

/**
 * {@link WriteBuffer} containing an array of bytes to write.
 * 
 * @author Daniel Sagenschneider
 */
public class ArrayWriteBuffer implements WriteBuffer {

	/**
	 * Data.
	 */
	private final byte[] data;

	/**
	 * Length of data to write.
	 */
	private final int length;

	/**
	 * Initiate.
	 * 
	 * @param data
	 *            Data.
	 * @param length
	 *            Length of data to write.
	 */
	public ArrayWriteBuffer(byte[] data, int length) {
		this.data = data;
		this.length = length;
	}

	/*
	 * ================== WriteBuffer =======================
	 */

	@Override
	public WriteBufferEnum getType() {
		return WriteBufferEnum.BYTE_ARRAY;
	}

	@Override
	public byte[] getData() {
		return this.data;
	}

	@Override
	public int length() {
		return this.length;
	}

	@Override
	public ByteBuffer getDataBuffer() {
		return null;
	}

}