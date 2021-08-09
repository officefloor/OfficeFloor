/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;

/**
 * {@link org.junit.jupiter.api.Assertions} extended with additional features.
 * 
 * @author Daniel Sagenschneider
 */
public class Assertions extends org.junit.jupiter.api.Assertions {

	/**
	 * Operation for {@link #assertFail} that should fail.
	 */
	protected static interface FailOperation {

		/**
		 * Contains the operation that should fail.
		 * 
		 * @throws Throwable Expected failure of the operation.
		 */
		void run() throws Throwable;
	}

	/**
	 * Asserts the failure.
	 *
	 * @param <F>                 Failure type.
	 * @param operation           {@link FailOperation} that is expected fail.
	 * @param expectedFailureType Expect type of failure.
	 * @return Actual failure for further assertions.
	 */
	@SuppressWarnings("unchecked")
	public static <F extends Throwable> F assertFail(FailOperation operation, Class<F> expectedFailureType) {
		try {
			operation.run();
			fail("Operation expected to fail with cause " + expectedFailureType.getSimpleName());
			return null; // for compilation

		} catch (AssertionFailedError ex) {
			// Propagate unit test failure
			throw ex;
		} catch (Throwable ex) {
			// Ensure the correct type
			assertEquals(expectedFailureType, ex.getClass(), "Incorrect cause of failure");
			return (F) ex;
		}
	}

	/**
	 * Provides simplified facade to verify {@link Method} will fail.
	 *
	 * @param <F>                 Failure type.
	 * @param expectedFailureType Expected failure of method.
	 * @param object              Object to invoke {@link Method} on.
	 * @param methodName          Name of the {@link Method}.
	 * @param parameters          Parameters for the {@link Method}.
	 * @return Actual failure for further assertions.
	 */
	public static <F extends Throwable> F assertFail(Class<F> expectedFailureType, final Object object,
			final String methodName, final Object... parameters) {
		try {
			// Obtain the listing of parameter types
			Class<?>[] parameterTypes = new Class[parameters.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes[i] = parameters[i].getClass();
			}

			// Obtain the method
			Method method = object.getClass().getMethod(methodName, parameterTypes);

			// Assert fail
			return assertFail(expectedFailureType, object, method, parameters);

		} catch (Exception ex) {
			return fail(ex);
		}
	}

	/**
	 * Provides simplified facade to verify {@link Method} will fail.
	 *
	 * @param <F>                 Failure type.
	 * @param expectedFailureType Expected failure of method.
	 * @param object              Object to invoke {@link Method} on.
	 * @param method              {@link Method}.
	 * @param parameters          Parameters for the {@link Method}.
	 * @return Actual failure for further assertions.
	 */
	public static <F extends Throwable> F assertFail(Class<F> expectedFailureType, final Object object,
			final Method method, final Object... parameters) {
		return assertFail(new FailOperation() {
			@Override
			public void run() throws Throwable {
				// Invoke the method
				try {
					method.invoke(object, parameters);
				} catch (InvocationTargetException ex) {
					// Throw cause of method failure
					throw ex.getCause();
				}
			}
		}, expectedFailureType);
	}

	/**
	 * Asserts the input texts match taking into account platform differences.
	 * 
	 * @param expected Raw expected text.
	 * @param actual   Raw actual text.
	 * @param message  Message.
	 */
	public static void assertTextEquals(String expected, String actual, String message) {
		String expectedText = createPlatformIndependentText(expected);
		String actualText = createPlatformIndependentText(actual);
		assertEquals(expectedText, actualText, message);
	}

	/**
	 * Creates the platform independent text for comparing.
	 * 
	 * @param rawText Raw text.
	 * @return Platform independent text.
	 */
	public static String createPlatformIndependentText(String rawText) {
		rawText = rawText.replace("\r\n", "\n");
		rawText = rawText.replace("\r", "\n");
		return rawText;
	}

