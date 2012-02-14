/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.stream.squirtfactory;

import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Heap {@link ByteBuffer} {@link BufferSquirtFactory}.
 *
 * @author Daniel Sagenschneider
 */
public class HeapByteBufferSquirtFactory implements BufferSquirtFactory {

	/**
	 * Size of the {@link ByteBuffer} instances.
	 */
	private final int bufferSize;

	/**
	 * Initiate.
	 *
	 * @param bufferSize
	 *            Size of the {@link ByteBuffer} instances.
	 */
	public HeapByteBufferSquirtFactory(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/*
	 * =================== BufferSquirtFactory ==============================
	 */

	@Override
	public BufferSquirt createBufferSquirt() {
		return new BufferSquirtImpl(ByteBuffer.allocate(this.bufferSize));
	}

}