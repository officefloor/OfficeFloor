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

package net.officefloor.frame.test;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import junit.framework.TestCase;
import net.officefloor.frame.test.match.ArgumentsMatcher;
import net.officefloor.test.SkipUtil;

/**
 * {@link TestCase} providing additional helper functions.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class OfficeFrameTestCase extends TestCase {

	/**
	 * End line.
	 */
	protected static String END_OF_LINE = System.getProperty("line.separator");

	/**
	 * {@link Package} name of the extra class for the new {@link ClassLoader}.
	 */
	public static final String CLASS_LOADER_EXTRA_PACKAGE_NAME = ClassLoaderTestSupport.CLASS_LOADER_EXTRA_PACKAGE_NAME;

	/**
	 * {@link Class} name of the extra class for the new {@link ClassLoader}.
	 */
	public static final String CLASS_LOADER_EXTRA_CLASS_NAME = ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME;

	/*
	 * ==================== TestCase =========================
	 */

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface StressTest {
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface GuiTest {
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface UsesDockerTest {
	}

	@Retention(RetentionPolicy.RUNTIME)
	protected static @interface UsesGCloudTest {
	}

	@Override
	public void runBare() throws Throwable {

		// Determines if test annotated with type
		Function<Class<? extends Annotation>, Boolean> isTestAnnotatedWith = (annotationType) -> {
			try {
				Method testMethod = this.getClass().getMethod(this.getName());
				return testMethod.isAnnotationPresent(annotationType)
						|| this.getClass().isAnnotationPresent(annotationType);
			} catch (Throwable ex) {
				// Ignore and not annotated
				return false;
			}
		};

		// Determine if a graphical test
		if (isTestAnnotatedWith.apply(GuiTest.class) && (!this.isGuiAvailable())) {
			System.out.println("NOT RUNNING GUI TEST " + this.getClass().getSimpleName() + "." + this.getName());
			return;
		}

		// Determine if run stress test
		if (isTestAnnotatedWith.apply(StressTest.class) && isSkipStressTests()) {
			System.out.println("NOT RUNNING STRESS TEST " + this.getClass().getSimpleName() + "." + this.getName());
			return;
		}

		// Determine if run using docker
		if (isTestAnnotatedWith.apply(UsesDockerTest.class) && isSkipTestsUsingDocker()) {
			System.out
					.println("NOT RUNNING TEST USING DOCKER " + this.getClass().getSimpleName() + "." + this.getName());
			return;
		}

		// Determine if run using gcloud
		if (isTestAnnotatedWith.apply(UsesGCloudTest.class) && isSkipTestsUsingGCloud()) {
			System.out
					.println("NOT RUNNING TEST USING GCLOUD " + this.getClass().getSimpleName() + "." + this.getName());
			return;
		}

		// Run the test
		this.logTestSupport.beforeAll();
		try {
			super.runBare();
		} finally {
			this.logTestSupport.afterAll();
		}
	}

	/**
	 * <p>
	 * Indicates if not to run stress tests.
	 * <p>
	 * Stress tests should normally be run, but in cases of quick unit testing
	 * running for functionality the stress tests can reduce turn around time and
	 * subsequently the effectiveness of the tests. This is therefore provided to
	 * maintain effectiveness of unit tests.
	 * <p>
	 * Furthermore, builds time out on Travis so avoid running.
	 * 
	 * @return <code>true</code> to ignore doing a stress test.
	 */
	public static boolean isSkipStressTests() {
		return SkipUtil.isSkipStressTests();
	}

	/**
	 * <p>
	 * Indicates if not to run tests using docker.
	 * <p>
	 * Some environments do not support docker, so this enables disabling these
	 * tests.
	 * 
	 * @return <code>true</code> to ignore doing a docker test.
	 */
	public static boolean isSkipTestsUsingDocker() {
		return SkipUtil.isSkipTestsUsingDocker();
	}

	/**
	 * <p>
	 * Indicates if not to run tests using GCloud (Google Cloud).
	 * <p>
	 * Some environments do not have GCloud available, so this enables disabling
	 * these tests.
	 * 
	 * @return <code>true</code> to ignore doing a GCloud test.
	 */
	public static boolean isSkipTestsUsingGCloud() {
		return SkipUtil.isSkipTestsUsingGCloud();
	}

	/**
	 * <p>
	 * Triggers failure due to exception.
	 * <p>
	 * This is useful to not have to provide throws clauses on tests.
	 * 
	 * @param ex Failure.
	 * @return {@link RuntimeException} to allow <code>throw fail(ex);</code> for
	 *         compilation. Note this is never returned as always throws exception.
	 * @throws TestFail Handled by {@link #runBare()}.
	 */
	public static RuntimeException fail(Throwable ex) {
		return Assertions.fail(ex);
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
	 * This is useful for such methods as {@link TestCase#tearDown()} that do not
	 * allow throwing {@link Throwable}.
	 * 
	 * @param ex {@link Throwable} to propagate as an {@link Exception}.
	 * @throws Exception The failure.
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
	public static <F extends Throwable> F assertFail(FailOperation operation, Class<F> expectedFailureType) {
		return Assertions.assertFail(() -> operation.run(), expectedFailureType);
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
		return Assertions.assertFail(expectedFailureType, object, methodName, parameters);
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
		return Assertions.assertFail(expectedFailureType, object, method, parameters);
	}

	/**
	 * Asserts the input texts match taking into account platform differences.
	 * 
	 * @param message  Message.
	 * @param expected Raw expected text.
	 * @param actual   Raw actual text.
	 */
	public static void assertTextEquals(String message, String expected, String actual) {
		Assertions.assertTextEquals(expected, actual, message);
	}

	/**
	 * Creates the platform independent text for comparing.
	 * 
	 * @param rawText Raw text.
	 * @return Platform independent text.
	 */
	public static String createPlatformIndependentText(String rawText) {
		return Assertions.createPlatformIndependentText(rawText);
	}

	/**
	 * Asserts the input XML's match with white spacing removed.
	 * 
	 * @param message  Message.
	 * @param expected Raw expected text.
	 * @param actual   Raw actual text.
	 */
	public static void assertXmlEquals(String message, String expected, String actual) {
		Assertions.assertXmlEquals(expected, actual, message);
	}

	/**
	 * Removes the white spacing from the XML.
	 * 
	 * @param xml XML.
	 * @return XML with white spacing removed.
	 */
	public static String removeXmlWhiteSpacing(String xml) {
		return Assertions.removeXmlWhiteSpacing(xml);
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
	public static <O> void assertGraph(O expectedRoot, O actualRoot, String... ignoreMethodNames) throws Exception {
		Assertions.assertGraph(expectedRoot, actualRoot, ignoreMethodNames);
	}

	/**
	 * Asserts the contents of the input {@link File} instances are the same.
	 * 
	 * @param expected Expected file.
	 * @param actual   Actual file.
	 * @throws IOException If fails to read contents.
	 */
	public static void assertContents(File expected, File actual) throws IOException {
		Assertions.assertContents(expected, actual);
	}

	/**
	 * Asserts the contents of the input {@link Reader} instances are the same.
	 * 
	 * @param expected Expected content.
	 * @param actual   Actual content.
	 */
	public static void assertContents(Reader expected, Reader actual) {
		Assertions.assertContents(expected, actual);
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
		Assertions.assertList(matcher, list, expectedItems);
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
		Assertions.assertList(list, expectedItems);
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
		Assertions.assertList(methods, list, expectedItems);
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
		Assertions.assertList(methods, array, expectedItems);
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
		Assertions.assertList(sortMethod, methods, list, expectedItems);
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
		Assertions.assertProperties(expected, actual, methods);
	}

	/**
	 * Obtains the property on the Object.
	 * 
	 * @param object     Object.
	 * @param methodName Method name to obtain property value.
	 * @return Value of property.
	 */
	public static Object getProperty(Object object, String methodName) {
		return Assertions.getProperty(object, methodName);
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
		return Assertions.getItem(items, methodName, value);
	}

	/**
	 * {@link ThreadedTestSupport}.
	 */
	public final ThreadedTestSupport threadedTestSupport = new ThreadedTestSupport();

	/**
	 * {@link FileTestSupport}.
	 */
	public final FileTestSupport fileTestSupport = new FileTestSupport();

	/**
	 * {@link ClassLoaderTestSupport}.
	 */
	public final ClassLoaderTestSupport classLoaderTestSupport = new ClassLoaderTestSupport(this.fileTestSupport);

	/**
	 * {@link LogTestSupport}.
	 */
	public final LogTestSupport logTestSupport = new LogTestSupport();

	/**
	 * {@link MockTestSupport}.
	 */
	public final MockTestSupport mockTestSupport = new MockTestSupport(this.logTestSupport);

	/**
	 * Default constructor, so will use {@link #setName(String)}.
	 */
	public OfficeFrameTestCase() {
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		this.logTestSupport.setTestName(this.getClass().getSimpleName() + "." + name);
	}

	/**
	 * Initiate allowing specifying name of test.
	 * 
	 * @param name Test name.
	 */
	public OfficeFrameTestCase(String name) {
		super(name);
		this.setName(name);
	}

	/**
	 * Indicates if the GUI is available.
	 * 
	 * @return <code>true</code> if the GUI is available.
	 */
	protected boolean isGuiAvailable() {
		return !GraphicsEnvironment.isHeadless();
	}

	/**
	 * Specifies to provide verbose output to aid in debugging.
	 * 
	 * @param isVerbose <code>true</code> to turn on verbose output.
	 */
	public void setVerbose(boolean isVerbose) {
		this.logTestSupport.setVerbose(isVerbose);
	}

	/**
	 * Specifies to provide debug verbose output to aid in debugging.
	 */
	public void setDebugVerbose() {
		this.logTestSupport.setDebugVerbose();
	}

	/**
	 * Turns on logging of GC as part of test.
	 */
	public void setLogGC() {
		this.logTestSupport.setLogGC();
	}

	/**
	 * <p>
	 * Creates a new {@link ClassLoader} from current process's java class path.
	 * <p>
	 * {@link Class} instances loaded via this {@link ClassLoader} will be different
	 * to the current {@link ClassLoader}. This is to allow testing multiple
	 * {@link ClassLoader} environments (such as Eclipse plug-ins).
	 * 
	 * @return New {@link ClassLoader}.
	 */
	public ClassLoader createNewClassLoader() {
		return this.classLoaderTestSupport.createNewClassLoader();
	}

	/**
	 * Displays the graph of objects starting at root.
	 * 
	 * @param root Root of graph to display.
	 * @throws Exception If fails.
	 */
	public void displayGraph(Object root) throws Exception {
		this.logTestSupport.displayGraph(root);
	}

	/**
	 * Displays the graph of objects starting at root ignoring following verticies
	 * by the input method names.
	 * 
	 * @param root              Root of graph to display.
	 * @param ignoreMethodNames Method names to ignore.
	 * @throws Exception If fails.
	 */
	public void displayGraph(Object root, String... ignoreMethodNames) throws Exception {
		this.logTestSupport.displayGraph(root, ignoreMethodNames);
	}

	/**
	 * Creates a mock object registering the mock object with registry for
	 * management.
	 * 
	 * @param <M>         Interface type.
	 * @param classToMock {@link Class} to be mocked.
	 * @return Mock object.
	 */
	public final <M> M createMock(Class<M> classToMock) {
		return this.mockTestSupport.createMock(classToMock);
	}

	/**
	 * Creates a thread safe mock object.
	 * 
	 * @param <M>             Interface type.
	 * @param interfaceToMock {@link Class} to mock.
	 * @return Mock object.
	 */
	public final <M> M createSynchronizedMock(Class<M> interfaceToMock) {
		return this.mockTestSupport.createSynchronizedMock(interfaceToMock);
	}

	/**
	 * Wraps a parameter value when attempting to capture.
	 * 
	 * @param <T>   Value type.
	 * @param value Value.
	 * @return Value for parameter.
	 */
	public <T> T param(T value) {
		return this.mockTestSupport.param(value);
	}

	/**
	 * Wraps a parameter type expected.
	 * 
	 * @param <T>  Value type.
	 * @param type Expected type.
	 * @return Value for parameter.
	 */
	public <T> T paramType(Class<T> type) {
		return this.mockTestSupport.paramType(type);
	}

	/**
	 * Convenience method to record a method and its return on a mock object.
	 * 
	 * @param <T>            Expected result type.
	 * @param mockObject     Mock object.
	 * @param ignore         Result of operation on the mock object. This is only
	 *                       provided to obtain correct return type for recording
	 *                       return.
	 * @param recordedReturn Value that is recorded to be returned from the mock
	 *                       object.
	 */
	public final <T> void recordReturn(Object mockObject, T ignore, T recordedReturn) {
		this.mockTestSupport.recordReturn(mockObject, ignore, recordedReturn);
	}

	/**
	 * Convenience method to record a method, an {@link ArgumentsMatcher} and return
	 * value.
	 *
	 * @param <T>            Expected result type.
	 * @param mockObject     Mock object.
	 * @param ignore         Result of operation on the mock object. This is only
	 *                       provided to obtain correct return type for recording
	 *                       return.
	 * @param recordedReturn Value that is recorded to be returned from the mock
	 *                       object.
	 * @param matcher        {@link ArgumentsMatcher}.
	 */
	public final <T> void recordReturn(Object mockObject, T ignore, T recordedReturn, ArgumentsMatcher matcher) {
		this.mockTestSupport.recordReturn(mockObject, ignore, recordedReturn, matcher);
	}

	/**
	 * Convenience method to record void method.
	 * 
	 * @param mockObject Mock object.
	 * @param matcher    {@link ArgumentsMatcher}.
	 */
	public final void recordVoid(Object mockObject, ArgumentsMatcher matcher) {
		this.mockTestSupport.recordVoid(mockObject, matcher);
	}

	/**
	 * Convenience method to record an {@link Exception}.
	 * 
	 * @param <T>        Expected result type.
	 * @param mockObject Mock object.
	 * @param ignore     Result of operation on the mock object. This is only
	 *                   provided to obtain correct return type for recording
	 *                   return.
	 * @param exception  {@link Throwable}.
	 */
	public final <T> void recordThrows(Object mockObject, T ignore, Throwable exception) {
		this.mockTestSupport.recordThrows(mockObject, ignore, exception);
	}

	/**
	 * Flags all the mock objects to replay.
	 */
	protected final void replayMockObjects() {
		this.mockTestSupport.replayMockObjects();
	}

	/**
	 * Verifies all mock objects.
	 */
	protected final void verifyMockObjects() {
		this.mockTestSupport.verifyMockObjects();
	}

	/**
	 * Test logic interface.
	 * 
	 * @param <R> Return type.
	 * @param <T> Possible {@link Throwable}.
	 */
	protected static interface TestLogic<R, T extends Throwable> {
		R run() throws T;
	}

	/**
	 * Undertakes test wrapping with mock object replay and verify.
	 * 
	 * @param <R>  Return type of test logic.
	 * @param <T>  Possible {@link Throwable}.
	 * @param test Test logic to wrap in replay/verify.
	 * @return Result of test logic.
	 * @throws T If logic throws {@link Exception}.
	 */
	protected final <R, T extends Throwable> R doTest(TestLogic<R, T> test) throws T {
		return this.mockTestSupport.doTest(() -> test.run());
	}

	/**
	 * Multi-threaded test logic interface.
	 * 
	 * @param <T> Possible {@link Throwable}.
	 */
	protected static interface MultithreadedTestLogic<T extends Throwable> {
		void run() throws T;
	}

	/**
	 * Undertakes multi-threaded testing of {@link TestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link TestLogic} per
	 *                       {@link Thread}.
	 * @param test           {@link TestLogic}.
	 * @throws T Possible failure from failing {@link TestLogic}.
	 */
	protected final <T extends Throwable> void doMultiThreadedTest(int threadCount, int iterationCount,
			MultithreadedTestLogic<T> test) throws T {
		this.threadedTestSupport.doMultiThreadedTest(threadCount, iterationCount, () -> test.run());
	}

	/**
	 * Undertakes multi-threaded testing of {@link TestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link TestLogic} per
	 *                       {@link Thread}.
	 * @param timeout        Timeout.
	 * @param test           {@link TestLogic}.
	 * @throws T Possible failure from failing {@link TestLogic}.
	 */
	protected final <T extends Throwable> void doMultiThreadedTest(int threadCount, int iterationCount, int timeout,
			MultithreadedTestLogic<T> test) throws T {
		this.threadedTestSupport.doMultiThreadedTest(threadCount, iterationCount, timeout, () -> test.run());
	}

	/**
	 * Test capture interface.
	 * 
	 * @param <T> Possible {@link Throwable}.
	 */
	protected static interface TestCapture<T extends Throwable> {
		void run() throws T;
	}

	/**
	 * Capture <code>std err</code> of test logic.
	 * 
	 * @param <T>  Possible {@link Exception} type.
	 * @param test Test logic to capture <code>std err</code>.
	 * @return <code>std err</code> output.
	 * @throws T Possible {@link Throwable}.
	 */
	protected final <T extends Throwable> String captureLoggerOutput(TestCapture<T> test) throws T {
		return this.logTestSupport.captureLoggerOutput(() -> test.run());
	}

	/**
	 * Obtains the file at the relative path.
	 * 
	 * @param relativePath Relative path to the file.
	 * @return {@link File}.
	 * @throws FileNotFoundException If file could not be found.
	 */
	public File findFile(String relativePath) throws FileNotFoundException {
		return this.fileTestSupport.findFile(relativePath);
	}

	/**
	 * Obtains the file by the input file name located in the package of the input
	 * class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException Should the file not be found.
	 */
	public File findFile(Class<?> packageClass, String fileName) throws FileNotFoundException {
		return this.fileTestSupport.findFile(packageClass, fileName);
	}

	/**
	 * Obtains the file location of the input file located in the package of the
	 * input class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of the file within the package directory.
	 * @return Path to the file.
	 */
	public String getFileLocation(Class<?> packageClass, String fileName) {
		return this.fileTestSupport.getFileLocation(packageClass, fileName);
	}

	/**
	 * Creates the input directory.
	 * 
	 * @param directory Directory to be cleared.
	 */
	public void clearDirectory(File directory) {
		this.fileTestSupport.clearDirectory(directory);
	}

	/**
	 * Deletes the input directory.
	 * 
	 * @param directory Directory to be deleted.
	 */
	public void deleteDirectory(File directory) {
		this.fileTestSupport.deleteDirectory(directory);
	}

	/**
	 * Copies the contents of the <code>source</code> directory to the
	 * <code>target</code> directory.
	 * 
	 * @param source Source directory.
	 * @param target Target directory.
	 * @throws IOException If fails to copy the directory.
	 */
	public void copyDirectory(File source, File target) throws IOException {
		this.fileTestSupport.copyDirectory(source, target);
	}

	/**
	 * <p>
	 * Obtains the input stream to the file by the input file name located in the
	 * package of the input class.
	 * <p>
	 * Note: this also searches the class path for the file.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException Should the file not be found.
	 */
	public InputStream findInputStream(Class<?> packageClass, String fileName) throws FileNotFoundException {
		return this.fileTestSupport.findInputStream(packageClass, fileName);
	}

	/**
	 * Obtains the relative path of the package of the class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @return Relative path of class's package.
	 */
	public String getPackageRelativePath(Class<?> packageClass) {
		return this.fileTestSupport.getPackageRelativePath(packageClass);
	}

	/**
	 * Obtains the contents of the output file.
	 * 
	 * @param file File to obtain contents from.
	 * @return Contents of the output file.
	 * @throws FileNotFoundException Should output file not yet be created.
	 * @throws IOException           Should fail to read from output file.
	 */
	public String getFileContents(File file) throws FileNotFoundException, IOException {
		return this.fileTestSupport.getFileContents(file);
	}

	/**
	 * Creates the target file with the content.
	 * 
	 * @param content Content for the file.
	 * @param target  Target file.
	 * @throws IOException If fails to create.
	 */
	public void createFile(File target, InputStream content) throws IOException {
		this.fileTestSupport.createFile(target, content);
	}

	/**
	 * Facade helper function for invoking {@link Thread#sleep(long)}.
	 * 
	 * @param time Sleep time in seconds.
	 */
	public void sleep(int time) {
		this.threadedTestSupport.sleep(time);
	}

	/**
	 * Facade method to timeout operations after 3 seconds.
	 * 
	 * @param startTime Start time from {@link System#currentTimeMillis()}.
	 */
	public void timeout(long startTime) {
		this.threadedTestSupport.timeout(startTime);
	}

	/**
	 * Facade method to timeout operations after a second.
	 * 
	 * @param startTime    Start time from {@link System#currentTimeMillis()}.
	 * @param secondsToRun Seconds to run before timeout.
	 */
	public void timeout(long startTime, int secondsToRun) {
		this.threadedTestSupport.timeout(startTime, secondsToRun);
	}

	/**
	 * Predicate to check for is true.
	 */
	@FunctionalInterface
	public static interface WaitForTruePredicate<T extends Throwable> {

		/**
		 * Predicate test.
		 * 
		 * @return <code>true</code> to indicate no further waiting.
		 * @throws T Possible exception.
		 */
		boolean test() throws T;
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>   Possible failure type.
	 * @param check Check.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check) throws T {
		this.threadedTestSupport.waitForTrue(() -> check.test());
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>          Possible failure type.
	 * @param check        Check.
	 * @param secondsToRun Seconds to wait before timing out.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check, int secondsToRun) throws T {
		this.threadedTestSupport.waitForTrue(() -> check.test(), secondsToRun);
	}

	/**
	 * Determines if printing messages.
	 * 
	 * @return <code>true</code> to print messages.
	 */
	protected boolean isPrintMessages() {
		return this.logTestSupport.isPrintMessages();
	}

	/**
	 * Prints heap memory details.
	 */
	public void printHeapMemoryDiagnostics() {
		this.logTestSupport.printHeapMemoryDiagnostics();
	}

	/**
	 * Obtains run time in human readable form.
	 * 
	 * @param startTime Start time of running.
	 * @return Run time in human readable form.
	 */
	public String getDisplayRunTime(long startTime) {
		return this.logTestSupport.getDisplayRunTime(startTime);
	}

	/**
	 * Obtains run time in human readable form.
	 * 
	 * @param startTime Start time of running.
	 * @param endTime   End time of running.
	 * @return Run time in human readable form.
	 */
	public String getDisplayRunTime(long startTime, long endTime) {
		return this.logTestSupport.getDisplayRunTime(startTime, endTime);
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 */
	public void printMessage(String message) {
		this.logTestSupport.printMessage(message);
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 * @throws IOException If fails to print message.
	 */
	public void printMessage(InputStream message) throws IOException {
		this.logTestSupport.printMessage(message);
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 * @throws IOException If fails to print message.
	 */
	public void printMessage(Reader message) throws IOException {
		this.logTestSupport.printMessage(message);
	}

}