	/**
	 * Asserts the input XML's match with white spacing removed.
	 * 
	 * @param expected Raw expected text.
	 * @param actual   Raw actual text.
	 * @param message  Message.
	 */
	public static void assertXmlEquals(String expected, String actual, String message) {
		String expectedXml = removeXmlWhiteSpacing(createPlatformIndependentText(expected));
		String actualXml = removeXmlWhiteSpacing(createPlatformIndependentText(actual));
		assertEquals(expectedXml, actualXml, message);
	}

	/**
	 * Removes the white spacing from the XML.
	 * 
	 * @param xml XML.
	 * @return XML with white spacing removed.
	 */
	public static String removeXmlWhiteSpacing(String xml) {

		final char[] whiteSpacing = new char[] { ' ', '\t', '\n', '\r' };

		// Iterate until all white spacing is removed
		boolean isComplete;
		do {

			// Remove white spacing
			for (char character : whiteSpacing) {
				xml = xml.replace(">" + character, ">");
				xml = xml.replace(character + "<", "<");
				xml = xml.replace(character + "/>", "/>");
			}

			// Determine if no further white spacing
			isComplete = true;
			for (char character : whiteSpacing) {
				if (xml.contains(">" + character)) {
					isComplete = false;
				}
				if (xml.contains(character + "<")) {
					isComplete = false;
				}
				if (xml.contains(character + "/>")) {
					isComplete = false;
				}
			}

		} while (!isComplete);

		// Return the XML minus the white spacing
		return xml;
	}

	/**
	 * Assets that the input graph is as expected.
	 *
	 * @param <O>               Type.
	 * @param expectedRoot      Expected root.
	 * @param actualRoot        Actual root.
	 * @param ignoreMethodNames Listing of methods to be ignored in checking.
	 * @throws Exception If fails.
	 */
	public synchronized static <O> void assertGraph(O expectedRoot, O actualRoot, String... ignoreMethodNames)
			throws Exception {
		assertGraph(expectedRoot, actualRoot, new HashMap<CheckedObject, Integer>(), "<root>", ignoreMethodNames);
	}

	/**
	 * Flags whether the {@link #assertGraph(O, O, Set, String)} exception has been
	 * logged.
	 */
	private static boolean isAssetGraphExceptionLogged = false;

	/**
	 * Wrapper around {@link Object} being checked to ensure
	 * {@link Object#equals(Object)} does not equate {@link Object} instances to be
	 * the same and not fully check the object graph.
	 */
	private static class CheckedObject {

		/**
		 * Object being checked.
		 */
		private final Object object;

		/**
		 * Initiate.
		 * 
		 * @param object Object being checked.
		 */
		public CheckedObject(Object object) {
			this.object = object;
		}

		/*
		 * ================== Object ================================
		 */

		@Override
		public boolean equals(Object obj) {

			// Object must be checked object
			assertTrue((obj instanceof CheckedObject), "Must be CheckedObject " + obj);
			CheckedObject that = (CheckedObject) obj;

			// Ensure same instance (rather than equals)
			return (this.object == that.object);
		}

		@Override
		public int hashCode() {
			// Use the object's hash to ensuring able to find itself
			return this.object.hashCode();
		}
	}

