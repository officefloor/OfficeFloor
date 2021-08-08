/*-
 * #%L
 * Dependency
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

package net.officefloor.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.sf.cglib.proxy.Enhancer;

/**
 * Tests the {@link OfficeFloorThreadLocalDependency}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class OfficeFloorThreadLocalDependencyTest {

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * Ensure can proxy interface.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void interfaceDependency() {

		// Create mocks
		Supplier<InterfaceDependency> supplierThreadLocal = this.mocks.createMock(Supplier.class);
		InterfaceDependency dependencyOne = this.mocks.createMock(InterfaceDependency.class);
		InterfaceDependency dependencyTwo = this.mocks.createMock(InterfaceDependency.class);

		// Record accessing thread local
		this.mocks.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyOne);
		this.mocks.recordReturn(dependencyOne, dependencyOne.getValue(), "interface");
		this.mocks.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyTwo);
		dependencyTwo.setValue("interface");

		// Create class loader
		ClassLoader classLoader = this.getClass().getClassLoader();

		// Test
		this.mocks.replayMockObjects();

		// Create the proxy
		InterfaceDependency proxy = OfficeFloorThreadLocalDependency.newStaticProxy(InterfaceDependency.class,
				classLoader, supplierThreadLocal);
		assertTrue(Proxy.isProxyClass(proxy.getClass()), "Dependency should be a proxy");

		// Ensure dependencies obtain thread local
		assertEquals("interface", proxy.getValue(), "Incorrect first dependency value");
		proxy.setValue("interface");

		// Verify
		this.mocks.verifyMockObjects();
	}

	public static interface InterfaceDependency {

		String getValue();

		void setValue(String value);
	}

	/**
	 * Ensure can proxy object.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void objectDependency() {

		// Create mocks
		Supplier<ObjectDependency> supplierThreadLocal = this.mocks.createMock(Supplier.class);
		ObjectDependency dependencyOne = new ObjectDependency();
		ObjectDependency dependencyTwo = new ObjectDependency();

		// Record accessing thread local
		this.mocks.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyOne);
		this.mocks.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyTwo);

		// Create class loader
		ClassLoader classLoader = this.getClass().getClassLoader();

		// Test
		this.mocks.replayMockObjects();

		// Create the proxy
		ObjectDependency proxy = OfficeFloorThreadLocalDependency.newStaticProxy(ObjectDependency.class, classLoader,
				supplierThreadLocal);
		assertFalse(Proxy.isProxyClass(proxy.getClass()), "Dependency should not be a reflection proxy");
		assertTrue(Enhancer.isEnhanced(proxy.getClass()), "Dependency should be a cglib enhanced");

		// Ensure correct first dependency
		proxy.setValue("value");
		assertEquals("value", dependencyOne.getValue(), "Should set on first dependency");

		// Ensure correct second dependency
		assertEquals("object", proxy.getValue(), "Incorrect second dependency");

		// Verify
		this.mocks.verifyMockObjects();
	}

	public static class ObjectDependency {

		private String value = "object";

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}
