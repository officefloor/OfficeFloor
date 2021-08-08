/*-
 * #%L
 * OfficeCompiler
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
		Assert.assertEquals(message + "\n\tE: " + eText + "\n\tA: " + aText + "\n", expected.length, actual.length);
	}

	/**
	 * All access via static methods.
	 */
	private LoaderUtil() {
	}

}