	/**
	 * Assets that the input graph is as expected.
	 * 
	 * @param O                 Type of root object for graph.
	 * @param expectedRoot      Expected root.
	 * @param actualRoot        Actual root.
	 * @param checkedObjects    Set of objects already checked to stop cyclic
	 *                          checking.
	 * @param path              Path to item failing check.
	 * @param ignoreMethodNames Listing of methods to be ignored in checking.
	 */
	@SuppressWarnings("unchecked")
	private static <O> void assertGraph(O expectedRoot, O actualRoot, Map<CheckedObject, Integer> checkedObjects,
			String path, String[] ignoreMethodNames) throws Exception {

		// Reset
		isAssetGraphExceptionLogged = false;

		try {

			// Always check contents of:
			// - null
			// - Collection
			// - primitive types
			// (stops equals instances from matching)
			if ((expectedRoot != null) && (!(expectedRoot instanceof Collection))
					&& (!(expectedRoot.getClass().isPrimitive()))) {
				// Ensure checked only twice
				// (allows checking bi-directional references)
				CheckedObject checkedObject = new CheckedObject(expectedRoot);
				Integer timesChecked = checkedObjects.get(checkedObject);
				if (timesChecked != null) {
					// Ensure only check twice at most
					int times = timesChecked.intValue() + 1;
					if (times > 2) {
						// Already checked twice
						return;
					}

					// Specify another check of object
					checkedObjects.put(checkedObject, Integer.valueOf(times));
				} else {
					// First time accessed, therefore flag first time
					checkedObjects.put(checkedObject, Integer.valueOf(1));
				}
			}

			// Ensure matches
			if ((expectedRoot == null) && (actualRoot == null)) {
				// Match as both null
				return;
			} else if ((expectedRoot != null) && (actualRoot != null)) {
				// Both not null therefore ensure of same type
				assertEquals(expectedRoot.getClass(), actualRoot.getClass(), "Path " + path + " type mismatch");

				if (expectedRoot instanceof Class) {
					// Validate the same class
					assertEquals(expectedRoot, actualRoot, "Path " + path + " incorrect type");
				} else if ((expectedRoot.getClass().isPrimitive()) || (expectedRoot instanceof String)
						|| (expectedRoot instanceof Boolean) || (expectedRoot instanceof Byte)
						|| (expectedRoot instanceof Character) || (expectedRoot instanceof Short)
						|| (expectedRoot instanceof Integer) || (expectedRoot instanceof Long)
						|| (expectedRoot instanceof Float) || (expectedRoot instanceof Double)) {
					// Do primitive comparison
					assertEquals(expectedRoot, actualRoot, "Path " + path + " mismatch");
				} else if (expectedRoot instanceof Collection) {
					// Do deep collection comparison
					assertGraphCollection((Collection<Object>) expectedRoot, (Collection<Object>) actualRoot,
							checkedObjects, path, ignoreMethodNames);
				} else {
					// Do POJO comparison of accessors
					for (Method method : expectedRoot.getClass().getMethods()) {

						// Obtain the method name
						String methodName = method.getName();

						// Ignore Object methods
						if (Object.class.equals(method.getDeclaringClass())) {
							continue;
						}

						// Determine if a method to ignore
						boolean isIgnoreMethod = false;
						for (String ignoreMethodName : ignoreMethodNames) {
							if (methodName.equals(ignoreMethodName)) {
								isIgnoreMethod = true;
							}
						}
						if (isIgnoreMethod) {
							continue;
						}

						// Determine if accessor method
						if ((!Modifier.isPublic(method.getModifiers())) || (method.getReturnType() == Void.TYPE)
								|| (method.getParameterTypes().length != 0)) {
							continue;
						}

						// Obtain the values of the accessors
						Object expectedValue = method.invoke(expectedRoot, (Object[]) null);
						Object actualValue = method.invoke(actualRoot, (Object[]) null);

						// Do deep comparison
						assertGraph(expectedValue, actualValue, checkedObjects, path + "." + methodName + "()",
								ignoreMethodNames);
					}
				}
			} else {
				// One null while other not
				fail("Path " + path + " mismatch [e " + expectedRoot + ", a " + actualRoot + "]");
			}
		} catch (Exception ex) {
			if (!isAssetGraphExceptionLogged) {
				// Log failure with path
				System.err.println("Failure " + path + " - " + ex.getMessage());
				ex.printStackTrace(System.err);

				// Flag logged
				isAssetGraphExceptionLogged = true;
			}

			// Propagate failure
			throw ex;
		}
	}

