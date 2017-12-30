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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link ProcessAwareContext} writing {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerWriter extends ServerWriter {

	/**
	 * Unsafe {@link ServerWriter}.
	 */
	private final ServerWriter unsafeServerWriter;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

	/**
	 * Instantiate.
	 * 
	 * @param unsafeServerWriter
	 *            Unsafe {@link ServerWriter}.
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public ProcessAwareServerWriter(ServerWriter unsafeServerWriter, ProcessAwareContext context) {
		this.unsafeServerWriter = unsafeServerWriter;
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
	 * =================== ServerWriter ===========================
	 */

	@Override
	public void write(byte[] encodedBytes) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(encodedBytes));
	}

	@Override
	public void write(FileChannel file, long position, long count) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(file, position, count));
	}

	@Override
	public void write(FileChannel file) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(file));
	}

	@Override
	public void write(ByteBuffer encodedBytes) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(encodedBytes));
	}

	@Override
	public void write(int c) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(c));
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(cbuf));
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(cbuf, off, len));
	}

	@Override
	public void write(String str) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(str));
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(str, off, len));
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		this.safe(() -> this.unsafeServerWriter.append(csq));
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		this.safe(() -> this.unsafeServerWriter.append(csq, start, end));
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		this.safe(() -> this.unsafeServerWriter.append(c));
		return this;
	}

	@Override
	public void flush() throws IOException {
		this.safe(() -> this.unsafeServerWriter.flush());
	}

	@Override
	public void close() throws IOException {
		this.safe(() -> this.unsafeServerWriter.close());
	}

}