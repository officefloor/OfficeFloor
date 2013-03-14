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
package net.officefloor.bootstrap;

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
import net.officefloor.bootstrap.Bootstrap;
import net.officefloor.building.console.OfficeFloorConsoleMain;
import net.officefloor.building.util.OfficeBuildingTestUtil;

/**
 * Tests the {@link Bootstrap}.
 * 
 * @author Daniel Sagenschneider
 */
public class BootstrapTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		// Flag testing
		Bootstrap.isTesting = true;

		// Setup to be test directory.
		// (Ensure use same property as Console)
		String usrDirValue = System.getProperty("user.dir");
		File userDir = new File(usrDirValue, "target/test-classes");
		String userDirValue = userDir.getAbsolutePath();
		System.setProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME,
				userDirValue);

		// Reset for testing
		MockMain.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		// Clear the OFFICE_FLOOR_HOME
		System.clearProperty(OfficeFloorConsoleMain.OFFICE_FLOOR_HOME);
	}

	/**
	 * Ensure issue if {@link Bootstrap#OFFICE_FLOOR_HOME} is not specified.
	 */
	public void testEnsureOfficeFloorHomeSet() throws Throwable {

		// Clear the OFFICE_FLOOR_HOME (so not specified)
		System.clearProperty(Bootstrap.OFFICE_FLOOR_HOME);

		try {
			// Should not Bootstrap as require OFFICE_FLOOR_HOME
			Bootstrap.main();
			fail("Should not be successful");
		} catch (Error ex) {
			assertEquals(
					"Incorrect cause",
					"Exit: ERROR: OFFICE_FLOOR_HOME not specified. Must be an environment variable pointing to the OfficeFloor install directory.",
					ex.getMessage());
		}
	}

	/**
	 * Ensure able to obtain the additional resources from bootstrapping.
	 */
	public void testBootstrapResources() throws Throwable {

		// Write the Java source of class to bootstrap.
		// This provides the object with the appropriate class loader.
		final String CLASS_NAME = "MockBootstrapFor" + this.getName();
		final StringWriter source = new StringWriter();
		PrintWriter s = new PrintWriter(source);
		s.println("import " + this.getClass().getName() + ".MockMain;");
		s.println("public class " + CLASS_NAME + " extends MockMain {");
		s
				.println("  public static void main(String[] arguments) throws Throwable {");
		s.println("    classLoaderObject = new " + CLASS_NAME + "();");
		s.println("    MockMain.main(arguments);");
		s.println("  }");
		s.println("}");
		s.close();

		// Compile the source
		this.compileSourceToClass(CLASS_NAME, source.toString());

		// Bootstrap the mock main
		Bootstrap.main(CLASS_NAME, "test");

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
		 * Specified from the extending class only available via bootstrapping.
		 * This results in the {@link ClassLoader} for the object being the
		 * bootstrapped {@link ClassLoader}.
		 */
		protected static Object classLoaderObject;

		/**
		 * Resets for testing.
		 */
		public static void reset() {
			isMainInvoked = false;
			classLoaderObject = null;
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

			// Ensure able to obtain file from class path directory.
			// (Both from thread and class ClassLoaders)
			assertStreamContent("Directory File", "Directory File", Thread
					.currentThread().getContextClassLoader()
					.getResourceAsStream("DirectoryFile.txt"));
			assertStreamContent("Directory File", "Directory File",
					classLoaderObject.getClass().getClassLoader()
							.getResourceAsStream("DirectoryFile.txt"));

			// Ensure able to obtain file from jar
			// (Both from thread and class ClassLoaders)
			assertStreamContent("Jar File", "Jar File", Thread.currentThread()
					.getContextClassLoader().getResourceAsStream("JarFile.txt"));
			assertStreamContent("Jar File", "Jar File", classLoaderObject
					.getClass().getClassLoader().getResourceAsStream(
							"JarFile.txt"));
		}
	}

	/**
	 * Ensure able to bootstrap the class.
	 */
	public void testBootstrapClass() throws Throwable {

		// Create temporary file
		File temporaryFile = OfficeBuildingTestUtil.createTempFile(this);

		// Write the Java source of class to bootstrap
		final String CLASS_NAME = "MockBootstrapFor" + this.getName();
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

		// Compile the source
		this.compileSourceToClass(CLASS_NAME, source.toString());

		// Bootstrap the generated source class
		Bootstrap.main(CLASS_NAME, "test");

		// Ensure content written to file
		OfficeBuildingTestUtil.validateFileContent(
				"Bootstrapped file should write content to file", "test",
				temporaryFile);
	}

	/**
	 * Compiles the source to a class.
	 * 
	 * @param className
	 *            Name of the class.
	 * @param source
	 *            Source to be compiled for the class.
	 */
	private void compileSourceToClass(String className, final String source) {

		// Create the class to bootstrap
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		// Create the Java source file
		URI sourceUri = URI.create("string:///" + className
				+ Kind.SOURCE.extension);
		JavaFileObject sourceFile = new SimpleJavaFileObject(sourceUri,
				Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignoreEncodingErrors)
					throws IOException {
				return source;
			}
		};

		// Compile the Java source file to class file
		String destDir = System.getProperty(Bootstrap.OFFICE_FLOOR_HOME)
				+ "/lib/directory";
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask task = compiler.getTask(null, null, diagnostics, Arrays
				.asList("-d", destDir), null, Arrays.asList(sourceFile));
		assertTrue("Failed to compile source class", task.call());
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