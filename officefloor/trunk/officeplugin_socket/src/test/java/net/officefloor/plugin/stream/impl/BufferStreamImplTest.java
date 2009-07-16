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
package net.officefloor.plugin.stream.impl;

import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.AbstractBufferStreamTest;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;
import net.officefloor.plugin.stream.BufferStream;

/**
 * Tests the {@link BufferStreamImpl}.
 *
 * @author Daniel Sagenschneider
 */
public class BufferStreamImplTest extends AbstractBufferStreamTest implements
		BufferSquirtFactory {

	@Override
	protected BufferStream createBufferStream() {
		return new BufferStreamImpl(this);
	}

	@Override
	public BufferSquirt createBufferSquirt() {
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		return new BufferSquirt() {
			@Override
			public ByteBuffer getBuffer() {
				return buffer;
			}

			@Override
			public void close() {
				// TODO Implement BufferSquirt.close
				throw new UnsupportedOperationException("BufferSquirt.close");
			}
		};
	}

}