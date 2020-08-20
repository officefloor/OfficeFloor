package net.officefloor.test;

/**
 * Provides JUnit agnostic assertions.
 * 
 * @author Daniel Sagenschneider
 */
public class JUnitAgnosticAssert {

	/**
	 * Assert not <code>null</code>.
	 * 
	 * @param actual  Actual.
	 * @param message Message.
	 */
	public static void assertNotNull(Object actual, String message) {
		if (actual == null) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Asserts equals.
	 * 
	 * @param expected Expected.
	 * @param actual   Actual.
	 * @param message  Message.
	 */
	public static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + ": Expected <" + expected + "> but was <" + actual + ">");
		}
	}

	/**
	 * Asserts <code>true</code>.
	 * 
	 * @param actual  Actual.
	 * @param message Message.
	 */
	public static void assertTrue(boolean actual, String message) {
		if (!actual) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Asserts <code>false</code>.
	 * 
	 * @param actual  Actual.
	 * @param message Message.
	 */
	public static void assertFalse(boolean actual, String message) {
		if (actual) {
			throw new AssertionError(message);
		}
	}

	/**
	 * Propagates the failure.
	 * 
	 * @param <R>     Any result.
	 * @param failure Failure.
	 * @return Never returns, however allows for return statements.
	 */
	public static <R> R fail(Throwable failure) {
		if (failure instanceof RuntimeException) {
			throw (RuntimeException) failure;
		} else if (failure instanceof Error) {
			throw (Error) failure;
		} else {
			throw new AssertionError(failure.getMessage(), failure);
		}
	}

	/**
	 * Fails.
	 * 
	 * @param <R>     Any result.
	 * @param message Failure message.
	 * @return Never returns, however allows for return statements.
	 */
	public static <R> R fail(String message) {
		throw new AssertionError(message);
	}

}