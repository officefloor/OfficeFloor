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
import java.util.Map;
import java.util.Properties;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
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
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

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
	 * Sourcing {@link AssetManager}.
	 */
	private final AssetManager sourcingAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * Operations {@link AssetManager}.
	 */
	private final AssetManager operationsAssetManager = this
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

	/**
	 * {@link TaskMetaDataLocator}.
	 */
	private final TaskMetaDataLocator taskMetaDataLocator = this
			.createMock(TaskMetaDataLocator.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this
			.createMock(OfficeMetaData.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Reset the mock managed object source state
		MockManagedObjectSource.reset(this.workFactory, this.taskFactory,
				this.metaData);
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
		this.record_issue("No ManagedObjectSource class provided");

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
		this.record_issue("Failed to instantiate "
				+ MockManagedObjectSource.class.getName(), failure);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.instantiateFailure = failure;
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link ManagingOfficeConfiguration}.
	 */
	public void testNoManagingOfficeConfiguration() {

		// Record no managing office configuration
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceName(), MANAGED_OBJECT_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectSourceClass(), MockManagedObjectSource.class);
		this.recordReturn(this.configuration, this.configuration
				.getProperties(), new Properties());
		this.recordReturn(this.configuration, this.configuration.getBuilder(),
				this.managedObjectBuilder);
		this.recordReturn(this.configuration, this.configuration
				.getManagingOfficeConfiguration(), null);
		this.record_issue("No managing office configuration");

		// Attempt to construct managed object
		this.replayMockObjects();
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
				.getManagingOfficeConfiguration(),
				this.managingOfficeConfiguration);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration.getOfficeName(), null);
		this.record_issue("No managing office specified");

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
				.getManagingOfficeConfiguration(),
				this.managingOfficeConfiguration);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration.getOfficeName(), "OFFICE");
		this.recordReturn(this.officeFloorConfiguration,
				this.officeFloorConfiguration.getOfficeConfiguration(),
				new OfficeConfiguration[0]);
		this.record_issue("Can not find managing office 'OFFICE'");

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
		this.record_issue("Property 'required.property' must be specified");

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
		this.record_issue("Failed to initialise "
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
		this.record_issue("Must provide meta-data");

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
	 * Ensures issue if no {@link ManagedObject} class.
	 */
	public void testNoManagedObjectClass() {

		// Record no managed object class
		this.record_initManagedObject();
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				null);
		this.record_issue("No managed object class provided");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if negative default timeout.
	 */
	public void testNegativeDefaultTimeout() {

		// Record negative default timeout
		this.record_initManagedObject();
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				ManagedObject.class);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), -1);
		this.record_issue("Must not have negative default timeout");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no process bound name when required.
	 */
	public void testNoProcessBoundName() {

		// Record no process bound name
		this.record_initManagedObject();
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				ManagedObject.class);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), 0);
		this.recordReturn(this.metaData, this.metaData.getHandlerKeys(),
				HandlerKey.class);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration
						.getProcessBoundManagedObjectName(), null);
		this
				.record_issue("Must specify the process bound name as Managed Object Source requires handlers");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add a {@link Task}.
	 */
	public void testAddTask() {

		final String WORK_NAME = MANAGED_OBJECT_NAME + ".WORK";
		final String TASK_NAME = MANAGED_OBJECT_NAME + ".TASK";

		// Record adding a task
		this.record_initManagedObject();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				WORK_NAME, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(TASK_NAME,
				this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(TASK_NAME);
		this.record_createRawMetaData(ManagedObject.class, null, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.addWorkName = "WORK";
		MockManagedObjectSource.addTaskName = "TASK";
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able add the recycle {@link Task}.
	 */
	public void testRecycleTask() {

		String recycleWorkName = MANAGED_OBJECT_NAME
				+ "."
				+ ManagedObjectSourceContextImpl.MANAGED_OBJECT_CLEAN_UP_WORK_NAME;
		String recycleTaskName = MANAGED_OBJECT_NAME + ".TASK";

		// Record adding the recycle task
		this.record_initManagedObject();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				recycleWorkName, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(
				recycleTaskName, this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(recycleTaskName);
		this.record_createRawMetaData(ManagedObject.class, null, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.recycleWorkFactory = this.workFactory;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Verify the recycle name is correct
		assertEquals("Incorrect recycle work name", recycleWorkName,
				rawMetaData.getRecycleWorkName());
	}

	/**
	 * Ensure able to add a startup {@link Task}.
	 */
	public void testAddStartupTask() {

		final String STARTUP_WORK_NAME = MANAGED_OBJECT_NAME + ".STARTUP_WORK";
		final String STARTUP_TASK_NAME = MANAGED_OBJECT_NAME + ".STARTUP_TASK";

		// Record registering a start up task
		this.record_initManagedObject();
		this.officeBuilder.addStartupTask(STARTUP_WORK_NAME, STARTUP_TASK_NAME);
		this.record_createRawMetaData(ManagedObject.class, null, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.startupWorkName = "STARTUP_WORK";
		MockManagedObjectSource.startupTaskName = "STARTUP_TASK";
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle plain {@link ManagedObject} (ie not
	 * {@link AsynchronousManagedObject} or {@link CoordinatingManagedObject}).
	 */
	public void testPlainManagedObject() {

		// Record plain managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(ManagedObject.class, null, null);

		// Attempt to construct managed object
		this.replayMockObjects();
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
		assertEquals("Incorrect sourcing asset manager",
				this.sourcingAssetManager, rawMetaData
						.getSourcingAssetManager());
		assertEquals("Incorrect default timeout", 0, rawMetaData
				.getDefaultTimeout());
		assertFalse("Should not be asynchronous", rawMetaData.isAsynchronous());
		assertNull("Should not have operations asset manager", rawMetaData
				.getOperationsAssetManager());
		assertFalse("Should not be coordinating", rawMetaData.isCoordinating());
		assertEquals("Incorrect managed object pool", this.managedObjectPool,
				rawMetaData.getManagedObjectPool());
		assertEquals("Incorrect sourcing asset manager",
				this.sourcingAssetManager, rawMetaData
						.getSourcingAssetManager());
		assertEquals("Ensure round trip managing office details", rawMetaData,
				rawMetaData.getManagingOfficeMetaData()
						.getRawManagedObjectMetaData());
		assertEquals("Should not have handler keys", 0, rawMetaData
				.getHandlerKeys().length);
	}

	/**
	 * Ensures flag asynchronous for {@link AsynchronousManagedObject}.
	 */
	public void testAsynchronousManagedObject() {

		// Record asynchronous managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(AsynchronousManagedObject.class, null,
				null);

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Verify flagged as asynchronous
		assertTrue("Should be asynchronous", rawMetaData.isAsynchronous());
		assertEquals("Incorrect operations asset manager",
				this.operationsAssetManager, rawMetaData
						.getOperationsAssetManager());
	}

	/**
	 * Ensures flag coordinating for {@link CoordinatingManagedObject}.
	 */
	public void testCoordinatingManagedObject() {

		// Record coordinating managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(CoordinatingManagedObject.class, null,
				null);

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();

		// Verify flagged as coordinating
		assertTrue("Should be coordinating", rawMetaData.isCoordinating());
	}

	/**
	 * Ensures issues if {@link Handler} instances configured but no
	 * {@link Handler} instances required.
	 */
	public void testNoHandlersButHandlersConfigured() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);

		// Record handlers configured but none required
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, null, null);
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this
				.record_issue("Managed Object Source meta-data specifies no handlers but handlers configured for it");

		// Attempt to construct managed object and have it managed
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issues if {@link ManagedObject} is not managed by the
	 * {@link Office}.
	 */
	public void testNotManagedByOffice() {

		final OfficeMetaData officeMetaData = this
				.createMock(OfficeMetaData.class);
		final ProcessMetaData processMetaData = this
				.createMock(ProcessMetaData.class);

		// Record managed object not managed by office
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(), new HandlerConfiguration[0]);
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), officeMetaData);
		this.recordReturn(officeMetaData, officeMetaData.getProcessMetaData(),
				processMetaData);
		this.recordReturn(processMetaData, processMetaData
				.getManagedObjectMetaData(), new ManagedObjectMetaData[0]);
		this.recordReturn(officeMetaData, officeMetaData.getOfficeName(),
				"OFFICE");
		this
				.record_issue("Managed Object Source by process bound name 'BOUND_MO' not managed by Office OFFICE");

		// Attempt to construct managed object and have it managed
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if no {@link Handler} key from the
	 * {@link HandlerConfiguration}.
	 */
	public void testNoHandlerKey() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);

		// Record no handler key for handler configuration
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess("BOUND_MO");
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), null);
		this.record_issue("Handler Key not provided");
		this.record_issue("No handler configured for key " + HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if configured {@link Handler} key is not of correct type.
	 */
	public void testWrongHandlerKeyType() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);

		// Record wrong handler key type
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess("BOUND_MO");
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), WrongHandlerKeyType.WRONG_KEY);
		this
				.record_issue("Handler key "
						+ WrongHandlerKeyType.WRONG_KEY
						+ " is not of type specified by Managed Object Source meta-data ("
						+ HandlerKey.class.getName() + ")");
		this.record_issue("No handler configured for key " + HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * {@link Handler} key of wrong type.
	 */
	private enum WrongHandlerKeyType {
		WRONG_KEY
	};

	/**
	 * Ensures issue if no {@link Handler} is configured.
	 */
	public void testNoHandlerConfigured() {

		// Record no handler configured
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(), new HandlerConfiguration[0]);
		this.record_bindToProcess("BOUND_MO");
		this.record_issue("No handler configured for key " + HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if no {@link HandlerFactory}.
	 */
	public void testNoHandlerFactory() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);

		// Record no handler factory
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess("BOUND_MO");
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), HandlerKey.KEY);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerFactory(), null);
		this.record_issue("Handler Factory must be provided for handler key "
				+ HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if the {@link HandlerFactory} does not create a
	 * {@link Handler}.
	 */
	public void testHandlerNotCreated() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFactory<?> handlerFactory = this
				.createMock(HandlerFactory.class);

		// Record no handler factory
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess("BOUND_MO");
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), HandlerKey.KEY);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerFactory(), handlerFactory);
		this.recordReturn(handlerFactory, handlerFactory.createHandler(), null);
		this.record_issue("Handler Factory must create a Handler (handler key "
				+ HandlerKey.KEY + ")");

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if the {@link HandlerFactory} fails in creating the
	 * {@link Handler}.
	 */
	public void testHandlerCreationFailure() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFactory<?> handlerFactory = this
				.createMock(HandlerFactory.class);
		final RuntimeException createFailure = new RuntimeException(
				"Create failure");

		// Record no handler factory
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess("BOUND_MO");
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), HandlerKey.KEY);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerFactory(), handlerFactory);
		this.control(handlerFactory).expectAndThrow(
				handlerFactory.createHandler(), createFailure);
		this.record_issue(
				"Handler Factory failed creating the Handler (handler key "
						+ HandlerKey.KEY + ")", createFailure);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if no {@link Flow} name for {@link Handler}.
	 */
	public void testNoFlowName() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFlowConfiguration<?> flowConfiguration = this
				.createMock(HandlerFlowConfiguration.class);

		// Record no flow name for handler
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.record_manageByOfficeAndCreateHandler("BOUND_MO",
				handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowName(),
				null);
		this.record_issue("No flow name provided for flow 0 of handler "
				+ HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue {@link Flow} key out of sync.
	 */
	public void testFlowKeyOutOfSync() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFlowConfiguration<?> flowConfiguration = this
				.createMock(HandlerFlowConfiguration.class);

		// Record no flow name for handler
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.record_manageByOfficeAndCreateHandler("BOUND_MO",
				handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowName(),
				"FLOW");
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				OutOfSyncFlowKey.OUT_OF_SYNC_KEY);
		this.record_issue("Flow keys are out of sync for handler "
				+ HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * {@link Flow} key that is out of sync for {@link Handler}.
	 */
	private enum OutOfSyncFlowKey {
		IN_SYNC_KEY, OUT_OF_SYNC_KEY
	}

	/**
	 * Ensures issue no {@link TaskNodeReference} for the {@link Flow}.
	 */
	public void testFlowTaskReference() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFlowConfiguration<?> flowConfiguration = this
				.createMock(HandlerFlowConfiguration.class);

		// Record no flow name for handler
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.record_manageByOfficeAndCreateHandler("BOUND_MO",
				handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowName(),
				"FLOW");
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getTaskNodeReference(), null);
		this
				.record_issue("No task reference provided on flow FLOW for handler "
						+ HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if no {@link TaskMetaData} for the {@link Flow}.
	 */
	public void testNoFlowTask() {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFlowConfiguration<?> flowConfiguration = this
				.createMock(HandlerFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no flow name for handler
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		this.record_manageByOfficeAndCreateHandler("BOUND_MO",
				handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowName(),
				"FLOW");
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getTaskMetaData("WORK", "TASK"), null);
		this
				.record_issue("Can not find task meta-data (work=WORK, task=TASK) for flow FLOW of handler "
						+ HandlerKey.KEY);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures issue if fails to set the {@link HandlerContext}.
	 */
	public void testFailSettingHandlerContext() throws Exception {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		Error handlerFailure = new Error("Set Handler Context Failure");

		// Record fail to set handler context
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		Handler<?> handler = this.record_manageByOfficeAndCreateHandler(
				"BOUND_MO", handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[0]);
		handler.setHandlerContext(null);
		this.control(handler).setMatcher(new TypeMatcher(HandlerContext.class));
		this.control(handler).expectAndThrow(null, handlerFailure);
		this.record_issue("Failed to set Handler Context for handler "
				+ HandlerKey.KEY, handlerFailure);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be no handlers", 0, handlers.size());
	}

	/**
	 * Ensures {@link HandlerContext} contains correct details.
	 */
	public void testHandlerContextCorrect() throws Exception {

		final HandlerConfiguration<?, ?> handlerConfiguration = this
				.createMock(HandlerConfiguration.class);
		final HandlerFlowConfiguration<?> flowConfiguration = this
				.createMock(HandlerFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);
		final Object parameter = new Object();
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final JobNode jobNode = this.createMock(JobNode.class);

		// Record no flow name for handler
		this.record_initManagedObject();
		this.record_registerHandler();
		this.record_createRawMetaData(ManagedObject.class, HandlerKey.class,
				"BOUND_MO");
		Handler<?> handler = this.record_manageByOfficeAndCreateHandler(
				"BOUND_MO", handlerConfiguration);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getLinkedProcessConfiguration(),
				new HandlerFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowName(),
				"FLOW");
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getTaskMetaData("WORK", "TASK"), taskMetaData);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "Handler " + HandlerKey.KEY
								+ " Flow 0", this.issues), assetManager);
		handler.setHandlerContext(null);
		this.control(handler).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				// Invoke the process to validate details
				HandlerContext<HandlerKey> handlerContext = (HandlerContext<HandlerKey>) actual[0];
				handlerContext.invokeProcess(HandlerKey.KEY, parameter,
						managedObject);
				return true;
			}
		});
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.createProcess(null, parameter, managedObject, 0, null),
				jobNode, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						FlowMetaData<?> flowMetaData = (FlowMetaData<?>) actual[0];
						assertEquals("Incorrect parameter", parameter,
								actual[1]);
						assertEquals("Incorrect managed object", managedObject,
								actual[2]);
						assertEquals("Incorrect process index", 0, actual[3]);
						assertNull("Should not have escalation handler",
								actual[4]);

						// Validate flow meta-data
						assertEquals("Incorrect task meta-data", taskMetaData,
								flowMetaData.getInitialTaskMetaData());
						assertEquals("Incorrect asset manager", assetManager,
								flowMetaData.getFlowManager());

						// Matches if at this point
						return true;
					}
				});
		jobNode.activateJob();

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.handlerKey = HandlerKey.KEY;
		RawManagedObjectMetaData<?, ?> rawMetaData = this
				.fullyConstructRawManagedObjectMetaData();
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		Object[] handlerKeys = rawMetaData.getHandlerKeys();
		assertEquals("Should have a handler key", 1, handlerKeys.length);
		assertEquals("Incorrect handler key", HandlerKey.KEY, handlerKeys[0]);
		Map<?, Handler<?>> handlers = rawMetaData.getHandlers();
		assertEquals("Should be a handler", 1, handlers.size());
		assertEquals("Incorrect handler", handler, handlers.get(HandlerKey.KEY));
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
	 * Registers the {@link Handler}.
	 */
	private void record_registerHandler() {
		this.recordReturn(this.managedObjectBuilder, this.managedObjectBuilder
				.getManagedObjectHandlerBuilder(),
				this.managedObjectHandlerBuilder);
		this.recordReturn(this.managedObjectHandlerBuilder,
				this.managedObjectHandlerBuilder
						.registerHandler(HandlerKey.KEY), this.handlerBuilder);
	}

	/**
	 * Records creating the {@link RawManagedObjectMetaData} after initialising.
	 * 
	 * @param managedObjectClass
	 *            {@link ManagedObject} class.
	 * @param handlerKeyClass
	 *            Handler key class.
	 * @param processBoundName
	 *            Process bound name for {@link ManagedObject}.
	 */
	private <M extends ManagedObject> void record_createRawMetaData(
			Class<M> managedObjectClass, Class<?> handlerKeyClass,
			String processBoundName) {
		// Record completing creating raw meta data
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_NAME, "sourcing", this.issues),
				this.sourcingAssetManager);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), 0);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				managedObjectClass);
		if (AsynchronousManagedObject.class
				.isAssignableFrom(managedObjectClass)) {
			this.recordReturn(this.assetManagerFactory,
					this.assetManagerFactory.createAssetManager(
							AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
							"operations", this.issues),
					this.operationsAssetManager);
		}
		this.recordReturn(this.metaData, this.metaData.getHandlerKeys(),
				handlerKeyClass);
		if (handlerKeyClass != null) {
			this.recordReturn(this.managingOfficeConfiguration,
					this.managingOfficeConfiguration
							.getProcessBoundManagedObjectName(),
					processBoundName);
		}
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectPool(), this.managedObjectPool);
	}

	/**
	 * Records binding to the {@link ProcessState} of the managing
	 * {@link Office}.
	 */
	private void record_bindToProcess(String processBoundName) {

		final ProcessMetaData processMetaData = this
				.createMock(ProcessMetaData.class);
		final ManagedObjectMetaData<?> managedObjectMetaData = this
				.createMock(ManagedObjectMetaData.class);

		this.recordReturn(this.taskMetaDataLocator, this.taskMetaDataLocator
				.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.getProcessMetaData(), processMetaData);
		this.recordReturn(processMetaData, processMetaData
				.getManagedObjectMetaData(),
				new ManagedObjectMetaData[] { managedObjectMetaData });
		this.recordReturn(managedObjectMetaData, managedObjectMetaData
				.getBoundManagedObjectName(), processBoundName);
	}

	/**
	 * Records creating the {@link Handler}.
	 * 
	 * @param processBoundName
	 *            Name that {@link ManagedObject} bound to {@link ProcessState}
	 *            of the {@link Office}.
	 * @param handlerConfiguration
	 *            {@link HandlerConfiguration}.
	 */
	private Handler<?> record_manageByOfficeAndCreateHandler(
			String processBoundName,
			HandlerConfiguration<?, ?> handlerConfiguration) {

		final HandlerFactory<?> handlerFactory = this
				.createMock(HandlerFactory.class);
		final Handler<?> handler = this.createMock(Handler.class);

		// Record creating the handler
		this.recordReturn(this.configuration, this.configuration
				.getHandlerConfiguration(),
				new HandlerConfiguration[] { handlerConfiguration });
		this.record_bindToProcess(processBoundName);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerKey(), HandlerKey.KEY);
		this.recordReturn(handlerConfiguration, handlerConfiguration
				.getHandlerFactory(), handlerFactory);
		this.recordReturn(handlerFactory, handlerFactory.createHandler(),
				handler);

		// Return the handler
		return handler;
	}

	/**
	 * Records an issue for the {@link ManagedObject}.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				issueDescription);
	}

	/**
	 * Records an issue for the {@link ManagedObject}.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Cause.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT, MANAGED_OBJECT_NAME,
				issueDescription, cause);
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
		 * Name to add {@link Work}.
		 */
		public static String addWorkName = null;

		/**
		 * Name to add {@link Task}.
		 */
		public static String addTaskName = null;

		/**
		 * {@link WorkFactory}.
		 */
		public static WorkFactory<?> workFactory = null;

		/**
		 * {@link TaskFactory}.
		 */
		public static TaskFactory<Object, Work, None, Indexed> taskFactory = null;

		/**
		 * Init exception.
		 */
		public static Exception initFailure = null;

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
		public static void reset(WorkFactory<Work> workFactory,
				TaskFactory<Object, Work, None, Indexed> taskFactory,
				ManagedObjectSourceMetaData<Indexed, HandlerKey> metaData) {
			instantiateFailure = null;
			requiredPropertyName = null;
			handlerKey = null;
			recycleWorkFactory = null;
			addWorkName = null;
			addTaskName = null;
			MockManagedObjectSource.workFactory = workFactory;
			MockManagedObjectSource.taskFactory = taskFactory;
			initFailure = null;
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
				// Add work and task that should have name spaced names
				ManagedObjectWorkBuilder recycleWorkBuilder = context
						.getRecycleWork(recycleWorkFactory);
				recycleWorkBuilder.addTask("TASK", taskFactory);
			}

			// Add a task
			if (addWorkName != null) {
				// Add work and task that should have name spaced names
				context.addWork(addWorkName, workFactory).addTask(addTaskName,
						taskFactory);
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
	 * @return {@link RawManagedObjectMetaData}.
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

	/**
	 * Constructs the {@link RawManagedObjectMetaData} and has it managed by its
	 * {@link Office}.
	 * 
	 * @return {@link RawManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private RawManagedObjectMetaData fullyConstructRawManagedObjectMetaData() {

		// Attempt to construct
		RawManagedObjectMetaData metaData = this
				.constructRawManagedObjectMetaData(true);

		// Manage by the office
		metaData.manageByOffice(this.taskMetaDataLocator,
				this.assetManagerFactory, this.issues);

		// Return the meta-data
		return metaData;
	}

}