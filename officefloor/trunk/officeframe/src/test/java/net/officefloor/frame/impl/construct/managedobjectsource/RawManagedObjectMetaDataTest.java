/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.io.InputStream;
import java.util.Properties;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaDataImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the creation of a {@link RawManagedObjectMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawManagedObjectMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private final String MANAGED_OBJECT_NAME = "MANAGED OBJECT NAME";

	/**
	 * {@link ManagedObjectSourceConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectSourceConfiguration configuration = this
			.createMock(ManagedObjectSourceConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link OfficeFloorConfiguration}.
	 */
	private final OfficeFloorConfiguration officeFloorConfiguration = this
			.createMock(OfficeFloorConfiguration.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * {@link ManagedObjectSourceMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectSourceMetaData<Indexed, HandlerKey> metaData = this
			.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link AssetManager}.
	 */
	private final AssetManager sourcingAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool = this
			.createMock(ManagedObjectPool.class);

	/**
	 * {@link ManagedObjectBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectBuilder managedObjectBuilder = this
			.createMock(ManagedObjectBuilder.class);

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration managingOfficeConfiguration = this
			.createMock(ManagingOfficeConfiguration.class);

	/**
	 * {@link OfficeConfiguration}.
	 */
	private final OfficeConfiguration officeConfiguration = this
			.createMock(OfficeConfiguration.class);

	/**
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder = this
			.createMock(OfficeBuilder.class);

	/**
	 * {@link ManagedObjectHandlerBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectHandlerBuilder<HandlerKey> managedObjectHandlerBuilder = this
			.createMock(ManagedObjectHandlerBuilder.class);

	/**
	 * {@link HandlerBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final HandlerBuilder<Indexed> handlerBuilder = this
			.createMock(HandlerBuilder.class);

	/**
	 * {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkBuilder<Work> workBuilder = this
			.createMock(WorkBuilder.class);

	/**
	 * {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskBuilder<Object, Work, None, Indexed> taskBuilder = this
			.createMock(TaskBuilder.class);

	/**
	 * {@link WorkFactory}
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<Work> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * {@link TaskFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskFactory<Object, Work, None, Indexed> taskFactory = this
			.createMock(TaskFactory.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Reset the mock managed object source state
		MockManagedObjectSource.reset(this.taskFactory, this.metaData);
	}

	/**
	 * Ensures issue if no {@link ManagedObjectSource} name.
	 */
	public void testNoManagedObjectSourceName() {

		// Record no name
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), null);
		this.issues.addIssue(AssetType.OFFICE_FLOOR, "OfficeFloor",
				"ManagedObject added without a name");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagedObjectSource} class.
	 */
	public void testNoManagedObjectSourceClass() {

		// Record no class
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"No ManagedObjectSource class provided");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate {@link ManagedObjectSource}.
	 */
	public void testFailInstantiateManagedObjectSource() {

		final Exception failure = new Exception("instantiate failure");

		// Record fail instantiate
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), MockManagedObjectSource.class);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Failed to instantiate "
						+ MockManagedObjectSource.class.getName(), failure);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.instantiateFailure = failure;
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no managing {@link Office} name.
	 */
	public void testNoManagingOfficeName() {

		// Record no managing office name provided
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), MockManagedObjectSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());
		this.recordReturn(this.configuration, this.configuration.getBuilder(),
				this.managedObjectBuilder);
		this.recordReturn(this.configuration, this.configuration
				.getManagingOfficeConfiguration().getOfficeName(), null);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"No managing office specified");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no managing {@link Office} found.
	 */
	public void testNoManagingOfficeFound() {

		// Record no managing office found
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), MockManagedObjectSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());
		this.recordReturn(this.configuration, this.configuration.getBuilder(),
				this.managedObjectBuilder);
		this.recordReturn(this.configuration, this.configuration
				.getManagingOfficeConfiguration().getOfficeName(), "OFFICE");
		this.recordReturn(this.officeFloorConfiguration,
				this.officeFloorConfiguration.getOfficeConfiguration(),
				new OfficeConfiguration[0]);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Can not find managing office 'OFFICE'");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if missing required property.
	 */
	public void testMissingProperty() {

		// Record fail instantiate due to missing property
		this.record_initManagedObject();
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Property 'required.property' must be specified");

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.requiredPropertyName = "required.property";
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in initialising {@link ManagedObjectSource}.
	 */
	public void testFailInitManagedObjectSource() {

		final Exception failure = new Exception("init failure");

		// Record fail instantiate
		this.record_initManagedObject();
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Failed to initialise "
						+ MockManagedObjectSource.class.getName(), failure);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.initFailure = failure;
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if null {@link ManagedObjectSourceMetaData}.
	 */
	public void testNullMetaData() {

		// Record null meta-data
		this.record_initManagedObject();
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Must provide meta-data");

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.metaData = null;
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no sourcing {@link AssetManager}.
	 */
	public void testNoSourcingAssetManager() {

		// Record no sourcing asset manager
		this.record_initManagedObject();
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues), null);

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if negative default timeout.
	 */
	public void testNegativeDefaultTimeout() {

		// Record null meta-data
		this.record_initManagedObject();
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), -1);
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				"Must not have negative default timeout");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to successfully create the
	 * {@link RawManagedObjectMetaDataImpl} with:
	 * <ol>
	 * <li>registering {@link Handler}
	 * <li>requiring a recycle {@link Work}
	 * <li>providing {@link ManagedObjectPool}
	 * </ol>
	 */
	public void testSuccessful() {

		// Record init
		this.record_initManagedObject();

		// Record registering handler
		this.recordReturn(this.managedObjectBuilder, this.managedObjectBuilder
				.getManagedObjectHandlerBuilder(),
				this.managedObjectHandlerBuilder);
		this.recordReturn(this.managedObjectHandlerBuilder,
				this.managedObjectHandlerBuilder
						.registerHandler(HandlerKey.KEY), this.handlerBuilder);

		// Record registering recycle task
		String recycleWorkName = MANAGED_OBJECT_NAME
				+ "."
				+ ManagedObjectSourceContextImpl.MANAGED_OBJECT_CLEAN_UP_WORK_NAME;
		String recycleTaskName = MANAGED_OBJECT_NAME + ".TASK";
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				recycleWorkName, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(
				recycleTaskName, this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(recycleTaskName);

		// Record registering a start up task
		this.officeBuilder.addStartupTask(
				MANAGED_OBJECT_NAME + ".STARTUP_WORK", MANAGED_OBJECT_NAME
						+ ".STARTUP_TASK");

		// Record completing creating raw meta data
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), 0);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectPool(), this.managedObjectPool);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		MockManagedObjectSource.recycleWorkFactory = this.workFactory;
		MockManagedObjectSource.startupWorkName = "STARTUP_WORK";
		MockManagedObjectSource.startupTaskName = "STARTUP_TASK";
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect managed object name", MANAGED_OBJECT_NAME,
				rawMetaData.getManagedObjectName());
		assertEquals("Incorrect managed object source configuration",
				this.configuration, rawMetaData
						.getManagedObjectSourceConfiguration());
		assertTrue("Incorrect managed object source", (rawMetaData
				.getManagedObjectSource() instanceof MockManagedObjectSource));
		assertEquals("Incorrect source meta-data", this.metaData, rawMetaData
				.getManagedObjectSourceMetaData());
		assertEquals("Incorrect default timeout", 0, rawMetaData
				.getDefaultTimeout());
		assertEquals("Incorrect managed object pool", this.managedObjectPool,
				rawMetaData.getManagedObjectPool());
		assertEquals("Incorrect sourcing asset manager",
				this.sourcingAssetManager, rawMetaData
						.getSourcingAssetManager());
		assertEquals("Incorrect recycle work name", recycleWorkName,
				rawMetaData.getRecycleWorkName());
	}

	/**
	 * Records initialising the {@link ManagedObjectSource}.
	 */
	private void record_initManagedObject() {

		// Record instantiating managed object
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), MockManagedObjectSource.class);

		// Record obtaining details from configuration to init
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());
		this.recordReturn(this.configuration, this.configuration.getBuilder(),
				this.managedObjectBuilder);

		// Record obtaining the managing office
		final String managingOfficeName = "OFFICE";
		this.recordReturn(this.configuration, this.configuration
				.getManagingOfficeConfiguration(),
				this.managingOfficeConfiguration);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration.getOfficeName(),
				managingOfficeName);
		this.recordReturn(this.officeFloorConfiguration,
				this.officeFloorConfiguration.getOfficeConfiguration(),
				new OfficeConfiguration[] { this.officeConfiguration });
		this.recordReturn(this.officeConfiguration, this.officeConfiguration
				.getOfficeName(), managingOfficeName);
		this.recordReturn(this.officeConfiguration, this.officeConfiguration
				.getBuilder(), this.officeBuilder);
	}

	/**
	 * Handler keys.
	 */
	private static enum HandlerKey {
		KEY
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public static class MockManagedObjectSource implements ManagedObjectSource {

		/**
		 * Instantiate exception.
		 */
		public static Exception instantiateFailure = null;

		/**
		 * Name of required property.
		 */
		public static String requiredPropertyName = null;

		/**
		 * Handler key.
		 */
		public static HandlerKey handlerKey = null;

		/**
		 * Recycle {@link WorkFactory}.
		 */
		public static WorkFactory<?> recycleWorkFactory = null;

		/**
		 * Init exception.
		 */
		public static Exception initFailure = null;

		/**
		 * {@link TaskFactory}.
		 */
		public static TaskFactory<Object, Work, None, Indexed> taskFactory = null;

		/**
		 * Name of startup {@link Work}.
		 */
		public static String startupWorkName = null;

		/**
		 * Name of startup {@link Task}.
		 */
		public static String startupTaskName = null;

		/**
		 * {@link ManagedObjectMetaData}.
		 */
		public static ManagedObjectSourceMetaData<Indexed, HandlerKey> metaData;

		/**
		 * Resets state of {@link MockManagedObjectSource} for testing.
		 * 
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param metaData
		 *            {@link ManagedObjectSourceMetaData}.
		 */
		public static void reset(
				TaskFactory<Object, Work, None, Indexed> taskFactory,
				ManagedObjectSourceMetaData<Indexed, HandlerKey> metaData) {
			instantiateFailure = null;
			requiredPropertyName = null;
			handlerKey = null;
			recycleWorkFactory = null;
			initFailure = null;
			MockManagedObjectSource.taskFactory = taskFactory;
			startupWorkName = null;
			startupTaskName = null;
			MockManagedObjectSource.metaData = metaData;
		}

		/**
		 * Instantiate.
		 * 
		 * @throws Exception
		 *             Possible instantiate failure.
		 */
		public MockManagedObjectSource() throws Exception {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ==================== ManagedObjectSource ====================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			fail("Should not call getSpecification");
			return null;
		}

		@Override
		public void init(ManagedObjectSourceContext context) throws Exception {

			// Ensure able to obtain resources
			String objectPath = Object.class.getName().replace('.', '/')
					+ ".class";
			InputStream inputStream = context.getResourceLocator()
					.locateInputStream(objectPath);
			assertNotNull("Must be able to obtain resources", inputStream);

			// Obtain the required property
			if (requiredPropertyName != null) {
				context.getProperty(requiredPropertyName);
			}

			// Obtain and register handler
			if (handlerKey != null) {
				ManagedObjectHandlerBuilder managedObjectHandlerBuilder = context
						.getHandlerBuilder();
				HandlerBuilder<?> handlerBuilder = managedObjectHandlerBuilder
						.registerHandler(handlerKey);
				assertNotNull("Must have handler builder", handlerBuilder);
			}

			// Register the recycle work
			if (recycleWorkFactory != null) {
				// Add work and task that should have namespaced names
				ManagedObjectWorkBuilder recycleWorkBuilder = context
						.getRecycleWork(recycleWorkFactory);
				recycleWorkBuilder.addTask("TASK", taskFactory);
			}

			// Register the startup task
			if (startupWorkName != null) {
				context.addStartupTask(startupWorkName, startupTaskName);
			}

			// Determine if failure in initialising
			if (initFailure != null) {
				throw initFailure;
			}
		}

		@Override
		public ManagedObjectSourceMetaData getMetaData() {
			return metaData;
		}

		@Override
		public void start(ManagedObjectExecuteContext context) throws Exception {
			fail("Should not call start");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not call sourceManagedObject");
		}
	}

	/**
	 * Constructs the {@link RawManagedObjectMetaDataImpl} with the mock
	 * objects.
	 * 
	 * @return {@link RawManagedObjectMetaDataImpl}.
	 */
	@SuppressWarnings("unchecked")
	private RawManagedObjectMetaData constructRawManagedObjectMetaData(
			boolean isExpectConstruction) {

		// Attempt to construct
		RawManagedObjectMetaData metaData = RawManagedObjectMetaDataImpl
				.getFactory()
				.constructRawManagedObjectMetaData(this.configuration,
						this.issues, this.assetManagerFactory,
						this.officeFloorConfiguration);

		// Provide assertion on whether should be constructed
		if (isExpectConstruction) {
			assertNotNull("Should have constructed meta-data", metaData);
		} else {
			assertNull("Should not construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}

}
