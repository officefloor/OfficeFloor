/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.building.bootstrap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import junit.framework.TestCase;
import net.officefloor.building.util.OfficeBuildingTestUtil;

/**
 * Tests the {@link Bootstrap}.
 * 
 * @author Daniel Sagenschneider
 */
public class BootstrapTest extends TestCase {

	/**
	 * user.dir to rollback to after test.
	 */
	private String rollbackUsrDirValue = null;

	@Override
	protected void setUp() throws Exception {
		// Flag testing
		Bootstrap.isTesting = true;

		// Setup to be test directory
		this.rollbackUsrDirValue = System.getProperty("user.dir");
		File userDir = new File(this.rollbackUsrDirValue, "target/test-classes");
		String userDirValue = userDir.getAbsolutePath();
		System.setProperty("user.dir", userDirValue);

		// Reset for testing
		MockMain.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		// Reinstate the user.dir
		System.setProperty("user.dir", this.rollbackUsrDirValue);
	}

	/**
	 * Ensure able to obtain the additional resources from bootstrapping.
	 */
	public void testBootstrapResources() throws Throwable {

		// Bootstrap the mock main
		Bootstrap.main(MockMain.class.getName(), "test");

		// Ensure main invoked
		assertTrue("Main method should be invoked", MockMain.isMainInvoked);
	}

	/**
	 * Provides mock class to be bootstrapped.
	 */
	public static class MockMain {

		/**
		 * Flag indicating if <code>main</code> method invoked.
		 */
		public static boolean isMainInvoked = false;

		/**
		 * Resets for testing.
		 */
		public static void reset() {
			isMainInvoked = false;
		}

		/**
		 * Main to be bootstrapped.
		 * 
		 * @param arguments
		 *            Command line arguments.
		 * @throws Throwable
		 *             If fails.
		 */
		public static void main(String[] arguments) throws Throwable {

			// Flag invoked
			isMainInvoked = true;

			// Ensure correct command line arguments
			assertEquals("Incorrect number of command line arguments", 1,
					arguments.length);
			assertEquals("Incorrect command line argument", "test",
					arguments[0]);

			// Ensure able to obtain file from class path directory
			InputStream dirFile = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							"DirectoryFile.txt");
			assertStreamContent("Directory File", "Directory File", dirFile);

			// Ensure able to obtain file from jar
			InputStream jarFile = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream("JarFile.txt");
			assertStreamContent("Jar File", "Jar File", jarFile);
		}
	}

	/**
	 * Ensure able to bootstrap the class.
	 */
	public void testBootstrapClass() throws Throwable {

		// Create temporary file
		File temporaryFile = OfficeBuildingTestUtil.createTempFile(this);

		// Create the class to bootstrap
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		// Write the Java source of class to bootstrap
		final String CLASS_NAME = "MockBootstrappedClass";
		final StringWriter source = new StringWriter();
		PrintWriter s = new PrintWriter(source);
		s.println("import " + Writer.class.getName() + ";");
		s.println("import " + FileWriter.class.getName() + ";");
		s.println("import " + File.class.getName() + ";");
		s.println("public class " + CLASS_NAME + " {");
		s
				.println("  public static void main(String[] args) throws Throwable {");
		s.println("    Writer writer = new FileWriter(new File(\""
				+ temporaryFile.getAbsolutePath() + "\"));");
		s.println("    writer.write(\"test\");");
		s.println("    writer.close();");
		s.println("  }");
		s.println("}");
		s.close();

		// Create the Java source file
		URI sourceUri = URI.create("string:///" + CLASS_NAME
				+ Kind.SOURCE.extension);
		JavaFileObject sourceFile = new SimpleJavaFileObject(sourceUri,
				Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors)
					throws IOException {
				return source.toString();
			}
		};

		// Compile the Java source file to class file
		String destDir = System.getProperty("user.dir")
				+ "/lib/plugins/directory";
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask task = compiler.getTask(null, null, diagnostics, Arrays
				.asList("-d", destDir), null, Arrays.asList(sourceFile));
		assertTrue("Failed to compile source class", task.call());

		// Bootstrap the generated source class
		Bootstrap.main(CLASS_NAME, "test");

		// Ensure content written to file
		OfficeBuildingTestUtil.validateFileContent(
				"Bootstrapped file should write content to file", "test",
				temporaryFile);
	}

	/**
	 * Asserts the {@link InputStream} contains the expected content.
	 * 
	 * @param message
	 *            Assertion message.
	 * @param expectedContent
	 *            Expected content of the {@link InputStream}.
	 * @param stream
	 *            {@link InputStream}.
	 */
	private static void assertStreamContent(String message,
			String expectedContent, InputStream stream) throws IOException {

		// Ensure input stream
		assertNotNull(message + ": no input stream", stream);

		// Read in the contents of the input stream
		Reader reader = new InputStreamReader(stream);
		StringBuilder content = new StringBuilder();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			content.append((char) value);
		}

		// Assert the content
		assertEquals(message + ": incorrect content", expectedContent, content
				.toString());
	}

}