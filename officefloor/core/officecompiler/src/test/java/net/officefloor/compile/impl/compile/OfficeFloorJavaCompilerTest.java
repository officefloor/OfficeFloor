/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.compile.impl.compile;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.ClassName;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.JavaSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to compile java classes for avoiding use of {@link Proxy}
 * implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJavaCompilerTest extends OfficeFrameTestCase {

	/**
	 * Obtains the {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	private static ClassLoader getClassLoader() {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = OfficeFloorJavaCompilerTest.class.getClassLoader();
		}

		// Ensure can load simple class
		try {
			assertNotNull("Ensure can load simple class", classLoader.loadClass(Simple.class.getName()));
		} catch (Exception ex) {
			fail(ex);
		}

		// Return the class loader
		return classLoader;
	}

	/**
	 * {@link OfficeFloorJavaCompiler} being tested.
	 */
	private final OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(getClassLoader());

	/**
	 * Ensure invalid to compiler.
	 */
	public void testInvalid() {
		assertNotNull("Should have compiler available (for testing to be available)", this.compiler);
	}

	/**
	 * Ensure disable Java compiling.
	 */
	public void testDisableJavaCompilingViaSystemProperties() {
		String originalValue = System.getProperty(OfficeFloorJavaCompiler.SYSTEM_PROPERTY_JAVA_COMPILING);
		try {

			// Disable system property
			System.setProperty(OfficeFloorJavaCompiler.SYSTEM_PROPERTY_JAVA_COMPILING, "false");

			// Ensure no Java compiler
			assertNull("Ensure no compiling", OfficeFloorJavaCompiler.newInstance(getClassLoader()));

		} finally {
			if (originalValue == null) {
				System.clearProperty(OfficeFloorJavaCompiler.SYSTEM_PROPERTY_JAVA_COMPILING);
			} else {
				System.setProperty(OfficeFloorJavaCompiler.SYSTEM_PROPERTY_JAVA_COMPILING, originalValue);
			}
		}
	}

	/**
	 * Ensure disable Java compiling.
	 */
	public void testDisableJavaCompilingForTesting() {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> {

			// Ensure no Java compiler
			assertNull("Ensure no compiling", OfficeFloorJavaCompiler.newInstance(getClassLoader()));
		});
	}

	/**
	 * Ensure can compile object implementing existing interface.
	 */
	public void testSimpleImplementation() throws Exception {

		// Create the source
		StringWriter buffer = new StringWriter();
		PrintWriter source = new PrintWriter(buffer);
		source.println("package net.officefloor.test;");
		source.println("public class SimpleImpl implements " + Simple.class.getName().replace('$', '.') + " {");
		source.println("   public String getMessage() {");
		source.println("       return \"TEST\";");
		source.println("   }");
		source.println("}");
		source.flush();

		// Compile the source
		JavaSource javaSource = this.compiler.addSource("net.officefloor.test.SimpleImpl", buffer.toString());
		Class<?> clazz = javaSource.compile();

		// Ensure can use without class compatibility issues
		Simple simple = (Simple) clazz.getConstructor().newInstance();
		assertEquals("Incorrect compiled result", "TEST", simple.getMessage());
	}

	public static interface Simple {
		String getMessage();
	}

	/**
	 * Ensure can compile multiple objects depend on each other.
	 */
	public void testMultipleImplementations() throws Exception {

		// Create the source
		StringWriter bufferOne = new StringWriter();
		PrintWriter sourceOne = new PrintWriter(bufferOne);
		sourceOne.println("package net.officefloor.test;");
		sourceOne.println("public class SimpleImpl implements " + Simple.class.getName().replace('$', '.') + "{");
		sourceOne.println("   public String getMessage() {");
		sourceOne.println("       return net.officefloor.test.MockMultiple.MESSAGE;");
		sourceOne.println("   }");
		sourceOne.println("}");
		sourceOne.flush();

		// Create the source
		StringWriter bufferTwo = new StringWriter();
		PrintWriter sourceTwo = new PrintWriter(bufferTwo);
		sourceTwo.println("package net.officefloor.test;");
		sourceTwo.println("public class MockMultiple {");
		sourceTwo.println("   public static final String MESSAGE = \"MULTIPLE\";");
		sourceTwo.println("}");
		sourceTwo.flush();

		// Compile the sources
		JavaSource javaSource = this.compiler.addSource("net.officefloor.test.SimpleImpl", bufferOne.toString());
		this.compiler.addSource("net.officefloor.test.MockMultiple", bufferTwo.toString());
		Map<JavaSource, Class<?>> classes = this.compiler.compile();
		Class<?> clazz = classes.get(javaSource);

		// Ensure can use
		Simple multiple = (Simple) clazz.getConstructor().newInstance();
		assertEquals("Incorrect compiled result", "MULTIPLE", multiple.getMessage());
	}

	/**
	 * Ensure able to create unique class name.
	 */
	public void testClassName() {
		ClassName name = this.compiler.createClassName("test.Example");
		assertEquals("Incorrect package", "generated.officefloor.test", name.getPackageName());
		assertTrue("Incorrect class", name.getClassName().startsWith("Example"));
		assertEquals("Incorrect qualified name", name.getPackageName() + "." + name.getClassName(), name.getName());
	}

	/**
	 * Ensure able to create unique class name from inner class.
	 */
	public void testInnerClassName() {
		ClassName name = this.compiler.createClassName("test.Example$Inner");
		assertEquals("Incorrect pckage", "generated.officefloor.test.Example", name.getPackageName());
		assertTrue("Incorrect class", name.getClassName().startsWith("Inner"));
		assertEquals("Incorrect qualified name", name.getPackageName() + "." + name.getClassName(), name.getName());
	}

	/**
	 * Ensure able to obtain the source name.
	 */
	public void testSourceName() {
		assertEquals("boolean", this.compiler.getSourceName(boolean.class));
		assertEquals("java.lang.Integer", this.compiler.getSourceName(Integer.class));
		assertEquals("java.sql.Connection", this.compiler.getSourceName(Connection.class));
		assertEquals("char[]", this.compiler.getSourceName(char[].class));
		assertEquals("java.lang.String[]", this.compiler.getSourceName(String[].class));
	}

	/**
	 * Ensure able to write constructor.
	 */
	public void testConstructor() throws Exception {
		StringWriter source = new StringWriter();
		this.compiler.writeConstructor(source, "Simple", this.compiler.createField(String.class, "field"),
				this.compiler.createField(int.class, "value"), this.compiler.createField(boolean[].class, "flags"));
		StringWriter expected = new StringWriter();
		expected.append("  private java.lang.String field;\n");
		expected.append("  private int value;\n");
		expected.append("  private boolean[] flags;\n");
		expected.append("  public Simple(java.lang.String field, int value, boolean[] flags) {\n");
		expected.append("    this.field = field;\n");
		expected.append("    this.value = value;\n");
		expected.append("    this.flags = flags;\n");
		expected.append("  }\n");
		assertEquals("Incorrect constructor", expected.toString(), source.toString());
	}

	/**
	 * Ensure can write the {@link Method} signature.
	 */
	public void testMethodSignature() throws Exception {
		this.assertMethodSignature("void simple()", false, Signature.class.getMethod("simple"));
		this.assertMethodSignature("boolean[] returnValue()", true, Signature.class.getMethod("returnValue"));
		this.assertMethodSignature("void exception() throws java.io.IOException", false,
				Signature.class.getMethod("exception"));
		this.assertMethodSignature("void parameter(java.lang.String p0)", false,
				Signature.class.getMethod("parameter", String.class));
		this.assertMethodSignature(
				"java.sql.Connection parameters(int p0, java.lang.Integer[] p1) throws java.sql.SQLException, java.lang.IllegalArgumentException",
				true, Signature.class.getMethod("parameters", int.class, Integer[].class));
	}

	private void assertMethodSignature(String expected, boolean isExpectReturn, Method method) throws Exception {
		StringWriter source = new StringWriter();
		boolean isReturn = this.compiler.writeMethodSignature(source, method);
		assertEquals(expected, source.toString());
		assertEquals("Incorrect return indicator for " + source.toString(), isExpectReturn, isReturn);
	}

	public static interface Signature {
		void simple();

		boolean[] returnValue();

		void exception() throws IOException;

		void parameter(String parameter);

		Connection parameters(int paramOne, Integer[] paramTwo) throws SQLException, IllegalArgumentException;
	}

	/**
	 * Ensure can provide default wrapper.
	 */
	public void testDefaultWrapper() throws Exception {

		// Create the wrapper
		Class<?> wrapperClass = this.compiler.addWrapper(Connection.class, Connection.class, null).compile();

		// Other mocks
		PreparedStatement statement = this.createMock(PreparedStatement.class);

		// Record interactions for appropriate wrapping
		Connection mock = this.createMock(Connection.class);
		mock.rollback();
		mock.setAutoCommit(true);
		this.recordReturn(mock, mock.getTransactionIsolation(), 2);
		this.recordReturn(mock, mock.prepareStatement("SELECT * FROM TEST"), statement);
		this.replayMockObjects();

		// Wrap the connection
		Connection wrapper = (Connection) wrapperClass.getDeclaredConstructor(Connection.class).newInstance(mock);
		assertNotSame("Should be wrapped", mock, wrapper);

		// Undertake operations to ensure correct wrapping
		wrapper.rollback();
		wrapper.setAutoCommit(true);
		assertEquals("Incorrect get", 2, wrapper.getTransactionIsolation());
		assertSame("Incorrect statement", statement, wrapper.prepareStatement("SELECT * FROM TEST"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide wrapper with override logic.
	 */
	public void testWrapper() throws Exception {

		// Create the wrapper
		JavaSource preparedStatementSource = this.compiler.addWrapper(PreparedStatement.class, (context) -> {
		});
		JavaSource javaSource = this.compiler.addWrapper(Connection.class, (context) -> {
			switch (context.getMethod().getName()) {
			case "prepareStatement":
				context.setReturnWrapClass(preparedStatementSource);
				break;
			case "close":
				context.write("");
				break;
			case "setAutoCommit":
				context.writeln("    this.delegate.setAutoCommit(!p0);");
				break;
			}
		});

		// Compile with wrapper
		Class<?> wrapperClass = javaSource.compile();

		// Record interactions for appropriate wrapping
		Connection mock = this.createMock(Connection.class);
		PreparedStatement mockStatement = this.createMock(PreparedStatement.class);
		ResultSet mockResultSet = this.createMock(ResultSet.class);
		this.recordReturn(mock, mock.prepareStatement("SELECT * FROM TEST"), mockStatement);
		this.recordReturn(mockStatement, mockStatement.executeQuery(), mockResultSet);
		mock.setAutoCommit(true);
		this.replayMockObjects();

		// Wrap the connection
		Connection wrapper = (Connection) wrapperClass.getDeclaredConstructor(Connection.class).newInstance(mock);
		assertNotSame("Connection should be wrapped", mock, wrapper);

		// Undertake operations to ensure correct wrapping
		PreparedStatement wrappedStatement = wrapper.prepareStatement("SELECT * FROM TEST");
		assertNotSame("Statement should be wrapped", mockStatement, wrappedStatement);
		ResultSet resultSet = wrappedStatement.executeQuery();
		assertSame("Should not wrap ResultSet", mockResultSet, resultSet);

		// Ensure able to override
		wrapper.setAutoCommit(false);

		// Ensure no operation
		wrapper.close();

		this.verifyMockObjects();
	}

}