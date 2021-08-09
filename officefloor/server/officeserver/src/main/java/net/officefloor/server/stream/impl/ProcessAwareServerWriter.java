/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link ProcessState} aware writing {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerWriter extends ServerWriter {

	/**
	 * Unsafe {@link ServerWriter}.
	 */
	private final ServerWriter unsafeServerWriter;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private final ManagedObjectContext context;

	/**
	 * Instantiate.
	 * 
	 * @param unsafeServerWriter Unsafe {@link ServerWriter}.
	 * @param context            {@link ManagedObjectContext}.
	 */
	public ProcessAwareServerWriter(ServerWriter unsafeServerWriter, ManagedObjectContext context) {
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
		 * @throws T Possible failure from operation.
		 */
		void run() throws T;
	}

	/**
	 * Wraps execution to be {@link ProcessState} ({@link Thread}) safe.
	 * 
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Result of {@link ProcessSafeOperation}.
	 * @throws T If {@link ProcessSafeOperation} fails.
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
	public void write(FileChannel file, long position, long count, FileCompleteCallback callback) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(file, position, count, callback));
	}

	@Override
	public void write(FileChannel file, FileCompleteCallback callback) throws IOException {
		this.safe(() -> this.unsafeServerWriter.write(file, callback));
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
