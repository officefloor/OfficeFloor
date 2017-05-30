/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.pool;

import java.sql.Connection;
import java.util.Properties;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceMetaData;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolFactory;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests loading the {@link ManagedObjectPoolType}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectPoolTypeTest extends OfficeFrameTestCase {

	/**
	 * {@link CompilerIssues}.
	 */
	private final MockCompilerIssues issues = new MockCompilerIssues(this);

	/**
	 * {@link ManagedObjectPoolSourceMetaData}.
	 */
	private final ManagedObjectPoolSourceMetaData metaData = this.createMock(ManagedObjectPoolSourceMetaData.class);

	/**
	 * {@link ManagedObjectPoolFactory}.
	 */
	private final ManagedObjectPoolFactory factory = this.createMock(ManagedObjectPoolFactory.class);

	@Override
	protected void setUp() throws Exception {
		MockManagedObjectPoolSource.reset(this.metaData);
	}

	/**
	 * Ensure issue if fail to instantiate the {@link ManagedObjectPoolSource}.
	 */
	public void testFailInstantiate() {

		final RuntimeException failure = new RuntimeException("instantiate failure");

		// Record failure to instantiate
		this.issues.recordIssue(
				"Failed to instantiate " + MockManagedObjectPoolSource.class.getName() + " by default constructor",
				failure);

		// Attempt to load
		MockManagedObjectPoolSource.instantiateFailure = failure;
		this.loadManagedObjectPoolType(false, null);
	}

	/**
	 * Ensure issue if missing {@link Property}.
	 */
	public void testMissingProperty() {

		// Record missing property
		this.issues.recordIssue("Missing property 'missing'");

		// Attempt to load
		this.loadManagedObjectPoolType(false, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				context.getProperty("missing");
			}
		});
	}

	/**
	 * Ensure able to get properties.
	 */
	public void testGetProperties() {

		// Record basic meta-data
		this.recordReturn(this.metaData, this.metaData.getManagedObjectPoolFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getPooledObjectType(), Connection.class);

		// Attempt to load
		this.loadManagedObjectPoolType(true, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				assertEquals("Ensure get defaulted property", "DEFAULT", context.getProperty("missing", "DEFAULT"));
				assertEquals("Ensure get property ONE", "1", context.getProperty("ONE"));
				assertEquals("Ensure get property TWO", "2", context.getProperty("TWO"));
				Properties properties = context.getProperties();
				assertEquals("Incorrect number of properties", 2, properties.size());
				assertEquals("Incorrect property ONE", "1", properties.get("ONE"));
				assertEquals("Incorrect property TWO", "2", properties.get("TWO"));
			}
		}, "ONE", "1", "TWO", "2");
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testMissingClass() {

		// Record missing class
		this.issues.recordIssue("Can not load class 'missing'");

		// Attempt to load
		this.loadManagedObjectPoolType(false, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				context.loadClass("missing");
			}
		});
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testMissingResource() {

		// Record missing class
		this.issues.recordIssue("Can not obtain resource at location 'missing'");

		// Attempt to load
		this.loadManagedObjectPoolType(false, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				context.getResource("missing");
			}
		});
	}

	/**
	 * Ensure issue if fails to init the {@link ManagedObjectPoolSource}.
	 */
	public void testFailInitManagedObjectPoolSource() {

		final NullPointerException failure = new NullPointerException("Fail init ManagedObjectPoolSource");

		// Record failure to init the ManagedObjectPool Source
		this.issues.recordIssue("Failed to init", failure);

		// Attempt to load
		this.loadManagedObjectPoolType(false, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				throw failure;
			}
		});
	}

	/**
	 * Ensure issue if <code>null</code>
	 * {@link ManagedObjectPoolSourceMetaData}.
	 */
	public void testNullManagedObjectPoolSourceMetaData() {

		// Record null the ManagedObjectPool Source meta-data
		this.issues.recordIssue("Returned null ManagedObjectPoolSourceMetaData");

		// Attempt to load
		this.loadManagedObjectPoolType(false, new Init() {
			@Override
			public void init(ManagedObjectPoolSourceContext context) {
				MockManagedObjectPoolSource.metaData = null;
			}
		});
	}

	/**
	 * Ensure issue if fails to obtain the
	 * {@link ManagedObjectPoolSourceMetaData}.
	 */
	public void testFailGetManagedObjectPoolSourceMetaData() {

		final Error failure = new Error("Obtain meta-data failure");

		// Record failure to obtain the meta-data
		this.issues.recordIssue("Failed to get ManagedObjectPoolSourceMetaData", failure);

		// Attempt to load
		MockManagedObjectPoolSource.metaDataFailure = failure;
		this.loadManagedObjectPoolType(false, null);
	}

	/**
	 * Ensure issue if no pooled object type from meta-data.
	 */
	public void testNoPooledObjectType() {

		// Record no pooled object type
		this.recordReturn(this.metaData, this.metaData.getPooledObjectType(), null);
		this.issues.recordIssue("No pooled object type provided");

		// Attempt to load
		this.loadManagedObjectPoolType(false, null);
	}

	/**
	 * Ensure issue if no {@link ManagedObjectPoolFactory} from meta-data.
	 */
	public void testNoManagedObjectPoolFactory() {

		// Record no ManagedObjectPool factory
		this.recordReturn(this.metaData, this.metaData.getPooledObjectType(), Connection.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectPoolFactory(), null);
		this.issues.recordIssue("No ManagedObjectPoolFactory provided");

		// Attempt to load
		this.loadManagedObjectPoolType(false, null);
	}

	/**
	 * Ensure can load simple {@link ManagedObjectPool} without
	 * {@link ThreadCompletionListener}.
	 */
	public void testSimpleManagedObjectPool() {

		// Record simple ManagedObjectPool
		this.record_init();

		// Validate details of type
		ManagedObjectPoolType type = this.loadManagedObjectPoolType(true, null);
		assertEquals("Incorrect pooled object type", Connection.class, type.getPooledObjectType());
		assertEquals("Incorrect ManagedObjectPool factory", this.factory, type.getManagedObjectPoolFactory());
		assertEquals("Should be no ThreadCompletionListener factories", 0,
				type.getThreadCompletionListenerFactories().length);
	}

	/**
	 * Ensure can load simple {@link ManagedObjectPool} without
	 * {@link ThreadCompletionListener}.
	 */
	public void testManagedObjectPoolWithThreadCompletionListeners() {

		final ThreadCompletionListenerFactory threadCompletionListenerFactory = this
				.createMock(ThreadCompletionListenerFactory.class);

		// Record simple ManagedObjectPool
		this.record_init(threadCompletionListenerFactory);

		// Validate details of type
		ManagedObjectPoolType type = this.loadManagedObjectPoolType(true, null);
		assertEquals("Incorrect pooled object type", Connection.class, type.getPooledObjectType());
		assertEquals("Incorrect ManagedObjectPool factory", this.factory, type.getManagedObjectPoolFactory());
		ThreadCompletionListenerFactory[] threadCompletionListenerFactories = type
				.getThreadCompletionListenerFactories();
		assertEquals("Should be no ThreadCompletionListener factories", 1, threadCompletionListenerFactories.length);
		assertEquals("Incorrect ThreadCompletionListener factory", threadCompletionListenerFactory,
				threadCompletionListenerFactories[0]);
	}

	/**
	 * Record initial details of extension and {@link ManagedObjectPoolFactory}.
	 * 
	 * @param threadCompletionListenerFactories
	 *            Optional {@link ThreadCompletionListenerFactory} instances.
	 */
	private void record_init(ThreadCompletionListenerFactory... threadCompletionListenerFactories) {
		this.recordReturn(this.metaData, this.metaData.getPooledObjectType(), Connection.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectPoolFactory(), this.factory);
		this.recordReturn(this.metaData, this.metaData.getThreadCompleteListenerFactories(),
				(threadCompletionListenerFactories.length == 0) ? null : threadCompletionListenerFactories);
	}

	/**
	 * Loads the {@link ManagedObjectPoolType}.
	 * 
	 * @param isExpectedToLoad
	 *            Flag indicating if expecting to load the
	 *            {@link ManagedObjectPoolType}.
	 * @param init
	 *            {@link Init}.
	 * @param propertyNameValuePairs
	 *            {@link Property} name value pairs.
	 * @return Loaded {@link ManagedObjectPoolType}.
	 */
	public ManagedObjectPoolType loadManagedObjectPoolType(boolean isExpectedToLoad, Init init,
			String... propertyNameValuePairs) {

		// Replay mock objects
		this.replayMockObjects();

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the pool loader and load the pool type
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(this.issues);
		ManagedObjectPoolLoader adminLoader = compiler.getManagedObjectPoolLoader();
		MockManagedObjectPoolSource.init = init;
		ManagedObjectPoolType adminType = adminLoader.loadManagedObjectPoolType(MockManagedObjectPoolSource.class,
				propertyList);

		// Verify the mock objects
		this.verifyMockObjects();

		// Ensure if should be loaded
		if (isExpectedToLoad) {
			assertNotNull("Expected to load the pool type", adminType);
		} else {
			assertNull("Should not load the pool type", adminType);
		}

		// Return the administrator type
		return adminType;
	}

	/**
	 * Implement to initialise the {@link MockManagedObjectPoolSource}.
	 */
	private static interface Init {

		/**
		 * Implemented to init the {@link ManagedObjectPoolSource}.
		 * 
		 * @param context
		 *            {@link ManagedObjectPoolSourceContext}.
		 */
		void init(ManagedObjectPoolSourceContext context);
	}

	/**
	 * Mock {@link ManagedObjectPoolSource}.
	 */
	@TestSource
	public static class MockManagedObjectPoolSource implements ManagedObjectPoolSource {

		/**
		 * Failure to instantiate an instance.
		 */
		public static RuntimeException instantiateFailure = null;

		/**
		 * {@link Init} to init the {@link ManagedObjectPoolSource}.
		 */
		public static Init init = null;

		/**
		 * Failure to obtain the {@link ManagedObjectPoolMetaData}.
		 */
		public static Error metaDataFailure = null;

		/**
		 * {@link ManagedObjectPoolSourceSpecification}.
		 */
		public static ManagedObjectPoolSourceMetaData metaData;

		/**
		 * Resets the state for next test.
		 * 
		 * @param metaData
		 *            {@link ManagedObjectPoolSourceMetaData}.
		 */
		public static void reset(ManagedObjectPoolSourceMetaData metaData) {
			instantiateFailure = null;
			init = null;
			metaDataFailure = null;
			MockManagedObjectPoolSource.metaData = metaData;
		}

		/**
		 * Initiate with possible failure.
		 */
		public MockManagedObjectPoolSource() {
			// Throw instantiate failure
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ================== ManagedObjectPoolSource ==================
		 */

		@Override
		public ManagedObjectPoolSourceSpecification getSpecification() {
			fail("Should not obtain specification");
			return null;
		}

		@Override
		public void init(ManagedObjectPoolSourceContext context) throws Exception {
			// Run the init if available
			if (init != null) {
				init.init(context);
			}
		}

		@Override
		public ManagedObjectPoolSourceMetaData getMetaData() {

			// Throw meta-data failure
			if (metaDataFailure != null) {
				throw metaDataFailure;
			}

			// Return the meta-data
			return metaData;
		}
	}

}