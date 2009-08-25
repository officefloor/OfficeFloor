/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.io.InputStream;
import java.util.Properties;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the creation of a {@link RawManagedObjectMetaDataImpl}.
 *
 * @author Daniel Sagenschneider
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
	private final ManagedObjectSourceMetaData<Indexed, FlowKey> metaData = this
			.createMock(ManagedObjectSourceMetaData.class);

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool = this
			.createMock(ManagedObjectPool.class);

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<?> managingOfficeConfiguration = this
			.createMock(ManagingOfficeConfiguration.class);

	/**
	 * {@link ManagingOfficeBuilder}.
	 */
	private final ManagingOfficeBuilder<?> managingOfficeBuilder = this
			.createMock(ManagingOfficeBuilder.class);

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
	 * {@link WorkBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkBuilder<Work> workBuilder = this
			.createMock(WorkBuilder.class);

	/**
	 * {@link TaskBuilder}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskBuilder<Work, None, Indexed> taskBuilder = this
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
	private final TaskFactory<Work, None, Indexed> taskFactory = this
			.createMock(TaskFactory.class);

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
	 * Ensures issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		// Record no object type
		this.record_initManagedObject();
		this.recordReturn(this.metaData, this.metaData.getObjectClass(), null);
		this.record_issue("No object type provided");

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
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
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
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
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
	 * Ensures issue if {@link AsynchronousManagedObject} but 0 timeout.
	 */
	public void testAsynchronousManagedObjectWithZeroTimeout() {

		// Record asynchronous managed object with no timeout
		this.record_initManagedObject();
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				AsynchronousManagedObject.class);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), 0);
		this
				.record_issue("Non-zero timeout must be provided for AsynchronousManagedObject");

		// Attempt to construct managed object
		this.replayMockObjects();
		this.constructRawManagedObjectMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no process bound name when required.
	 */
	public void testNoProcessBoundName() {

		ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record no process bound name
		this.record_initManagedObject();
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				ManagedObject.class);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), 0);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				new ManagedObjectFlowMetaData[] { flowMetaData });
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration
						.getProcessBoundManagedObjectName(), null);
		this
				.record_issue("Must specify the process bound name as Managed Object Source requires flows");

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
		final String TASK_NAME = "TASK";

		// Record adding a task
		this.record_initManagedObject();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				WORK_NAME, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(TASK_NAME,
				this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(TASK_NAME);
		this.record_createRawMetaData(ManagedObject.class, 0, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.addWorkName = "WORK";
		MockManagedObjectSource.addTaskName = TASK_NAME;
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link a parameter to the added {@link Task}.
	 */
	public void testLinkParameterToAddedTask() {

		final String WORK_NAME = MANAGED_OBJECT_NAME + ".WORK";
		final String TASK_NAME = "TASK";
		final Class<?> parameterType = String.class;

		// Record linking a parameter to the added task
		this.record_initManagedObject();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				WORK_NAME, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(TASK_NAME,
				this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(TASK_NAME);
		this.taskBuilder.linkParameter(0, parameterType);
		this.record_createRawMetaData(ManagedObject.class, 0, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.addWorkName = "WORK";
		MockManagedObjectSource.addTaskName = TASK_NAME;
		MockManagedObjectSource.addTaskLinkedParameter = parameterType;
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link a parameter to the added {@link Task}.
	 */
	public void testLinkFlowToAddedTask() {

		final String WORK_NAME = MANAGED_OBJECT_NAME + ".WORK";
		final String TASK_NAME = "TASK";
		final String LINK_WORK_NAME = MANAGED_OBJECT_NAME + ".LINK_WORK";
		final String LINK_TASK_NAME = "LINK_TASK";

		// Record linking a parameter to the added task
		this.record_initManagedObject();
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				WORK_NAME, this.workFactory), this.workBuilder);
		this.recordReturn(this.workBuilder, this.workBuilder.addTask(TASK_NAME,
				this.taskFactory), this.taskBuilder);
		this.workBuilder.setInitialTask(TASK_NAME);
		this.taskBuilder.linkFlow(0, LINK_WORK_NAME, LINK_TASK_NAME,
				FlowInstigationStrategyEnum.SEQUENTIAL, Object.class);
		this.record_createRawMetaData(ManagedObject.class, 0, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.addWorkName = "WORK";
		MockManagedObjectSource.addTaskName = TASK_NAME;
		MockManagedObjectSource.addTaskLinkWorkName = "LINK_WORK";
		MockManagedObjectSource.addTaskLinkTaskName = LINK_TASK_NAME;
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to add a startup {@link Task}.
	 */
	public void testAddStartupTask() {

		final String STARTUP_WORK_NAME = MANAGED_OBJECT_NAME + ".STARTUP_WORK";
		final String STARTUP_TASK_NAME = "STARTUP_TASK";

		// Record registering a start up task
		this.record_initManagedObject();
		this.officeBuilder.addStartupTask(STARTUP_WORK_NAME, STARTUP_TASK_NAME);
		this.record_createRawMetaData(ManagedObject.class, 0, null);

		// Attempt to construct managed object
		this.replayMockObjects();
		MockManagedObjectSource.startupWorkName = "STARTUP_WORK";
		MockManagedObjectSource.startupTaskName = STARTUP_TASK_NAME;
		this.constructRawManagedObjectMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle plain {@link ManagedObject} (ie not
	 * {@link AsynchronousManagedObject} or {@link CoordinatingManagedObject}).
	 */
	@SuppressWarnings("unchecked")
	public void testPlainManagedObject() {

		final RawBoundManagedObjectMetaData boundMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final int INSTANCE_INDEX = 0;
		final RawBoundManagedObjectInstanceMetaData<?> boundInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final String BOUND_NAME = "BOUND_NAME";
		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);

		// Record plain managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(ManagedObject.class, 0, null);
		this.recordReturn(boundMetaData, boundMetaData
				.getBoundManagedObjectName(), BOUND_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getManagedObjectIndex(),
				moIndex);
		this.recordReturn(moIndex, moIndex.getManagedObjectScope(),
				ManagedObjectScope.WORK);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						ManagedObjectScope.WORK + ":" + BOUND_NAME, "source",
						this.issues), assetManager);

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.createManagedObjectMetaData(boundMetaData, INSTANCE_INDEX,
						boundInstanceMetaData, new ManagedObjectIndex[0],
						this.assetManagerFactory, this.issues);
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
		assertEquals("Incorrect managed object pool", this.managedObjectPool,
				rawMetaData.getManagedObjectPool());
		assertEquals("Ensure round trip managing office details", rawMetaData,
				rawMetaData.getRawManagingOfficeMetaData()
						.getRawManagedObjectMetaData());

		// Verify managed object meta-data
		assertEquals("Incorrect bound name", BOUND_NAME, moMetaData
				.getBoundManagedObjectName());
		assertTrue("Incorrect managed object source", (moMetaData
				.getManagedObjectSource() instanceof MockManagedObjectSource));
		assertEquals("Incorrect object type", Object.class, moMetaData
				.getObjectType());
		assertEquals("Incorrect instance index", INSTANCE_INDEX, moMetaData
				.getInstanceIndex());
		assertEquals("Incorrect timeout", 0, moMetaData.getTimeout());
		assertFalse("Should not be asynchronous", moMetaData
				.isManagedObjectAsynchronous());
		assertFalse("Should not be coordinating", moMetaData
				.isCoordinatingManagedObject());
		assertEquals("Incorrect source asset manager", assetManager, moMetaData
				.getSourcingManager());
		assertNull("Not asynchronous so no operations asset manager",
				moMetaData.getOperationsManager());
	}

	/**
	 * Ensures flag asynchronous for {@link AsynchronousManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testAsynchronousManagedObject() {

		final RawBoundManagedObjectMetaData boundMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final int INSTANCE_INDEX = 3;
		final RawBoundManagedObjectInstanceMetaData<?> boundInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final String BOUND_NAME = "BOUND_NAME";
		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);
		final AssetManager sourceAssetManager = this
				.createMock(AssetManager.class);
		final AssetManager operationsAssetManager = this
				.createMock(AssetManager.class);

		// Record asynchronous managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(AsynchronousManagedObject.class, 1000,
				null);
		this.recordReturn(boundMetaData, boundMetaData
				.getBoundManagedObjectName(), BOUND_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getManagedObjectIndex(),
				moIndex);
		this.recordReturn(moIndex, moIndex.getManagedObjectScope(),
				ManagedObjectScope.THREAD);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						ManagedObjectScope.THREAD + ":" + BOUND_NAME, "source",
						this.issues), sourceAssetManager);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						ManagedObjectScope.THREAD + ":" + BOUND_NAME,
						"operations", this.issues), operationsAssetManager);

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.createManagedObjectMetaData(boundMetaData, INSTANCE_INDEX,
						boundInstanceMetaData, new ManagedObjectIndex[0],
						this.assetManagerFactory, this.issues);
		this.verifyMockObjects();

		// Verify different index
		assertEquals("Incorrect instance index", INSTANCE_INDEX, moMetaData
				.getInstanceIndex());

		// Verify is asynchronous with operations manager
		assertTrue("Should be asynchronous", moMetaData
				.isManagedObjectAsynchronous());
		assertEquals("Incorrect operations asset manager",
				operationsAssetManager, moMetaData.getOperationsManager());
		assertEquals("Incorrect timeout", 1000, moMetaData.getTimeout());
	}

	/**
	 * Ensures flag coordinating for {@link CoordinatingManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testCoordinatingManagedObject() {

		final RawBoundManagedObjectMetaData boundMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final int INSTANCE_INDEX = 0;
		final RawBoundManagedObjectInstanceMetaData<?> boundInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final String BOUND_NAME = "BOUND_NAME";
		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);
		final AssetManager sourceAssetManager = this
				.createMock(AssetManager.class);

		// Record coordinating managed object
		this.record_initManagedObject();
		this.record_createRawMetaData(CoordinatingManagedObject.class, 0, null);
		this.recordReturn(boundMetaData, boundMetaData
				.getBoundManagedObjectName(), BOUND_NAME);
		this.recordReturn(boundMetaData, boundMetaData.getManagedObjectIndex(),
				moIndex);
		this.recordReturn(moIndex, moIndex.getManagedObjectScope(),
				ManagedObjectScope.PROCESS);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						ManagedObjectScope.PROCESS + ":" + BOUND_NAME,
						"source", this.issues), sourceAssetManager);

		// Attempt to construct managed object
		this.replayMockObjects();
		RawManagedObjectMetaData rawMetaData = this
				.constructRawManagedObjectMetaData(true);
		ManagedObjectMetaData<?> moMetaData = rawMetaData
				.createManagedObjectMetaData(boundMetaData, INSTANCE_INDEX,
						boundInstanceMetaData, new ManagedObjectIndex[0],
						this.assetManagerFactory, this.issues);
		this.verifyMockObjects();

		// Verify flagged as coordinating
		assertTrue("Should be coordinating", moMetaData
				.isCoordinatingManagedObject());
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

		// Record obtaining the managing office
		final String managingOfficeName = "OFFICE";
		this.recordReturn(this.configuration, this.configuration
				.getManagingOfficeConfiguration(),
				this.managingOfficeConfiguration);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration.getOfficeName(),
				managingOfficeName);
		this.recordReturn(this.managingOfficeConfiguration,
				this.managingOfficeConfiguration.getBuilder(),
				this.managingOfficeBuilder);
		this.recordReturn(this.officeFloorConfiguration,
				this.officeFloorConfiguration.getOfficeConfiguration(),
				new OfficeConfiguration[] { this.officeConfiguration });
		this.recordReturn(this.officeConfiguration, this.officeConfiguration
				.getOfficeName(), managingOfficeName);
		this.recordReturn(this.officeConfiguration, this.officeConfiguration
				.getBuilder(), this.officeBuilder);
	}

	/**
	 * Records creating the {@link RawManagedObjectMetaData} after initialising.
	 *
	 * @param managedObjectClass
	 *            {@link ManagedObject} class.
	 * @param timeout
	 *            Timeout for the {@link ManagedObjectSource}.
	 * @param processBoundName
	 *            Process bound name for {@link ManagedObject}.
	 * @param {@link ManagedObjectFlowMetaData} for the
	 *        {@link ManagedObjectSource}.
	 */
	private <MO extends ManagedObject> void record_createRawMetaData(
			Class<MO> managedObjectClass, long timeout,
			String processBoundName,
			ManagedObjectFlowMetaData<?>... moFlowMetaData) {
		// Record completing creating raw meta data
		this.recordReturn(this.metaData, this.metaData.getObjectClass(),
				Object.class);
		this.recordReturn(this.metaData, this.metaData.getManagedObjectClass(),
				managedObjectClass);
		this.recordReturn(this.configuration, this.configuration
				.getDefaultTimeout(), timeout);
		this.recordReturn(this.metaData, this.metaData.getFlowMetaData(),
				moFlowMetaData);
		if (moFlowMetaData.length > 0) {
			this.recordReturn(this.managingOfficeConfiguration,
					this.managingOfficeConfiguration
							.getProcessBoundManagedObjectName(),
					processBoundName);
		}
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectPool(), this.managedObjectPool);
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
	 * {@link Flow} keys.
	 */
	private static enum FlowKey {
		KEY
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
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
		 * Parameter type for a linked parameter to the {@link Task}.
		 */
		public static Class<?> addTaskLinkedParameter = null;

		/**
		 * Name of {@link Flow} to link to the added {@link Task}.
		 */
		public static String addTaskLinkWorkName = null;

		/**
		 * Name of {@link Flow} to link to the added {@link Task}.
		 */
		public static String addTaskLinkTaskName = null;

		/**
		 * {@link WorkFactory}.
		 */
		public static WorkFactory<?> workFactory = null;

		/**
		 * {@link TaskFactory}.
		 */
		public static TaskFactory<Work, None, Indexed> taskFactory = null;

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
		public static ManagedObjectSourceMetaData<Indexed, FlowKey> metaData;

		/**
		 * Resets state of {@link MockManagedObjectSource} for testing.
		 *
		 * @param taskFactory
		 *            {@link TaskFactory}.
		 * @param metaData
		 *            {@link ManagedObjectSourceMetaData}.
		 */
		public static void reset(WorkFactory<Work> workFactory,
				TaskFactory<Work, None, Indexed> taskFactory,
				ManagedObjectSourceMetaData<Indexed, FlowKey> metaData) {
			instantiateFailure = null;
			requiredPropertyName = null;
			recycleWorkFactory = null;
			addWorkName = null;
			addTaskName = null;
			addTaskLinkedParameter = null;
			addTaskLinkTaskName = null;
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
			InputStream inputStream = context.getClassLoader()
					.getResourceAsStream(objectPath);
			assertNotNull("Must be able to obtain resources", inputStream);

			// Obtain the required property
			if (requiredPropertyName != null) {
				context.getProperty(requiredPropertyName);
			}

			// Ensure can obtain defaulted property
			assertEquals("Must default property", "DEFAULT", context
					.getProperty("property to default", "DEFAULT"));

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
				ManagedObjectTaskBuilder<?, ?> taskBuilder = context.addWork(
						addWorkName, workFactory).addTask(addTaskName,
						taskFactory);

				// Link in the parameter
				if (addTaskLinkedParameter != null) {
					taskBuilder.linkParameter(0, addTaskLinkedParameter);
				}

				// Link in the flow
				if (addTaskLinkWorkName != null) {
					taskBuilder.linkFlow(0, addTaskLinkWorkName,
							addTaskLinkTaskName,
							FlowInstigationStrategyEnum.SEQUENTIAL,
							Object.class);
				}
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
				.getFactory().constructRawManagedObjectMetaData(
						this.configuration, this.issues,
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