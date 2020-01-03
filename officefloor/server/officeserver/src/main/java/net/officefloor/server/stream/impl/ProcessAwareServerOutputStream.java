package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.ServerOutputStream;

/**
 * {@link ProcessState} aware wrapping {@link ServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerOutputStream extends ServerOutputStream {

	/**
	 * Unsafe {@link OutputStream}.
	 */
	private final ServerOutputStream unsafeOutputStream;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private final ManagedObjectContext context;

	/**
	 * Instantiate.
	 * 
	 * @param unsafeOutputStream Unsafe {@link ServerOutputStream}.
	 * @param context            {@link ManagedObjectContext}.
	 */
	public ProcessAwareServerOutputStream(ServerOutputStream unsafeOutputStream, ManagedObjectContext context) {
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