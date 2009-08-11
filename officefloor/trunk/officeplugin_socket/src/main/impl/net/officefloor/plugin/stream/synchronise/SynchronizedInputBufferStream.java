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
package net.officefloor.plugin.stream.synchronise;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Synchronised {@link InputBufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchronizedInputBufferStream implements InputBufferStream {

	/**
	 * Backing {@link InputBufferStream}.
	 */
	private final InputBufferStream backingStream;

	/**
	 * Mutex to synchronise on.
	 */
	private final Object mutex;

	/**
	 * Initiate.
	 *
	 * @param backingStream
	 *            Backing {@link InputBufferStream}.
	 * @param mutex
	 *            Mutex to synchronise on.
	 */
	public SynchronizedInputBufferStream(InputBufferStream backingStream,
			Object mutex) {
		this.backingStream = backingStream;
		this.mutex = mutex;
	}

	/*
	 * =============== InputBufferStream ==================================
	 */

	@Override
	public InputStream getInputStream() {
		synchronized (this.mutex) {
			return new SynchronizedInputStream(this.backingStream
					.getInputStream(), this.mutex);
		}
	}

	@Override
	public InputStream getBrowseStream() {
		synchronized (this.mutex) {
			return new SynchronizedInputStream(this.backingStream
					.getBrowseStream(), this.mutex);
		}
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(readBuffer);
		}
	}

	@Override
	public int read(byte[] readBuffer, int offset, int length)
			throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(readBuffer, offset, length);
		}
	}

	@Override
	public int read(BufferProcessor processor) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(processor);
		}
	}

	@Override
	public int read(int numberOfBytes, GatheringBufferProcessor processor)
			throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(numberOfBytes, processor);
		}
	}

	@Override
	public int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(numberOfBytes, outputBufferStream);
		}
	}

	@Override
	public long skip(long numberOfBytes) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.skip(numberOfBytes);
		}
	}

	@Override
	public long available() {
		synchronized (this.mutex) {
			return this.backingStream.available();
		}
	}

	@Override
	public void close() {
		synchronized (this.mutex) {
			this.backingStream.close();
		}
	}

}