	/**
	 * Assets that the input collection is as expected.
	 * 
	 * @param O                 Type of root object for graph.
	 * @param expected          {@link Collection} of expected.
	 * @param actual            {@link Collection} of actual.
	 * @param checkedObjects    Set of objects already checked to stop cyclic
	 *                          checking.
	 * @param path              Path to item failing check.
	 * @param ignoreMethodNames Listing of methods to be ignored in checking.
	 */
	@SuppressWarnings("unchecked")
	private static <O> void assertGraphCollection(Collection<O> expected, Collection<O> actual,
			Map<CheckedObject, Integer> checkedObjects, String path, String[] ignoreMethodNames) throws Exception {

		// Validate the size
		assertEquals(expected.size(), actual.size(), "Path " + path + " incorrect size");

		if (expected instanceof List) {
			// Downcast to list for checking
			List<Object> expectedList = (List<Object>) expected;
			List<Object> actualList = (List<Object>) actual;
			for (int i = 0; i < expectedList.size(); i++) {
				// Do deep comparison of item
				assertGraph(expectedList.get(i), actualList.get(i), checkedObjects, path + "[" + i + "]",
						ignoreMethodNames);
			}
		} else {
			// Unknown collection type
			fail("Path " + path + " unknown collection type " + expected.getClass().getName());
		}
	}

	/**
	 * Asserts the contents of the input {@link File} instances are the same.
	 * 
	 * @param expected Expected file.
	 * @param actual   Actual file.
	 * @throws IOException If fails to read contents.
	 */
	public static void assertContents(File expected, File actual) throws IOException {
		assertContents(new FileReader(expected), new FileReader(actual));
	}

	/**
	 * Asserts the contents of the input {@link Reader} instances are the same.
	 * 
	 * @param expected Expected content.
	 * @param actual   Actual content.
	 */
	public static void assertContents(Reader expected, Reader actual) {
		try {
			BufferedReader expectedReader = new BufferedReader(expected);
			BufferedReader actualReader = new BufferedReader(actual);
			String expectedLine;
			String actualLine;
			int lineNumber = 1;
			while ((actualLine = actualReader.readLine()) != null) {
				expectedLine = expectedReader.readLine();
				assertEquals(expectedLine, actualLine, "Incorrect line " + lineNumber);
				lineNumber++;
			}
		} catch (IOException ex) {
			fail(ex);
		}
	}

	/**
	 * Asserts that the input list is as expected.
	 *
	 * @param <O>           Type.
	 * @param matcher       Matches the items of the list.
	 * @param list          List to be checked.
	 * @param expectedItems Items expected to be in the list.
	 */
	@SafeVarargs
	public static <O> void assertList(ListItemMatcher<O> matcher, List<O> list, O... expectedItems) {

		// Ensure similar number of items in each list
		assertEquals(expectedItems.length, list.size(), "List lengths not match");

		// Ensure similar items
		for (int i = 0; i < expectedItems.length; i++) {
			matcher.match(i, expectedItems[i], list.get(i));
		}
	}

	/**
	 * Asserts that the input list equals the expected.
	 *
	 * @param <O>           Type.
	 * @param list          List to be checked.
	 * @param expectedItems Items expected in the list.
	 */
	@SafeVarargs
	public static <O> void assertList(List<O> list, O... expectedItems) {
		assertList(new ListItemMatcher<O>() {
			public void match(int index, O expected, O actual) {
				assertEquals(expected, actual, "Incorrect item " + index);
			}
		}, list, expectedItems);
	}

	/**
	 * Asserts that properties on items within list match.
	 *
	 * @param <O>           Type.
	 * @param methods       Method names to specify the properties on the items to
	 *                      match.
	 * @param list          List to be checked.
	 * @param expectedItems Items expected in the list.
	 */
	@SafeVarargs
	public static <O> void assertList(final String[] methods, List<O> list, O... expectedItems) {
		assertList(new ListItemMatcher<O>() {
			public void match(int index, O expected, O actual) {
				// Match the properties
				for (String method : methods) {
					assertEquals(getProperty(expected, method), getProperty(actual, method),
							"Incorrect property " + method + " for item " + index);
				}
			}
		}, list, expectedItems);
	}

