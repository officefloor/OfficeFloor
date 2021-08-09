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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;

/**
 * Test support for logging.
 * 
 * @author Daniel Sagenschneider
 */
public class LogTestSupport implements TestSupport, BeforeAllCallback, AfterAllCallback {

	/**
	 * Name of test.
	 */
	private String testName;

	/**
	 * GC logging.
	 */
	private Map<NotificationEmitter, NotificationListener> gcLoggers = null;

	/**
	 * Indicates whether to have verbose output.
	 */
	private boolean isVerbose = false;

	/**
	 * Indicates whether to have debug verbose output.
	 */
	private boolean isDebugVerbose = false;

	/**
	 * Indicates whether to log GC in test.
	 */
	private boolean isLogGC = false;

	/*
	 * ===================== TestSupport ================================
	 */

	/**
	 * Default instantiate for use as {@link ExtensionContext}.
	 */
	public LogTestSupport() {
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		this.testName = context.getDisplayName();
	}

	/**
	 * Specifies the name of test.
	 * 
	 * @param testName Name of test.
	 */
	void setTestName(String testName) {
		this.testName = testName;
	}

	/**
	 * Specifies to provide verbose output to aid in debugging.
	 * 
	 * @param isVerbose <code>true</code> to turn on verbose output.
	 */
	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;

