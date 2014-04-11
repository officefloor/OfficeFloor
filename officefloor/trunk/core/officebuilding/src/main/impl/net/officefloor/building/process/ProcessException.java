/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.building.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.ManagementFactory;

/**
 * <p>
 * Indicates a failure with a {@link ManagedProcess}.
 * <p>
 * As {@link ProcessException} may be sent over streams between proceses, this
 * class handles being {@link Serializable} (particularly for causing
 * {@link Exception}).
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessException extends Exception implements Serializable {

	/**
	 * Serial version number for {@link Serializable}.
	 */
	static final long serialVersionUID = 1487316913125229048L;

	/**
	 * Propagates the {@link ProcessException}.
	 * 
	 * @param cause
	 *            Cause.
	 * @return {@link ProcessException} to propagate.
	 */
	public static ProcessException propagate(Throwable cause) {
		return propagate(null, cause);
	}

	/**
	 * Propagates the {@link ProcessException}.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 * @return {@link ProcessException} to propagate.
	 */
	public static ProcessException propagate(String message, Throwable cause) {

		// Determine if cause is process exception
		if (cause instanceof ProcessException) {
			return (ProcessException) cause;
		}

		// Return newly constructed process exception
		return new ProcessException(message, cause);
	}

	/**
	 * Cause of the {@link ProcessException}.
	 */
	private Throwable cause;

	/**
	 * Stack trace. <code>null</code> if not serialised (so assume still in same
	 * process).
	 */
	private String stackTrace;

	/**
	 * Runtime names of the Java processes as propagation occurs through them.
	 * <code>null</code> if not serialised (so assume still in same process).
	 */
	private String[] propagationRuntimeNames;

	/**
	 * Java class paths of the Java processes as propagation occurs through
	 * them. <code>null</code> if not serialised (so assume still in same
	 * process).
	 */
	private String[] propagationJavaClassPaths;

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	public ProcessException(String message) {
		this(message, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	private ProcessException(String message, Throwable cause) {
		super(message != null ? message : cause != null ? cause.getMessage()
				: null);
		this.cause = cause;
		this.stackTrace = null;
		this.propagationRuntimeNames = null;
		this.propagationJavaClassPaths = null;
	}

	/*
	 * ===================== Serialise methods =====================
	 */

	/**
	 * Serialises this {@link ProcessException} keeping track of details useful
	 * to debugging.
	 * 
	 * @param out
	 *            {@link ObjectOutputStream}.
	 * @throws IOException
	 *             {@link IOException}.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {

		// Load the stack trace
		String stackTrace = this.stackTrace;
		if (stackTrace == null) {
			// No stack trace, so in original process throwing
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PrintStream stream = new PrintStream(buffer);
			this.printStackTrace(stream);
			stream.flush();
			stackTrace = new String(buffer.toByteArray());
		}
		out.writeObject(stackTrace);

		// Attempt to serialise cause
		boolean isCauseSerialisable = false;
		try {
			if (this.cause != null) {
				// Have cause, so determine if serialisable
				ObjectOutputStream outputStream = new ObjectOutputStream(
						new ByteArrayOutputStream());
				outputStream.writeObject(this.cause);
				outputStream.flush();

				// As here, the cause is serialisable
				isCauseSerialisable = true;
			}
		} catch (IOException ex) {
			// Cause not serialisable
			isCauseSerialisable = false;
		}

		// Serialise cause if serialisable
		out.writeObject(isCauseSerialisable ? this.cause : null);

		// Load the runtime name to identify the runtime (process)
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		out.writeObject(newArray(this.propagationRuntimeNames, runtimeName));

		// Load the Java class path
		String javaClassPath = System.getProperty("java.class.path");
		out.writeObject(newArray(this.propagationJavaClassPaths, javaClassPath));
	}

	/**
	 * Appends the value to the end of the array.
	 * 
	 * @param array
	 *            Array. May be <code>null</code> to indicate empty array.
	 * @param value
	 *            Value.
	 * @return Array with value appended.
	 */
	private static String[] newArray(String[] array, String value) {

		// Determine if have array
		if (array == null) {
			// Return value in array as empty array
			return new String[] { value };
		}

		// Create and load the values to a new array
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = value;

		// Return the array with value
		return newArray;
	}

	/**
	 * Deserialises the {@link ProcessException} loading in details useful to
	 * debugging.
	 * 
	 * @param in
	 *            {@link ObjectInputStream}.
	 * @throws IOException
	 *             {@link IOException}.
	 * @throws ClassNotFoundException
	 *             {@link ClassNotFoundException}.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		// Read in the stack trace
		this.stackTrace = (String) in.readObject();

		// Load the cause
		this.cause = (Throwable) in.readObject();

		// Read in the runtime name
		this.propagationRuntimeNames = (String[]) in.readObject();

		// Read in the Java class path
		this.propagationJavaClassPaths = (String[]) in.readObject();
	}

	/*
	 * ===================== Override methods =====================
	 */

	@Override
	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public void printStackTrace() {
		this.printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream s) {
		this.printStackTrace(new PrintWriter(s));
	}

	@Override
	public void printStackTrace(PrintWriter s) {

		// Determine if local to process exception
		if (this.stackTrace == null) {
			super.printStackTrace(s);
			s.flush();
			return;
		}

		// Provide remote process details
		String remoteStackTrace = this.stackTrace.replaceFirst(this.getClass()
				.getName(), this.getClass().getName()
				+ " (from remote runtime " + this.propagationRuntimeNames[0]
				+ ")");

		// Write out the stack trace
		s.write(remoteStackTrace);
		s.write("\n\nPropagation path:\n");
		for (int i = 0; i < this.propagationRuntimeNames.length; i++) {
			if (i > 0) {
				s.write("\n");
			}
			s.write("\nRuntime: " + this.propagationRuntimeNames[i]);
			s.write("\nClassPath: " + this.propagationJavaClassPaths[i]);
		}
		s.flush();
	}

}