	/**
	 * Asserts that properties on items within the array match.
	 *
	 * @param <O>           Type.
	 * @param methods       Method names to specify the properties on the items to
	 *                      match.
	 * @param array         Array to be checked.
	 * @param expectedItems Items expected in the array.
	 */
	@SafeVarargs
	public static <O> void assertList(final String[] methods, O[] array, O... expectedItems) {
		assertList(methods, Arrays.asList(array), expectedItems);
	}

	/**
	 * Asserts that properties on items within list match after the list is sorted.
	 * 
	 * @param <O>           Type.
	 * @param sortMethod    Name of method on the items to sort the list by to
	 *                      ensure match in order.
	 * @param methods       Method names to specify the properties on the items to
	 *                      match.
	 * @param list          List to be checked.
	 * @param expectedItems Items expected in the list.
	 */
	@SafeVarargs
	public static <O> void assertList(final String sortMethod, String[] methods, List<O> list, O... expectedItems) {

		// Sort the list
		Collections.sort(list, new Comparator<O>() {
			@Override
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public int compare(O a, O b) {

				// Obtain the property values
				Object valueA = getProperty(a, sortMethod);
				Object valueB = getProperty(b, sortMethod);

				// Return the comparison
				Comparable comparableA = (Comparable) valueA;
				return comparableA.compareTo(valueB);
			}
		});

		// Assert the list
		assertList(methods, list, expectedItems);
	}

	/**
	 * Asserts that properties on the input objects match for the specified methods.
	 * 
	 * @param <O>      Type.
	 * @param expected Expected item.
	 * @param actual   Actual item.
	 * @param methods  Method names to specify the properties on the item to match.
	 */
	public static <O> void assertProperties(O expected, O actual, String... methods) {
		// Match the properties
		for (String method : methods) {
			assertEquals(getProperty(expected, method), getProperty(actual, method), "Incorrect property " + method);
		}
	}

	/**
	 * Obtains the property on the Object.
	 * 
	 * @param object     Object.
	 * @param methodName Method name to obtain property value.
	 * @return Value of property.
	 */
	public static Object getProperty(Object object, String methodName) {

		// Ensure have an object to retrieve value
		assertNotNull(object, "Can not source property '" + methodName + "' from null object");

		Object value = null;
		try {
			// Find the method on the object
			Method method = object.getClass().getMethod(methodName, (Class[]) new Class[0]);

			// Obtain the property value
			value = method.invoke(object, new Object[0]);

		} catch (SecurityException ex) {
			fail("No access to method '" + methodName + "' on object of class " + object.getClass().getName());
		} catch (NoSuchMethodException ex) {
			fail("Method '" + methodName + "' not found on object of class " + object.getClass().getName());
		} catch (IllegalArgumentException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#" + methodName + "()]");
		} catch (IllegalAccessException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#" + methodName + "()]");
		} catch (InvocationTargetException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#" + methodName + "()]");
		}

		// Return the value
		return value;
	}

	/**
	 * Obtains the item within the items whose property by methodName matches the
	 * input value.
	 * 
	 * @param <T>        Item type.
	 * @param items      Items to search.
	 * @param methodName Property on the item.
	 * @param value      Value of property the item should match.
	 * @return Item with the matching property.
	 */
	public static <T> T getItem(Collection<T> items, String methodName, Object value) {

		// Iterate over the items finding the matching item
		for (T item : items) {

			// Obtain the property value
			Object itemValue = getProperty(item, methodName);

			// Determine if matches
			if (value.equals(itemValue)) {
				// Found the item
				return item;
			}
		}

		// Did not find the item
		fail("Did not find item by property '" + methodName + "' for return value " + value);
		return null;
	}

}
