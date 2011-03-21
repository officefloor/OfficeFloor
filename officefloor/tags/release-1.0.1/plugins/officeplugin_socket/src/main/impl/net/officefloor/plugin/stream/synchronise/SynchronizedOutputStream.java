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
import java.io.OutputStream;

/**
 * Synchronized {@link OutputStream}.
 *
 * @author Daniel Sagenschneider
 */
public class SynchronizedOutputStream extends OutputStream {

	/**
	 * Backing {@link OutputStream}.
	 */
	private final OutputStream backingStream;

	/**
	 * Mutex to synchronise on.
	 */
	private final Object mutex;

	/**
	 * Initiate.
	 *
	 * @param backingStream
	 *            Backing {@link OutputStream}.
	 * @param mutex
	 *            Mutex to synchronise on.
	 */
	public SynchronizedOutputStream(OutputStream backingStream, Object mutex) {
		this.backingStream = backingStream;
		this.mutex = mutex;
	}

	/*
	 * ================== OutputStream ============================
	 */

	@Override
	public void write(int b) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		synchronized (this.mutex) {
			this.backingStream.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		synchronized (this.mutex) {
			this.backingStream.flush();
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this.mutex) {
			this.backingStream.close();
		}
	}

}