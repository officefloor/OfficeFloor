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

package net.officefloor.compile.impl.adapt;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.test.ClassLoaderTestSupport;
import net.officefloor.frame.test.TestSupportExtension;

/**
 * Tests the {@link TypeAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class TypeAdapterTest {

	private final ClassLoaderTestSupport classLoading = new ClassLoaderTestSupport();

	/**
	 * Client {@link ClassLoader}.
	 */
	private final ClassLoader clientClassLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * Implementation {@link ClassLoader}.
	 */
	private ClassLoader implClassLoader;

	@BeforeEach
	public void setup() {
		this.implClassLoader = this.classLoading.createNewClassLoader();
	}

	/**
	 * Adapt String parameter.
	 */
	@Test
	public void adaptStringParameter() {
		this.doParameterTest(StringParameter.class, "TEST");
	}

	public static class StringParameter {
		public boolean run(String parameter) {
			assertEquals("TEST", parameter, "Incorrect parameter");
			return true;
		}
	}

	/**
	 * Adapt primitive.
	 */
	@Test
	public void adaptPrimitive() {
		this.doParameterTest(PrimitiveParameter.class, 1);
	}

	public static class PrimitiveParameter {
		public boolean run(int parameter) {
			assertEquals(1, parameter, "Incorrect parameter");
			return true;
		}
	}

	/**
	 * Adapt an array.
	 */
	@Test
	public void adaptPrimitiveArray() {
		this.doParameterTest(PrimitiveArray.class, new long[] { 1, 2 });
	}

	public static class PrimitiveArray {
		public boolean run(long[] array) {
			assertEquals(2, array.length, "Incorrect number of elements");
			assertEquals(1, array[0], "Incorrect first element");
			assertEquals(2, array[1], "Incorrect second element");
			return true;
		}
	}

	/**
	 * Adapt a primitive {@link List}.s
	 */
	@Test
	public void adaptPrimativeList() {
		this.doParameterTest(PrimitiveList.class, Arrays.asList(10));
	}

	public static class PrimitiveList {
		public boolean run(List<Integer> list) {
			assertEquals(1, list.size(), "Incorrect number of elements");
			assertEquals(10, list.get(0), "Incorrect value");
			return true;
		}
	}

	/**
	 * Ensure can adapt a {@link Class}.
	 */
	@Test
	public void adaptClass() {
		this.doParameterTest(ClassParameter.class, ClassParameter.class);
	}

	public static class ClassParameter {
		public boolean run(Class<?> parameter) {
			assertEquals(ClassParameter.class, parameter, "Incorrect class");
			return true;
		}
	}

	/**
	 * Ensure can adapt a {@link ClassLoader}.
	 */
	@Test
	public void adaptClassLoader() {
		ClassLoader classLoader = this.classLoading.createNewClassLoader();
		this.doParameterTest(ClassLoaderParameter.class, classLoader);
	}

	public static class ClassLoaderParameter {
		public boolean run(ClassLoader parameter) throws Exception {
			assertNotNull(parameter.loadClass(ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME),
					"Should load class");
			return true;
		}
	}

	/**
	 * Ensure can adapt {@link Enum}.
	 */
	@Test
	public void adaptEnum() {
		this.doParameterTest(EnumParameter.class, MockEnum.TEST);
	}

	public static enum MockEnum {
		TEST
	}

	public static class EnumParameter {
		public boolean run(MockEnum parameter) {
			assertEquals(MockEnum.TEST, parameter, "Incorrect enum");
			return true;
		}
	}

	/**
	 * Ensure can adapt {@link Logger}.
	 */
	@Test
	public void adaptLogger() {
		this.doParameterTest(LoggerParameter.class, OfficeFrame.getLogger("LOGGER"));
	}

	public static class LoggerParameter {
		public boolean run(Logger logger) {
			assertEquals("LOGGER", logger.getName(), "Incorrect logger");
			return true;
		}
	}

	/**
	 * Ensure can adapt an {@link Exception}.
	 */
	@Test
	public void adaptException() {
		this.doParameterTest(ExceptionParameter.class, new MockException("TEST"));
	}

	public static class MockException extends Exception {
		private static final long serialVersionUID = 1L;

		public MockException(String message) {
			super(message);
		}
	}

	public static class ExceptionParameter {
		public boolean run(MockException exception) {
			assertEquals("TEST", exception.getMessage(), "Incorrect exception");
			return true;
		}
	}

	/**
	 * Ensure adapt {@link Exception}.
	 */
	@Test
	public void adaptedException() {
		this.doParameterTest(AdaptedExceptionParameter.class, new NonAdaptableException());
	}

	public static class NonAdaptableException extends Exception {
		private static final long serialVersionUID = 1L;

		public NonAdaptableException() {
			super("TEST");
		}
	}

	public static class AdaptedExceptionParameter {
		public boolean run(Throwable ex) {
			assertTrue(ex instanceof AdaptedException, "Should be adapted exception");
			assertEquals("TEST", ex.getMessage(), "Incorrect adapted message");
			return true;
		}
	}

	/**
	 * Ensure can adapt {@link Exception} implementation when should be object.
	 */
	@Test
	public void adaptExceptionAsObject() {
		this.doParameterTest(ObjectParamater.class, new MockExceptionAsObject());
	}

	public static class MockExceptionAsObject extends Exception implements MockInterface {
		private static final long serialVersionUID = 1L;

		@Override
		public String getValue() {
			return "TEST";
		}
	}

	/**
	 * Ensure can adapt an object.
	 */
	@Test
	public void adapatObject() {
		this.doParameterTest(ObjectParamater.class, new MockClass("TEST"));
	}

	public static interface MockInterface {
		String getValue();
	}

	public static class MockClass implements MockInterface {

		private final String value;

		public MockClass(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

	public static class ObjectParamater {
		public boolean run(MockInterface parameter) {
			assertEquals("TEST", parameter.getValue(), "Incorrect object");
			return true;
		}
	}

	/**
	 * Ensure adapt object array.
	 */
	@Test
	public void adaptObjectArray() {
		this.doParameterTest(ObjectArray.class,
				(Object) new MockInterface[] { new MockClass("ONE"), new MockClass("TWO") });
	}

	public static class ObjectArray {
		public boolean run(MockInterface[] parameter) {
			assertEquals(2, parameter.length, "Incorrect number of elements");
			assertEquals("ONE", parameter[0].getValue(), "Incorrect first element");
			assertEquals("TWO", parameter[1].getValue(), "Incorrect second element");
			return true;
		}
	}

	/**
	 * Ensure adapt object {@link List}.
	 */
	@Test
	public void adaptObjectList() {
		this.doParameterTest(ObjectList.class, Arrays.asList(new MockClass("ONE"), new MockClass("TWO")));
	}

	public static class ObjectList {
		public boolean run(List<MockInterface> parameter) {
			assertEquals(2, parameter.size(), "Incorrect number of elements");
			assertEquals("ONE", parameter.get(0).getValue(), "Incorrect first element");
			assertEquals("TWO", parameter.get(1).getValue(), "Incorrect second element");
			return true;
		}
	}

	/**
	 * Ensure not adapt {@link InputStream}.
	 */
	@Test
	public void inputStreamParameter() {
		this.doParameterTest(InputStreamParameter.class, new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
	}

	public static class InputStreamParameter {
		public boolean run(InputStream parameter) throws IOException {

			// Check all InputStream methods adapted
			assertEquals(4, parameter.available(), "Incorrect available");
			assertEquals(1, parameter.skip(1), "Incorrect skip");
			assertEquals(2, parameter.read(), "Should have value");
			byte[] buffer = new byte[1];
			parameter.read(buffer);
			assertEquals(3, buffer[0], "Incorrect buffer");
			parameter.read(buffer, 0, 1);
			assertEquals(4, buffer[0], "Incorrect offset buffer");

			// Ensure can invoke other methods
			parameter.mark(1);
			parameter.markSupported();
			parameter.reset();

			// Close the stream
			parameter.close();

			return true;
		}
	}

	/**
	 * Adapt the return value.
	 */
	@Test
	public void returnValue() {
		Object value = this.doTest(ReturnValue.class);
		assertTrue(value instanceof MockInterface, "Should be adapted return value");
		MockInterface adapted = (MockInterface) value;
		assertEquals("TEST", adapted.getValue(), "Incorrect adapted return value");
	}

	public static class ReturnValue {
		public MockInterface run() {
			return new MockClass("TEST");
		}
	}

	/**
	 * Adapt the return {@link Class}.
	 */
	@Test
	public void classReturnValue() {
		Object value = this.doTest(ClassReturnValue.class);
		assertTrue(value instanceof Class, "Should be adapted return value");
		Class<?> adapted = (Class<?>) value;
		assertEquals(ClassReturnValue.class, adapted, "Incorrect adapted class return value");
	}

	public static class ClassReturnValue {
		public Class<?> run() {
			return ClassReturnValue.class;
		}
	}

	/**
	 * Adapt the return {@link ClassLoader}.
	 */
	@Test
	public void classLoaderReturnValue() {
		Object value = this.doTest(ClassLoaderReturnValue.class);
		assertTrue(value instanceof ClassLoader, "Should be adapted return value");
		ClassLoader adapted = (ClassLoader) value;
		assertEquals(this.implClassLoader, adapted, "Incorrect adapted class loader return value");
	}

	public static class ClassLoaderReturnValue {
		public ClassLoader run() {
			return this.getClass().getClassLoader();
		}
	}

	/**
	 * Adapt the return {@link Logger}.
	 */
	@Test
	public void loggerReturnValue() {
		Object value = this.doTest(LoggerReturnValue.class);
		assertTrue(value instanceof Logger, "Should be the logger");
		Logger adapted = (Logger) value;
		assertEquals(OfficeFrame.getLogger("TEST"), adapted, "Incorrect looger");
	}

	public static class LoggerReturnValue {
		public Logger run() {
			return OfficeFrame.getLogger("TEST");
		}
	}

	/**
	 * Handle {@link Class} not on {@link ClassLoader}.
	 */
	@Test
	public void classNotOnClasspath() {
		Object value = this.doTest(ClassNotOnClasspath.class);
		assertNotNull(value, "Should always get the class");
		assertTrue(value instanceof Class, "Should be adapted return value");
		Class<?> clazz = (Class<?>) value;
		assertEquals(ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME, clazz.getName(),
				"Incorrect adapted class (name of class used by Eclipse plugins)");
	}

	public static class ClassNotOnClasspath {
		public Class<?> run() throws ClassNotFoundException {
			return this.getClass().getClassLoader().loadClass(ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME);
		}
	}

	/**
	 * Ensure can obtain {@link Class} from property of returned adapted object.
	 */
	@Test
	public void propertyClassReturnValue() throws ClassNotFoundException {
		Object value = this.doTest(PropertyClassReturnValue.class);
		assertTrue(value instanceof PropertyClass, "Should be adapted return value");
		PropertyClass propertyClass = (PropertyClass) value;
		assertEquals(ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME, propertyClass.getPropertyClass().getName(),
				"Incorrect adapted property class (name of class used by Eclipse plugins");
	}

	public static class PropertyClassReturnValue {
		public PropertyClass run() {
			return new PropertyClassImpl();
		}
	}

	public static interface PropertyClass {
		Class<?> getPropertyClass() throws ClassNotFoundException;
	}

	public static class PropertyClassImpl implements PropertyClass {
		@Override
		public Class<?> getPropertyClass() throws ClassNotFoundException {
			return this.getClass().getClassLoader().loadClass(ClassLoaderTestSupport.CLASS_LOADER_EXTRA_CLASS_NAME);
		}
	}

	/**
	 * Ensure can return {@link Throwable}.
	 */
	@Test
	public void throwableReturnValue() {
		Object value = this.doTest(ThrowableReturnValue.class);
		assertTrue(value instanceof Error, "Should be adapted return value");
		Throwable exception = (Throwable) value;
		assertEquals("test", exception.getMessage(), "Incorrect cause");
	}

	public static class ThrowableReturnValue {
		public Throwable run() {
			return new Error("test");
		}
	}

	/**
	 * Undertakes the parameter test.
	 * 
	 * @param clazz      {@link Class}.
	 * @param parameters Parameters.
	 */
	private void doParameterTest(Class<?> clazz, Object... parameters) {
		Object result = this.doTest(clazz, parameters);
		assertTrue(((Boolean) result).booleanValue(), "Should be successful");
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param clazz     {@link Class}.
	 * @param paramters Parameters.
	 */
	private Object doTest(Class<?> clazz, Object... parameters) {
		try {

			// Load the implementation
			Object implementation = this.implClassLoader.loadClass(clazz.getName()).getDeclaredConstructor()
					.newInstance();

			// Ensure non-compatible implementation
			assertNotSame(clazz, implementation.getClass(), "Implementation should not be compatible");

			// Obtain the method
			Method method = null;
			for (Method check : clazz.getMethods()) {
				if ("run".equals(check.getName())) {
					method = check;
				}
			}
			assertNotNull(method, "Can not find method 'run'");

			// Obtain the parameter types
			Class<?>[] parameterTypes = method.getParameterTypes();

			// Invoke the method
			Object result = TypeAdapter.invokeMethod(implementation, "run", parameters, parameterTypes,
					this.clientClassLoader, this.implClassLoader);

			// Return the result
			return result;

		} catch (Throwable ex) {
			return fail(ex);
		}
	}

}
