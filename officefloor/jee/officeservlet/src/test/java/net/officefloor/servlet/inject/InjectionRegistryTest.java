/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;

/**
 * Tests the {@link InjectionRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectionRegistryTest extends OfficeFrameTestCase {

	/**
	 * {@link InjectionRegistry} to test.
	 */
	private final InjectionRegistry registry = new InjectionRegistry(new FieldDependencyExtractor[] {
			(field) -> field.isAnnotationPresent(Dependency.class) ? new RequiredDependency(null, field.getType())
					: null });

	/**
	 * {@link SupplierSourceContext}.
	 */
	private final SupplierSourceContext supplierSource = this.createMock(SupplierSourceContext.class);

	/**
	 * Ensure can load interface dependency.
	 */
	public void testInterfaceDependency() throws Exception {

		// Record supplier source
		SupplierThreadLocal<String> supplierThreadLocal = this.mockSupplierThreadLocal();
		this.recordReturn(this.supplierSource, this.supplierSource.getClassLoader(), this.getClass().getClassLoader());
		this.recordReturn(this.supplierSource,
				this.supplierSource.addSupplierThreadLocal(null, MockInterfaceDependency.class), supplierThreadLocal);

		// Obtained twice for each context
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), new MockInterfaceDependencyImpl("ONE"));
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), new MockInterfaceDependencyImpl("TWO"));

		// Ensure can inject dependencies
		this.replayMockObjects();

		// Register class
		this.registry.registerForInjection(MockSingleInterfaceInject.class, this.supplierSource);

		// Create factory
		InjectContextFactory factory = this.registry.createInjectContextFactory();

		// Load the dependencies
		MockSingleInterfaceInject one = factory.injectDependencies(new MockSingleInterfaceInject());
		MockSingleInterfaceInject two = factory.injectDependencies(new MockSingleInterfaceInject());

		// Ensure can load injections
		factory.createInjectContext().activate();
		assertEquals("Incorrect dependency", "ONE", one.dependency.getValue());

		// Ensure sources thread local for other context
		factory.createInjectContext().activate();
		assertEquals("Incorrect dependency", "TWO", two.dependency.getValue());

		this.verifyMockObjects();
	}

	public static class MockSingleInterfaceInject {
		@Dependency
		private MockInterfaceDependency dependency;
	}

	public static interface MockInterfaceDependency {
		String getValue();
	}

	private static class MockInterfaceDependencyImpl implements MockInterfaceDependency {

		private final String value;

		private MockInterfaceDependencyImpl(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Ensure can load class dependency.
	 */
	public void testClassDependency() throws Exception {

		// Record supplier source
		SupplierThreadLocal<String> supplierThreadLocal = this.mockSupplierThreadLocal();
		this.recordReturn(this.supplierSource, this.supplierSource.getClassLoader(), this.getClass().getClassLoader());
		this.recordReturn(this.supplierSource,
				this.supplierSource.addSupplierThreadLocal(null, MockClassDependency.class), supplierThreadLocal);
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), new MockClassDependency("CLASS"));

		// Ensure can inject dependencies
		this.replayMockObjects();

		// Register class
		this.registry.registerForInjection(MockSingleClassInject.class, this.supplierSource);

		// Create factory
		InjectContextFactory factory = this.registry.createInjectContextFactory();

		// Load the dependencies
		MockSingleClassInject object = factory.injectDependencies(new MockSingleClassInject());

		// Ensure can load injections
		factory.createInjectContext().activate();
		assertEquals("Incorrect dependency", "CLASS", object.dependency.getValue());

		this.verifyMockObjects();
	}

	@SuppressWarnings("unchecked")
	private <T> SupplierThreadLocal<T> mockSupplierThreadLocal() {
		return this.createMock(SupplierThreadLocal.class);
	}

	public static class MockSingleClassInject {
		@Dependency
		private MockClassDependency dependency;
	}

	public static class MockClassDependency {

		private final String value;

		private MockClassDependency(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Ensure can load objects re-using dependencies.
	 */
	public void testReusingDependencies() throws Exception {

		// Record supplier source
		SupplierThreadLocal<String> interfaceThreadLocal = this.mockSupplierThreadLocal();
		SupplierThreadLocal<String> classThreadLocal = this.mockSupplierThreadLocal();
		this.recordReturn(this.supplierSource, this.supplierSource.getClassLoader(), this.getClass().getClassLoader());
		this.recordReturn(this.supplierSource,
				this.supplierSource.addSupplierThreadLocal(null, MockInterfaceDependency.class), interfaceThreadLocal);
		this.recordReturn(this.supplierSource, this.supplierSource.getClassLoader(), this.getClass().getClassLoader());
		this.recordReturn(this.supplierSource,
				this.supplierSource.addSupplierThreadLocal(null, MockClassDependency.class), classThreadLocal);

		// Obtained only once (even if re-used)
		this.recordReturn(interfaceThreadLocal, interfaceThreadLocal.get(),
				new MockInterfaceDependencyImpl("INTERFACE"));
		this.recordReturn(classThreadLocal, classThreadLocal.get(), new MockClassDependency("CLASS"));

		// Ensure can inject dependencies
		this.replayMockObjects();

		// Register classes
		this.registry.registerForInjection(MockSingleInterfaceInject.class, this.supplierSource);
		this.registry.registerForInjection(MockSingleClassInject.class, this.supplierSource);
		this.registry.registerForInjection(MockMultipleInject.class, this.supplierSource);

		// Create factory
		InjectContextFactory factory = this.registry.createInjectContextFactory();

		// Load the dependencies
		MockSingleInterfaceInject interfaceInject = factory.injectDependencies(new MockSingleInterfaceInject());
		MockSingleClassInject classInject = factory.injectDependencies(new MockSingleClassInject());
		MockMultipleInject multipleInject = factory.injectDependencies(new MockMultipleInject());

		// Ensure correct injections
		factory.createInjectContext().activate();
		assertEquals("Incorrect single interface", "INTERFACE", interfaceInject.dependency.getValue());
		assertEquals("Incorrect single class", "CLASS", classInject.dependency.getValue());
		assertEquals("Incorrect multiple interface", "INTERFACE", multipleInject.interfaceDependency.getValue());
		assertEquals("Incorrect multiple class", "CLASS", multipleInject.classDependency.getValue());
		assertSame("Should be same interface dependency", interfaceInject.dependency,
				multipleInject.interfaceDependency);
		assertSame("Should be same class dependency", classInject.dependency, multipleInject.classDependency);

		this.verifyMockObjects();
	}

	public static class MockMultipleInject {
		@Dependency
		private MockClassDependency classDependency;

		@Dependency
		private MockInterfaceDependency interfaceDependency;
	}

	/**
	 * Ensure can activate to use on another {@link Thread}.
	 */
	public void testActivateForAnotherThread() throws Exception {

		// Record supplier source
		SupplierThreadLocal<String> supplierThreadLocal = this.mockSupplierThreadLocal();
		this.recordReturn(this.supplierSource, this.supplierSource.getClassLoader(), this.getClass().getClassLoader());
		this.recordReturn(this.supplierSource,
				this.supplierSource.addSupplierThreadLocal(null, MockClassDependency.class), supplierThreadLocal);
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), new MockClassDependency("CLASS"));

		// Ensure can inject dependencies
		this.replayMockObjects();

		// Register class
		this.registry.registerForInjection(MockSingleClassInject.class, this.supplierSource);

		// Create factory
		InjectContextFactory factory = this.registry.createInjectContextFactory();

		// Load the dependencies
		MockSingleClassInject object = factory.injectDependencies(new MockSingleClassInject());

		// Activate for another thread
		InjectContext context = factory.createInjectContext();
		context.synchroniseForAnotherThread();

		this.verifyMockObjects();

		// Ensure obtain injections (as now loaded)
		context.activate();
		assertEquals("Incorrect dependency", "CLASS", object.dependency.getValue());
	}

}
