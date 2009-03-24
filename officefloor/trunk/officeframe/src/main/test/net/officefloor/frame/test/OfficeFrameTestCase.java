/*
 * Created on Dec 16, 2005
 */
package net.officefloor.frame.test;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import junit.framework.TestCase;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

/**
 * {@link junit.framework.TestCase}providing additional helper functions.
 * 
 * @author Daniel
 */
public abstract class OfficeFrameTestCase extends TestCase {

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

		} else if (root instanceof Collection) {
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

		} else if ((root.getClass().isPrimitive()) || (root instanceof Class)
				|| (root instanceof String) || (root instanceof Boolean)
				|| (root instanceof Byte) || (root instanceof Character)
				|| (root instanceof Short) || (root instanceof Integer)
				|| (root instanceof Long) || (root instanceof Float)
				|| (root instanceof Double)) {

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

				// Ignore hashCode
				if ("hashCode".equals(methodName)) {
					continue;
				}

				// Ignore toString
				if ("toString".equals(methodName)) {
					continue;
				}

				// Ignore getClass
				if ("getClass".equals(methodName)) {
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
						.getClass().getSimpleName()
						+ "." + methodName + "()", ignoreMethodNames, writer);
			}
		}
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
		assertGraph(expectedRoot, actualRoot, new HashMap<Object, Integer>(),
				"<root>", ignoreMethodNames);
	}

	/**
	 * Flags whether the {@link #assertGraph(O, O, Set, String)} exception has
	 * been logged.
	 */
	private static boolean isAssetGraphExceptionLogged = false;

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
			Map<Object, Integer> checkedObjects, String path,
			String[] ignoreMethodNames) throws Exception {

		// Reset
		isAssetGraphExceptionLogged = false;

		try {

			// Ensure checked only twice
			// (allows checking bi-directional references)
			if (checkedObjects.containsKey(expectedRoot)) {
				// Ensure only check twice at most
				int timesChecked = checkedObjects.get(expectedRoot).intValue();
				timesChecked++; // another check
				if (timesChecked > 2) {
					// Already checked twice
					return;
				}

				// Specify another check of object
				checkedObjects.put(expectedRoot, new Integer(timesChecked));
			} else {
				// First time accessed, therefore flag first time
				checkedObjects.put(expectedRoot, new Integer(1));
			}

			// Ensure matches
			if ((expectedRoot == null) && (actualRoot == null)) {
				// Match as both null
				return;
			} else if ((expectedRoot != null) && (actualRoot != null)) {
				// Both not null therefore check
				assertEquals("Path " + path + " type mismatch", expectedRoot
						.getClass(), actualRoot.getClass());

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

						// Ignore hashCode
						if ("hashCode".equals(methodName)) {
							continue;
						}

						// Ignore toString
						if ("toString".equals(methodName)) {
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
			Collection<O> actual, Map<Object, Integer> checkedObjects,
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
			assertEquals("Incorrect line " + lineNumber, actualLine,
					expectedLine);
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
		assertEquals("List lengths not match", expectedItems.length, list
				.size());

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
			@SuppressWarnings("unchecked")
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
			assertEquals("Incorrect property " + method, getProperty(expected,
					method), getProperty(actual, method));
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
	}

	/**
	 * Creates a mock object registering the {@link MockControl}of the mock
	 * object with registry for management.
	 * 
	 * @param classToMock
	 *            {@link Class}to be mocked.
	 * @return Mock object.
	 */
	@SuppressWarnings( { "unchecked" })
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

		// Handle primitive types
		if (recordedReturn instanceof Boolean) {
			control.setReturnValue(((Boolean) recordedReturn).booleanValue());
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

		// Record the arguments matcher
		this.control(mockObject).setMatcher(matcher);

		// Record the return
		this.recordReturn(mockObject, ignore, recordedReturn);
	}

	/**
	 * Flags all the mock objects to replay.
	 */
	protected final void replayMockObjects() {
		// Flag all mock objects to be replayed
		for (MockControl control : this.registry.values()) {
			control.replay();
		}
	}

	/**
	 * Verifies all mock objects.
	 */
	protected final void verifyMockObjects() {
		// Verify all mock objects
		for (MockControl control : this.registry.values()) {
			control.verify();
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
		paths.add(new File(new File(currentDirectory, "src/test/resources/"),
				relativePath)); // use src as target resources not copied
		paths.add(new File(new File(currentDirectory, "target/test-classes"),
				relativePath));
		paths.add(new File(new File(currentDirectory, "target/classes"),
				relativePath));
		paths.add(new File(new File(currentDirectory, "target"), relativePath));

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
	 * </p>
	 * <p>
	 * Note: this also searches the class path for the file.
	 * </p>
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
		return packageName.replaceAll("\\.", "/");
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
		// Obtain reader to file
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// Create the buffer to load contents of the file
		StringBuffer fileContents = new StringBuffer();

		// Load contents of file
		String fileLine = reader.readLine();
		while (fileLine != null) {
			// Add contents of line
			fileContents.append(fileLine);
			fileContents.append((char) Character.LINE_SEPARATOR);

			// Obtain the next line
			fileLine = reader.readLine();
		}

		// Close the reader (ensure no attachment to file)
		reader.close();

		// Return file contents
		return fileContents.toString();
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
	 * Prints a message regarding the test.
	 * 
	 * @param message
	 *            Message to be printed.
	 */
	public void printMessage(String message) {

		// Determine if show messages
		if (!Boolean.parseBoolean(System.getProperty("print.messages",
				Boolean.FALSE.toString()))) {
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