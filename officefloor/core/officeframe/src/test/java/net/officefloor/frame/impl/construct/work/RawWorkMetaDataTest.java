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
package net.officefloor.frame.impl.construct.work;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaDataFactory;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaDataFactory;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaDataFactory;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link RawWorkMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawWorkMetaDataTest<W extends Work> extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Work}.
	 */
	private static final String WORK_NAME = "WORK";

	/**
	 * {@link WorkConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkConfiguration<W> configuration = this
			.createMock(WorkConfiguration.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this
			.createMock(SourceContext.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData = this
			.createMock(RawOfficeMetaData.class);

	/**
	 * {@link RawBoundManagedObjectMetaDataFactory}.
	 */
	private final RawBoundManagedObjectMetaDataFactory rawBoundManagedObjectFactory = this
			.createMock(RawBoundManagedObjectMetaDataFactory.class);

	/**
	 * {@link RawBoundAdministratorMetaDataFactory}.
	 */
	private final RawBoundAdministratorMetaDataFactory rawBoundAdministratorFactory = this
			.createMock(RawBoundAdministratorMetaDataFactory.class);

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<W> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * {@link Office} scoped {@link RawBoundManagedObjectMetaData} instances by
	 * their scoped names.
	 */
	private final Map<String, RawBoundManagedObjectMetaData> officeScopeManagedObjects = new HashMap<String, RawBoundManagedObjectMetaData>();

	/**
	 * {@link Office} scoped {@link RawBoundAdministratorMetaData} instances by
	 * their scoped names.
	 */
	private final Map<String, RawBoundAdministratorMetaData<?, ?>> officeScopeAdministrators = new HashMap<String, RawBoundAdministratorMetaData<?, ?>>();

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * {@link RawTaskMetaDataFactory}.
	 */
	private final RawTaskMetaDataFactory rawTaskMetaDataFactory = this
			.createMock(RawTaskMetaDataFactory.class);

	/**
	 * Continue {@link Team}.
	 */
	private final Team continueTeam = this.createMock(Team.class);

	/**
	 * Ensure issue if no {@link Work} name.
	 */
	public void testNoWorkName() {

		// Record no work name
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				null);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getOfficeName(), "OFFICE");
		this.issues.addIssue(AssetType.OFFICE, "OFFICE",
				"Work added to office without name");

		// Attempt to construct work
		this.replayMockObjects();
		this.constructRawWorkMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link WorkFactory}.
	 */
	public void testNoWorkFactory() {

		// Record no work factory
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getWorkFactory(), null);
		this.record_workIssue("WorkFactory not provided");

		// Attempt to construct work
		this.replayMockObjects();
		this.constructRawWorkMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Work} without {@link ManagedObject}
	 * instances, {@link Administrator} instances or {@link ManagedFunction} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testNoManagedObjectsAdministratorsOrTasks() {

		// Record no managed objects or administrators
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks();

		// Fully construct work
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Ensure no managed objects, administrators or tasks
		assertEquals("Should not have managed object meta-data", 0,
				workMetaData.getManagedObjectMetaData().length);
		assertEquals("Should not have administrator meta-data", 0,
				workMetaData.getAdministratorMetaData().length);
		assertEquals("Should not have tasks", 0,
				workMetaData.getTaskMetaData().length);
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} bound
	 * {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testSingleWorkBoundManagedObject() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);

		// Task to use the work bound managed object
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertEquals("Incorrect scope mo", rawMoMetaData,
						rawWorkMetaData.getScopeManagedObjectMetaData("MO"));
			}
		});

		// Record single work bound managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects("MO", rawMoMetaData);
		this.record_workBoundAdministrators();
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getDefaultInstanceIndex(), 0);
		this.recordReturn(
				rawMoMetaData,
				rawMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData,
				rawMoInstanceMetaData.getManagedObjectMetaData(), moMetaData);
		this.record_tasks(task);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Verify work meta-data
		ManagedObjectMetaData<?>[] workMoMetaData = workMetaData
				.getManagedObjectMetaData();
		assertEquals("Should have a work bound managed object meta-data", 1,
				workMoMetaData.length);
		assertEquals("Incorrect managed object meta-data", moMetaData,
				workMoMetaData[0]);
	}

	/**
	 * Ensure can construct {@link Work} with a {@link Office} scoped
	 * {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testSingleOfficeScopedManagedObject() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		this.officeScopeManagedObjects.put("MO", rawMoMetaData);

		// Task to use the work bound managed object
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertEquals("Incorrect scope mo", rawMoMetaData,
						rawWorkMetaData.getScopeManagedObjectMetaData("MO"));
			}
		});

		// Record work linked managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks(task);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Verify work meta-data
		ManagedObjectMetaData<?>[] workMoMetaData = workMetaData
				.getManagedObjectMetaData();
		assertEquals(
				"Should not have managed object meta-data as office scoped", 0,
				workMoMetaData.length);
	}

	/**
	 * Ensure issue if not able to obtain {@link ManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public void testNoManagedObjectMetaData() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);

		// Record no managed object meta-data (has multiple instances)
		this.record_workNameFactory();
		this.record_workBoundManagedObjects("MO", rawMoMetaData);
		this.record_workBoundAdministrators();
		this.record_tasks();
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getDefaultInstanceIndex(), 2);
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { null, null,
						rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData,
				rawMoInstanceMetaData.getManagedObjectMetaData(), null);
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getBoundManagedObjectName(), "MO");
		this.record_workIssue("No managed object meta-data for work managed object MO");

		// Construct work
		this.replayMockObjects();
		this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure handle no {@link ManagedFunction} scoped {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void testNoTaskScopedManagedObject() {

		// Task to attempting to obtain non task scoped managed object
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertNull("Should not get raw work managed object meta-data",
						rawWorkMetaData.getScopeManagedObjectMetaData("MO"));
			}
		});

		// Record no task scoped managed object
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks(task);

		// Construct work
		this.replayMockObjects();
		this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Work} bound
	 * {@link Administrator}.
	 */
	@SuppressWarnings("unchecked")
	public void testSingleWorkBoundAdministrator() {

		final RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorMetaData<?, ?> adminMetaData = this
				.createMock(AdministratorMetaData.class);

		// Task to use the work bound administrator
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertEquals("Incorrect administrator", rawAdminMetaData,
						rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"));
			}
		});

		// Record single work bound administrator
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators("ADMIN", rawAdminMetaData);
		this.recordReturn(rawAdminMetaData,
				rawAdminMetaData.getAdministratorMetaData(), adminMetaData);
		this.record_tasks(task);

		// Construct work with task managed object
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Verify work meta-data
		AdministratorMetaData<?, ?>[] workAdminMetaData = workMetaData
				.getAdministratorMetaData();
		assertEquals("Should have a work bound administrator meta-data", 1,
				workAdminMetaData.length);
		assertEquals("Incorrect administrator meta-data", adminMetaData,
				workAdminMetaData[0]);
	}

	/**
	 * Ensure can construct {@link Work} with a single {@link Office} scoped
	 * {@link Administrator}.
	 */
	@SuppressWarnings("unchecked")
	public void testSingleOfficeScopeAdministrator() {

		final RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = this
				.createMock(RawBoundAdministratorMetaData.class);
		this.officeScopeAdministrators.put("ADMIN", rawAdminMetaData);

		// Task to use the work bound administrator
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertEquals("Incorrect administrator", rawAdminMetaData,
						rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"));
			}
		});

		// Record work linked administrator
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks(task);

		// Construct work with task administrator
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Verify work meta-data
		AdministratorMetaData<?, ?>[] workAdminMetaData = workMetaData
				.getAdministratorMetaData();
		assertEquals(
				"Should not have administrator meta-data as office scoped", 0,
				workAdminMetaData.length);
	}

	/**
	 * Ensure issue if no task scoped {@link Administrator}.
	 */
	@SuppressWarnings("unchecked")
	public void testNoTaskScopedAdministrator() {

		// Task to attempting to obtain non task scoped administrator
		RecordedTask task = new RecordedTask("TASK", new TaskConstruction() {
			@Override
			public void constructTask(RawWorkMetaData<?> rawWorkMetaData,
					OfficeFloorIssues issues) {
				assertNull("Should not get work administrator index",
						rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"));
			}
		});

		// Record no task scoped administrator
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks(task);

		// Construct work
		this.replayMockObjects();
		this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct with a single {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	public void testSingleTask() {

		final RecordedTask task = new RecordedTask("TASK");

		// Record a single task
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks(task);

		// Fully construct work
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Ensure contains the task
		ManagedFunctionMetaData<?, ?, ?>[] taskMetaDatas = workMetaData.getTaskMetaData();
		assertEquals("Should have a single task", 1, taskMetaDatas.length);
		assertEquals("Incorrect task meta-data", task.taskMetaData,
				taskMetaDatas[0]);
	}

	/**
	 * Ensure able to specify the initial {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	public void testInitialTask() {

		final RecordedTask task = new RecordedTask("TASK");
		final AssetManager initialFlowAssetManager = this
				.createMock(AssetManager.class);

		// Record a initial task
		this.record_workNameFactory();
		this.record_workBoundManagedObjects();
		this.record_workBoundAdministrators();
		this.record_tasks("TASK", initialFlowAssetManager, task);

		// Fully construct work
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.fullyConstructRawWorkMetaData();
		this.verifyMockObjects();
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();

		// Ensure initial task specified
		FlowMetaData<W> flowMetaData = workMetaData.getInitialFlowMetaData();
		assertNotNull("Must have initial flow", flowMetaData);
		assertEquals("Incorrect task of initial flow", task.taskMetaData,
				flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.ASYNCHRONOUS,
				flowMetaData.getInstigationStrategy());
		assertEquals("Incorrect asset manager", initialFlowAssetManager,
				flowMetaData.getFlowManager());
	}

	/**
	 * Ensure able to link {@link ManagedFunction} instances.
	 */
	@SuppressWarnings("unchecked")
	public void testLinkTasks() {

		final RawBoundManagedObjectMetaData rawMoMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdminMetaData = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorMetaData<?, ?> adminMetaData = this
				.createMock(AdministratorMetaData.class);
		final RecordedTask task = new RecordedTask("TASK");
		final OfficeMetaDataLocator taskLocator = this
				.createMock(OfficeMetaDataLocator.class);

		// Record a linking tasks
		this.record_workNameFactory();
		this.record_workBoundManagedObjects("MO", rawMoMetaData);
		this.record_workBoundAdministrators("ADMIN", rawAdminMetaData);
		this.recordReturn(rawMoMetaData,
				rawMoMetaData.getDefaultInstanceIndex(), 0);
		this.recordReturn(
				rawMoMetaData,
				rawMoMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { rawMoInstanceMetaData });
		this.recordReturn(rawMoInstanceMetaData,
				rawMoInstanceMetaData.getManagedObjectMetaData(), moMetaData);
		this.recordReturn(rawAdminMetaData,
				rawAdminMetaData.getAdministratorMetaData(), adminMetaData);
		this.record_tasks(task);
		rawAdminMetaData.linkOfficeMetaData(taskLocator,
				this.assetManagerFactory, this.issues);
		task.rawTaskMetaData.linkTasks(taskLocator, null,
				this.assetManagerFactory, this.issues);
		this.control(task.rawTaskMetaData).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect task locator", taskLocator, actual[0]);
				assertTrue("Must have work meta-data",
						actual[1] instanceof WorkMetaData);
				assertEquals("Incorrect asset manager factory",
						RawWorkMetaDataTest.this.assetManagerFactory, actual[2]);
				assertEquals("Incorrect issues",
						RawWorkMetaDataTest.this.issues, actual[3]);
				return true; // matches if at this point
			}
		});

		// Fully construct work
		this.replayMockObjects();
		RawWorkMetaData<W> metaData = this.fullyConstructRawWorkMetaData();
		metaData.linkOfficeMetaData(taskLocator, this.assetManagerFactory,
				this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining the {@link Work} name and {@link WorkFactory}.
	 */
	private void record_workNameFactory() {
		this.recordReturn(this.configuration, this.configuration.getWorkName(),
				WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getWorkFactory(), this.workFactory);
	}

	/**
	 * Records bounding {@link ManagedObject} instances to the {@link Work}.
	 * 
	 * @param nameMoPairs
	 *            Name and {@link RawBoundManagedObjectMetaData} pairs.
	 */
	private void record_workBoundManagedObjects(Object... nameMoPairs) {

		// Obtain the listing of work bound names and managed objects
		int moCount = nameMoPairs.length / 2;
		String[] boundMoNames = new String[moCount];
		RawBoundManagedObjectMetaData[] workBoundMo = new RawBoundManagedObjectMetaData[moCount];
		for (int i = 0; i < nameMoPairs.length; i += 2) {
			int loadIndex = i / 2;
			boundMoNames[loadIndex] = (String) nameMoPairs[i];
			workBoundMo[loadIndex] = (RawBoundManagedObjectMetaData) nameMoPairs[i + 1];
		}

		// Record bounding managed objects to work
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getOfficeScopeManagedObjects(),
				this.officeScopeManagedObjects);
		ManagedObjectConfiguration<?>[] moConfiguration = new ManagedObjectConfiguration[moCount];
		this.recordReturn(this.configuration,
				this.configuration.getManagedObjectConfiguration(),
				moConfiguration);
		if (moConfiguration.length > 0) {
			Map<String, RawManagedObjectMetaData<?, ?>> officeRegisteredManagedObjects = new HashMap<String, RawManagedObjectMetaData<?, ?>>(
					0);
			this.recordReturn(this.rawOfficeMetaData,
					this.rawOfficeMetaData.getManagedObjectMetaData(),
					officeRegisteredManagedObjects);
			Map<String, RawGovernanceMetaData<?, ?>> rawGovernanceMetaData = new HashMap<String, RawGovernanceMetaData<?, ?>>();
			this.recordReturn(this.rawOfficeMetaData,
					this.rawOfficeMetaData.getGovernanceMetaData(),
					rawGovernanceMetaData);
			this.recordReturn(this.rawBoundManagedObjectFactory,
					this.rawBoundManagedObjectFactory
							.constructBoundManagedObjectMetaData(
									moConfiguration, this.issues,
									ManagedObjectScope.WORK, AssetType.WORK,
									WORK_NAME, this.assetManagerFactory,
									officeRegisteredManagedObjects,
									this.officeScopeManagedObjects, null, null,
									rawGovernanceMetaData), workBoundMo);
			for (int i = 0; i < moCount; i++) {
				this.recordReturn(workBoundMo[i],
						workBoundMo[i].getBoundManagedObjectName(),
						boundMoNames[i]);
			}
		}
	}

	/**
	 * Records bounding {@link Administrator} instances to the {@link Work}.
	 * 
	 * @param nameAdminPairs
	 *            Name and {@link RawBoundAdministratorMetaData} pairs.
	 */
	private void record_workBoundAdministrators(Object... nameAdminPairs) {

		// Obtain the listing of work bound names and administrators
		int adminCount = nameAdminPairs.length / 2;
		String[] boundAdminNames = new String[adminCount];
		RawBoundAdministratorMetaData<?, ?>[] workBoundAdmins = new RawBoundAdministratorMetaData[adminCount];
		for (int i = 0; i < nameAdminPairs.length; i += 2) {
			int loadIndex = i / 2;
			boundAdminNames[loadIndex] = (String) nameAdminPairs[i];
			workBoundAdmins[loadIndex] = (RawBoundAdministratorMetaData<?, ?>) nameAdminPairs[i + 1];
		}

		// Record bounding administrators to work
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getOfficeScopeAdministrators(),
				this.officeScopeAdministrators);
		AdministratorSourceConfiguration<?, ?>[] adminConfiguration = new AdministratorSourceConfiguration[adminCount];
		this.recordReturn(this.configuration,
				this.configuration.getAdministratorConfiguration(),
				adminConfiguration);
		if (adminConfiguration.length > 0) {
			Map<String, TeamManagement> officeTeams = new HashMap<String, TeamManagement>(
					0);
			this.recordReturn(this.rawOfficeMetaData,
					this.rawOfficeMetaData.getTeams(), officeTeams);
			this.recordReturn(this.rawOfficeMetaData,
					this.rawOfficeMetaData.getContinueTeam(), this.continueTeam);
			this.recordReturn(this.rawBoundAdministratorFactory,
					this.rawBoundAdministratorFactory
							.constructRawBoundAdministratorMetaData(
									adminConfiguration, this.sourceContext,
									this.issues, AdministratorScope.WORK,
									AssetType.WORK, WORK_NAME, officeTeams,
									this.continueTeam, null), workBoundAdmins,
					new AbstractMatcher() {
						@Override
						public boolean matches(Object[] expected,
								Object[] actual) {
							// Ensure first arguments are as expected
							for (int i = 0; i < 6; i++) {
								assertEquals("Incorrect argument " + i,
										expected[i], actual[i]);
							}
							assertNotNull("Must have scope managed objects",
									actual[6]);
							return true; // matches if here
						}
					});
			for (int i = 0; i < adminCount; i++) {
				this.recordReturn(workBoundAdmins[i],
						workBoundAdmins[i].getBoundAdministratorName(),
						boundAdminNames[i]);
			}
		}
	}

	/**
	 * Convenience method to record {@link ManagedFunction} construction without an initial
	 * {@link ManagedFunction}.
	 * 
	 * @param tasks
	 *            {@link RecordedTask} instances for each {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	private void record_tasks(RecordedTask... tasks) {
		this.record_tasks(null, null, tasks);
	}

	/**
	 * Records creation of {@link ManagedFunction} instances on the {@link Work}.
	 * 
	 * @param initialTaskName
	 *            Name of the initial {@link ManagedFunction} on the {@link Work}. May be
	 *            <code>null</code> if no initial {@link ManagedFunction}.
	 * @param assetManager
	 *            {@link AssetManager} for the initial {@link ManagedFunction}.
	 * @param tasks
	 *            {@link RecordedTask} instances for each {@link ManagedFunction}.
	 */
	@SuppressWarnings("unchecked")
	private void record_tasks(String initialTaskName,
			AssetManager assetManager,
			RawWorkMetaDataTest<W>.RecordedTask... tasks) {

		// Record obtaining the initial task name
		this.recordReturn(this.configuration,
				this.configuration.getInitialTaskName(), initialTaskName);

		// Create a task configuration for each task
		TaskConfiguration<?, ?, ?>[] taskConfigurations = new TaskConfiguration[tasks.length];
		for (int i = 0; i < taskConfigurations.length; i++) {
			taskConfigurations[i] = this.createMock(TaskConfiguration.class);
		}

		// Record obtaining the task configuration
		this.recordReturn(this.configuration,
				this.configuration.getTaskConfiguration(), taskConfigurations);
		for (int i = 0; i < taskConfigurations.length; i++) {

			// Make available to use in matcher
			final TaskConfiguration<?, ?, ?> taskConfiguration = taskConfigurations[i];
			final RecordedTask task = tasks[i];

			// Record constructing the raw task meta-data
			this.recordReturn(this.rawTaskMetaDataFactory,
					this.rawTaskMetaDataFactory.constructRawTaskMetaData(
							taskConfiguration, this.issues, null),
					task.rawTaskMetaData, new AbstractMatcher() {
						@Override
						public boolean matches(Object[] expected,
								Object[] actual) {
							// Verify actual arguments
							assertEquals("Incorrect task configuration",
									taskConfiguration, actual[0]);
							assertEquals("Incorrect issues",
									RawWorkMetaDataTest.this.issues, actual[1]);
							assertTrue("Must be RawWorkMetaData",
									actual[2] instanceof RawWorkMetaData<?>);

							// Obtain the details for task construction
							OfficeFloorIssues issues = (OfficeFloorIssues) actual[1];
							RawWorkMetaData<?> rawWorkMetaData = (RawWorkMetaData<?>) actual[2];

							// Determine if need to construct task
							if (task.taskConstruction != null) {
								task.taskConstruction.constructTask(
										rawWorkMetaData, issues);
							}

							// Always matches
							return true;
						}
					});

			// Record obtaining the task meta-data
			this.recordReturn(task.rawTaskMetaData,
					task.rawTaskMetaData.getTaskMetaData(), task.taskMetaData);

			// Record obtaining task name only if searching for initial task
			if (initialTaskName != null) {
				this.recordReturn(task.rawTaskMetaData,
						task.rawTaskMetaData.getTaskName(), task.taskName);
			}
		}

		// Create the asset manager (if initial task)
		if (initialTaskName != null) {
			this.recordReturn(this.assetManagerFactory,
					this.assetManagerFactory.createAssetManager(AssetType.WORK,
							WORK_NAME, "InitialFlow", this.issues),
					assetManager);
		}
	}

	/**
	 * Details of a recorded {@link ManagedFunction}.
	 */
	private class RecordedTask {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		public final String taskName;

		/**
		 * {@link RawTaskMetaData}.
		 */
		public final RawTaskMetaData<?, ?, ?> rawTaskMetaData = RawWorkMetaDataTest.this
				.createMock(RawTaskMetaData.class);

		/**
		 * {@link ManagedFunctionMetaData}.
		 */
		public final ManagedFunctionMetaData<?, ?, ?> taskMetaData = RawWorkMetaDataTest.this
				.createMock(ManagedFunctionMetaData.class);

		/**
		 * {@link TaskConfiguration} that is run on {@link ManagedFunction} creation.
		 */
		public final TaskConstruction taskConstruction;

		/**
		 * Initiate.
		 * 
		 * @param taskName
		 *            Name of the {@link ManagedFunction}.
		 * @param taskConstruction
		 *            {@link TaskConstruction}.
		 */
		public RecordedTask(String taskName, TaskConstruction taskConstruction) {
			this.taskName = taskName;
			this.taskConstruction = taskConstruction;
		}

		/**
		 * Initiate.
		 * 
		 * @param taskName
		 *            Name of the {@link ManagedFunction}.
		 */
		public RecordedTask(String taskName) {
			this(taskName, null);
		}
	}

	/**
	 * Provides construction of {@link RawTaskMetaData}.
	 */
	private interface TaskConstruction {

		/**
		 * Constructs the {@link ManagedFunctionMetaData}.
		 * 
		 * @param rawWorkMetaData
		 *            {@link RawWorkMetaData}.
		 * @param issues
		 *            {@link OfficeFloorIssues}.
		 */
		void constructTask(RawWorkMetaData<?> rawWorkMetaData,
				OfficeFloorIssues issues);
	}

	/**
	 * Records a {@link Work} construction issue.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_workIssue(String issueDescription) {
		this.issues.addIssue(AssetType.WORK, WORK_NAME, issueDescription);
	}

	/**
	 * Construct the {@link RawWorkMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            Is expect to construct.
	 * @return {@link RawWorkMetaData}.
	 */
	private RawWorkMetaData<W> constructRawWorkMetaData(
			boolean isExpectConstruct) {

		// Construct the raw work meta-data
		RawWorkMetaData<W> metaData = RawWorkMetaDataImpl.getFactory()
				.constructRawWorkMetaData(this.configuration,
						this.sourceContext, this.issues,
						this.rawOfficeMetaData, this.assetManagerFactory,
						this.rawBoundManagedObjectFactory,
						this.rawBoundAdministratorFactory,
						this.rawTaskMetaDataFactory);
		if (isExpectConstruct) {
			assertNotNull("Expect to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}

	/**
	 * Fully constructs the {@link RawWorkMetaData} including making the
	 * {@link WorkMetaData} available. Will always expect to construct the
	 * {@link RawWorkMetaData}.
	 * 
	 * @return {@link RawWorkMetaData}.
	 */
	private RawWorkMetaData<W> fullyConstructRawWorkMetaData() {

		// Construct the raw work meta-data
		RawWorkMetaData<W> metaData = this.constructRawWorkMetaData(true);

		// Obtain the work meta-data
		WorkMetaData<W> workMetaData = metaData.getWorkMetaData();
		assertNotNull("Should obtain work meta-data", workMetaData);
		WorkMetaData<W> anotherWorkMetaData = metaData.getWorkMetaData();
		assertEquals("Work meta-data should be same on each getWorkMetaData",
				workMetaData, anotherWorkMetaData);

		// Return the meta-data
		return metaData;
	}
}
