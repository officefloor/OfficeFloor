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
/*
 * Created on Dec 16, 2005
 */
package net.officefloor.frame.test;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

/**
 * {@link TestCase} providing additional helper functions.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFrameTestCase extends TestCase {

	/*
	 * ==================== TestCase =========================
	 */

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface StressTest {
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface GuiTest {
	}

	/**
	 * Indicates if the GUI is available.
	 * 
	 * @return <code>true</code> if the GUI is available.
	 */
	protected boolean isGuiAvailable() {
		return !GraphicsEnvironment.isHeadless();
	}

	@Override
	public void runBare() throws Throwable {

		// Determine if a graphical test
		try {
			Method testMethod = this.getClass().getMethod(this.getName());
			if (testMethod.getAnnotation(GuiTest.class) != null) {
				// Determine if headed environment (i.e. can run graphical test)
				if (!this.isGuiAvailable()) {
					System.out.println("NOT RUNNING GUI TEST "
							+ this.getClass().getSimpleName() + "."
							+ this.getName());
					return;
				}
			}
		} catch (Throwable ex) {
			// Ignore and not consider a graphical test
		}

		// Determine if a stress test
		boolean isStressTest = false;
		try {
			Method testMethod = this.getClass().getMethod(this.getName());
			if (testMethod.getAnnotation(StressTest.class) != null) {
				isStressTest = true; // is stress test
			}
		} catch (Throwable ex) {
			// Ignore and not consider a stress test
		}

		// Determine if run the stress test
		if (isStressTest) {
			if (this.isIgnoreStressTests()) {
				System.out.println("NOT RUNNING STRESS TEST "
						+ this.getClass().getSimpleName() + "."
						+ this.getName());
				return;
			}
		}

		try {
			// Run the test
			super.runBare();
		} catch (TestFail ex) {
			// Propagate cause of wrapper
			throw ex.getCause();
		} finally {
			// Provide start of verbose output
			if (this.isVerbose) {
				System.out.println("++++++++++++++++++++++++++ END:"
						+ this.getName() + " ++++++++++++++++++++++++++");
			}
		}
	}

	/**
	 * <p>
	 * Triggers failure due to exception.
	 * <p>
	 * This is useful to not have to provide throws clauses on tests.
	 * 
	 * @param ex
	 *            Failure.
	 * @return {@link RuntimeException} to allow <code>throw fail(ex);</code>
	 *         for compilation. Note this is never returned as always throws
	 *         exception.
	 * @throws TestFail
	 *             Handled by {@link #runBare()}.
	 */
	public static RuntimeException fail(Throwable ex) {
		// Propagate for runBare to pick up
		throw new TestFail(ex);
	}

	/**
	 * Wrapping error for failures.
	 */
	private static class TestFail extends Error {

		/**
		 * Initiate.
		 * 
		 * @param cause
		 *            Cause of failure.
		 */
		public TestFail(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * <p>
	 * Propagates the {@link Throwable} as either:
	 * <ol>
	 * <li>downcast {@link Exception}</li>
	 * <li>downcast {@link Error}</li>
	 * <li>{@link Throwable} wrapped with an {@link Exception}</li>
	 * </ol>
	 * <p>
	 * This is useful for such methods as {@link TestCase#tearDown()} that do
	 * not allow throwing {@link Throwable}.
	 * 
	 * @param ex
	 *            {@link Throwable} to propagate as an {@link Exception}.
	 */
	public static void throwException(Throwable ex) throws Exception {
		if (ex instanceof Exception) {
			throw (Exception) ex;
		} else if (ex instanceof Error) {
			throw (Error) ex;
		} else {
			throw new Exception(ex);
		}
	}

	/**
	 * <p>
	 * Creates a new {@link ClassLoader} from current process's java class path.
	 * <p>
	 * {@link Class} instances loaded via this {@link ClassLoader} will be
	 * different to the current {@link ClassLoader}. This is to allow testing
	 * multiple {@link ClassLoader} environments (such as Eclipse plug-ins).
	 * 
	 * @return New {@link ClassLoader}.
	 */
	public static ClassLoader createNewClassLoader() {

		try {
			// Create Class Loader for testing
			String[] classPathEntries = System.getProperty("java.class.path")
					.split(File.pathSeparator);
			URL[] urls = new URL[classPathEntries.length];
			for (int i = 0; i < urls.length; i++) {
				String classPathEntry = classPathEntries[i];
				classPathEntry = (classPathEntry.startsWith(File.separator) ? "file://"
						+ classPathEntry
						: classPathEntry);
				classPathEntry = (classPathEntry.endsWith(".jar") ? classPathEntry
						: classPathEntry + "/");
				urls[i] = new URL(classPathEntry);
			}
			ClassLoader classLoader = new URLClassLoader(urls, null);

			// Return the class loader
			return classLoader;

		} catch (MalformedURLException ex) {
			throw fail(ex);
		}
	}

	/**
	 * Displays the graph of objects starting at root.
	 * 
	 * @param root
	 *            Root of graph to display.
	 */
	public static void displayGraph(Object root) throws Exception {
		displayGraph(root, new String[0]);
	}

	/**
	 * Displays the graph of objects starting at root ignoring following
	 * verticies by the input method names.
	 * 
	 * @param root
	 *            Root of graph to display.
	 * @param ignoreMethodNames
	 *            Method names to ignore.
	 */
	public static void displayGraph(Object root, String... ignoreMethodNames)
			throws Exception {
		PrintWriter writer = new PrintWriter(System.out);
		displayGraph(root, new HashSet<Object>(), 0, "root", ignoreMethodNames,
				writer);
		writer.flush();
	}

	/**
	 * Displays the graph of objects starting at root.
	 * 
	 * @param root
	 *            Root of graph to display.
	 * @param displayedObjects
	 *            Set of objects already displayed.
	 * @param depth
	 *            Depth into the graph.
	 * @param path
	 *            Path from previous graph.
	 * @param ignoreMethodNames
	 *            Method names not to follow in graph for display.
	 * @param writer
	 *            Writer to output display.
	 */
	private static void displayGraph(Object root, Set<Object> displayedObjects,
			int depth, String path, String[] ignoreMethodNames,
			PrintWriter writer) throws Exception {

		// Display path
		for (int i = 0; i < depth; i++) {
			writer.print("  ");
		}
		writer.print(path);

		// Ensure not already displayed
		if (displayedObjects.contains(root)) {
			// Already checked
			writer.println(" ... (" + root + ")");
			return;
		}

		if (root == null) {
			// Display null
			writer.println(" = null");

		} else if (root instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) root;

			// Display that collection
			writer.println("[]");

			// Display collection items
			int index = 0;
			for (Object item : collection) {

				// Display collection item
				displayGraph(item, displayedObjects, (depth + 1), (path + "["
						+ index + "]"), ignoreMethodNames, writer);
				index++;
			}

		} else if ((root.getClass().isPrimitive())
				|| (root instanceof Class<?>) || (root instanceof String)
				|| (root instanceof Boolean) || (root instanceof Byte)
				|| (root instanceof Character) || (root instanceof Short)
				|| (root instanceof Integer) || (root instanceof Long)
				|| (root instanceof Float) || (root instanceof Double)) {

			// Display raw type
			writer.println(" = " + root);

		} else {

			// Add to displayed (as about to display)
			displayedObjects.add(root);

			// Contents below
			writer.println(" = " + root);

			// Do deep POJO
			for (Method method : root.getClass().getMethods()) {

				// Obtain the method name
				String methodName = method.getName();

				// Ignore Object methods
				if (Object.class.equals(method.getDeclaringClass())) {
					continue;
				}

				// Determine if to ignore pursing method in displaying
				boolean isIgnore = false;
				for (String ignoreMethodName : ignoreMethodNames) {
					if (methodName.equals(ignoreMethodName)) {
						isIgnore = true;
					}
				}
				if (isIgnore) {
					writer.println("- " + root.getClass().getSimpleName() + "."
							+ methodName + "() -");
					continue;
				}

				// Determine if accessor method
				if ((!Modifier.isPublic(method.getModifiers()))
						|| (method.getReturnType() == Void.TYPE)
						|| (method.getParameterTypes().length != 0)) {
					continue;
				}

				// Obtain the values of the accessors
				Object value = method.invoke(root, (Object[]) null);

				// Do deep display
				displayGraph(value, displayedObjects, (depth + 1), root
						.getClass().getSimpleName() + "." + methodName + "()",
						ignoreMethodNames, writer);
			}
		}
	}

	/**
	 * Operation for {@link #assertFail} that should fail.
	 */
	protected static interface FailOperation {

		/**
		 * Contains the operation that should fail.
		 * 
		 * @throws Throwable
		 *             Expected failure of the operation.
		 */
		void run() throws Throwable;
	}

	/**
	 * Asserts the failure.
	 * 
	 * @param operation
	 *            {@link FailOperation} that is expected fail.
	 * @param expectedFailureType
	 *            Expect type of failure.
	 * @return Actual failure for further assertions.
	 */
	@SuppressWarnings("unchecked")
	public static <F extends Throwable> F assertFail(FailOperation operation,
			Class<F> expectedFailureType) {
		try {
			operation.run();
			fail("Operation expected to fail with cause "
					+ expectedFailureType.getSimpleName());
			return null; // for compilation

		} catch (AssertionFailedError ex) {
			// Propagate unit test failure
			throw ex;
		} catch (Throwable ex) {
			// Ensure the correct type
			assertEquals("Incorrect cause of failure", expectedFailureType,
					ex.getClass());
			return (F) ex;
		}
	}

	/**
	 * Provides simplified facade to verify {@link Method} will fail.
	 * 
	 * @param expectedFailureType
	 *            Expected failure of method.
	 * @param object
	 *            Object to invoke {@link Method} on.
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @param parameters
	 *            Parameters for the {@link Method}.
	 * @return Actual failure for further assertions.
	 */
	public static <F extends Throwable> F assertFail(
			Class<F> expectedFailureType, final Object object,
			final String methodName, final Object... parameters) {
		try {
			// Obtain the listing of parameter types
			Class<?>[] parameterTypes = new Class[parameters.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				parameterTypes[i] = parameters[i].getClass();
			}

			// Obtain the method
			Method method = object.getClass().getMethod(methodName,
					parameterTypes);

			// Assert fail
			return assertFail(expectedFailureType, object, method, parameters);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Provides simplified facade to verify {@link Method} will fail.
	 * 
	 * @param expectedFailureType
	 *            Expected failure of method.
	 * @param object
	 *            Object to invoke {@link Method} on.
	 * @param method
	 *            {@link Method}.
	 * @param parameters
	 *            Parameters for the {@link Method}.
	 * @return Actual failure for further assertions.
	 */
	public static <F extends Throwable> F assertFail(
			Class<F> expectedFailureType, final Object object,
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
	 * @param message
	 *            Message.
	 * @param expected
	 *            Raw expected text.
	 * @param actual
	 *            Raw actual text.
	 */
	public static void assertTextEquals(String message, String expected,
			String actual) {
		String expectedText = createPlatformIndependentText(expected);
		String actualText = createPlatformIndependentText(actual);
		assertEquals(message, expectedText, actualText);
	}

	/**
	 * Creates the platform independent text for comparing.
	 * 
	 * @param text
	 *            Raw text.
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
	 * @param message
	 *            Message.
	 * @param expected
	 *            Raw expected text.
	 * @param actual
	 *            Raw actual text.
	 */
	public static void assertXmlEquals(String message, String expected,
			String actual) {
		String expectedXml = removeXmlWhiteSpacing(createPlatformIndependentText(expected));
		String actualXml = removeXmlWhiteSpacing(createPlatformIndependentText(actual));
		assertEquals(message, expectedXml, actualXml);
	}

	/**
	 * Removes the white spacing from the XML.
	 * 
	 * @param xml
	 *            XML.
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
	 * @param O
	 *            Type of root object for graph.
	 * @param expectedRoot
	 *            Expected root.
	 * @param actualRoot
	 *            Actual root.
	 */
	public synchronized static <O> void assertGraph(O expectedRoot, O actualRoot)
			throws Exception {
		assertGraph(expectedRoot, actualRoot, new String[0]);
	}

	/**
	 * Assets that the input graph is as expected.
	 * 
	 * @param O
	 *            Type of root object for graph.
	 * @param expectedRoot
	 *            Expected root.
	 * @param actualRoot
	 *            Actual root.
	 * @param ignoreMethodNames
	 *            Listing of methods to be ignored in checking.
	 */
	public synchronized static <O> void assertGraph(O expectedRoot,
			O actualRoot, String... ignoreMethodNames) throws Exception {
		assertGraph(expectedRoot, actualRoot,
				new HashMap<CheckedObject, Integer>(), "<root>",
				ignoreMethodNames);
	}

	/**
	 * Flags whether the {@link #assertGraph(O, O, Set, String)} exception has
	 * been logged.
	 */
	private static boolean isAssetGraphExceptionLogged = false;

	/**
	 * Wrapper around {@link Object} being checked to ensure
	 * {@link Object#equals(Object)} does not equate {@link Object} instances to
	 * be the same and not fully check the object graph.
	 */
	private static class CheckedObject {

		/**
		 * Object being checked.
		 */
		private final Object object;

		/**
		 * Initiate.
		 * 
		 * @param object
		 *            Object being checked.
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
			assertTrue("Must be CheckedObject " + obj,
					(obj instanceof CheckedObject));
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
	 * @param O
	 *            Type of root object for graph.
	 * @param expectedRoot
	 *            Expected root.
	 * @param actualRoot
	 *            Actual root.
	 * @param checkedObjects
	 *            Set of objects already checked to stop cyclic checking.
	 * @param path
	 *            Path to item failing check.
	 * @param ignoreMethodNames
	 *            Listing of methods to be ignored in checking.
	 */
	@SuppressWarnings("unchecked")
	private static <O> void assertGraph(O expectedRoot, O actualRoot,
			Map<CheckedObject, Integer> checkedObjects, String path,
			String[] ignoreMethodNames) throws Exception {

		// Reset
		isAssetGraphExceptionLogged = false;

		try {

			// Always check contents of:
			// - null
			// - Collection
			// - primitive types
			// (stops equals instances from matching)
			if ((expectedRoot != null)
					&& (!(expectedRoot instanceof Collection))
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
					checkedObjects.put(checkedObject, new Integer(times));
				} else {
					// First time accessed, therefore flag first time
					checkedObjects.put(checkedObject, new Integer(1));
				}
			}

			// Ensure matches
			if ((expectedRoot == null) && (actualRoot == null)) {
				// Match as both null
				return;
			} else if ((expectedRoot != null) && (actualRoot != null)) {
				// Both not null therefore ensure of same type
				assertEquals("Path " + path + " type mismatch",
						expectedRoot.getClass(), actualRoot.getClass());

				if (expectedRoot instanceof Class) {
					// Validate the same class
					assertEquals("Path " + path + " incorrect type",
							expectedRoot, actualRoot);
				} else if ((expectedRoot.getClass().isPrimitive())
						|| (expectedRoot instanceof String)
						|| (expectedRoot instanceof Boolean)
						|| (expectedRoot instanceof Byte)
						|| (expectedRoot instanceof Character)
						|| (expectedRoot instanceof Short)
						|| (expectedRoot instanceof Integer)
						|| (expectedRoot instanceof Long)
						|| (expectedRoot instanceof Float)
						|| (expectedRoot instanceof Double)) {
					// Do primitive comparison
					assertEquals("Path " + path + " mismatch", expectedRoot,
							actualRoot);
				} else if (expectedRoot instanceof Collection) {
					// Do deep collection comparison
					assertGraphCollection((Collection<Object>) expectedRoot,
							(Collection<Object>) actualRoot, checkedObjects,
							path, ignoreMethodNames);
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
						if ((!Modifier.isPublic(method.getModifiers()))
								|| (method.getReturnType() == Void.TYPE)
								|| (method.getParameterTypes().length != 0)) {
							continue;
						}

						// Obtain the values of the accessors
						Object expectedValue = method.invoke(expectedRoot,
								(Object[]) null);
						Object actualValue = method.invoke(actualRoot,
								(Object[]) null);

						// Do deep comparison
						assertGraph(expectedValue, actualValue, checkedObjects,
								path + "." + methodName + "()",
								ignoreMethodNames);
					}
				}
			} else {
				// One null while other not
				fail("Path " + path + " mismatch [e " + expectedRoot + ", a "
						+ actualRoot + "]");
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
	 * @param O
	 *            Type of root object for graph.
	 * @param expected
	 *            {@link Collection} of expected.
	 * @param actual
	 *            {@link Collection} of actual.
	 * @param checkedObjects
	 *            Set of objects already checked to stop cyclic checking.
	 * @param path
	 *            Path to item failing check.
	 * @param ignoreMethodNames
	 *            Listing of methods to be ignored in checking.
	 */
	@SuppressWarnings("unchecked")
	private static <O> void assertGraphCollection(Collection<O> expected,
			Collection<O> actual, Map<CheckedObject, Integer> checkedObjects,
			String path, String[] ignoreMethodNames) throws Exception {

		// Validate the size
		assertEquals("Path " + path + " incorrect size", expected.size(),
				actual.size());

		if (expected instanceof List) {
			// Downcast to list for checking
			List<Object> expectedList = (List<Object>) expected;
			List<Object> actualList = (List<Object>) actual;
			for (int i = 0; i < expectedList.size(); i++) {
				// Do deep comparison of item
				assertGraph(expectedList.get(i), actualList.get(i),
						checkedObjects, path + "[" + i + "]", ignoreMethodNames);
			}
		} else {
			// Unknown collection type
			fail("Path " + path + " unknown collection type "
					+ expected.getClass().getName());
		}
	}

	/**
	 * Asserts the contents of the input {@link File} instances are the same.
	 * 
	 * @param expected
	 *            Expected file.
	 * @param actual
	 *            Actual file.
	 * @throws IOException
	 *             If fails to read contents.
	 */
	public static void assertContents(File expected, File actual)
			throws IOException {
		assertContents(new FileReader(expected), new FileReader(actual));
	}

	/**
	 * Asserts the contents of the input {@link Reader} instances are the same.
	 * 
	 * @param expected
	 *            Expected content.
	 * @param actual
	 *            Actual content.
	 * @throws IOException
	 *             If fails to read contents.
	 */
	public static void assertContents(Reader expected, Reader actual)
			throws IOException {
		BufferedReader expectedReader = new BufferedReader(expected);
		BufferedReader actualReader = new BufferedReader(actual);
		String expectedLine;
		String actualLine;
		int lineNumber = 1;
		while ((actualLine = actualReader.readLine()) != null) {
			expectedLine = expectedReader.readLine();
			assertEquals("Incorrect line " + lineNumber, expectedLine,
					actualLine);
			lineNumber++;
		}
	}

	/**
	 * Asserts that the input list is as expected.
	 * 
	 * @param matcher
	 *            Matches the items of the list.
	 * @param list
	 *            List to be checked.
	 * @param expectedItems
	 *            Items expected to be in the list.
	 */
	public static <O> void assertList(ListItemMatcher<O> matcher, List<O> list,
			O... expectedItems) {

		// Ensure similar number of items in each list
		assertEquals("List lengths not match", expectedItems.length,
				list.size());

		// Ensure similar items
		for (int i = 0; i < expectedItems.length; i++) {
			matcher.match(i, expectedItems[i], list.get(i));
		}
	}

	/**
	 * Asserts that the input list equals the expected.
	 * 
	 * @param list
	 *            List to be checked.
	 * @param expectedItems
	 *            Items expected in the list.
	 */
	public static <O> void assertList(List<O> list, O... expectedItems) {
		assertList(new ListItemMatcher<O>() {
			public void match(int index, O expected, O actual) {
				assertEquals("Incorrect item " + index, expected, actual);
			}
		}, list, expectedItems);
	}

	/**
	 * Asserts that properties on items within list match.
	 * 
	 * @param methods
	 *            Method names to specify the properties on the items to match.
	 * @param list
	 *            List to be checked.
	 * @param expectedItems
	 *            Items expected in the list.
	 */
	public static <O> void assertList(final String[] methods, List<O> list,
			O... expectedItems) {
		assertList(new ListItemMatcher<O>() {
			public void match(int index, O expected, O actual) {
				// Match the properties
				for (String method : methods) {
					assertEquals("Incorrect property " + method + " for item "
							+ index, getProperty(expected, method),
							getProperty(actual, method));
				}
			}
		}, list, expectedItems);
	}

	/**
	 * Asserts that properties on items within the array match.
	 * 
	 * @param methods
	 *            Method names to specify the properties on the items to match.
	 * @param array
	 *            Array to be checked.
	 * @param expectedItems
	 *            Items expected in the array.
	 */
	public static <O> void assertList(final String[] methods, O[] array,
			O... expectedItems) {
		assertList(methods, Arrays.asList(array), expectedItems);
	}

	/**
	 * Asserts that properties on items within list match after the list is
	 * sorted.
	 * 
	 * @param sortMethod
	 *            Name of method on the items to sort the list by to ensure
	 *            match in order.
	 * @param methods
	 *            Method names to specify the properties on the items to match.
	 * @param list
	 *            List to be checked.
	 * @param expectedItems
	 *            Items expected in the list.
	 */
	public static <O> void assertList(final String sortMethod,
			String[] methods, List<O> list, O... expectedItems) {

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
	 * Asserts that properties on the input objects match for the specified
	 * methods.
	 * 
	 * @param expected
	 *            Expected item.
	 * @param actual
	 *            Actual item.
	 * @param methods
	 *            Method names to specify the properties on the item to match.
	 */
	public static <O> void assertProperties(O expected, O actual,
			String... methods) {
		// Match the properties
		for (String method : methods) {
			assertEquals("Incorrect property " + method,
					getProperty(expected, method), getProperty(actual, method));
		}
	}

	/**
	 * Obtains the property on the Object.
	 * 
	 * @param object
	 *            Object.
	 * @param methodName
	 *            Method name to obtain property value.
	 * @return Value of property.
	 */
	public static Object getProperty(Object object, String methodName) {

		// Ensure have an object to retrieve value
		assertNotNull("Can not source property '" + methodName
				+ "' from null object", object);

		Object value = null;
		try {
			// Find the method on the object
			Method method = object.getClass().getMethod(methodName,
					(Class[]) new Class[0]);

			// Obtain the property value
			value = method.invoke(object, new Object[0]);

		} catch (SecurityException ex) {
			fail("No access to method '" + methodName + "' on object of class "
					+ object.getClass().getName());
		} catch (NoSuchMethodException ex) {
			fail("Method '" + methodName + "' not found on object of class "
					+ object.getClass().getName());
		} catch (IllegalArgumentException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#"
					+ methodName + "()]");
		} catch (IllegalAccessException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#"
					+ methodName + "()]");
		} catch (InvocationTargetException ex) {
			fail(ex.getMessage() + " [" + object.getClass().getName() + "#"
					+ methodName + "()]");
		}

		// Return the value
		return value;
	}

	/**
	 * Obtains the item within the items whose property by methodName matches
	 * the input value.
	 * 
	 * @param items
	 *            Items to search.
	 * @param methodName
	 *            Property on the item.
	 * @param value
	 *            Value of property the item should match.
	 * @return Item with the matching property.
	 */
	public static <T> T getItem(Collection<T> items, String methodName,
			Object value) {

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
		fail("Did not find item by property '" + methodName
				+ "' for return value " + value);
		return null;
	}

	/**
	 * Map of object to its {@link MockControl}.
	 */
	private final Map<Object, MockControl> registry = new HashMap<Object, MockControl>();

	/**
	 * Indicates whether to have verbose output.
	 */
	private boolean isVerbose = false;

	/**
	 * Default constructor.
	 */
	public OfficeFrameTestCase() {
		super();
	}

	/**
	 * Initiate allowing specifying name of test.
	 * 
	 * @param name
	 *            Test name.
	 */
	public OfficeFrameTestCase(String name) {
		super(name);
	}

	/**
	 * Specifies to provide verbose output to aid in debugging.
	 * 
	 * @param isVerbose
	 *            <code>true</code> to turn on verbose output.
	 */
	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;

		// Provide start of verbose output
		if (this.isVerbose) {
			System.out.println("++++++++++++++++++++++++++ START:"
					+ this.getName() + " ++++++++++++++++++++++++++");
		}
	}

	/**
	 * Creates a mock object registering the {@link MockControl}of the mock
	 * object with registry for management.
	 * 
	 * @param classToMock
	 *            {@link Class} to be mocked.
	 * @return Mock object.
	 */
	@SuppressWarnings({ "unchecked" })
	public final <M> M createMock(Class<M> classToMock) {
		// Create the control
		MockControl mockControl = MockControl.createStrictControl(classToMock);

		// Obtain the mock object
		M mockObject = (M) mockControl.getMock();

		// Output details of mock
		if (this.isVerbose) {
			printMessage("mock '" + mockObject.getClass().getName()
					+ "' is of class " + classToMock.getSimpleName() + " ["
					+ classToMock.getName() + "]");
		}

		// Register the mock object
		this.registerMockObject(mockObject, mockControl);

		// Return the mocked object
		return mockObject;
	}

	/**
	 * Creates a mock object that synchronises on its {@link MockControl} before
	 * making any method calls.
	 * 
	 * @param interfaceToMock
	 *            {@link Class} to mock.
	 * @return Mock object.
	 */
	@SuppressWarnings("unchecked")
	public final <M> M createSynchronizedMock(Class<M> interfaceToMock) {

		// Create the mock object
		final M mockObject = this.createMock(interfaceToMock);

		// Obtain the control for the mock to synchronise on
		final MockControl control = this.control(mockObject);

		// Create a synchronised proxy wrapper around mock
		M proxy = (M) Proxy.newProxyInstance(interfaceToMock.getClassLoader(),
				new Class[] { interfaceToMock }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						// Invoke method with lock on control
						try {
							synchronized (control) {
								return method.invoke(mockObject, args);
							}
						} catch (InvocationTargetException ex) {
							// Propagate cause of invocation failure
							throw ex.getCause();
						}
					}
				});

		// Register the synchronized mock object
		this.registerMockObject(proxy, control);

		// Return the synchronised proxy
		return proxy;
	}

	/**
	 * Registers the object and its {@link MockControl}to be managed.
	 * 
	 * @param object
	 *            Mock object.
	 * @param mockControl
	 *            {@link MockControl} of the mock object.
	 */
	public final void registerMockObject(Object mockObject,
			MockControl mockControl) {
		this.registry.put(mockObject, mockControl);
	}

	/**
	 * Obtains the {@link MockControl} for the input mock object.
	 * 
	 * @param mockObject
	 *            Mock object of the {@link MockControl}.
	 * @return Registered {@link MockControl}.
	 */
	public final MockControl control(Object mockObject) {
		return this.registry.get(mockObject);
	}

	/**
	 * Convenience method to record a method and its return on a mock object.
	 * 
	 * @param mockObject
	 *            Mock object.
	 * @param ignore
	 *            Result of operation on the mock object. This is only provided
	 *            to obtain correct return type for recording return.
	 * @param recordedReturn
	 *            Value that is recorded to be returned from the mock object.
	 */
	public final <T> void recordReturn(Object mockObject, T ignore,
			T recordedReturn) {
		// Obtain the control
		MockControl control = this.control(mockObject);
		synchronized (control) {

			// Handle primitive types
			if (recordedReturn instanceof Boolean) {
				control.setReturnValue(((Boolean) recordedReturn)
						.booleanValue());
			} else if (recordedReturn instanceof Character) {
				control.setReturnValue(((Character) recordedReturn).charValue());
			} else if (recordedReturn instanceof Short) {
				control.setReturnValue(((Short) recordedReturn).shortValue());
			} else if (recordedReturn instanceof Integer) {
				control.setReturnValue(((Integer) recordedReturn).intValue());
			} else if (recordedReturn instanceof Long) {
				control.setReturnValue(((Long) recordedReturn).longValue());
			} else if (recordedReturn instanceof Float) {
				control.setReturnValue(((Float) recordedReturn).floatValue());
			} else if (recordedReturn instanceof Double) {
				control.setReturnValue(((Double) recordedReturn).doubleValue());
			} else {
				// Not primitive, so record as object
				control.setReturnValue(recordedReturn);
			}
		}
	}

	/**
	 * Convenience method to record a method, an {@link ArgumentsMatcher} and
	 * return value.
	 * 
	 * @param mockObject
	 *            Mock object.
	 * @param ignore
	 *            Result of operation on the mock object. This is only provided
	 *            to obtain correct return type for recording return.
	 * @param recordedReturn
	 *            Value that is recorded to be returned from the mock object.
	 * @param matcher
	 *            {@link ArgumentsMatcher}.
	 */
	public final <T> void recordReturn(Object mockObject, T ignore,
			T recordedReturn, ArgumentsMatcher matcher) {
		// Obtain the control
		MockControl control = this.control(mockObject);
		synchronized (control) {

			// Record the arguments matcher
			control.setMatcher(matcher);

			// Record the return
			this.recordReturn(mockObject, ignore, recordedReturn);
		}
	}

	/**
	 * Flags all the mock objects to replay.
	 */
	protected final void replayMockObjects() {
		// Flag all mock objects to be replayed
		for (MockControl control : this.registry.values()) {
			synchronized (control) {
				control.replay();
			}
		}
	}

	/**
	 * Verifies all mock objects.
	 */
	protected final void verifyMockObjects() {
		// Verify all mock objects
		for (MockControl control : this.registry.values()) {
			synchronized (control) {
				control.verify();
			}
		}
	}

	/**
	 * Obtains the file at the relative path.
	 * 
	 * @param relativePath
	 *            Relative path to the file.
	 * @return {@link File}.
	 * @throws FileNotFoundException
	 *             If file could not be found.
	 */
	public File findFile(String relativePath) throws FileNotFoundException {

		// Obtain the current directory
		File currentDirectory = new File(".");

		// Create the listing of paths to find the file
		List<File> paths = new LinkedList<File>();
		paths.add(new File(currentDirectory, relativePath));
		paths.add(new File(new File(currentDirectory, "target/test-classes"),
				relativePath));
		paths.add(new File(new File(currentDirectory, "target/classes"),
				relativePath));

		// As last resource, use src as target resources not copied
		paths.add(new File(new File(currentDirectory, "src/test/resources/"),
				relativePath));

		// Obtain the file
		for (File path : paths) {
			if (path.exists()) {
				return path;
			}
		}

		// File not found
		throw new FileNotFoundException(
				"Can not find file with relative path '" + relativePath + "'");
	}

	/**
	 * Obtains the file by the input file name located in the package of the
	 * input class.
	 * 
	 * @param packageClass
	 *            Class to obtain the relative path from for its package.
	 * @param fileName
	 *            Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException
	 *             Should the file not be found.
	 */
	public File findFile(Class<?> packageClass, String fileName)
			throws FileNotFoundException {

		// Obtain the relative file path
		File relativePath = new File(this.getPackageRelativePath(packageClass),
				fileName);

		// Obtain the file
		return this.findFile(relativePath.getPath());
	}

	/**
	 * Obtains the file location of the input file located in the package of the
	 * input class.
	 * 
	 * @param packageClass
	 *            Class to obtain the relative path from for its package.
	 * @param fileName
	 *            Name of the file within the package directory.
	 * @return Path to the file.
	 */
	public String getFileLocation(Class<?> packageClass, String fileName) {
		return this.getPackageRelativePath(packageClass) + "/" + fileName;
	}

	/**
	 * Creates the input directory.
	 * 
	 * @param directory
	 *            Directory to be cleared.
	 */
	public void clearDirectory(File directory) {

		// Ensure have a directory
		if (directory == null) {
			return;
		}

		// Clear only if directory
		if (directory.isDirectory()) {
			// Clear the directory
			for (File child : directory.listFiles()) {
				deleteDirectory(child);
			}
		}
	}

	/**
	 * Deletes the input directory.
	 * 
	 * @param directory
	 *            Directory to be deleted.
	 */
	public void deleteDirectory(File directory) {

		// Ensure have a directory
		if (directory == null) {
			return;
		}

		// Determine if directory
		if (directory.isDirectory()) {
			// Recursively delete children of directory
			for (File child : directory.listFiles()) {
				deleteDirectory(child);
			}
		}

		// Delete the directory (or file)
		assertTrue("Failed deleting " + directory.getPath(), directory.delete());
	}

	/**
	 * Copies the contents of the <code>source</code> directory to the
	 * <code>target</code> directory.
	 * 
	 * @param source
	 *            Source directory.
	 * @param target
	 *            Target directory.
	 * @throws IOException
	 *             If fails to copy the directory.
	 */
	public void copyDirectory(File source, File target) throws IOException {

		// Ensure the source directory exists
		assertTrue("Can not find source directory " + source.getAbsolutePath(),
				source.isDirectory());

		// Ensure the target directory is available
		if (target.exists()) {
			// Ensure is a directory
			assertTrue("Target is not a directory " + target.getAbsolutePath(),
					target.isDirectory());
		} else {
			// Create the target directory
			target.mkdir();
		}

		// Copy the files of the source directory to the target directory
		for (File file : source.listFiles()) {
			if (file.isDirectory()) {
				// Recursively copy sub directories
				this.copyDirectory(new File(source, file.getName()), new File(
						target, file.getName()));
			} else {
				// Copy the file
				InputStream reader = new FileInputStream(file);
				OutputStream writer = new FileOutputStream(new File(target,
						file.getName()));
				int value;
				while ((value = reader.read()) != -1) {
					writer.write((byte) value);
				}
				writer.close();
				reader.close();
			}
		}
	}

	/**
	 * <p>
	 * Obtains the input stream to the file by the input file name located in
	 * the package of the input class.
	 * <p>
	 * Note: this also searches the class path for the file.
	 * 
	 * @param packageClass
	 *            Class to obtain the relative path from for its package.
	 * @param fileName
	 *            Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException
	 *             Should the file not be found.
	 */
	public InputStream findInputStream(Class<?> packageClass, String fileName)
			throws FileNotFoundException {

		// Obtain the relative file path
		File relativePath = new File(this.getPackageRelativePath(packageClass),
				fileName);

		// Attempt to obtain input stream to file from class path
		InputStream inputStream = ClassLoader
				.getSystemResourceAsStream(relativePath.getPath());
		if (inputStream != null) {
			return inputStream;
		}

		// Not found on class path, thus obtain via finding the file
		return new FileInputStream(this.findFile(relativePath.getPath()));
	}

	/**
	 * Obtains the relative path of the package of the class.
	 * 
	 * @param packageClass
	 *            Class to obtain the relative path from for its package.
	 * @return Relative path of class's package.
	 */
	public String getPackageRelativePath(Class<?> packageClass) {
		// Obtain package
		String packageName = packageClass.getPackage().getName();

		// Return package name as relative path
		return packageName.replace('.', '/');
	}

	/**
	 * Obtains the contents of the output file.
	 * 
	 * @param file
	 *            File to obtain contents from.
	 * @return Contents of the output file.
	 * @throws FileNotFoundException
	 *             Should output file not yet be created.
	 * @throws IOException
	 *             Should fail to read from output file.
	 */
	public String getFileContents(File file) throws FileNotFoundException,
			IOException {

		// Read in contents of file
		StringWriter contents = new StringWriter();
		Reader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			contents.write(value);
		}
		reader.close();

		// Return file contents
		return contents.toString();
	}

	/**
	 * Creates the target file with the content.
	 * 
	 * @param content
	 *            Content for the file.
	 * @param target
	 *            Taret file.
	 * @throws IOException
	 *             If fails to create.
	 */
	public void createFile(File target, InputStream content) throws IOException {

		// Ensure the target file does not exist
		if (target.exists()) {
			throw new IOException("Target file already exists ["
					+ target.getAbsolutePath() + "]");
		}

		// Load the file content
		OutputStream outputStream = new FileOutputStream(target);
		int value;
		while ((value = content.read()) != -1) {
			outputStream.write(value);
		}
		outputStream.close();
	}

	/**
	 * Facade helper function for invoking {@link Thread#sleep(long)}.
	 * 
	 * @param time
	 *            Sleep time in seconds.
	 */
	public void sleep(int time) {
		try {
			Thread.sleep(time * 1000);
		} catch (InterruptedException ex) {
			fail("Sleep interrupted: " + ex.getMessage());
		}
	}

	/**
	 * Facade method to timeout operations after 3 seconds.
	 * 
	 * @param startTime
	 *            Start time from {@link System#currentTimeMillis()}.
	 */
	public void timeout(long startTime) {
		this.timeout(startTime, 3);
	}

	/**
	 * Facade method to timeout operations after a second.
	 * 
	 * @param startTime
	 *            Start time from {@link System#currentTimeMillis()}.
	 * @param millisecondsToRun
	 *            Milliseconds to run before timeout.
	 */
	public void timeout(long startTime, int secondsToRun) {
		if ((System.currentTimeMillis() - startTime) > (secondsToRun * 1000)) {
			fail("TIME OUT after " + secondsToRun + " seconds");
		}
	}

	/**
	 * Determines if printing messages.
	 * 
	 * @return <code>true</code> to print messages.
	 */
	protected boolean isPrintMessages() {
		return Boolean.parseBoolean(System.getProperty("print.messages",
				Boolean.FALSE.toString())) || this.isVerbose;
	}

	/**
	 * <p>
	 * Indicates if not to run stress tests.
	 * <p>
	 * Stress tests should normally be run, but in cases of quick unit testing
	 * running for functionality the stress tests can reduce turn around time
	 * and subsequently the effectiveness of the tests. This is therefore
	 * provided to maintain effectiveness of unit tests.
	 * 
	 * @return <code>true</code> to ignore doing a stress test.
	 */
	private boolean isIgnoreStressTests() {
		return Boolean.parseBoolean(System.getProperty("ignore.stress.tests",
				Boolean.FALSE.toString()));
	}

	/**
	 * Prints heap memory details.
	 */
	public void printHeapMemoryDiagnostics() {

		// Only do heap diagnosis if print messages
		if (!this.isPrintMessages()) {
			return; // do not do heap diagnosis
		}

		// Obtain the memory management bean
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

		// Do best to garbage collect to get clearer results
		memoryBean.gc(); // puts objects on finalise queue
		memoryBean.gc(); // clears the finalise queue

		// Obtain the heap diagnosis details
		MemoryUsage heap = memoryBean.getHeapMemoryUsage();
		float usedPercentage = (heap.getUsed() / (float) heap.getMax());

		// Print the results
		NumberFormat format = NumberFormat.getPercentInstance();
		this.printMessage("HEAP: " + format.format(usedPercentage) + " (used="
				+ this.getMemorySize(heap.getUsed()) + ", max="
				+ this.getMemorySize(heap.getMax()) + ", init="
				+ this.getMemorySize(heap.getInit()) + ", commit="
				+ this.getMemorySize(heap.getCommitted()) + ", fq="
				+ memoryBean.getObjectPendingFinalizationCount() + ")");
	}

	/**
	 * Obtains the memory size in human readable form.
	 * 
	 * @param memorySize
	 *            Memory size in bytes.
	 * @return Memory size in human readable form.
	 */
	private String getMemorySize(long memorySize) {

		final long gigabyteSize = 1 << 30;
		final long megabyteSize = 1 << 20;
		final long kilobyteSize = 1 << 10;

		if (memorySize >= gigabyteSize) {
			return (memorySize / gigabyteSize) + "g";
		} else if (memorySize >= megabyteSize) {
			return (memorySize / megabyteSize) + "m";
		} else if (memorySize >= kilobyteSize) {
			return (memorySize / kilobyteSize) + "k";
		} else {
			return memorySize + "b";
		}
	}

	/**
	 * Obtains run time in human readable form.
	 * 
	 * @param startTime
	 *            Start time of running.
	 * @return Run time in human readable form.
	 */
	public String getDisplayRunTime(long startTime) {

		// Obtain the run time in milliseconds
		long runTime = (System.currentTimeMillis() - startTime);

		final long milliseconds = 1;
		final long seconds = (1000 * milliseconds);
		final long minutes = (60 * seconds);

		if (runTime < seconds) {
			return (runTime) + "ms";
		} else if (runTime < minutes) {
			return (runTime / seconds) + "s";
		} else {
			return (runTime / minutes) + " minutes";
		}
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message
	 *            Message to be printed.
	 */
	public void printMessage(String message) {

		// Determine if show messages
		if (!this.isPrintMessages()) {
			return; // do no print messages
		}

		// Print the message
		System.out.println(this.getClass().getSimpleName() + "."
				+ this.getName() + ": " + message);
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message
	 *            Message to be printed.
	 * @throws IOException
	 *             If fails to print message.
	 */
	public void printMessage(InputStream message) throws IOException {
		this.printMessage(new InputStreamReader(message));
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message
	 *            Message to be printed.
	 * @throws IOException
	 *             If fails to print message.
	 */
	public void printMessage(Reader message) throws IOException {
		StringWriter buffer = new StringWriter();
		for (int value = message.read(); value != -1; value = message.read()) {
			buffer.append((char) value);
		}
		this.printMessage(buffer.toString());
	}

}