		// Provide start of verbose output
		if (this.isVerbose) {
			System.out.println("+++ START: " + this.testName + " +++");
		}
	}

	/**
	 * Specifies to provide debug verbose output to aid in debugging.
	 */
	public void setDebugVerbose() {
		if (!this.isDebugVerbose) {
			OfficeFloorImpl.getFrameworkLogger().setLevel(Level.FINEST);
			StreamHandler handler = new StreamHandler(System.out, new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage() + "\n";
				}
			});
			handler.setLevel(Level.FINEST);
			OfficeFloorImpl.getFrameworkLogger().addHandler(handler);
			this.isDebugVerbose = true;
		}
	}

	/**
	 * Turns on logging of GC as part of test.
	 */
	public void setLogGC() {
		this.isLogGC = true;
	}

	/**
	 * Test capture interface.
	 * 
	 * @param <T> Possible {@link Throwable}.
	 */
	public static interface TestCapture<T extends Throwable> {
		void run() throws T;
	}

	/**
	 * Capture <code>std out/err</code> of test logic.
	 * 
	 * @param <T>  Possible {@link Exception} type.
	 * @param test Test logic to capture <code>std err</code>.
	 * @return <code>std out/err</code> output.
	 * @throws T Possible {@link Throwable}.
	 */
	public <T extends Throwable> String captureStdOutErr(TestCapture<T> test) throws T {

		// Obtain original streams
		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;
		try {

			// Capture the stream data
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PrintStream stream = new PrintStream(buffer);
			System.setOut(stream);
			System.setErr(stream);

			// Undertake logic
			test.run();

			// Return the stream text
			return buffer.toString();

		} finally {
			// Ensure reinstate original streams
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
	}

	/**
	 * Capture {@link Logger} output of test logic.
	 * 
	 * @param <T>  Possible {@link Exception} type.
	 * @param test Test logic to capture <code>std err</code>.
	 * @return {@link Logger} output.
	 * @throws T Possible {@link Throwable}.
	 */
	public <T extends Throwable> String captureLoggerOutput(TestCapture<T> test) throws T {

		// Add handler to capture the log error
		ByteArrayOutputStream error = new ByteArrayOutputStream();
		Handler errorHandler = new StreamHandler(error, new SimpleFormatter());
		OfficeFloorImpl.getFrameworkLogger().addHandler(errorHandler);

		// Undertake operation
		try {

			// Undertake test
			test.run();

			// Flush the handler
			errorHandler.flush();

		} finally {
			OfficeFloorImpl.getFrameworkLogger().removeHandler(errorHandler);
		}
		return new String(error.toByteArray());
	}

	/**
	 * Determines if printing messages.
	 * 
	 * @return <code>true</code> to print messages.
	 */
	protected boolean isPrintMessages() {
		return this.isVerbose || Boolean.parseBoolean(System.getProperty("print.messages", Boolean.FALSE.toString()));
	}

	/**
	 * Obtains the memory size in human readable form.
	 * 
	 * @param memorySize Memory size in bytes.
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
	 * Prints heap memory details.
	 */
	public void printHeapMemoryDiagnostics() {

		// Only do heap diagnosis if print messages
		if (!this.isPrintMessages()) {
			return; // do not do heap diagnosis
		}

		// Obtain the memory management bean
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

		// Obtain the heap diagnosis details
		MemoryUsage heap = memoryBean.getHeapMemoryUsage();
		float usedPercentage = (heap.getUsed() / (float) heap.getMax());

		// Print the results
		NumberFormat format = NumberFormat.getPercentInstance();
		this.printMessage("    HEAP: " + format.format(usedPercentage) + " (used=" + this.getMemorySize(heap.getUsed())
				+ ", max=" + this.getMemorySize(heap.getMax()) + ", init=" + this.getMemorySize(heap.getInit())
				+ ", commit=" + this.getMemorySize(heap.getCommitted()) + ", fq="
				+ memoryBean.getObjectPendingFinalizationCount() + ")");
	}

	/**
	 * Obtains run time in human readable form.
	 * 
	 * @param startTime Start time of running.
	 * @return Run time in human readable form.
	 */
	public String getDisplayRunTime(long startTime) {
		return this.getDisplayRunTime(startTime, System.currentTimeMillis());
	}

	/**
	 * Obtains run time in human readable form.
	 * 
	 * @param startTime Start time of running.
	 * @param endTime   End time of running.
	 * @return Run time in human readable form.
	 */
	public String getDisplayRunTime(long startTime, long endTime) {

		// Obtain the run time in milliseconds
		long runTime = (System.currentTimeMillis() - startTime);

		final long milliseconds = 1;
		final long seconds = (1000 * milliseconds);
		final long minutes = (60 * seconds);

		if (runTime < seconds) {
			return (runTime) + " milliseconds";
		} else if (runTime < minutes) {
			return (((float) runTime) / seconds) + " seconds";
		} else {
			return (((float) runTime) / minutes) + " minutes";
		}
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 */
	public void printMessage(String message) {

		// Determine if show messages
		if (!this.isPrintMessages()) {
			return; // do no print messages
		}

		// Print the message
		System.out.println(message);
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 * @throws IOException If fails to print message.
	 */
	public void printMessage(InputStream message) throws IOException {
		this.printMessage(new InputStreamReader(message));
	}

	/**
	 * Prints a message regarding the test.
	 * 
	 * @param message Message to be printed.
	 * @throws IOException If fails to print message.
	 */
	public void printMessage(Reader message) throws IOException {
		StringWriter buffer = new StringWriter();
		for (int value = message.read(); value != -1; value = message.read()) {
			buffer.append((char) value);
		}
		this.printMessage(buffer.toString());
	}

	/**
	 * Displays the graph of objects starting at root.
	 * 
	 * @param root Root of graph to display.
	 * @throws Exception If fails.
	 */
	public void displayGraph(Object root) throws Exception {
		this.displayGraph(root, new String[0]);
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
		PrintWriter writer = new PrintWriter(System.out);
		this.displayGraph(root, new HashSet<Object>(), 0, "root", ignoreMethodNames, writer);
		writer.flush();
	}

	/**
	 * Displays the graph of objects starting at root.
	 * 
	 * @param root              Root of graph to display.
	 * @param displayedObjects  Set of objects already displayed.
	 * @param depth             Depth into the graph.
	 * @param path              Path from previous graph.
	 * @param ignoreMethodNames Method names not to follow in graph for display.
	 * @param writer            Writer to output display.
	 */
	private void displayGraph(Object root, Set<Object> displayedObjects, int depth, String path,
			String[] ignoreMethodNames, PrintWriter writer) throws Exception {

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
				this.displayGraph(item, displayedObjects, (depth + 1), (path + "[" + index + "]"), ignoreMethodNames,
						writer);
				index++;
			}

		} else if ((root.getClass().isPrimitive()) || (root instanceof Class<?>) || (root instanceof String)
				|| (root instanceof Boolean) || (root instanceof Byte) || (root instanceof Character)
				|| (root instanceof Short) || (root instanceof Integer) || (root instanceof Long)
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
					writer.println("- " + root.getClass().getSimpleName() + "." + methodName + "() -");
					continue;
				}

				// Determine if accessor method
				if ((!Modifier.isPublic(method.getModifiers())) || (method.getReturnType() == Void.TYPE)
						|| (method.getParameterTypes().length != 0)) {
					continue;
				}

				// Obtain the values of the accessors
				Object value = method.invoke(root, (Object[]) null);

				// Do deep display
				this.displayGraph(value, displayedObjects, (depth + 1),
						root.getClass().getSimpleName() + "." + methodName + "()", ignoreMethodNames, writer);
			}
		}
	}

	/*
	 * ====================== BeforeAllCallback ========================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		TestSupportExtension.getTestSupport(LogTestSupport.class, context).beforeAll();
	}

	/**
	 * {@link BeforeAllCallback} logic.
	 * 
	 * @throws Exception If fails.
	 */
	public void beforeAll() throws Exception {

		// Determine if set up GC logging
		if (this.isLogGC) {
			this.gcLoggers = new HashMap<>();
			for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
				NotificationEmitter emitter = (NotificationEmitter) gcBean;
				NotificationListener gcLogger = (notification, handback) -> {

					// Indicate Garbage collection
					System.out.println(" -> GC: " + gcBean.getName() + " (" + gcBean.getCollectionTime() + " ms) - "
							+ notification.getType());
				};
				emitter.addNotificationListener(gcLogger, null, null);
				this.gcLoggers.put(emitter, gcLogger);
			}
		}
	}

	/*
	 * ====================== AfterAllCallback ========================
	 */

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		TestSupportExtension.getTestSupport(LogTestSupport.class, context).afterAll();
	}

	/**
	 * {@link AfterAllCallback} logic.
	 * 
	 * @throws Exception If fails.
	 */
	public void afterAll() throws Exception {

		// Provide start of verbose output
		if (this.isVerbose) {
			System.out.println("+++ END: " + this.testName + " +++\n");
		}

		// Remove GC logging
		if (this.gcLoggers != null) {
			for (NotificationEmitter emitter : this.gcLoggers.keySet()) {
				NotificationListener gcLogger = this.gcLoggers.get(emitter);
				emitter.removeNotificationListener(gcLogger);
			}
		}
	}

}
