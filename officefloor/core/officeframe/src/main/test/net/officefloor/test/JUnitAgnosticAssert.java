/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.test;

import java.util.Objects;

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
	 * Assert <code>null</code>.
	 * 
	 * @param actual  Actual.
	 * @param message Message.
	 */
	public static void assertNull(Object actual, String message) {
		if (actual != null) {
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
		if (!Objects.equals(expected, actual)) {
			throw new AssertionError(message + ": Expected <" + expected + "> but was <" + actual + ">");
		}
	}

	/**
	 * Asserts same.
	 * 
	 * @param expected Expected.
	 * @param actual   Actual.
	 * @param message  Message.
	 */
	public static void assertSame(Object expected, Object actual, String message) {
		if (expected != actual) {
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

	/**
	 * Fails.
	 * 
	 * @param <R>     Any result.
	 * @param message Failure message.
	 * @param cause   Cause.
	 * @return Never reurns, however allows for return statements.
	 */
	public static <R> R fail(String message, Throwable cause) {
		throw new AssertionError(message, cause);
	}

}
