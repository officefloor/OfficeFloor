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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcessException}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessExceptionTest extends OfficeFrameTestCase {

	/**
	 * Propagation path header.
	 */
	private static String PROPAGATION_PATH_HEADER = "Propagation path:";

	/**
	 * Ensure can propagate {@link Throwable} appropriately.
	 */
	public void testPropagateNonProcessException() {

		// Create the throwable
		Throwable exception = new Throwable("TEST");

		// Ensure wrap in process exception
		ProcessException propagated = ProcessException.propagate("ANOTHER",
				exception);

		// Validate the propagate exception
		assertEquals("Incorrect message", "ANOTHER", propagated.getMessage());
		assertSame("Incorrect cause", exception, propagated.getCause());
	}

	/**
	 * Ensure can propagate {@link ProcessException} appropriately.
	 */
	public void testPropagateProcessException() {

		// Create the process exception
		ProcessException exception = new ProcessException("TEST");

		// Propagate to ensure same exception
		ProcessException propagated = ProcessException.propagate("ANOTHER",
				exception);
		assertEquals("Incorrect message", "TEST", propagated.getMessage());
		assertSame("Must be same exception", exception, propagated);
		assertNull("Should be no cause", propagated.getCause());
	}

	/**
	 * Ensure same exception on no serialise.
	 */
	public void testNoSerialise() throws Exception {

		// Create the exception
		ProcessException exception = new ProcessException("TEST");

		// Ensure correct details
		assertEquals("Incorrect message", "TEST", exception.getMessage());

		// Ensure stack trace is correct
		String stackTrace = getStackTrace(exception);
		assertTrue("Ensure stack trace starts with class",
				stackTrace.startsWith(exception.getClass().getName()));

		// Ensure no propagation path
		assertFalse("Ensure no propagation path",
				stackTrace.contains(PROPAGATION_PATH_HEADER));
	}

	/**
	 * Ensure propagation details on stack trace after serialise.
	 */
	public void testPropagate() throws Exception {

		// Create the exception
		ProcessException exception = new ProcessException("TEST");
		ProcessException serialised = serialise(exception);

		// Ensure correct details
		assertEquals("Incorrect message", "TEST", serialised.getMessage());

		// Obtain the runtime name
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();

		// Ensure stack trace is correct
		String stackTrace = getStackTrace(serialised);
		assertTrue(
				"Ensure stack trace starts with class and initial remote process",
				stackTrace.startsWith(exception.getClass().getName()
						+ " (from remote runtime " + runtimeName + ")"));
		assertTrue("Ensure has propagation path",
				stackTrace.contains(PROPAGATION_PATH_HEADER));
	}

	/**
	 * Ensure serialise appropriately with no cause.
	 */
	public void testWithNoCause() throws Exception {

		// Create the exception
		ProcessException exception = new ProcessException("TEST");

		// Serialise and deserialise to new exception
		ProcessException serialised = serialise(exception);

		// Validate no cause
		assertNull("Should be no cause", serialised.getCause());

		// Validate
		assertSerialise(serialised, exception, 0);
	}

	/**
	 * Ensure serialise appropriately with a serialisable cause.
	 */
	public void testWithSerialisableCause() throws Exception {

		// Create the exception
		ProcessException exception = ProcessException.propagate("TEST",
				new SerialisableException("CAUSE"));

		// Serialise and deserilise to new exception
		ProcessException serialised = serialise(exception);

		// Validate able to serialise the cause
		assertTrue("Should be correct cause type",
				serialised.getCause() instanceof SerialisableException);
		assertEquals("Incorrect cause message", "CAUSE", serialised.getCause()
				.getMessage());

		// Validate
		assertSerialise(serialised, exception, 0);
	}

	/**
	 * Serialisable {@link Exception} for testing.
	 */
	private static class SerialisableException extends Exception {
		public SerialisableException(String message) {
			super(message);
		}
	}

	/**
	 * Ensure serialise appropriately with non-serialiseable cause.
	 */
	public void testWithNonSerialisableCause() throws Exception {

		// Ensure test valid
		NonSerialisableException cause = new NonSerialisableException("CAUSE");
		try {
			serialise(cause);
			fail("Should not be serialisable");
		} catch (NotSerializableException ex) {
		}

		// Create the exception
		ProcessException exception = ProcessException.propagate("TEST", cause);

		// Serialise and deserilise to new exception
		ProcessException serialised = serialise(exception);

		// Validate not able to serialise the cause
		assertNull("Should be no cause on not serialisable cause",
				serialised.getCause());

		// Validate
		assertSerialise(serialised, exception, 0);
	}

	/**
	 * Non-serialisable {@link Exception} for testing.
	 */
	public static class NonSerialisableException extends Exception {

		/**
		 * Will cause this {@link Exception} to not be serialisable.
		 */
		Object nonSerialisable = new Object();

		/**
		 * Constructor.
		 * 
		 * @param message
		 *            Message to identify exception.
		 */
		public NonSerialisableException(String message) throws Exception {
			super(message);
		}
	}

	/**
	 * Ensure records multiple propagations.
	 */
	public void testMultiplePropagations() throws Exception {

		// Create the exception
		ProcessException exception = new ProcessException("TEST");

		// Serialise and deserilise to a few times
		final int serialiseCount = 10;
		ProcessException serialised = exception;
		String originalJavaClassPath = System.getProperty("java.class.path");
		try {
			for (int i = 0; i < serialiseCount; i++) {
				System.setProperty("java.class.path", "ClassPath-" + i);
				serialised = serialise(serialised);
			}
		} finally {
			System.setProperty("java.class.path", originalJavaClassPath);
		}

		// Validate
		assertSerialise(serialised, exception, serialiseCount);
	}

	/**
	 * Creates a new {@link Exception} instance by serialising and deserialising
	 * the input {@link Exception}.
	 * 
	 * @param exception
	 *            {@link Exception} to serialise.
	 * @return New {@link Exception}.
	 */
	@SuppressWarnings("unchecked")
	private static <E extends Exception> E serialise(E exception)
			throws IOException, ClassNotFoundException {

		// Serialise and deserilise to new exception
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
		outputStream.writeObject(exception);
		outputStream.flush();
		ObjectInputStream inputStream = new ObjectInputStream(
				new ByteArrayInputStream(buffer.toByteArray()));
		E serialised = (E) inputStream.readObject();

		// Return the serialised/deserialised object
		return serialised;
	}

	/**
	 * Asserts that details after serialise are correct given the original
	 * {@link ProcessException}.
	 * 
	 * @param serialised
	 *            {@link ProcessException} after serialisation and
	 *            deserialisation.
	 * @param original
	 *            Original {@link ProcessException}.
	 * @param serialiseCount
	 *            Number of times serialised if multiple propagations.
	 */
	private static void assertSerialise(ProcessException serialised,
			ProcessException original, int serialiseCount) {

		// Validate the values are correct
		assertEquals("Incorrect getMessage()", original.getMessage(),
				serialised.getMessage());
		assertEquals("Incorrect toString()", original.getLocalizedMessage(),
				serialised.getLocalizedMessage());

		// Obtain the details for the serialised stack trace
		String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
		String javaClassPath = System.getProperty("java.class.path");
		String originalStackTrace = getStackTrace(original);

		// Alter the stack trace (for remote exception)
		String remoteStackTrace = originalStackTrace.replaceFirst(original
				.getClass().getName(), original.getClass().getName()
				+ " (from remote runtime " + runtimeName + ")");

		// Construct the expected serialised stack trace
		StringBuilder expected = new StringBuilder();
		expected.append(remoteStackTrace);
		expected.append("\n\nPropagation path:\n");
		if (serialiseCount == 0) {
			// Single serialise
			expected.append("\nRuntime: " + runtimeName);
			expected.append("\nClassPath: " + javaClassPath);
		} else {
			// Multiple serialise
			for (int i = 0; i < serialiseCount; i++) {
				if (i > 0) {
					expected.append("\n");
				}
				expected.append("\nRuntime: " + runtimeName);
				expected.append("\nClassPath: ClassPath-" + i);
			}
		}

		// Obtain the serialised stack trace
		String serialisedStackTrace = getStackTrace(serialised);

		// Ensure matches
		assertEquals("Incorrect stack trace", expected.toString(),
				serialisedStackTrace);
	}

	/**
	 * Obtains the stack trace for the {@link ProcessException}.
	 * 
	 * @param exception
	 *            {@link ProcessException}.
	 * @return Stack trace for the {@link ProcessException}.
	 */
	private static String getStackTrace(ProcessException exception) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(buffer);
		exception.printStackTrace(stream);
		stream.flush();
		return new String(buffer.toByteArray());
	}

}