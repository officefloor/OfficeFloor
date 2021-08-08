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

package net.officefloor.frame.compatibility;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.officefloor.frame.compatibility.ClassCompatibility.ArgumentCompatibility;
import net.officefloor.frame.compatibility.ClassCompatibility.CompatibilityInvocationException;
import net.officefloor.frame.compatibility.ClassCompatibility.ObjectCompatibility;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClassCompatibility}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassCompatibilityTest extends OfficeFrameTestCase {

	/**
	 * {@link ClassCompatibility} to test.
	 */
	private ClassCompatibility clazz;

	@Override
	protected void setUp() throws Exception {
		this.clazz = new ClassCompatibility(MockClass.class.getName(), this.getClass().getClassLoader());
	}

	/**
	 * Tests unknown {@link Class}.
	 */
	public void testUnknownClass() {
		final String UNKNOWN_CLASS_NAME = "UNKNOWN_CLASS";
		try {
			new ClassCompatibility("UNKNOWN_CLASS", createNewClassLoader());
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect message", "Version compatiblity issue.  Unknown class " + UNKNOWN_CLASS_NAME,
					ex.getMessage());
		}
	}

	/**
	 * Ensure can use default constructor to instantiate object.
	 */
	public void testDefaultConstructor() {
		ObjectCompatibility object = this.clazz._new();
		assertEquals("Incorrect constructor", "default", object.$("getConstructor").get(String.class));
		assertEquals("Incorrect value", "default", object.$("getValue").get(String.class));
	}

	/**
	 * Ensure can instantiate object with argument.
	 */
	public void testInstantiateWithValue() {
		ObjectCompatibility object = this.clazz._new("init");
		assertEquals("Incorrect constructor", String.class.getName(), object.$("getConstructor").get(String.class));
		assertEquals("Incorrect value", "init", object.$("getValue").get(String.class));
	}

	/**
	 * Ensure can instantiate with {@link ArgumentCompatibility}.
	 */
	public void testInstantiateWithArgument() {
		ObjectCompatibility object = this.clazz._new(this.clazz.arg("argument", CharSequence.class.getName()));
		assertEquals("Incorrect constructor", CharSequence.class.getName(),
				object.$("getConstructor").get(String.class));
		assertEquals("Incorrect value", "argument", object.$("getValue").get(String.class));
	}

	/**
	 * Ensure fails on unknown constructor.
	 */
	public void testUnknownConstructor() {
		try {
			this.clazz._new(new HashMap<>());
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", "Version compatibility issue. Unknown constructor "
					+ MockClass.class.getName() + "(" + HashMap.class.getName() + ")", ex.getMessage());
		}
	}

	/**
	 * Ensure can invoke class method.
	 */
	public void testInvokeClassMethod() {
		assertEquals("Incorrect value", Integer.valueOf(100), this.clazz.$("getStatic").get(Integer.class));
	}

	/**
	 * Ensure can invoke class method with value.
	 */
	public void testInvokeClassMethodWithValue() {
		assertEquals("Incorrect value", Integer.valueOf(200), this.clazz.$("getStatic", 200).get(Integer.class));
	}

	/**
	 * Ensure can invoke class method with {@link ArgumentCompatibility}.
	 */
	public void testInvokeClassMethodWithArgument() {
		assertEquals("Incorrect value", "STATIC",
				this.clazz.$("getStatic", this.clazz.arg("STATIC", CharSequence.class)).get(String.class));
	}

	/**
	 * Ensure fails on unknown class method.
	 */
	public void testUnknownClassMethod() {
		try {
			this.clazz.$("unknownMethod", "test", 1, this.clazz.arg("test", CharSequence.class));
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Version compatibility issue. Unknown static method " + MockClass.class.getName()
							+ "#unknownMethod(" + String.class.getName() + ", " + Integer.class.getName() + ", "
							+ CharSequence.class.getName() + ")",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can invoke instance method.
	 */
	public void testInvokeInstanceMethod() {
		ObjectCompatibility object = this.clazz._new("instance");
		assertEquals("Incorrect value", "instance-10", object.$("getInstance").get(String.class));
	}

	/**
	 * Ensure can invoke instance method with value.
	 */
	public void testInvokeInstanceMethodWithValue() {
		ObjectCompatibility object = this.clazz._new("instance");
		assertEquals("Incorrect value", "instance-200", object.$("getInstance", 200).get(String.class));
	}

	/**
	 * Ensure can invoke instance method with {@link ArgumentCompatibility}.
	 */
	public void testInvokeInstanceMethodWithArgument() {
		ObjectCompatibility object = this.clazz._new("instance");
		assertEquals("Incorrect value", "instance-input",
				object.$("getInstance", this.clazz.arg("input", CharSequence.class)).get(String.class));
	}

	/**
	 * Ensure fails on unknown instance method.
	 */
	public void testUnknownInstanceMethod() {
		ObjectCompatibility object = this.clazz._new();
		try {
			object.$("unknownMethod", "test", 1, this.clazz.arg("test", CharSequence.class));
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Version compatibility issue. Unknown method " + MockClass.class.getName() + "#unknownMethod("
							+ String.class.getName() + ", " + Integer.class.getName() + ", "
							+ CharSequence.class.getName() + ")",
					ex.getMessage());
		}
	}

	/**
	 * Ensure handle void return.
	 */
	public void testVoidReturn() {
		ObjectCompatibility object = this.clazz._new();
		assertNull("Should not have return on void method", object.$("voidReturn"));
	}

	/**
	 * Ensure handle null return.
	 */
	public void testNullReturn() {
		ObjectCompatibility object = this.clazz._new();
		assertNull("Should not have null return", object.$("nullReturn").get(Object.class));
	}

	/**
	 * Ensure handle {@link Exception} from {@link Method}.
	 */
	public void testException() {
		ObjectCompatibility object = this.clazz._new();
		final IOException exception = new IOException("TEST");
		try {
			object.$("exception", this.clazz.arg(exception, Exception.class));
			fail("Should not be successful");
		} catch (CompatibilityInvocationException ex) {
			assertEquals("Incorrect message", "TEST", ex.getMessage());
			assertSame("Incorrect cause", exception, ex.getCause());
		}
	}

	/**
	 * Ensure provide primitive.
	 */
	public void testPrimitive() {
		ObjectCompatibility object = this.clazz._new();
		int result = object.$("getPrimitive", this.clazz.arg(100, int.class)).get(Integer.class);
		assertEquals("Incorrect result", 100, result);
	}

	/**
	 * Ensure handle multiple arguments.
	 */
	public void testMultipleArguments() {
		ObjectCompatibility object = this.clazz._new("instance");
		assertEquals("Incorrect value", "prefix-instance-10-sequence",
				object.$("getMultipleArguments", "prefix", 10, this.clazz.arg("sequence", CharSequence.class))
						.get(String.class));
	}

	/**
	 * Ensure handle compatibility arguments.
	 */
	public void testCompatibilityArguments() {
		ObjectCompatibility object = this.clazz._new();
		ObjectCompatibility value = this.clazz._new("input");
		assertEquals("Incorrect value", "input-10-20-default-10",
				object.$("getCompatibility", value.arg(MockClass.class.getName()),
						this.clazz.$("getStatic", 20).arg(Integer.class),
						object.$("getInstance").arg(String.class.getName())).get(String.class));
	}

	/**
	 * Ensure handle wrapping the object.
	 */
	public void testWrapObject() {
		MockClass object = new MockClass("wrap");
		ObjectCompatibility compatibility = ClassCompatibility.object(object);
		assertEquals("Incorrect wrapped value", "wrap-10", compatibility.$("getInstance").get(String.class));
	}

	/**
	 * Ensure can work on separate class loader.
	 */
	public void testDifferentClassLoader() {

		ClassCompatibility clazz = new ClassCompatibility(CLASS_LOADER_EXTRA_CLASS_NAME, createNewClassLoader());
		ObjectCompatibility object = clazz._new();

		// Ensure able to invoke method
		String className = object.$("getClass").$("getName").get(String.class);
		assertEquals("Incorrect class name", CLASS_LOADER_EXTRA_CLASS_NAME, className);
	}

	public static class MockClass {

		public static int getStatic() {
			return 100;
		}

		public static int getStatic(Integer value) {
			return value;
		}

		public static String getStatic(CharSequence sequence) {
			return sequence.toString();
		}

		private final String constructor;

		private String value = "default";

		public MockClass() {
			this.constructor = "default";
		}

		public MockClass(String value) {
			this.constructor = String.class.getName();
			this.value = value;
		}

		public MockClass(CharSequence sequence) {
			this.constructor = CharSequence.class.getName();
			this.value = sequence.toString();
		}

		public String getConstructor() {
			return this.constructor;
		}

		public String getValue() {
			return this.value;
		}

		public String getInstance() {
			return this.value + "-" + 10;
		}

		public String getInstance(Integer value) {
			return this.value + "-" + value;
		}

		public String getInstance(CharSequence sequence) {
			return this.value + "-" + sequence.toString();
		}

		public void voidReturn() {
		}

		public Object nullReturn() {
			return null;
		}

		public void exception(Exception exception) throws Exception {
			throw exception;
		}

		public int getPrimitive(int value) {
			return value;
		}

		public String getMultipleArguments(String prefix, Integer suffix, CharSequence sequence) {
			return prefix + "-" + this.value + "-" + suffix + "-" + sequence.toString();
		}

		public String getCompatibility(MockClass mock, Integer staticResult, String instanceResult) {
			return mock.getInstance() + "-" + staticResult + "-" + instanceResult;
		}
	}

}
