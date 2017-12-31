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
package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.ServerOutputStream;

/**
 * {@link ProcessAwareContext} wrapping {@link ServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerOutputStream extends ServerOutputStream {

	/**
	 * Unsafe {@link OutputStream}.
	 */
	private final ServerOutputStream unsafeOutputStream;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

	/**
	 * Instantiate.
	 * 
	 * @param unsafeOutputStream
	 *            Unsafe {@link ServerOutputStream}.
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public ProcessAwareServerOutputStream(ServerOutputStream unsafeOutputStream, ProcessAwareContext context) {
		this.unsafeOutputStream = unsafeOutputStream;
		this.context = context;
	}

	/**
	 * Function interface to define an operation with no return (void return).
	 */
	private static interface SafeVoidOperation<T extends Throwable> {

		/**
		 * Undertake operation.
		 * 
		 * @throws T
		 *             Possible failure from operation.
		 */
		void run() throws T;
	}

	/**
	 * Wraps execution to be {@link ProcessState} ({@link Thread}) safe.
	 * 
	 * @param operation
	 *            {@link ProcessSafeOperation}.
	 * @return Result of {@link ProcessSafeOperation}.
	 * @throws T
	 *             If {@link ProcessSafeOperation} fails.
	 */
	private <T extends Throwable> void safe(SafeVoidOperation<T> operation) throws T {
		this.context.run(() -> {
			operation.run();
			return null; // void return
		});
	}

	/*
	 * ==================== ServerOutputStream ===================
	 */

	@Override
	public void write(ByteBuffer buffer) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(buffer));
	}

	@Override
	public void write(FileChannel file, long position, long count, FileCompleteCallback callback) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(file, position, count, callback));

	}

	@Override
	public void write(FileChannel file, FileCompleteCallback callback) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(file, callback));
	}

	@Override
	public void write(int b) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(b));
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(b));
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.safe(() -> this.unsafeOutputStream.write(b, off, len));
	}

	@Override
	public void flush() throws IOException {
		this.safe(() -> this.unsafeOutputStream.flush());
	}

	@Override
	public void close() throws IOException {
		this.safe(() -> this.unsafeOutputStream.close());
	}

}