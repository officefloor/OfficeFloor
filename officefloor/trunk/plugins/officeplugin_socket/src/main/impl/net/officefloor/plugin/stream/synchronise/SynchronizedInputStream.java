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

package net.officefloor.plugin.stream.synchronise;

import java.io.IOException;
import java.io.InputStream;

/**
 * Synchronized {@link InputStream}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchronizedInputStream extends InputStream {

	/**
	 * Backing {@link InputStream}.
	 */
	private final InputStream backingStream;

	/**
	 * Mutex to synchronise on.
	 */
	private final Object mutex;

	/**
	 * Initiate.
	 *
	 * @param backingStream
	 *            Backing {@link InputStream}.
	 * @param mutex
	 *            Mutex to synchronise on.
	 */
	public SynchronizedInputStream(InputStream backingStream, Object mutex) {
		this.backingStream = backingStream;
		this.mutex = mutex;
	}

	/*
	 * ======================== InputStream ==================================
	 */

	@Override
	public int read() throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read();
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(b);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.read(b, off, len);
		}
	}

	@Override
	public long skip(long n) throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.skip(n);
		}
	}

	@Override
	public int available() throws IOException {
		synchronized (this.mutex) {
			return this.backingStream.available();
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this.mutex) {
			this.backingStream.close();
		}
	}

}