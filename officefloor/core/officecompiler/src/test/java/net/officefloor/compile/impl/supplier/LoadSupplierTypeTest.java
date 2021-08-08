/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompileContext;
import net.officefloor.compile.spi.supplier.source.SupplierCompletionContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link InitialSupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class LoadSupplierTypeTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link CompilerIssues}.
	 */
	private MockCompilerIssues issues;

	@BeforeEach
	protected void setUp() throws Exception {
		MockSupplierSource.reset();
		this.issues = new MockCompilerIssues(this.mocks);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link SupplierSource}.
	 */
	@Test
	public void failInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockSupplierSource.class.getName() + " by default constructor", failure);

		// Attempt to load
		MockSupplierSource.instantiateFailure = failure;
		this.loadInitialSupplierType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	@Test
	public void missingProperty() {

		// Record missing property
		this.issues.recordIssue("Missing property 'missing' for SupplierSource " + MockSupplierSource.class.getName());

		// Attempt to load
		this.loadInitialSupplierType(false, (context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	@Test
	public void getProperties() {

		// Attempt to load
		this.loadInitialSupplierType(true, (context) -> {
			assertEquals("DEFAULT", context.getProperty("missing", "DEFAULT"), "Ensure get defaulted property");
			assertEquals("1", context.getProperty("ONE"), "Ensure get property ONE");
			assertEquals("2", context.getProperty("TWO"), "Ensure get property TWO");
			Properties properties = context.getProperties();
			assertEquals(2, properties.size(), "Incorrect number of properties");
			assertEquals("1", properties.get("ONE"), "Incorrect property ONE");
			assertEquals("2", properties.get("TWO"), "Incorrect property TWO");
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure correctly named {@link Logger}.
	 */
	@Test
	public void logger() {
		Closure<String> closure = new Closure<>();
		this.loadInitialSupplierType(true, (context) -> {
			closure.value = context.getLogger().getName();
		});
		assertEquals(OfficeFloorCompiler.TYPE, closure.value, "Incorrect logger name");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	@Test
	public void missingClass() {

		// Record missing class
		this.issues
				.recordIssue("Can not load class 'missing' for SupplierSource " + MockSupplierSource.class.getName());

		// Attempt to load
		this.loadInitialSupplierType(false, (context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	@Test
	public void missingResource() {

		// Record missing resource
		this.issues.recordIssue("Can not obtain resource at location 'missing' for SupplierSource "
				+ MockSupplierSource.class.getName());

		// Attempt to load
		this.loadInitialSupplierType(false, (context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	@Test
	public void getResource() {

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/') + ".class";

		// Attempt to load
		this.loadInitialSupplierType(true, (context) -> {
			assertEquals(LoadSupplierTypeTest.class.getClassLoader().getResource(objectPath),
					context.getClassLoader().getResource(objectPath), "Incorrect resource locator");
		});
	}

	/**
	 * Ensure issue if fails to init the {@link SupplierSource}.
	 */
	@Test
	public void failInitSupplierSource() {

		final NullPointerException failure = new NullPointerException("Fail init SupplierSource");

		// Record failure to init the Supplier Source
		this.issues.recordIssue("Failed to source InitialSupplierType definition from SupplierSource "
				+ MockSupplierSource.class.getName(), failure);

		// Attempt to load
		this.loadInitialSupplierType(false, (context) -> {
			throw failure;
		});
	}

	/**
	 * Ensure issue if fails to complete the {@link SupplierSource}.
	 */
	@Test
	public void failCompleteSupplierSource() {

		final NullPointerException failure = new NullPointerException("Fail complete SupplierSource");

		// Record failure to init the Supplier Source
		this.issues.recordIssue("Failed to complete SupplierType", failure);

		// Test
		this.mocks.replayMockObjects();

		// Attempt to load
		SupplierLoader supplierLoader = this.getSupplierLoader();
		InitialSupplierType initialType = this.loadInitialSupplierType(supplierLoader, (context) -> {
			context.addCompileCompletion((completion) -> {
				throw failure;
			});
		});
		SupplierType supplierType = this.getSupplierLoader().loadSupplierType(initialType);
		assertNull(supplierType, "Should not load supplier type");

		// Verify
		this.mocks.verifyMockObjects();
	}

	/**
	 * <p>
	 * Ensure able to load with no {@link ManagedObject} instances.
	 * <p>
	 * No {@link ManagedObject} instances is allowed as may have
	 * {@link SupplierSource} turn off supplying {@link ManagedObject} instances.
	 * Allowing none, will prevent this case having to load an arbitrary
	 * {@link ManagedObject}.
	 */
	@Test
	public void noSuppliedManagedObjects() {
		this.loadSupplierType(null, (context) -> {
			// No supplied managed objects
		}, (type) -> {
			// Ensure no supplied managed object types
			assertEquals(0, type.getSuppliedManagedObjectTypes().length, "Supplier should not have managed objects");
		});
	}

	/**
	 * Ensure issue if no type for {@link ManagedObject}.
	 */
	@Test
	public void noType() {
		this.loadSupplierType(() -> {
			// Record must have type
			this.issues.recordIssue("Must provide type for ManagedObject 0");
		}, (context) -> {
			context.addManagedObjectSource("Qualifier", null, new ClassManagedObjectSource());
		}, null);
	}

	/**
	 * Ensure issue if null {@link ManagedObjectSource}.
	 */
	@Test
	public void nullManagedObjectSource() {
		this.loadSupplierType(() -> {
			// Record no null managed object source
			this.issues
					.recordIssue("Must provide a ManagedObjectSource for ManagedObject " + Connection.class.getName());
		}, (context) -> {
			context.addManagedObjectSource(null, Connection.class, null);
		}, null);
	}

	/**
	 * Ensure can load simple {@link ManagedObject}.
	 */
	@Test
	public void simpleManagedObject() {
		this.loadSupplierType(null, (context) -> {
			addClassManagedObjectSource(context, SimpleManagedObject.class);
		}, (type) -> {

			// Ensure only a single supplied managed object
			SuppliedManagedObjectSourceType[] moTypes = type.getSuppliedManagedObjectTypes();
			assertEquals(1, moTypes.length, "Expecting only one supplied managed object type");
			SuppliedManagedObjectSourceType moType = moTypes[0];

			// Validate the managed object type
			assertEquals(SimpleManagedObject.class, moType.getObjectType(), "Incorrect type");
			assertNull(moType.getQualifier(), "Should be no qualifier");
		});
	}

	/**
	 * Mock class for test by similar name.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Ensure can load complex {@link ManagedObject}.
	 */
	@Test
	public void complexManagedObject() {

		// Load
		InitialSupplierType type = this.loadInitialSupplierType(true, (context) -> {
			new MockLoadSupplierSource().supply(context);
		}, MockLoadSupplierSource.PROPERTY_TEST, MockLoadSupplierSource.PROPERTY_TEST);

		// Validate the supplier type
		MockLoadSupplierSource.assertSupplierType(type);
	}

	/**
	 * Ensure able to load multiple {@link ManagedObject} instances and order is
	 * maintained in {@link InitialSupplierType}.
	 */
	@Test
	public void multipleManagedObjects() {
		this.loadSupplierType(null, (context) -> {
			context.addManagedObjectSource(null, Object.class, new MockTypeManagedObjectSource(Object.class, null));
			context.addManagedObjectSource(null, Connection.class,
					new MockTypeManagedObjectSource(Connection.class, null));
			addClassManagedObjectSource(context, SimpleManagedObject.class);
			context.addManagedObjectSource("QUALIFIER", Object.class,
					new MockTypeManagedObjectSource(Object.class, null));
		}, (type) -> {
			// Validate the managed object types (and order)
			SuppliedManagedObjectSourceType[] types = type.getSuppliedManagedObjectTypes();
			assertEquals(4, types.length, "Incorrect number of managed objects");
			assertEquals(Object.class, types[0].getObjectType(), "Incorrect first managed object");
			assertEquals(Connection.class, types[1].getObjectType(), "Incorrect second managed object");
			assertEquals(SimpleManagedObject.class, types[2].getObjectType(), "Incorrect third managed object");
			assertEquals(Object.class, types[3].getObjectType(), "Incorrect fourth managed object");
			assertEquals("QUALIFIER", types[3].getQualifier(), "Incorrect fourth qualifier");
		});
	}

	/**
	 * Ensure issue if no object type for {@link SupplierThreadLocal}.
	 */
	@Test
	public void issueIfNoSupplierThreadLocalObjectType() {
		this.loadSupplierType(() -> {
			// Record no null object type
			this.issues.recordIssue("Must provide type for SupplierThreadLocal 0");
		}, (context) -> {
			context.addSupplierThreadLocal("qualifier", null);
		}, null);
	}

	/**
	 * Ensure can load {@link SupplierThreadLocal}.
	 */
	@Test
	public void supplierThreadLocal() {
		this.loadSupplierType(null, (context) -> {
			context.addSupplierThreadLocal("qualification", String.class);
			context.addSupplierThreadLocal(null, Connection.class);
		}, (type) -> {
			// Validate the supplier thread locals
			SupplierThreadLocalType[] types = type.getSupplierThreadLocalTypes();
			assertEquals(2, types.length, "Incorrect number of thread locals");
			assertEquals("qualification", types[0].getQualifier(), "Incorrect qualifier for first");
			assertEquals(String.class, types[0].getObjectType(), "Incorrect type for first");
			assertNull(types[1].getQualifier(), "Should be no qualifier for second");
			assertEquals(Connection.class, types[1].getObjectType(), "Incorrect type for second");
		});
	}

	/**
	 * Ensure issue if no {@link InternalSupplier}.
	 */
	@Test
	public void issueIfNoInternalSupplier() {
		this.loadSupplierType(() -> {
			// Record no internal supplier
			this.issues.recordIssue("Must provide InternalSupplier for added instance 0");
		}, (context) -> {
			context.addInternalSupplier(null);
		}, null);
	}

	/**
	 * Ensure can load {@link InternalSupplier}.
	 */
	@Test
	public void internalSupplier() {
		InternalSupplier internalSupplier = this.mocks.createMock(InternalSupplier.class);
		this.loadSupplierType(null, (context) -> {
			context.addInternalSupplier(internalSupplier);
		}, (type) -> {
			// Validate the internal supplier
			InternalSupplier[] internalSuppliers = type.getInternalSuppliers();
			assertEquals(1, internalSuppliers.length, "Incorrect number of internal suppliers");
			assertSame(internalSupplier, internalSuppliers[0], "Incorrect internal supplier");
		});
	}

	/**
	 * Ensure issue if no {@link ThreadSynchroniserFactory} provided.
	 */
	@Test
	public void issueIfNoSupplierThreadSynchroniserFactory() {
		this.loadSupplierType(() -> {
			// Return no thread synchroniser factory
			this.issues.recordIssue("Must provide ThreadSynchroniserFactory for added instance 0");
		}, (context) -> {
			context.addThreadSynchroniser(null);
		}, null);
	}

	/**
	 * Ensure can load {@link ThreadSynchroniserFactory}.
	 */
	@Test
	public void threadSynchroniser() {
		ThreadSynchroniserFactory threadSynchroniserOne = this.mocks.createMock(ThreadSynchroniserFactory.class);
		ThreadSynchroniserFactory threadSynchroniserTwo = this.mocks.createMock(ThreadSynchroniserFactory.class);
		this.loadSupplierType(null, (context) -> {
			context.addThreadSynchroniser(threadSynchroniserOne);
			context.addThreadSynchroniser(threadSynchroniserTwo);
		}, (type) -> {
			// Validate the thread synchroniser
			ThreadSynchroniserFactory[] threadSynchronisers = type.getThreadSynchronisers();
			assertEquals(2, threadSynchronisers.length, "Incorrect number of thread synchronisers");
			assertSame(threadSynchroniserOne, threadSynchronisers[0], "Incorrect first thread synchroniser");
			assertSame(threadSynchroniserTwo, threadSynchronisers[1], "Incorrect second thread synchroniser");
		});
	}

	/**
	 * Ensure issue if no {@link SupplierCompileCompletion} provided.
	 */
	@Test
	public void issueIfNoSupplierCompileCompletion() {

		// No compile completion
		this.issues.recordIssue("Must provide SupplierCompileCompletion for added instance 0");

		// Attempt to load supplier type
		this.loadInitialSupplierType(false, (context) -> {
			context.addCompileCompletion(null);
		});
	}

	/**
	 * Ensure can load {@link SupplierCompileCompletion}.
	 */
	@Test
	public void compileCompletion() {

		// Load the supplier type
		SupplierCompileCompletion completionOne = this.mocks.createMock(SupplierCompileCompletion.class);
		SupplierCompileCompletion completionTwo = this.mocks.createMock(SupplierCompileCompletion.class);
		InitialSupplierType type = this.loadInitialSupplierType(true, (context) -> {
			context.addCompileCompletion(completionOne);
			context.addCompileCompletion(completionTwo);
		});

		// Validate the compile completions
		SupplierCompileCompletion[] compileCompletions = type.getCompileCompletions();
		assertEquals(2, compileCompletions.length, "Incorrect number of compile completions");
		assertSame(completionOne, compileCompletions[0], "Incorrect first compile completion");
		assertSame(completionTwo, compileCompletions[1], "Incorrect second compile completion");
	}

	/**
	 * Ensure can add via {@link SupplierSourceContext} and
	 * {@link SupplierCompileCompletion}.
	 */
	@Test
	public void addInContextAndCompletion() {

		ThreadSynchroniserFactory threadSynchroniserOne = this.mocks.createMock(ThreadSynchroniserFactory.class);
		ThreadSynchroniserFactory threadSynchroniserTwo = this.mocks.createMock(ThreadSynchroniserFactory.class);

		// Replay mock objects
		this.mocks.replayMockObjects();

		// Obtains the supplier loader
		SupplierLoader supplierLoader = this.getSupplierLoader();

		// Load the initial supplier type
		InitialSupplierType initialSupplierType = this.loadInitialSupplierType(supplierLoader, (context) -> {
			context.addSupplierThreadLocal(null, String.class);
			context.addThreadSynchroniser(threadSynchroniserOne);
			context.addManagedObjectSource(null, Object.class, new MockTypeManagedObjectSource(Object.class, null));

			context.addCompileCompletion((completion) -> {
				completion.addSupplierThreadLocal(null, Integer.class);
				completion.addThreadSynchroniser(threadSynchroniserTwo);
				completion.addManagedObjectSource(null, Connection.class,
						new MockTypeManagedObjectSource(Connection.class, null));
			});
		});

		// Load the supplier type
		SupplierType supplierType = supplierLoader.loadSupplierType(initialSupplierType);

		// Verify the mock objects
		this.mocks.verifyMockObjects();

		// Ensure includes both context and completion items
		assertEquals(2, supplierType.getSupplierThreadLocalTypes().length, "Incorrect number of thread local types");
		assertEquals(2, supplierType.getThreadSynchronisers().length, "Incorrect number of thread synchronisers");
		assertEquals(2, supplierType.getSuppliedManagedObjectTypes().length, "Incorrect number of managed objects");
	}

	/**
	 * Loads the {@link InitialSupplierType}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link InitialSupplierType}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link InitialSupplierType}.
	 */
	private InitialSupplierType loadInitialSupplierType(boolean isExpectedToLoad, Init init,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.mocks.replayMockObjects();

		// Load the initial supplier type
		InitialSupplierType supplierType = this.loadInitialSupplierType(this.getSupplierLoader(), init,
				propertyNameValuePairs);

		// Verify the mock objects
		this.mocks.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull(supplierType, "Expected to load the supplier type");
		} else {
			assertNull(supplierType, "Should not load the supplier type");
		}

		// Return the supplier type
		return supplierType;
	}

	/**
	 * Loads the {@link SupplierType}.
	 * 
	 * @param setup                  Sets up testing.
	 * @param completion             {@link SupplierCompileCompletion}.
	 * @param validate               {@link Validate}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link SupplierType}.
	 */
	private SupplierType loadSupplierType(Runnable setup, SupplierCompileCompletion completion, Validate validate,
			String... propertyNameValuePairs) {

		// Setup twice (for initial and completion)
		if (setup != null) {
			setup.run();
			setup.run();
		}

		// Replay mock objects
		this.mocks.replayMockObjects();

		// Obtains the supplier loader
		SupplierLoader supplierLoader = this.getSupplierLoader();

		// Load the initial supplier type
		InitialSupplierType initialSupplierType = this.loadInitialSupplierType(supplierLoader, (context) -> {
			SupplierCompletionContext completionContext = SupplierLoaderUtil.getSupplierCompletionContext(context);
			completion.complete(completionContext);
		}, propertyNameValuePairs);

		// Ensure if should be loaded
		if (validate != null) {
			validate.validate(initialSupplierType);
		} else {
			assertNull(initialSupplierType, "Should not load the initial supplier type");
		}

		// Load the supplier type
		initialSupplierType = this.loadInitialSupplierType(supplierLoader, (context) -> {
			// Register the completion
			context.addCompileCompletion(completion);
		}, propertyNameValuePairs);
		assertNotNull(initialSupplierType, "Should load initial supplier type");
		SupplierType supplierType = supplierLoader.loadSupplierType(initialSupplierType);

		// Ensure if should be loaded
		if (validate != null) {
			validate.validate(supplierType);
		} else {
			assertNull(supplierType, "Should not load the supplier type");
		}

		// Verify the mock objects
		this.mocks.verifyMockObjects();

		// Return the supplier type
		return supplierType;
	}

	/**
	 * Loads the {@link InitialSupplierType}.
	 * 
	 * @param supplierLoader         {@link SupplierLoader}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link InitialSupplierType}.
	 */
	private InitialSupplierType loadInitialSupplierType(SupplierLoader supplierLoader, Init init,
			String... propertyNameValuePairs) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the managed object loader and load the managed object type
		MockSupplierSource.init = init;
		InitialSupplierType supplierType = supplierLoader.loadInitialSupplierType(MockSupplierSource.class,
				propertyList);

		// Return the supplier type
		return supplierType;
	}

	/**
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @return {@link SupplierLoader}.
	 */
	private SupplierLoader getSupplierLoader() {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		return compiler.getSupplierLoader();
	}

	/**
	 * Implement to initialise the {@link MockSupplierSource}.
	 */
	@FunctionalInterface
	private static interface Init {

		/**
		 * Implemented to init the {@link SupplierSource}.
		 * 
		 * @param context {@link SupplierSourceContext}.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Implement to validate the {@link SupplierType}.
	 */
	@FunctionalInterface
	private static interface Validate {

		/**
		 * Validates the {@link SupplierType}.
		 * 
		 * @param type {@link SupplierType}.
		 */
		void validate(SupplierType type);
	}

	/**
	 * Convenience method to add a {@link ClassManagedObjectSource}.
	 * 
	 * @param context {@link SupplierSourceContext}.
	 * @param clazz   Class for the {@link ClassManagedObjectSource}.
	 * @return {@link SuppliedManagedObjectSource}.
	 */
	private static SuppliedManagedObjectSource addClassManagedObjectSource(SupplierCompileContext context,
			Class<?> clazz) {

		// Configure in the managed object source
		SuppliedManagedObjectSource managedObjectSource = context.addManagedObjectSource(null, clazz,
				new ClassManagedObjectSource());
		managedObjectSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());

		// Return the managed object source
		return managedObjectSource;
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	@TestSource
	public static class MockSupplierSource implements SupplierSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link SupplierSource}.
		 */
		public static Init init = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			instantiateFailure = null;
			init = null;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockSupplierSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== SupplierSource ===========================
		 */

		@Override
		public SupplierSourceSpecification getSpecification() {
			return fail("Should not obtain specification");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Run the init if available
			if (init != null) {
				init.supply(context);
			}
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

}
