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

package net.officefloor.compile.impl.supplier;

import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests loading the {@link SupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadSupplierTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	@Override
	protected void setUp() throws Exception {
		MockSupplierSource.reset();
	}

	/**
	 * Ensure issue if fail to instantiate the {@link SupplierSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockSupplierSource.class.getName() + " by default constructor", failure);

		// Attempt to load
		MockSupplierSource.instantiateFailure = failure;
		this.loadSupplierType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Missing property 'missing' for SupplierSource " + MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			context.getProperty("missing");
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Attempt to load
		this.loadSupplierType(true, (context) -> {
			assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
			assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
			assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
			Properties properties = context.getProperties();
			assertEquals("Incorrect number of properties", 2, properties.size());
			assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
			assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure correctly named {@link Logger}.
	 */
	public void testLogger() {
		Closure<String> closure = new Closure<>();
		this.loadSupplierType(true, (context) -> {
			closure.value = context.getLogger().getName();
		});
		assertEquals("Incorrect logger name", OfficeFloorCompiler.TYPE, closure.value);
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues
				.recordIssue("Can not load class 'missing' for SupplierSource " + MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			context.loadClass("missing");
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing resource
		this.issues.recordIssue("Can not obtain resource at location 'missing' for SupplierSource "
				+ MockSupplierSource.class.getName());

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			context.getResource("missing");
		});
	}

	/**
	 * Ensure able to get resource.
	 */
	public void testGetResource() {

		// Obtain path
		final String objectPath = Object.class.getName().replace('.', '/') + ".class";

		// Attempt to load
		this.loadSupplierType(true, (context) -> {
			assertEquals("Incorrect resource locator",
					LoadSupplierTypeTest.class.getClassLoader().getResource(objectPath),
					context.getClassLoader().getResource(objectPath));
		});
	}

	/**
	 * Ensure issue if fails to init the {@link SupplierSource}.
	 */
	public void testFailInitSupplierSource() {

		final NullPointerException failure = new NullPointerException("Fail init SupplierSource");

		// Record failure to init the Supplier Source
		this.issues.recordIssue(
				"Failed to source SupplierType definition from SupplierSource " + MockSupplierSource.class.getName(),
				failure);

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			throw failure;
		});
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
	public void testNoSuppliedManagedObjects() {

		// Attempt to load
		this.loadEmptySupplierType((context) -> {
			// No supplied managed objects
		});
	}

	/**
	 * Ensure issue if no type for {@link ManagedObject}.
	 */
	public void testNoType() {

		// Record no auto-wiring
		this.issues.recordIssue("Must provide type for ManagedObject 0");

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			context.addManagedObjectSource("Qualifier", null, new ClassManagedObjectSource());
		});
	}

	/**
	 * Ensure issue if null {@link ManagedObjectSource}.
	 */
	public void testNullManagedObjectSource() {

		// Record no null managed object source
		this.issues.recordIssue("Must provide a ManagedObjectSource for ManagedObject " + Connection.class.getName());

		// Attempt to load
		this.loadSupplierType(false, (context) -> {
			context.addManagedObjectSource(null, Connection.class, null);
		});
	}

	/**
	 * Ensure can load simple {@link ManagedObject}.
	 */
	public void testSimpleManagedObject() {

		// Load
		SuppliedManagedObjectSourceType type = this.loadSuppliedManagedObjectType((context) -> {
			addClassManagedObjectSource(context, SimpleManagedObject.class);
		});

		// Validate the managed object type
		assertEquals("Incorrect type", SimpleManagedObject.class, type.getObjectType());
		assertNull("Should be no qualifier", type.getQualifier());
	}

	/**
	 * Mock class for test by similar name.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Ensure can load complex {@link ManagedObject}.
	 */
	public void testComplexManagedObject() {

		// Load
		SupplierType type = this.loadSupplierType(true, (context) -> {
			new MockLoadSupplierSource().supply(context);
		}, MockLoadSupplierSource.PROPERTY_TEST, MockLoadSupplierSource.PROPERTY_TEST);

		// Validate the supplier type
		MockLoadSupplierSource.assertSupplierType(type);
	}

	/**
	 * Ensure able to load multiple {@link ManagedObject} instances and order is
	 * maintained in {@link SupplierType}.
	 */
	public void testMultipleManagedObjects() {

		// Load the managed objects
		SupplierType type = this.loadSupplierType(true, (context) -> {
			context.addManagedObjectSource(null, Object.class,
					new MockTypeManagedObjectSource(Object.class, "TODO LOGGER NAME"));
			context.addManagedObjectSource(null, Connection.class,
					new MockTypeManagedObjectSource(Connection.class, "TODO LOGGER NAME"));
			addClassManagedObjectSource(context, SimpleManagedObject.class);
			context.addManagedObjectSource("QUALIFIER", Object.class,
					new MockTypeManagedObjectSource(Object.class, "TODO LOGGER NAME"));
		});

		// Validate the managed object types (and order)
		SuppliedManagedObjectSourceType[] types = type.getSuppliedManagedObjectTypes();
		assertEquals("Incorrect number of managed objects", 4, types.length);
		assertEquals("Incorrect first managed object", Object.class, types[0].getObjectType());
		assertEquals("Incorrect second managed object", Connection.class, types[1].getObjectType());
		assertEquals("Incorrect third managed object", SimpleManagedObject.class, types[2].getObjectType());
		assertEquals("Incorrect fourth managed object", Object.class, types[3].getObjectType());
		assertEquals("Incorrect fourth qualifier", "QUALIFIER", types[3].getQualifier());
	}

	/**
	 * Ensure issue if no object type for {@link SupplierThreadLocal}.
	 */
	public void testIssueIfNoSupplierThreadLocalObjectType() {

		// Record no null object type
		this.issues.recordIssue("Must provide type for SupplierThreadLocal 0");

		// Attempt to load supplier type
		this.loadSupplierType(false, (context) -> {
			context.addSupplierThreadLocal("qualifier", null);
		});
	}

	/**
	 * Ensure can load {@link SupplierThreadLocal}.
	 */
	public void testSupplierThreadLocal() {

		// Load the supplier type
		SupplierType type = this.loadSupplierType(true, (context) -> {
			context.addSupplierThreadLocal("qualification", String.class);
			context.addSupplierThreadLocal(null, Connection.class);
		});

		// Validate the supplier thread locals
		SupplierThreadLocalType[] types = type.getSupplierThreadLocalTypes();
		assertEquals("Incorrect number of thread locals", 2, types.length);
		assertEquals("Incorrect qualifier for first", "qualification", types[0].getQualifier());
		assertEquals("Incorrect type for first", String.class, types[0].getObjectType());
		assertNull("Should be no qualifier for second", types[1].getQualifier());
		assertEquals("Incorrect type for second", Connection.class, types[1].getObjectType());
	}

	/**
	 * Ensure issue if no {@link ThreadSynchroniserFactory} provided.
	 */
	public void testIssueIfNoSupplierThreadSynchroniserFactory() {

		// Return no thread synchroniser factory
		this.issues.recordIssue("Must provide ThreadSynchroniserFactory for added instance 0");

		// Attempt to load supplier type
		this.loadSupplierType(false, (context) -> {
			context.addThreadSynchroniser(null);
		});
	}

	/**
	 * Ensure can load {@link ThreadSynchroniserFactory}.
	 */
	public void testThreadSynchroniser() {

		// Load the supplier type
		ThreadSynchroniserFactory threadSynchroniserOne = this.createMock(ThreadSynchroniserFactory.class);
		ThreadSynchroniserFactory threadSynchroniserTwo = this.createMock(ThreadSynchroniserFactory.class);
		SupplierType type = this.loadSupplierType(true, (context) -> {
			context.addThreadSynchroniser(threadSynchroniserOne);
			context.addThreadSynchroniser(threadSynchroniserTwo);
		});

		// Validate the thread synchroniser
		ThreadSynchroniserFactory[] threadSynchronisers = type.getThreadSynchronisers();
		assertEquals("Incorrect number of thread synchronisers", 2, threadSynchronisers.length);
		assertSame("Incorrect first thread synchroniser", threadSynchroniserOne, threadSynchronisers[0]);
		assertSame("Incorrect second thread synchroniser", threadSynchroniserTwo, threadSynchronisers[1]);
	}

	/**
	 * Ensure can load {@link SupplierCompileCompletion}.
	 */
	public void testCompileCompletion() {

		// Load the supplier type
		SupplierCompileCompletion completionOne = this.createMock(SupplierCompileCompletion.class);
		SupplierCompileCompletion completionTwo = this.createMock(SupplierCompileCompletion.class);
		SupplierType type = this.loadSupplierType(true, (context) -> {
			context.addCompileCompletion(completionOne);
			context.addCompileCompletion(completionTwo);
		});

		// Validate the compile completions
		SupplierCompileCompletion[] compileCompletions = type.getCompileCompletions();
		assertEquals("Incorrect number of compile completions", 2, compileCompletions.length);
		assertSame("Incorrect first compile completion", completionOne, compileCompletions[0]);
		assertSame("Incorrect second compile completion", completionTwo, compileCompletions[1]);
	}

	/**
	 * Convenience method to load a {@link SupplierSource} with only one
	 * {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Single {@link SuppliedManagedObjectSourceType}.
	 */
	private SuppliedManagedObjectSourceType loadSuppliedManagedObjectType(Init init, String... propertyNameValuePairs) {

		// Load the supplier type
		SupplierType type = this.loadSupplierType(true, init, propertyNameValuePairs);

		// Ensure only a single supplied managed object
		SuppliedManagedObjectSourceType[] moTypes = type.getSuppliedManagedObjectTypes();
		assertEquals("Expecting only one supplied managed object type", 1, moTypes.length);

		// Return the single supplied managed object type
		return moTypes[0];
	}

	/**
	 * Ensure loads an empty {@link SupplierType}. In other words, a
	 * {@link SupplierType} with no {@link SuppliedManagedObjectSourceType}
	 * instances.
	 * 
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 */
	private void loadEmptySupplierType(Init init, String... propertyNameValuePairs) {

		// Load the supplier type
		SupplierType type = this.loadSupplierType(true, init, propertyNameValuePairs);

		// Ensure no supplied managed object types
		assertEquals("Supplier should not have managed objects", 0, type.getSuppliedManagedObjectTypes().length);
	}

	/**
	 * Loads the {@link SupplierType}.
	 * 
	 * @param isExpectedToLoad       Flag indicating if expecting to load the
	 *                               {@link SupplierType}.
	 * @param init                   {@link Init}.
	 * @param propertyNameValuePairs {@link Property} name value pairs.
	 * @return Loaded {@link SupplierType}.
	 */
	private SupplierType loadSupplierType(boolean isExpectedToLoad, Init init, String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the managed object loader and load the managed object type
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		SupplierLoader supplierLoader = compiler.getSupplierLoader();
		MockSupplierSource.init = init;
		SupplierType supplierType = supplierLoader.loadSupplierType(MockSupplierSource.class, propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the supplier type", supplierType);
		} else {
			assertNull("Should not load the supplier type", supplierType);
		}

		// Return the supplier type
		return supplierType;
	}

	/**
	 * Implement to initialise the {@link MockSupplierSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link SupplierSource}.
		 * 
		 * @param context {@link SupplierSourceContext}.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Convenience method to add a {@link ClassManagedObjectSource}.
	 * 
	 * @param context {@link SupplierSourceContext}.
	 * @param clazz   Class for the {@link ClassManagedObjectSource}.
	 * @return {@link SuppliedManagedObjectSource}.
	 */
	private static SuppliedManagedObjectSource addClassManagedObjectSource(SupplierSourceContext context,
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
			fail("Should not obtain specification");
			return null;
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
