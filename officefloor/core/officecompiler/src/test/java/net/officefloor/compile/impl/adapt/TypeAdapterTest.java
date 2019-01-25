/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.adapt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link TypeAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeAdapterTest extends OfficeFrameTestCase {

	/**
	 * Client {@link ClassLoader}.
	 */
	private final ClassLoader clientClassLoader = Thread.currentThread().getContextClassLoader();

	/**
	 * Implementation {@link ClassLoader}.
	 */
	private ClassLoader implClassLoader = createNewClassLoader();

	/**
	 * Adapt String parameter.
	 */
	public void testAdaptStringParameter() {
		this.doParameterTest(StringParameter.class, "TEST");
	}

	public static class StringParameter {
		public boolean run(String parameter) {
			assertEquals("Incorrect parameter", "TEST", parameter);
			return true;
		}
	}

	/**
	 * Adapt primitive.
	 */
	public void testAdaptPrimitive() {
		this.doParameterTest(PrimitiveParameter.class, 1);
	}

	public static class PrimitiveParameter {
		public boolean run(int parameter) {
			assertEquals("Incorrect parameter", 1, parameter);
			return true;
		}
	}

	/**
	 * Adapt an array.
	 */
	public void testAdaptPrimitiveArray() {
		this.doParameterTest(PrimitiveArray.class, new long[] { 1, 2 });
	}

	public static class PrimitiveArray {
		public boolean run(long[] array) {
			assertEquals("Incorrect number of elements", 2, array.length);
			assertEquals("Incorrect first element", 1, array[0]);
			assertEquals("Incorrect second element", 2, array[1]);
			return true;
		}
	}

	/**
	 * Ensure can adapt a {@link Class}.
	 */
	public void testAdaptClass() {
		this.doParameterTest(ClassParameter.class, ClassParameter.class);
	}

	public static class ClassParameter {
		public boolean run(Class<?> parameter) {
			assertEquals("Incorrect class", ClassParameter.class, parameter);
			return true;
		}
	}

	/**
	 * Ensure can adapt a {@link ClassLoader}.
	 */
	public void testAdaptClassLoader() {
		ClassLoader classLoader = createNewClassLoader();
		this.doParameterTest(ClassLoaderParameter.class, classLoader);
	}

	public static class ClassLoaderParameter {
		public boolean run(ClassLoader parameter) throws Exception {
			assertNotNull("Should load class", parameter.loadClass(CLASS_LOADER_EXTRA_CLASS_NAME));
			return true;
		}
	}

	/**
	 * Ensure can adapt {@link Enum}.
	 */
	public void testAdaptEnum() {
		this.doParameterTest(EnumParameter.class, MockEnum.TEST);
	}

	public static enum MockEnum {
		TEST
	}

	public static class EnumParameter {
		public boolean run(MockEnum parameter) {
			assertEquals("Incorrect enum", MockEnum.TEST, parameter);
			return true;
		}
	}

	/**
	 * Ensure can adapt an {@link Exception}.
	 */
	public void testAdaptException() {
		this.doParameterTest(ExceptionParameter.class, new MockException("TEST"));
	}

	public static class MockException extends Exception {
		public MockException(String message) {
			super(message);
		}
	}

	public static class ExceptionParameter {
		public boolean run(MockException exception) {
			assertEquals("Incorrect exception", "TEST", exception.getMessage());
			return true;
		}
	}

	public void testAdaptedException() {
		this.doParameterTest(AdaptedExceptionParameter.class, new NonAdaptableException());
	}

	public static class NonAdaptableException extends Exception {
		public NonAdaptableException() {
			super("TEST");
		}
	}

	public static class AdaptedExceptionParameter {
		public boolean run(Throwable ex) {
			assertTrue("Should be adapted exception", ex instanceof AdaptedException);
			assertEquals("Incorrect adapted message", "TEST", ex.getMessage());
			return true;
		}
	}

	/**
	 * Ensure can adapt {@link Exception} implementation when should be object.
	 */
	public void testAdaptExceptionAsObject() {
		this.doParameterTest(ObjectParamater.class, new MockExceptionAsObject());
	}

	public static class MockExceptionAsObject extends Exception implements MockInterface {
		@Override
		public String getValue() {
			return "TEST";
		}
	}

	/**
	 * Ensure can adapt an object.
	 */
	public void testAdapatObject() {
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
			assertEquals("Incorrect object", "TEST", parameter.getValue());
			return true;
		}
	}

	public void testAdaptObjectArray() {
		this.doParameterTest(ObjectArray.class,
				(Object) new MockInterface[] { new MockClass("ONE"), new MockClass("TWO") });
	}

	public static class ObjectArray {
		public boolean run(MockInterface[] parameter) {
			assertEquals("Incorrect number of elements", 2, parameter.length);
			assertEquals("Incorrect first element", "ONE", parameter[0].getValue());
			assertEquals("Incorrect second element", "TWO", parameter[1].getValue());
			return true;
		}
	}

	/**
	 * Ensure not adapt {@link InputStream}.
	 */
	public void testInputStreamParameter() {
		this.doParameterTest(InputStreamParameter.class, new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
	}

	public static class InputStreamParameter {
		public boolean run(InputStream parameter) throws IOException {

			// Check all InputStream methods adapted
			assertEquals("Incorrect available", 4, parameter.available());
			assertEquals("Incorrect skip", 1, parameter.skip(1));
			assertEquals("Should have value", 2, parameter.read());
			byte[] buffer = new byte[1];
			parameter.read(buffer);
			assertEquals("Incorrect buffer", 3, buffer[0]);
			parameter.read(buffer, 0, 1);
			assertEquals("Incorrect offset buffer", 4, buffer[0]);

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
	public void testReturnValue() {
		Object value = this.doTest(ReturnValue.class);
		assertTrue("Should be adapted return value", value instanceof MockInterface);
		MockInterface adapted = (MockInterface) value;
		assertEquals("Incorrect adapted return value", "TEST", adapted.getValue());
	}

	public static class ReturnValue {
		public MockInterface run() {
			return new MockClass("TEST");
		}
	}

	/**
	 * Adapt the return {@link Class}.
	 */
	public void testClassReturnValue() {
		Object value = this.doTest(ClassReturnValue.class);
		assertTrue("Should be adapted return value", value instanceof Class);
		Class<?> adapted = (Class<?>) value;
		assertEquals("Incorrect adapted class return value", ClassReturnValue.class, adapted);
	}

	public static class ClassReturnValue {
		public Class<?> run() {
			return ClassReturnValue.class;
		}
	}

	/**
	 * Adapt the return {@link ClassLoader}.
	 */
	public void testClassLoaderReturnValue() {
		Object value = this.doTest(ClassLoaderReturnValue.class);
		assertTrue("Should be adapted return value", value instanceof ClassLoader);
		ClassLoader adapted = (ClassLoader) value;
		assertEquals("Incorrect adapted class loader return value", this.implClassLoader, adapted);
	}

	public static class ClassLoaderReturnValue {
		public ClassLoader run() {
			return this.getClass().getClassLoader();
		}
	}

	public void testClassNotOnClasspath() {
		Object value = this.doTest(ClassNotOnClasspath.class);
		assertNotNull("Should always get the class", value);
		assertTrue("Should be adapted return value", value instanceof Class);
		Class<?> clazz = (Class<?>) value;
		assertEquals("Incorrect adapted class (name of class used by Eclipse plugins)", CLASS_LOADER_EXTRA_CLASS_NAME,
				clazz.getName());
	}

	public static class ClassNotOnClasspath {
		public Class<?> run() throws ClassNotFoundException {
			return this.getClass().getClassLoader().loadClass(CLASS_LOADER_EXTRA_CLASS_NAME);
		}
	}

	public void testPropertyClassReturnValue() throws ClassNotFoundException {
		Object value = this.doTest(PropertyClassReturnValue.class);
		assertTrue("Should be adapted return value", value instanceof PropertyClass);
		PropertyClass propertyClass = (PropertyClass) value;
		assertEquals("Incorrect adapted property class (name of class used by Eclipse plugins",
				CLASS_LOADER_EXTRA_CLASS_NAME, propertyClass.getPropertyClass().getName());
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
			return this.getClass().getClassLoader().loadClass(CLASS_LOADER_EXTRA_CLASS_NAME);
		}
	}

	/**
	 * Ensure can return {@link Throwable}.
	 */
	public void testThrowableReturnValue() {
		Object value = this.doTest(ThrowableReturnValue.class);
		assertTrue("Should be adapted return value", value instanceof Error);
		Throwable exception = (Throwable) value;
		assertEquals("Incorrect cause", "test", exception.getMessage());
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
		assertTrue("Should be successful", ((Boolean) result).booleanValue());
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
			assertNotSame("Implementation should not be compatible", clazz, implementation.getClass());

			// Obtain the method
			Method method = null;
			for (Method check : clazz.getMethods()) {
				if ("run".equals(check.getName())) {
					method = check;
				}
			}
			assertNotNull("Can not find method 'run'", method);

			// Obtain the parameter types
			Class<?>[] parameterTypes = method.getParameterTypes();

			// Invoke the method
			Object result = TypeAdapter.invokeMethod(implementation, "run", parameters, parameterTypes,
					this.clientClassLoader, this.implClassLoader);

			// Return the result
			return result;

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

}