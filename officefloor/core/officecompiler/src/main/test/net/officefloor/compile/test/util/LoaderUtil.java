/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.test.util;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;

/**
 * Utility methods for loader test utilities.
 * 
 * @author Daniel Sagenschneider
 */
public class LoaderUtil {

	/**
	 * Asserts the arrays are of the same length, providing useful debug information
	 * if not.
	 * 
	 * @param <T>      Entry type.
	 * @param message  Message.
	 * @param expected Expected items of array.
	 * @param actual   Actual items of array.
	 * @param toString {@link Function} to obtain {@link String} description of
	 *                 item.
	 */
	public static <T> void assertLength(String message, T[] expected, T[] actual, Function<T, String> toString) {
		assertLength(message, expected, toString, actual, toString);
	}

	/**
	 * Asserts the arrays are of the same length, providing useful debug information
	 * if not.
	 *
	 * @param <E>              Expected type.
	 * @param <A>              Actual type.
	 * @param message          Message.
	 * @param expected         Expected items of array.
	 * @param expectedToString {@link Function} to obtain {@link String} description
	 *                         of expected item.
	 * @param actual           Actual items of array.
	 * @param actualToString   {@link Function} to obtain {@link String} description
	 *                         of actual item.
	 */
	public static <E, A> void assertLength(String message, E[] expected, Function<E, String> expectedToString,
			A[] actual, Function<A, String> actualToString) {

		// Determine if lengths match
		if (expected.length == actual.length) {
			return; // same length
		}

		// Not same length, so assert with debug information
		String eText = String.join(", ", Arrays.stream(expected).map(expectedToString).toArray(String[]::new));
		String aText = String.join(", ", Arrays.stream(actual).map(actualToString).toArray(String[]::new));

		// Assert the length
		Assert.assertEquals(message + "\n\tE: " + eText + "\n\tA: " + aText, expected.length, actual.length);
	}

	/**
	 * All access via static methods.
	 */
	private LoaderUtil() {
	}

}