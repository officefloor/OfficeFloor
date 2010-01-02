/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.stream.synchronise;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Synchronized {@link OutputBufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchronizedOutputBufferStream implements OutputBufferStream {

	/**
	 * Backing {@link OutputBufferStream}.
	 */
	private final OutputBufferStream backingStream;

	/**
	 * Mutex for synchronising on.
	 */
	private final Object mutex;

	/**
	 * Initiate.
	 *
	 * @param backingStream
	 *            Backing {@link OutputBufferStream}.
	 * @param mutex
	 *            Mutex for synchronising on.
	 */
	public SynchronizedOutputBufferStream(OutputBufferStream backingStream,
			Object mutex) {
		this.backingStream = backingStream;
		this.mutex = mutex;
	}

	/*
	 * =================== OutputBufferStream ==============================
	 */

	@Override
	public OutputStream getOutputStream() {
		synchronized (this.mutex) {
			return new SynchronizedOutputStream(this.backingStream
					.getOutputStream(), this.mutex);
		}
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(bytes);
		}
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(data, offset, length);
		}
	}

	@Override
	public void write(BufferPopulator populator) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(populator);
		}
	}

	@Override
	public void append(ByteBuffer buffer) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.append(buffer);
		}
	}

	@Override
	public void append(BufferSquirt squirt) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.append(squirt);
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this.mutex) {
			this.backingStream.close();
		}
	}

}