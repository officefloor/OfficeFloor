/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Synchronized {@link BufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchronizedBufferStream implements BufferStream {

	/**
	 * Backing {@link BufferStream}.
	 */
	private final BufferStream backingStream;

	/**
	 * Mutex to synchronise on.
	 */
	private final Object mutex;

	/**
	 * Initiate.
	 *
	 * @param backingStream
	 *            Backing {@link BufferStream}.
	 * @param mutex
	 *            Mutex to synchronise on.
	 */
	public SynchronizedBufferStream(BufferStream backingStream, Object mutex) {
		this.backingStream = backingStream;
		this.mutex = mutex;
	}

	/*
	 * ================== BufferStream ==============================
	 */

	@Override
	public OutputBufferStream getOutputBufferStream() {
		synchronized (this.mutex) {
			return new SynchronizedOutputBufferStream(this.backingStream
					.getOutputBufferStream(), this.mutex);
		}
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(bytes);
		}
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(bytes, offset, length);
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
	public void closeOutput() {
		synchronized (this.mutex) {
			this.backingStream.closeOutput();
		}
	}

	@Override
	public InputBufferStream getInputBufferStream() {
		synchronized (this.mutex) {
			return new SynchronizedInputBufferStream(this.backingStream
					.getInputBufferStream(), this.mutex);
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
	public void closeInput() {
		synchronized (this.mutex) {
			this.backingStream.closeInput();
		}
	}

}