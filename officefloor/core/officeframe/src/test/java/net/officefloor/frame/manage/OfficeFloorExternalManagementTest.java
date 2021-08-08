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

package net.officefloor.frame.manage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.frame.test.ThreadedTestSupport;

/**
 * Ensures the external management API of {@link OfficeFloor} responds
 * correctly.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class OfficeFloorExternalManagementTest {

	/**
	 * {@link ConstructTestSupport}.
	 */
	private final ConstructTestSupport construct = new ConstructTestSupport();

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mock = new MockTestSupport();

	/**
	 * {@link ThreadedTestSupport}.
	 */
	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * {@link Office} name.
	 */
	private String officeName;

	/**
	 * {@link LifecycleCheckManagedObjectSource}.
	 */
	private LifecycleCheckManagedObjectSource objectLifecycleCheck;

	/**
	 * {@link AsyncLoadManagedObjectSource}.
	 */
	private AsyncLoadManagedObjectSource asyncLoadObject;

	/**
	 * {@link MockObject} instance.
	 */
	private MockObject mockObject;

	/**
	 * {@link MockWork} instance.
	 */
	private MockWork mockWork;

	/**
	 * Annotation.
	 */
	private final Object annotation = "Annotation";

	/**
	 * {@link OfficeFloor} to test management.
	 */
	private OfficeFloor officeFloor;

	@BeforeEach
	protected void setUp() throws Exception {

		// Obtain the Office name
		this.officeName = this.construct.getOfficeName();

		// Construct the object
		this.mockObject = new MockObject();
		this.construct.constructManagedObject(this.mockObject, "mockObject", this.officeName);

		// Construct the lifecycle object
		this.objectLifecycleCheck = new LifecycleCheckManagedObjectSource();
		this.construct.constructManagedObject("checkLifecyle", this.objectLifecycleCheck, this.officeName);
		this.construct.getOfficeBuilder().addThreadManagedObject("checkLifecycle", "checkLifecyle");

		// Construct the async object
		this.asyncLoadObject = new AsyncLoadManagedObjectSource();
		this.construct.constructManagedObject("asyncLoadObject", this.asyncLoadObject, this.officeName);
		this.construct.getOfficeBuilder().addThreadManagedObject("asyncLoadObject", "asyncLoadObject");

		// Construct function
		this.mockWork = new MockWork();
		ReflectiveFunctionBuilder initialTask = this.construct.constructFunction(this.mockWork, "initialTask");
		initialTask.buildParameter();
		initialTask.getBuilder().addAnnotation(this.annotation);

		// Construct function
		ReflectiveFunctionBuilder anotherTask = this.construct.constructFunction(this.mockWork, "anotherTask");
		anotherTask.buildParameter();
		anotherTask.buildObject("mockObject", ManagedObjectScope.THREAD);

		// Compile OfficeFloor
		this.officeFloor = this.construct.constructOfficeFloor();

		// Ensure open to allow obtaining meta-data
		this.officeFloor.openOfficeFloor();
	}

	@AfterEach
	protected void cleanUp() throws Exception {
		this.officeFloor.close();
	}

	/**
	 * Ensure that not able to open {@link OfficeFloor} instance twice.
	 */
	@Test
	public void testOpenOfficeFloorOnce() throws Exception {
		try {
			this.officeFloor.openOfficeFloor();
			fail("Should not be able open twice");
		} catch (IllegalStateException ex) {
			assertEquals(ex.getMessage(), "OfficeFloor is already open", "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link Office} listing.
	 */
	@Test
	public void testOfficeListing() throws Exception {

		// Ensure correct office listing
		String[] officeNames = this.officeFloor.getOfficeNames();
		this.assertNames(officeNames, this.officeName);

		// Ensure able to obtain Office
		Office office = this.officeFloor.getOffice(this.officeName);
		assertNotNull(office, "Must have Office");
	}

	/**
	 * Ensure throws exception on unknown {@link Office} requested.
	 */
	@Test
	public void testUnknownOffice() {
		final String unknownOfficeName = "Unknown Office - adding name to ensure different " + this.officeName;
		try {
			this.officeFloor.getOffice(unknownOfficeName);
			fail("Should not be able to obtain unknown Office");
		} catch (UnknownOfficeException ex) {
			assertEquals(unknownOfficeName, ex.getUnknownOfficeName(), "Incorrect office");
			assertEquals("Unknown Office '" + unknownOfficeName + "'", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} listing.
	 */
	@Test
	public void testFunctionListing() throws Exception {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Ensure correct function list
		String[] functionNames = office.getFunctionNames();
		this.assertNames(functionNames, "initialTask", "anotherTask", "of-checkLifecyle.#recycle#");

		// Ensure able to obtain FunctionManager
		FunctionManager function = office.getFunctionManager("anotherTask");
		assertNotNull(function, "Must have FunctionManager");
	}

	/**
	 * Ensures throws exception on unknown {@link FunctionManager}.
	 */
	@Test
	public void testUnknownFunction() throws UnknownOfficeException {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		final String unknownFunctionName = "Unknown Function";
		try {
			office.getFunctionManager(unknownFunctionName);
			fail("Should not be able to obtain unknown FunctionManager");
		} catch (UnknownFunctionException ex) {
			assertEquals(unknownFunctionName, ex.getUnknownFunctionName(), "Incorrect function");
			assertEquals("Unknown Function '" + unknownFunctionName + "'", ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} annotation.
	 */
	@Test
	public void testFunctionAnnotation() throws Exception {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct annotation
		assertEquals(this.annotation, function.getAnnotations()[0], "Incorrect annotation");
	}

	/**
	 * Ensure able to obtain {@link ManagedFunction} parameter type.
	 */
	@Test
	public void testFunctionParameterType() throws Exception {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure correct parameter type
		assertEquals(Connection.class, function.getParameterType(), "Incorrect parameter type");
	}

	/**
	 * Ensure able to invoke the {@link ManagedFunction}.
	 */
	@Test
	public void testInvokeFunction() throws Exception {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Invoke the Function
		Connection connection = this.mock.createMock(Connection.class);
		function.invokeProcess(connection, null);

		// Ensure function invoked with correct parameter
		assertSame(connection, this.mockWork.parameter, "Should be invoked with parameter");
	}

	/**
	 * Ensure can invoke function with {@link ManagedObject} dependency.
	 */
	@Test
	public void testInvokeFunctionWithDependency() throws Exception {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("anotherTask");

		// Invoke the Function
		Integer parameter = 1;
		function.invokeProcess(parameter, null);

		// Ensure function invoked with dependency
		assertSame(parameter, this.mockWork.parameter, "Should be invoked with parameter");
		assertEquals(this.mockObject, this.mockWork.object, "Incorrect injected object");
	}

	/**
	 * Ensure indicates if invalid parameter type.
	 */
	@Test
	public void testFailInvokeFunctionAsInvalidParameterType() throws Exception {

		// Obtain the Function Manager
		FunctionManager function = this.officeFloor.getOffice(this.officeName).getFunctionManager("initialTask");

		// Ensure fail on invalid parameter
		try {
			function.invokeProcess("Invalid parameter type", null);
			fail("Should not be successful on invalid parameter type");
		} catch (InvalidParameterTypeException ex) {
			assertEquals("Invalid parameter type (input=" + String.class.getName() + ", required="
					+ Connection.class.getName() + ")", ex.getMessage(), "Incorrect failure");
		}
	}

	/**
	 * Ensure able to obtain object.
	 */
	@Test
	public void obtainObject() throws Throwable {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		try (StateManager state = office.createStateManager()) {

			// Ensure able to obtain object (should load synchronously)
			MockObject object = state.getObject("mockObject", 0);
			assertSame(this.mockObject, object, "Should obtain dependency");
		}
	}

	/**
	 * Ensure able to obtain asynchronously loaded object.
	 */
	@Test
	public void loadAsyncObject() throws Throwable {

		// Obtian the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		try (StateManager state = office.createStateManager()) {

			// Trigger loading the object
			Closure<Object> capture = new Closure<>();
			state.load("asyncLoadObject", (object, failure) -> capture.value = object);

			// Should not have object
			assertNull(capture.value, "Object should not yet be loaded");

			// Load the object
			final Object object = new Object();
			this.asyncLoadObject.managedObjectUser.setManagedObject(() -> object);

			// Should now have object loaded
			assertSame(object, capture.value, "Should have object loaded");
		}
	}

	/**
	 * Ensure able to get an asynchronously loaded object synchronously.
	 */
	@Test
	public void getAsyncObject() throws Throwable {

		// Obtian the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		try (StateManager state = office.createStateManager()) {

			// Run obtain in another thread
			ThreadSafeClosure<Object> capture = new ThreadSafeClosure<>();
			new Thread(() -> {
				try {
					capture.set(state.getObject("asyncLoadObject", 3000));
				} catch (Throwable ex) {
					capture.set(ex);
				}
			}).start();

			// Wait for managed object user
			this.threading.waitForTrue(() -> this.asyncLoadObject.managedObjectUser != null);

			// Load the object
			final Object object = new Object();
			this.asyncLoadObject.managedObjectUser.setManagedObject(() -> object);

			// Wait for the object
			Object loadedObject = capture.waitAndGet();
			assertSame(object, loadedObject, "Should have object");
		}
	}

	/**
	 * Ensure able to handle load failure.
	 */
	@Test
	public void handleLoadFailure() throws Throwable {

		// Obtian the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		try (StateManager state = office.createStateManager()) {

			// Trigger loading the object
			Closure<Throwable> capture = new Closure<>();
			state.load("asyncLoadObject", (object, failure) -> capture.value = failure);

			// Load the failure
			final SQLException failure = new SQLException();
			this.asyncLoadObject.managedObjectUser.setFailure(failure);

			// Should have failure
			assertSame(failure, capture.value, "Should have failure");
		}
	}

	/**
	 * Ensure able to handle an asynchronously load failure.
	 */
	@Test
	public void getAsyncFailure() throws Throwable {

		// Obtian the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		try (StateManager state = office.createStateManager()) {

			// Run obtain in another thread
			ThreadSafeClosure<Throwable> capture = new ThreadSafeClosure<>();
			new Thread(() -> {
				try {
					state.getObject("asyncLoadObject", 3000);
					fail("Should not successfully load object");
				} catch (Throwable ex) {
					capture.set(ex);
				}
			}).start();

			// Wait for managed object user
			this.threading.waitForTrue(() -> this.asyncLoadObject.managedObjectUser != null);

			// Load the failure
			final SQLException failure = new SQLException();
			this.asyncLoadObject.managedObjectUser.setFailure(failure);

			// Wait for the failure
			Object loadedFailure = capture.waitAndGet();
			assertSame(failure, loadedFailure, "Should throw the failure");
		}
	}

	/**
	 * Ensure able to obtain {@link ManagedObject} listing.
	 */
	@Test
	public void ensureObjectListing() throws Exception {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Ensure correct object list
		String[] objectNames = office.getObjectNames();
		this.assertNames(objectNames, "asyncLoadObject", "checkLifecycle", "mockObject");
	}

	@Test
	public void ensureManageObjectLifecycle() throws Throwable {

		// Obtain the Office
		Office office = this.officeFloor.getOffice(this.officeName);

		// Obtain the state manager
		StateManager state = office.createStateManager();

		// Obtain the object
		LifecycleCheckManagedObjectSource object = state.getObject("checkLifecycle", 0);
		assertSame(this.objectLifecycleCheck, object, "Should object dependency");

		// Ensure object not recycled
		assertFalse(object.isRecycled, "Should still be active");

		// Close state manager
		state.close();

		// Ensure object recycled
		assertTrue(object.isRecycled, "Should clean up object");
	}

	/**
	 * Asserts the list of names match.
	 * 
	 * @param actual   Actual names.
	 * @param expected Expected names.
	 */
	private void assertNames(String[] actual, String... expected) {

		// Ensure same length
		assertEquals(expected.length, actual.length, "Incorrect number of names");

		// Sort
		Arrays.sort(expected);
		Arrays.sort(actual);

		// Ensure values the same
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i], "Incorrect name " + i);
		}
	}

	/**
	 * Mock functionality.
	 */
	public class MockWork {

		/**
		 * Capture of the parameter.
		 */
		public Object parameter = null;

		/**
		 * {@link MockObject} that is injected.
		 */
		public MockObject object = null;

		/**
		 * Initial {@link ManagedFunction}.
		 * 
		 * @param parameter Parameter with distinct type for identifying in testing.
		 */
		public void initialTask(Connection parameter) {
			this.parameter = parameter;
		}

		/**
		 * Another task to possibly execute.
		 * 
		 * @param parameter Parameter with distinct type for identifying in testing.
		 * @param object    {@link MockObject} to be injected.
		 */
		public void anotherTask(Integer parameter, MockObject object) {
			this.parameter = parameter;
			this.object = object;
		}
	}

	/**
	 * Mock object.
	 */
	public class MockObject {
	}

	/**
	 * {@link ManagedObjectSource} to test lifecycle management.
	 */
	@TestSource
	public static class LifecycleCheckManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		/**
		 * Indicates if cleaned up {@link ManagedObject}.
		 */
		private boolean isRecycled = false;

		/*
		 * ==================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
			context.getManagedObjectSourceContext().getRecycleFunction(() -> (moContext) -> {
				this.isRecycled = true;
			});
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================== ManagedObject ============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/**
	 * {@link ManagedObjectSource} to test asynchronous loading.
	 */
	@TestSource
	public static class AsyncLoadManagedObjectSource extends AbstractAsyncManagedObjectSource<None, None> {

		/**
		 * {@link ManagedObjectUser}.
		 */
		private volatile ManagedObjectUser managedObjectUser;

		/*
		 * ===================== ManagedObjectSource ========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			this.managedObjectUser = user;
		}
	}

}
