/*
 * Created on Dec 16, 2005
 */
package net.officefloor.frame.util;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

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
		assertGraph(expectedRoot, actualRoot, new HashSet<Object>(), "<root>",
				ignoreMethodNames);
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
			Set<Object> checkedObjects, String path, String[] ignoreMethodNames)
			throws Exception {

		// Reset
		isAssetGraphExceptionLogged = false;

		try {

			// Ensure not already checked
			if (checkedObjects.contains(expectedRoot)) {
				// Already checked
				return;
			}

			// Add to checked (as about to check)
			checkedObjects.add(expectedRoot);

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
			Collection<O> actual, Set<Object> checkedObjects, String path,
			String[] ignoreMethodNames) throws Exception {

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
	 * Obtains the property on the Object.
	 * 
	 * @param object
	 *            Object.
	 * @param methodName
	 *            Method name to obtain property value.
	 * @return Value of property.
	 */
	public static Object getProperty(Object object, String methodName) {
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
	 * Map of object to its {@link MockControl}.
	 */
	private final Map<Object, MockControl> registry = new HashMap<Object, MockControl>();

	/**
	 * Indicates whether to have verbose output.
	 */
	private boolean isVerbose = false;

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
	 * Obtains the {@link MockControl}for the input mock object.
	 * 
	 * @param mockObject
	 *            Mock object of the {@link MockControl}.
	 * @return Registered {@link MockControl}.
	 */
	public final MockControl control(Object mockObject) {
		return this.registry.get(mockObject);
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
	 * Prints a message regarding the test.
	 * 
	 * @param message
	 *            Message to be printed.
	 */
	public void printMessage(String message) {
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
		int value;
		while ((value = message.read()) != -1) {
			System.out.print((char) value);
		}
	}
}
