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

package net.officefloor.frame.impl.execute.managedobject.threadlocal;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests accessing the {@link OptionalThreadLocal} from a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure the {@link OptionalThreadLocal} not provide object if
	 * {@link ManagedObject} not instantiated.
	 */
	public void test_Process_NotAvailable() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.PROCESS, false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void test_Process_Available() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.PROCESS, true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void test_Process_NotAvailableWithTeam() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.PROCESS, false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void test_Process_AvailableWithTeam() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.PROCESS, true, true);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} not provide object if
	 * {@link ManagedObject} not instantiated.
	 */
	public void test_Thread_NotAvailable() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.THREAD, false, false);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value.
	 */
	public void test_Thread_Available() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.THREAD, true, false);
	}

	/**
	 * Ensure the {@link OptionalThreadLocal} no provide object if
	 * {@link ManagedObject} not instantiated via {@link Team} on another
	 * {@link Thread}.
	 */
	public void test_Thread_NotAvailableWithTeam() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.THREAD, false, true);
	}

	/**
	 * Ensure the {@link ManagedFunction} can access the {@link OptionalThreadLocal}
	 * value run by {@link Team} on another {@link Thread}.
	 */
	public void test_Thread_AvailableWithTeam() throws Exception {
		this.doManagedObjectTest(ManagedObjectScope.THREAD, true, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope             {@link ManagedObjectScope}.
	 * @param isExpectAvailable If object should be available.
	 * @param isTeam            If use {@link Team} for {@link ManagedFunction}.
	 */
	private void doManagedObjectTest(ManagedObjectScope scope, boolean isExpectAvailable, boolean isTeam)
			throws Exception {

		// Construct the thread local
		String value = "TEST";
		this.constructManagedObject(value, "VALUE_MOS", this.getOfficeName());

		// Construct the dependency
		Dependency dependency = new Dependency();
		this.constructManagedObject(dependency, "DEPENDENCY_MOS", this.getOfficeName());

		// Link into office
		ThreadDependencyMappingBuilder dependencies;
		switch (scope) {
		case PROCESS:
			dependencies = this.getOfficeBuilder().addProcessManagedObject("MO", "VALUE_MOS");
			this.getOfficeBuilder().addProcessManagedObject("DEPENDENCY", "DEPENDENCY_MOS");
			break;
		case THREAD:
			dependencies = this.getOfficeBuilder().addThreadManagedObject("MO", "VALUE_MOS");
			this.getOfficeBuilder().addThreadManagedObject("DEPENDENCY", "DEPENDENCY_MOS");
			break;
		default:
			throw new IllegalArgumentException("Invalid scope " + scope);
		}
		OptionalThreadLocal<String> threadLocal = dependencies.getOptionalThreadLocal();

		// Obtain the expected thread local value
		String expectedValue = isExpectAvailable ? value : null;

		// Construct the check on thread local
		ThreadLocalManagedObjectSource mos = new ThreadLocalManagedObjectSource(threadLocal, expectedValue);
		ManagedObjectBuilder<None> check = this.constructManagedObject("CHECK", mos, this.getOfficeName());
		check.setTimeout(10);

		// Construct the functions
		Work work = new Work(expectedValue, dependency);
		ReflectiveFunctionBuilder expectObject = this.constructFunction(work, "expectObject");
		expectObject.buildObject("MO");
		expectObject.setNextFunction("service");
		ReflectiveFunctionBuilder service = this.constructFunction(work, "service");
		service.buildObject("CHECK", scope).mapDependency(DependencyKeys.DEPENDENCY, "DEPENDENCY");

		// Invoke the function
		String methodName = isExpectAvailable ? "expectObject" : "service";
		this.invokeFunction(methodName, null);
		assertTrue("Should be serviced", work.isServiced);
	}

	public static class Work {

		private final String expectedValue;

		private final Dependency expectedDependency;

		private volatile boolean isServiced = false;

		public Work(String expectedValue, Dependency expectedDependency) {
			this.expectedValue = expectedValue;
			this.expectedDependency = expectedDependency;
		}

		public void expectObject(String value) {
			assertSame("Should have dependency object", this.expectedValue, value);
		}

		public void service(ThreadLocalManagedObjectSource mo) {
			assertTrue("Accessible via context aware", mo.isContextAware);
			assertTrue("Accessible via asynchronous", mo.isAsynchronousContext);
			assertTrue("Accessible via co-ordination", mo.isLoadObjects);
			assertSame("Incorrect dependency", this.expectedDependency, mo.dependency);
			assertTrue("Accessible via managed object", mo.isManagedObject);
			assertTrue("Accessible via object", mo.isObject);
			this.isServiced = true;
		}
	}

	public static enum DependencyKeys {
		DEPENDENCY
	}

	public static class Dependency {
	}

	@TestSource
	public static class ThreadLocalManagedObjectSource extends AbstractManagedObjectSource<DependencyKeys, None>
			implements ContextAwareManagedObject, AsynchronousManagedObject, CoordinatingManagedObject<DependencyKeys>,
			ManagedObject {

		private final OptionalThreadLocal<String> threadLocal;

		private final String expectedThreadLocalValue;

		private boolean isContextAware = false;

		private boolean isAsynchronousContext = false;

		private boolean isLoadObjects = false;

		private Dependency dependency = null;

		private boolean isManagedObject = false;

		private boolean isObject = false;

		private ThreadLocalManagedObjectSource(OptionalThreadLocal<String> threadLocal,
				String exepctedThreadLocalValue) {
			this.threadLocal = threadLocal;
			this.expectedThreadLocalValue = exepctedThreadLocalValue;
		}

		/*
		 * ================= ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			assertNull("Thread local should not be available for specification", this.threadLocal.get());
		}

		@Override
		protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.setManagedObjectClass(this.getClass());
			context.addDependency(DependencyKeys.DEPENDENCY, Dependency.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			assertSame("Incorrect getManagedObject", this.expectedThreadLocalValue, this.threadLocal.get());
			this.isManagedObject = true;
			return this;
		}

		/*
		 * ================ ContextAwareManagedObject ======================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			assertSame("Incorrect setManagedObjectContext", this.expectedThreadLocalValue, this.threadLocal.get());
			this.isContextAware = true;
		}

		/*
		 * ================ AsynchronousManagedObject =====================
		 */

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			assertSame("Incorrect setAsynchronousContext", this.expectedThreadLocalValue, this.threadLocal.get());
			this.isAsynchronousContext = true;
		}

		/*
		 * ================ CoordinatingManagedObject ======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
			assertSame("Incorrect loadObjects", this.expectedThreadLocalValue, this.threadLocal.get());
			this.dependency = (Dependency) registry.getObject(DependencyKeys.DEPENDENCY);
			this.isLoadObjects = true;
		}

		/*
		 * ===================== ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			assertSame("Incorrect getObject", this.expectedThreadLocalValue, this.threadLocal.get());
			this.isObject = true;
			return this;
		}
	}

}
