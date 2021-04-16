/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.classes;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.ClassName;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.JavaSource;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.JavaSourceWriter;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.compatibility.ModulesJavaFacet;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to compile java classes for avoiding use of {@link Proxy}
 * implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorJavaCompilerTest extends OfficeFrameTestCase {

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link SourceContext}.
	 */
	private static SourceContext getSourceContext(ClassLoader classLoader) {
		return OfficeFloorCompiler.newOfficeFloorCompiler(classLoader).createRootSourceContext();
	}

	/**
	 * Obtains the {@link SourceContext} with default {@link ClassLoader} for
	 * testing.
	 * 
	 * @return {@link SourceContext}.
	 */
	private static SourceContext getSourceContext() {
		return getSourceContext(getClassLoader());
	}

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
	private OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(getSourceContext());

	/**
	 * Ensure invalid to compiler.
	 */
	public void testInvalid() {
		assertNotNull("Should have compiler available (for testing to be available)", this.compiler);
	}

	/**
	 * Ensure uses correct default implementation.
	 */
	public void testCorrectDefaultImplementation() {
		assertEquals("Incorrect default implementation", OfficeFloorJavaCompilerImpl.class.getName(),
				OfficeFloorJavaCompiler.DEFAULT_OFFICE_FLOOR_JAVA_COMPILER_IMPLEMENTATION);
	}

	/**
	 * Ensure able to run with different {@link OfficeFloorJavaCompiler}
	 * implementation.
	 */
	public void testRunWithDifferentImplementation() {
		Closure<Boolean> isRun = new Closure<>(false);
		OfficeFloorJavaCompiler.runWithImplementation(MockOfficeFloorJavaCompilerImpl.class.getName(), () -> {
			// Ensure correctly uses mock
			OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(getSourceContext());
			assertTrue("Compiler implementation should be overridden",
					compiler instanceof MockOfficeFloorJavaCompilerImpl);
			isRun.value = true;
		});
		assertTrue("Should be run", isRun.value);
	}

	/**
	 * Mock {@link OfficeFloorJavaCompiler} implementation for testing.
	 */
	public static class MockOfficeFloorJavaCompilerImpl extends OfficeFloorJavaCompilerImpl {

		/**
		 * Instantiate.
		 * 
		 * @param sourceContext {@link SourceContext}.
		 */
		public MockOfficeFloorJavaCompilerImpl(SourceContext sourceContext) throws ClassNotFoundException {
			super(sourceContext);
		}
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
			assertNull("Ensure no compiling", OfficeFloorJavaCompiler.newInstance(getSourceContext()));

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
			assertNull("Ensure no compiling", OfficeFloorJavaCompiler.newInstance(getSourceContext()));
		});
	}

	/**
	 * Ensure disable if Java compiler not available.
	 */
	public void testDisableAsNoJavaCompiler() throws Exception {

		// Ensure compiler available for test
		Class<?> javacProcessingEnvironmentClass = Class
				.forName("com.sun.tools.javac.processing.JavacProcessingEnvironment");
		assertNotNull("Invalid test: should have javac processing environment", javacProcessingEnvironmentClass);
		assertTrue("Should be processing environment",
				ProcessingEnvironment.class.isAssignableFrom(javacProcessingEnvironmentClass));

		// Ensure correct java compiler class to check
		assertEquals("Incorrect check class", javacProcessingEnvironmentClass.getName(),
				OfficeFloorJavaCompilerImpl.JAVAC_PROCESSING_ENVIRONMENT_CLASS_NAME);

		// Create class loader without compiler
		SourceContext sourceContext = getSourceContext(new ClassLoader() {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if (name.equals(javacProcessingEnvironmentClass.getName())) {
					throw new ClassNotFoundException(name);
				}
				return super.loadClass(name);
			}
		});
		assertNull("No Java compiler, so no compiling", OfficeFloorJavaCompiler.newInstance(sourceContext));
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
	 * Ensure can compile object implementing class on different class path.
	 */
	public void testNewClassPathImplementation() throws Exception {

		// Create the source
		StringWriter buffer = new StringWriter();
		PrintWriter source = new PrintWriter(buffer);
		source.println("package net.officefloor.test;");
		source.println("public class ExtraImpl extends " + CLASS_LOADER_EXTRA_CLASS_NAME + " {");
		source.println("   public String getMessage() {");
		source.println("       return \"TEST\";");
		source.println("   }");
		source.println("}");
		source.flush();

		// Override compiler with extra class path
		URLClassLoader extraClassLoader = (URLClassLoader) createNewClassLoader();
		ClassLoader classLoader = new URLClassLoader(extraClassLoader.getURLs()) {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if (OfficeFloorJavaCompilerImpl.class.getName().equals(name)) {
					return OfficeFloorJavaCompilerImpl.class;
				} else {
					return extraClassLoader.loadClass(name);
				}
			}
		};
		this.compiler = OfficeFloorJavaCompiler.newInstance(getSourceContext(classLoader));

		// Compile the source
		JavaSource javaSource = this.compiler.addSource("net.officefloor.test.ExtraImpl", buffer.toString());
		Class<?> clazz;
		try {
			clazz = javaSource.compile();
		} catch (CompileError error) {

			// Maven + Java8 has class path issues in running Lombok
			if (!new ModulesJavaFacet().isSupported()) {
				System.err.println("KNOWN GOTCHA: " + this.getClass().getSimpleName()
						+ " new class path on Java8 with Maven is having Lombok issues");
				return;
			}

			// Propagate failure
			throw error;
		}

		// Ensure appropriate compile to extra class
		Object object = clazz.getConstructor().newInstance();
		assertEquals("Incorrect parent class from class path", CLASS_LOADER_EXTRA_CLASS_NAME,
				object.getClass().getSuperclass().getName());
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
		Class<?> wrapperClass = this.compiler.addWrapper(Connection.class, null).compile();

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
			case "commit":
				context.getSource();
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
		wrapper.commit();

		this.verifyMockObjects();
	}

	/**
	 * Ensure can provide wrapper implementing additional features.
	 */
	public void testMultiInterfaceWrapper() throws Exception {

		// Obtain the non-interface method
		Method notExposedMethod = WrapperNonInterfaceMethods.class.getMethod("notExposedMethod");

		// Create the wrapper
		ClassName[] className = new ClassName[1];
		JavaSource javaSource = this.compiler.addWrapper(new Class[] { Connection.class, AdditionalFeature.class },
				Connection.class, "instance", (constructorContext) -> {
					this.compiler.writeConstructor(constructorContext.getSource(),
							constructorContext.getClassName().getClassName(),
							this.compiler.createField(Connection.class, "instance"),
							this.compiler.createField(int.class, "value"));
				}, (methodContext) -> {
					if (AdditionalFeature.class.equals(methodContext.getInterface())) {
						methodContext.write("return \"added\";");
					}
				}, (sourceContext) -> {
					className[0] = sourceContext.getClassName();
					Appendable source = sourceContext.getSource();
					source.append("  public ");
					this.compiler.writeMethodSignature(source, notExposedMethod);
					source.append(" {\n");
					source.append("     return \"value-\" + this.value;\n");
					source.append("  }\n");
				});

		// Ensure correct class name
		assertEquals("Incorrect class name", javaSource.getClassName(), className[0].getName());

		// Compile with wrapper
		Class<?> wrapperClass = javaSource.compile();

		// Record interactions for appropriate wrapping
		Connection mock = this.createMock(Connection.class);
		mock.setAutoCommit(true);
		this.recordReturn(mock, mock.getTransactionIsolation(), 1);
		this.replayMockObjects();

		// Wrap the connection
		Connection wrapper = (Connection) wrapperClass.getDeclaredConstructor(Connection.class, int.class)
				.newInstance(mock, 2);
		assertNotSame("Connection should be wrapped", mock, wrapper);

		// Undertake operations to ensure correct wrapping
		wrapper.setAutoCommit(true);
		assertEquals("Incorrect return value", 1, wrapper.getTransactionIsolation());

		// Ensure can cast to other interface and accessible
		assertTrue("Should be able to cast to other interfaces", wrapper instanceof AdditionalFeature);
		AdditionalFeature feature = (AdditionalFeature) wrapper;
		assertEquals("Incorrect additional feature value", "added", feature.getAdditionalFeature());

		// Ensure additional source available
		Method implementedNotExposedMethod = wrapper.getClass().getMethod("notExposedMethod");
		String result = (String) implementedNotExposedMethod.invoke(wrapper);
		assertEquals("Incorrect additional source value", "value-2", result);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Additional feature interface for testing wrapping.
	 */
	public static interface AdditionalFeature {

		String getAdditionalFeature();
	}

	/**
	 * Ensures can use {@link OfficeFloorJavaCompiler} helper methods in writing
	 * further methods.
	 */
	public static interface WrapperNonInterfaceMethods {

		String notExposedMethod();
	}

	/**
	 * <p>
	 * Ensure does not write the static and default methods.
	 * <p>
	 * Use the {@link JavaSourceWriter} if implementation is required for these
	 * methods.
	 */
	public void testIgnoreStaticAndDefaultMethods() throws Exception {

		// Obtain the methods
		Method staticMethod = StaticDefaultMethods.class.getMethod("staticMethod");
		Method defaultMethod = StaticDefaultMethods.class.getMethod("defaultMethod");
		Method implementedDefaultMethod = StaticDefaultMethods.class.getMethod("implementedDefaultMethod");

		// Create the wrapper
		JavaSource javaSource = this.compiler.addWrapper(StaticDefaultMethods.class, (context) -> {
			assertNotEquals("Should not be static method", staticMethod.getName(), context.getMethod().getName());
			assertNotEquals("Should not be default method", defaultMethod.getName(), context.getMethod().getName());
			assertNotEquals("Should not be implementing default method", implementedDefaultMethod.getName(),
					context.getMethod().getName());
		}, (sourceContext) -> {
			Appendable source = sourceContext.getSource();
			source.append("  public ");
			this.compiler.writeMethodSignature(source, implementedDefaultMethod);
			source.append(" {\n");
			source.append("    return \"OVERRIDDEN\";");
			source.append("  }\n");
		});

		// Compile with wrapper
		Class<?> wrapperClass = javaSource.compile();

		// Record interaction
		StaticDefaultMethods mock = this.createMock(StaticDefaultMethods.class);
		this.recordReturn(mock, mock.instanceMethod(), "INSTANCE");
		this.replayMockObjects();

		// Wrap the interface
		StaticDefaultMethods wrapper = (StaticDefaultMethods) wrapperClass
				.getDeclaredConstructor(StaticDefaultMethods.class).newInstance(mock);
		assertNotSame("Should be wrapped", mock, wrapper);

		// Undertake operations to ensure appropriate implementation
		assertEquals("Incorrect static field", "IGNORED", wrapper.getClass().getField("IGNORED_FIELD").get(wrapper));
		assertEquals("Incorrect default value", "DEFAULT", wrapper.defaultMethod());
		assertEquals("Incorrect implemented valued", "OVERRIDDEN", wrapper.implementedDefaultMethod());
		assertEquals("Incorrect instance value", "INSTANCE", wrapper.instanceMethod());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Interface containing static and default {@link Method} instances.
	 */
	public static interface StaticDefaultMethods {

		String IGNORED_FIELD = "IGNORED";

		static String staticMethod() {
			return "STATIC";
		}

		default String defaultMethod() {
			return "DEFAULT";
		}

		default String implementedDefaultMethod() {
			return "IMPLEMENTED";
		}

		String instanceMethod();
	}

}
