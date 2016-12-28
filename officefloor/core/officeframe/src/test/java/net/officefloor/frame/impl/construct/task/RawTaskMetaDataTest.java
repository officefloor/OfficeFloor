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
package net.officefloor.frame.impl.construct.task;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.construct.function.RawManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.duty.DutyKeyImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionDutyConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionEscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawManagedFunctionMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawManagedFunctionMetaDataImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTaskMetaDataTest<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private static final String TASK_NAME = "TASK";

	/**
	 * Name of the {@link Work} containing the {@link ManagedFunction}.
	 */
	private static final String DEFAULT_WORK_NAME = "DEFAULT_WORK_NAME";

	/**
	 * {@link ManagedFunctionConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionConfiguration<W, D, F> configuration = this
			.createMock(ManagedFunctionConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawWorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final RawWorkMetaData<W> rawWorkMetaData = this
			.createMock(RawWorkMetaData.class);

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionFactory<W, D, F> taskFactory = this
			.createMock(ManagedFunctionFactory.class);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData = this
			.createMock(RawOfficeMetaData.class);

	/**
	 * Responsible {@link TeamManagement}.
	 */
	private final TeamManagement responsibleTeam = this
			.createMock(TeamManagement.class);

	/**
	 * Continue {@link Team}.
	 */
	private final Team continueTeam = this.createMock(Team.class);

	/**
	 * {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkMetaData<W> workMetaData = this
			.createMock(WorkMetaData.class);

	/**
	 * {@link ManagedFunctionLocator}.
	 */
	private final ManagedFunctionLocator inputTaskMetaDataLocator = this
			.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link ManagedFunctionLocator} to find the {@link ManagedFunctionMetaData}.
	 */
	private final ManagedFunctionLocator taskLocator = this
			.createMock(ManagedFunctionLocator.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * Ensure issue if not {@link ManagedFunction} name.
	 */
	public void testNoTaskName() {

		// Record no task name
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				null);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), "WORK");
		this.issues.addIssue(AssetType.WORK, "WORK", "Task added without name");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionFactory}.
	 */
	public void testNoTaskFactory() {

		// Record no task factory
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), DEFAULT_WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getTaskFactory(), null);
		this.record_taskIssue("No TaskFactory provided");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to have a differentiator.
	 */
	public void testDifferentiator() {

		final Object DIFFERENTIATOR = "Differentiator";

		// Record with differentiator
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), DEFAULT_WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration,
				this.configuration.getDifferentiator(), DIFFERENTIATOR);
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), "TEAM");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getRawOfficeMetaData(),
				this.rawOfficeMetaData);
		Map<String, TeamManagement> teams = new HashMap<String, TeamManagement>();
		teams.put("TEAM", this.responsibleTeam);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getTeams(), teams);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getContinueTeam(), this.continueTeam);
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Verify differentiator
		assertEquals("Incorrect differentiator", DIFFERENTIATOR, metaData
				.getTaskMetaData().getDifferentiator());
	}

	/**
	 * Ensure able to have no differentiator.
	 */
	public void testNoDifferentiator() {

		// Record with differentiator
		this.record_taskNameFactoryTeam(); // no differentiator loaded
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Verify no differentiator
		assertNull("Should have no differentiator", metaData.getTaskMetaData()
				.getDifferentiator());
	}

	/**
	 * Ensure issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), DEFAULT_WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration,
				this.configuration.getDifferentiator(), null);
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), null);
		this.record_taskIssue("No team name provided for task");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record unknown team
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), DEFAULT_WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration,
				this.configuration.getDifferentiator(), null);
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), "TEAM");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getRawOfficeMetaData(),
				this.rawOfficeMetaData);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getTeams(), new HashMap<String, Team>());
		this.record_taskIssue("Unknown team 'TEAM' responsible for task");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link ManagedFunction} name, {@link ManagedFunctionFactory} and {@link Team} are
	 * available.
	 */
	public void testTaskInitialDetails() {

		// Record initial task details
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Verify initial raw details
		assertEquals("Incorrect task name", TASK_NAME, metaData.getTaskName());
		assertEquals("Incorrect raw work meta-data", this.rawWorkMetaData,
				metaData.getRawWorkMetaData());

		// Verify initial details
		ManagedFunctionMetaData<?, ?, ?> taskMetaData = metaData.getTaskMetaData();
		assertEquals("Incorrect job name", DEFAULT_WORK_NAME + "." + TASK_NAME,
				taskMetaData.getFunctionName());
		assertEquals("Incorect task factory", this.taskFactory,
				taskMetaData.getManagedFunctionFactory());
		assertNull("No differentiator", taskMetaData.getDifferentiator());
		assertEquals("Incorrect team", this.responsibleTeam,
				taskMetaData.getResponsibleTeam());
	}

	/**
	 * Ensure issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record no managed object name
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				null);
		this.record_taskIssue("No type for object at index 0");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in a parameter.
	 */
	public void testParameter() {

		final Class<?> parameterType = Connection.class;
		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameter
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				parameterType);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), true);
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		ManagedFunctionMetaData<W, D, F> metaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0,
				metaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect parameter type", parameterType,
				metaData.getParameterType());

		// Ensure can translate to parameter
		ManagedObjectIndex parameterIndex = metaData
				.translateManagedObjectIndexForWork(0);
		assertEquals("Incorrect translation to parameter",
				ManagedFunctionImpl.PARAMETER_INDEX,
				parameterIndex.getIndexOfManagedObjectWithinScope());
		assertNull("Should not have scope for parameter index",
				parameterIndex.getManagedObjectScope());
	}

	/**
	 * Ensure able to link in the parameter being used twice.
	 */
	public void testParameterUsedTwice() {

		final Class<?> parameterType = Connection.class;

		// Parameter will be Connection but can be passed as Object
		final Class<?> paramTypeOne = Connection.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigOne = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final Class<?> paramTypeTwo = Object.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigTwo = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameter used twice
		this.record_taskNameFactoryTeam();
		this.recordReturn(
				this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(),
				paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(),
				paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		ManagedFunctionMetaData<W, D, F> metaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0,
				metaData.getRequiredManagedObjects().length);
		assertEquals("Parameter type should be most specific", parameterType,
				metaData.getParameterType());

		// Ensure can translate to parameters
		assertEquals("Incorrect translation to parameter one",
				ManagedFunctionImpl.PARAMETER_INDEX, metaData
						.translateManagedObjectIndexForWork(0)
						.getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect translation to parameter two",
				ManagedFunctionImpl.PARAMETER_INDEX, metaData
						.translateManagedObjectIndexForWork(1)
						.getIndexOfManagedObjectWithinScope());
	}

	/**
	 * Ensure issue if parameter used twice with incompatible types.
	 */
	public void testIncompatibleParameters() {

		// Parameter types are incompatible
		final Class<?> paramTypeOne = Integer.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigOne = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final Class<?> paramTypeTwo = String.class;
		final ManagedFunctionObjectConfiguration<?> paramConfigTwo = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record parameters incompatible
		this.record_taskNameFactoryTeam();
		this.recordReturn(
				this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(),
				paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(),
				paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_taskIssue("Incompatible parameter types ("
				+ paramTypeOne.getName() + ", " + paramTypeTwo.getName() + ")");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can handle {@link ManagedObject} not configured for dependency.
	 */
	public void testMissingManagedObject() {

		// Record null managed object configuration
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { null });
		this.record_taskIssue("No object configuration at index 0");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} name.
	 */
	public void testNoScopeManagedObjectName() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record no managed object name
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), null);
		this.record_taskIssue("No name for managed object at index 0");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObject}.
	 */
	public void testUnknownManagedObject() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeManagedObjectMetaData("MO"), null);
		this.record_taskIssue("Can not find scope managed object 'MO'");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should be no managed objects", 0, metaData
				.getTaskMetaData().getRequiredManagedObjects().length);
	}

	/**
	 * Ensure issue {@link Object} required is incompatible with {@link Object}
	 * from the {@link ManagedObject}.
	 */
	public void testIncompatibleManagedObject() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final RawBoundManagedObjectMetaData rawWorkMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawWorkMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMo = this
				.createMock(RawManagedObjectMetaData.class);

		// Record incompatible managed object type
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Connection.class); // require Connection but Integer
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeManagedObjectMetaData("MO"),
				rawWorkMo);
		this.record_singleInstance_getRawManagedObjectMetaData(rawWorkMo,
				rawWorkMoInstance, rawMo);
		this.recordReturn(rawMo, rawMo.getObjectType(), Integer.class);
		this.recordReturn(rawWorkMoInstance,
				rawWorkMoInstance.getRawManagedObjectMetaData(), rawMo);
		this.recordReturn(rawMo, rawMo.getManagedObjectName(),
				"MANAGED_OBJECT_SOURCE");
		this.record_taskIssue("Managed object MO is incompatible (require="
				+ Connection.class.getName()
				+ ", object of managed object type=" + Integer.class.getName()
				+ ", ManagedObjectSource=MANAGED_OBJECT_SOURCE)");
		this.record_NoAdministration();
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should be no managed objects", 0, metaData
				.getTaskMetaData().getRequiredManagedObjects().length);
	}

	/**
	 * Ensure able to construct with a single {@link ManagedObject} that has no
	 * dependencies or administration.
	 */
	public void testManagedObject() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Work required Managed Object
		final RawBoundManagedObjectMetaData rawMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> mo = this
				.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);

		// Record
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeManagedObjectMetaData("MO"), rawMo);
		this.record_singleInstance_getRawManagedObjectMetaData(rawMo,
				rawMoInstance, mo);
		this.recordReturn(mo, mo.getObjectType(), Connection.class);
		LoadDependencies moLinked = new LoadDependencies(rawMo, moIndex,
				rawMoInstance); // No dependencies
		this.record_loadDependencies(moLinked);
		this.record_NoAdministration();
		// Record dependency sorting
		this.record_dependencySortingForCoordination(moLinked);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure have the required managed object
		ManagedObjectIndex[] requiredManagedObjects = metaData
				.getTaskMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of managed objects", 1,
				requiredManagedObjects.length);
		assertEquals("Incorrect required managed object", moIndex,
				requiredManagedObjects[0]);

		// Ensure can translate
		assertEquals("Incorrect task managed object", moIndex, metaData
				.getTaskMetaData().translateManagedObjectIndexForWork(0));
	}

	/**
	 * Ensure able to order {@link ManagedObject} dependencies so that
	 * dependencies come first (required for coordinating).
	 */
	public void testManagedObjectDependencyOrdering() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);

		// Work required Managed Object
		final RawBoundManagedObjectMetaData rawWorkMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawWorkMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> workMo = this
				.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex workMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);

		// Dependency of the work required Managed Object
		final RawBoundManagedObjectMetaData dependencyMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 1);

		// Dependency of the dependency Managed Object
		final RawBoundManagedObjectMetaData dependencyDependency = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> dependencyDependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyDependencyIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);

		// Record
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeManagedObjectMetaData("MO"),
				rawWorkMo);
		this.record_singleInstance_getRawManagedObjectMetaData(rawWorkMo,
				rawWorkMoInstance, workMo);
		this.recordReturn(workMo, workMo.getObjectType(), Connection.class);
		// Record loading dependencies
		LoadDependencies dependencyDependencyLinked = new LoadDependencies(
				dependencyDependency, dependencyDependencyIndex,
				dependencyDependencyInstance);
		LoadDependencies dependencyLinked = new LoadDependencies(dependencyMo,
				dependencyMoIndex, dependencyMoInstance,
				dependencyDependencyLinked);
		LoadDependencies workLinked = new LoadDependencies(rawWorkMo,
				workMoIndex, rawWorkMoInstance, dependencyLinked);
		this.record_loadDependencies(workLinked);
		this.record_NoAdministration();
		// Record dependency sorting
		this.record_dependencySortingForCoordination(workLinked,
				dependencyLinked, dependencyDependencyLinked);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed objects with dependency first
		ManagedObjectIndex[] requiredManagedObjects = metaData
				.getTaskMetaData().getRequiredManagedObjects();
		assertEquals("Incorrect number of managed objects", 3,
				requiredManagedObjects.length);
		assertEquals("Dependency of dependency should be first",
				dependencyDependencyIndex, requiredManagedObjects[0]);
		assertEquals("Dependency should be after its dependency",
				dependencyMoIndex, requiredManagedObjects[1]);
		assertEquals("Coordinating should be after its dependency",
				workMoIndex, requiredManagedObjects[2]);

		// Ensure can translate
		assertEquals("Incorrect task managed object", workMoIndex, metaData
				.getTaskMetaData().translateManagedObjectIndexForWork(0));
	}

	/**
	 * Ensure issue if there is a cyclic dependency between the required
	 * {@link ManagedObject} instances.
	 */
	public void testManagedObjectCyclicDependency() {

		final ManagedFunctionObjectConfiguration<?> moConfiguration = this
				.createMock(ManagedFunctionObjectConfiguration.class);
		final RawBoundManagedObjectMetaData rawMoA = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoAInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> moA = this
				.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex aIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData rawMoB = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoBInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex bIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 1);

		// Record
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), false);
		this.recordReturn(moConfiguration,
				moConfiguration.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeManagedObjectMetaData("MO"),
				rawMoA);
		this.record_singleInstance_getRawManagedObjectMetaData(rawMoA,
				rawMoAInstance, moA);
		this.recordReturn(moA, moA.getObjectType(), Connection.class);
		// Record cyclic dependency
		LoadDependencies aLinked = new LoadDependencies(rawMoA, aIndex,
				rawMoAInstance, (LoadDependencies) null);
		LoadDependencies bLinked = new LoadDependencies(rawMoB, bIndex,
				rawMoBInstance, aLinked);
		aLinked.dependencies[0] = bLinked; // create cycle
		this.record_loadDependencies(aLinked);
		this.record_NoAdministration();
		// Record dependency sorting
		this.record_dependencySortingForCoordination(aLinked, bLinked);
		// Record reporting of cycle
		this.recordReturn(rawMoA, rawMoA.getBoundManagedObjectName(), "MO_A");
		this.recordReturn(rawMoB, rawMoB.getBoundManagedObjectName(), "MO_B");
		this.record_taskIssue("Can not have cyclic dependencies (MO_A, MO_B)");

		// Should not construct task meta-data as cyclic dependency
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Administrator} name.
	 */
	public void testNoAdministratorName() {

		final ManagedFunctionDutyConfiguration<?> dutyConfiguration = this
				.createMock(ManagedFunctionDutyConfiguration.class);

		// Record no administrator name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration,
				dutyConfiguration.getScopeAdministratorName(), null);
		this.record_taskIssue("No administrator name for pre-task at index 0");
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if unknown {@link Administrator}.
	 */
	public void testUnknownAdministrator() {

		final ManagedFunctionDutyConfiguration<?> dutyConfiguration = this
				.createMock(ManagedFunctionDutyConfiguration.class);

		// Record unknown administrator
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration,
				dutyConfiguration.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"),
				null);
		this.record_taskIssue("Can not find scope administrator 'ADMIN'");
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if no {@link Duty} name.
	 */
	public void testNoDutyName() {

		final ManagedFunctionDutyConfiguration<?> dutyConfiguration = this
				.createMock(ManagedFunctionDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);

		// Record no duty key
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration,
				dutyConfiguration.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"),
				rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				null);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyName(),
				null);
		this.record_taskIssue("No duty name/key for pre-task at index 0");
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure able to construct pre {@link ManagedFunctionDutyAssociation}.
	 */
	public void testConstructPreAdministratorDuty() {

		final ManagedFunctionDutyConfiguration<?> dutyConfiguration = this
				.createMock(ManagedFunctionDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);
		final RawBoundManagedObjectMetaData rawMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);
		final DutyKeyImpl<DutyKey> dutyKey = new DutyKeyImpl<DutyKey>(
				DutyKey.KEY);

		// Record construct task duty association
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration,
				dutyConfiguration.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"),
				rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				DutyKey.KEY);
		this.recordReturn(rawAdmin, rawAdmin.getDutyKey(DutyKey.KEY), dutyKey);
		this.recordReturn(rawAdmin,
				rawAdmin.getAdministeredRawBoundManagedObjects(),
				new RawBoundManagedObjectMetaData[] { rawMo });
		LoadDependencies moLinked = new LoadDependencies(rawMo, moIndex,
				rawMoInstance); // no dependencies
		this.record_loadDependencies(moLinked);
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.record_dependencySortingForCoordination(moLinked);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		ManagedFunctionMetaData<W, D, F> taskMetaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have duty
		assertEquals("Should have duty", 1,
				taskMetaData.getPreAdministrationMetaData().length);
		ManagedFunctionDutyAssociation<?> taskDuty = taskMetaData
				.getPreAdministrationMetaData()[0];
		assertEquals("Incorrect administrator index", adminIndex,
				taskDuty.getAdministratorIndex());
		assertEquals("Incorrect duty key", DutyKey.KEY, taskDuty.getDutyKey()
				.getKey());

		// Ensure have administered managed object included in required
		assertEquals("Administered managed objects should be required", 1,
				taskMetaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect administered managed object index", moIndex,
				taskMetaData.getRequiredManagedObjects()[0]);
	}

	/**
	 * Ensures that dependency of administered {@link ManagedObject} is also
	 * included in required {@link ManagedObjectIndex} instances.
	 */
	public void testPostTaskDutyWithAdministeredManagedObjectDependency() {

		final ManagedFunctionDutyConfiguration<?> dutyConfiguration = this
				.createMock(ManagedFunctionDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);
		final RawBoundManagedObjectMetaData rawMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawMoInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData rawDependency = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> rawDependencyInstance = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final ManagedObjectIndex dependencyIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final DutyKeyImpl<DutyKey> dutyKey = new DutyKeyImpl<DutyKey>(
				DutyKey.KEY);

		// Record construct task duty association
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration,
				dutyConfiguration.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getScopeAdministratorMetaData("ADMIN"),
				rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				null);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyName(),
				DutyKey.KEY.name());
		this.recordReturn(rawAdmin, rawAdmin.getDutyKey(DutyKey.KEY.name()),
				dutyKey);
		this.recordReturn(rawAdmin,
				rawAdmin.getAdministeredRawBoundManagedObjects(),
				new RawBoundManagedObjectMetaData[] { rawMo });
		LoadDependencies dependencyLinked = new LoadDependencies(rawDependency,
				dependencyIndex, rawDependencyInstance);
		LoadDependencies moLinked = new LoadDependencies(rawMo, moIndex,
				rawMoInstance, dependencyLinked);
		this.record_loadDependencies(moLinked);
		this.record_dependencySortingForCoordination(moLinked, dependencyLinked);
		this.record_NoGovernance();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		ManagedFunctionMetaData<W, D, F> taskMetaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have duty
		assertEquals("Should have post task duty", 1,
				taskMetaData.getPostAdministrationMetaData().length);
		ManagedFunctionDutyAssociation<?> taskDuty = taskMetaData
				.getPostAdministrationMetaData()[0];
		assertEquals("Incorrect administrator index", adminIndex,
				taskDuty.getAdministratorIndex());
		assertEquals("Incorrect duty key", DutyKey.KEY, taskDuty.getDutyKey()
				.getKey());

		// Ensure have administered managed object and dependency as required
		ManagedObjectIndex[] requiredManagedObjects = taskMetaData
				.getRequiredManagedObjects();
		assertEquals("Administered managed objects should be required", 2,
				requiredManagedObjects.length);
		assertEquals("Administered dependency must be first", dependencyIndex,
				requiredManagedObjects[0]);
		assertEquals(
				"Administered managed object must be after its dependency",
				moIndex, requiredManagedObjects[1]);
	}

	/**
	 * Ensure issue if no {@link Flow} {@link ManagedFunctionReference}.
	 */
	public void testNoFlowTaskNodeReference() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);

		// Record no task node reference
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), null);
		this.record_taskIssue("No task referenced for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Flow} {@link ManagedFunction} name.
	 */
	public void testNoFlowTaskName() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);

		// Record no task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Work} containing the {@link Flow}
	 * {@link ManagedFunction}.
	 */
	public void testUnknownFlowWork() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);

		// Record unknown work
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"UNKNOWN WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"IGNORED TASK NAME");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"UNKNOWN WORK", "IGNORED TASK NAME"), null);
		this.record_taskIssue("Can not find task meta-data (work=UNKNOWN WORK, task=IGNORED TASK NAME) for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Flow} {@link ManagedFunction}.
	 */
	public void testUnknownFlowTask() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);

		// Record unknown task
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("TASK"), null);
		this.recordReturn(this.taskLocator,
				this.taskLocator.getDefaultWorkMetaData(), this.workMetaData);
		this.recordReturn(this.workMetaData, this.workMetaData.getWorkName(),
				"WORK");
		this.record_taskIssue("Can not find task meta-data (work=WORK, task=TASK) for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if argument to {@link Flow} is not compatible.
	 */
	public void testIncompatibleFlowArgument() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record incompatible flow argument
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("WORK", "TASK"),
				flowTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), Integer.class);
		this.record_taskIssue("Argument is not compatible with task parameter (argument="
				+ Connection.class.getName()
				+ ", parameter="
				+ Integer.class.getName()
				+ ", work=WORK, task=TASK) for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link FlowInstigationStrategyEnum}.
	 */
	public void testNoFlowInstigationStrategy() {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record no instigation strategy
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("TASK"), flowTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), null);
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInstigationStrategy(), null);
		this.record_taskIssue("No instigation strategy provided for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct an asynchronous {@link Flow}.
	 */
	public void testConstructAsynchronousFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.ASYNCHRONOUS);
	}

	/**
	 * Ensure able to construct a parallel {@link Flow}.
	 */
	public void testConstructParallelFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.PARALLEL);
	}

	/**
	 * Ensure able to construct a sequential {@link Flow}.
	 */
	public void testConstructSequentialFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.SEQUENTIAL);
	}

	/**
	 * Ensure able to construct {@link Flow}.
	 */
	public void doConstructFlowTest(
			FlowInstigationStrategyEnum instigationStrategy) {

		final ManagedFunctionFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedFunctionFlowConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);

		// Record construct flow
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("TASK"), flowTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), Connection.class);
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInstigationStrategy(), instigationStrategy);
		if (instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			this.recordReturn(this.assetManagerFactory,
					this.assetManagerFactory.createAssetManager(AssetType.TASK,
							DEFAULT_WORK_NAME + "." + TASK_NAME, "Flow0",
							this.issues), assetManager);
		}
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify flow
		FlowMetaData<?> flowMetaData = metaData.getTaskMetaData().getFlow(0);
		assertEquals("Incorrect initial task meta-data", flowTaskMetaData,
				flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy", instigationStrategy,
				flowMetaData.getInstigationStrategy());

		// Ensure correct flow manager
		if (instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			// Asynchronous so should have flow manager
			assertEquals("Incorrect flow manager", assetManager,
					flowMetaData.getFlowManager());
		} else {
			// Not asynchronous so not require flow manager
			assertNull("Should not require flow manager",
					flowMetaData.getFlowManager());
		}
	}

	/**
	 * Ensure issue if no next {@link ManagedFunction} name.
	 */
	public void testNoNextTaskName() {

		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);

		// Record no next task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.recordReturn(this.configuration,
				this.configuration.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for next task");
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if argument to next {@link ManagedFunction} is incompatible.
	 */
	public void testIncompatibleNextTaskArgument() {

		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> nextTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record construct next task (which is on another work)
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.recordReturn(this.configuration,
				this.configuration.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"ANOTHER_WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"NEXT_TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("ANOTHER_WORK", "NEXT_TASK"),
				nextTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), Integer.class);
		this.recordReturn(nextTaskMetaData,
				nextTaskMetaData.getParameterType(), Connection.class);
		this.record_taskIssue("Argument is not compatible with task parameter (argument="
				+ Integer.class.getName()
				+ ", parameter="
				+ Connection.class.getName()
				+ ", work=ANOTHER_WORK, task=NEXT_TASK) for next task");
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct next {@link ManagedFunctionMetaData}.
	 */
	public void testConstructNextTask() {

		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> nextTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record construct next task (which is on another work)
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.recordReturn(this.configuration,
				this.configuration.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"ANOTHER_WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"NEXT_TASK");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("ANOTHER_WORK", "NEXT_TASK"),
				nextTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), null);
		this.recordReturn(nextTaskMetaData,
				nextTaskMetaData.getParameterType(), null);
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed next task
		ManagedFunctionMetaData<?, ?, ?> nextTask = metaData.getTaskMetaData()
				.getNextManagedFunctionContainerMetaData();
		assertEquals("Incorrect next task meta-data", nextTaskMetaData,
				nextTask);
	}

	/**
	 * Ensure issue if no {@link EscalationFlow} type.
	 */
	public void testNoEscalationType() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);

		// Record no escalation type
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTypeOfCause(), null);
		this.record_taskIssue("No escalation type for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunctionReference} for {@link EscalationFlow}.
	 */
	public void testNoEscalationTaskNodeReference() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);

		// Record no task referenced
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTaskNodeReference(), null);
		this.record_taskIssue("No task referenced for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedFunction} name for {@link EscalationFlow}.
	 */
	public void testNoEscalationTaskName() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);

		// Record no escalation task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incompatible {@link EscalationFlow}.
	 */
	public void testIncompatibleEscalation() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> escalationTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record construct escalation
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"ESCALATION_HANDLER");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("WORK", "ESCALATION_HANDLER"),
				escalationTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), NullPointerException.class);
		this.recordReturn(escalationTaskMetaData,
				escalationTaskMetaData.getParameterType(), Integer.class);
		this.record_taskIssue("Argument is not compatible with task parameter (argument="
				+ NullPointerException.class.getName()
				+ ", parameter="
				+ Integer.class.getName()
				+ ", work=WORK, task=ESCALATION_HANDLER) for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link EscalationFlow}.
	 */
	public void testConstructEscalation() {

		final ManagedFunctionEscalationConfiguration escalationConfiguration = this
				.createMock(ManagedFunctionEscalationConfiguration.class);
		final ManagedFunctionReference taskNodeReference = this
				.createMock(ManagedFunctionReference.class);
		final ManagedFunctionMetaData<?, ?, ?> escalationTaskMetaData = this
				.createMock(ManagedFunctionMetaData.class);

		// Record construct escalation
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_NoGovernance();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration,
				escalationConfiguration.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"ESCALATION_HANDLER");
		this.recordReturn(this.taskLocator,
				this.taskLocator.getTaskMetaData("ESCALATION_HANDLER"),
				escalationTaskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), NullPointerException.class);
		this.recordReturn(escalationTaskMetaData,
				escalationTaskMetaData.getParameterType(),
				RuntimeException.class);

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed escalation
		EscalationProcedure escalationProcedure = metaData.getTaskMetaData()
				.getEscalationProcedure();
		EscalationFlow escalation = escalationProcedure
				.getEscalation(new IOException("test"));
		assertEquals("Incorrect type of cause", IOException.class,
				escalation.getTypeOfCause());
		assertEquals("Incorrect escalation task meta-data",
				escalationTaskMetaData, escalation.getTaskMetaData());
	}

	/**
	 * Ensure issue if no {@link Governance} name.
	 */
	public void testNoGovernanceName() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();

		// Record construct governance
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Record configuring governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(gov, gov.getGovernanceName(), null);
		this.record_taskIssue("No Governance name provided for Governance 0");

		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Governance}.
	 */
	public void testUnknownGovernance() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();

		// Record construct governance
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Record configuring governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(gov, gov.getGovernanceName(), "UNKNOWN");
		this.record_taskIssue("Unknown Governance 'UNKNOWN'");

		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct required {@link Governance}.
	 */
	public void testGovernance() {

		final ManagedFunctionGovernanceConfiguration govOne = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final RawGovernanceMetaData<?, ?> rawGovOne = this
				.createMock(RawGovernanceMetaData.class);
		final ManagedFunctionGovernanceConfiguration govTwo = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);
		final RawGovernanceMetaData<?, ?> rawGovTwo = this
				.createMock(RawGovernanceMetaData.class);
		final Map<String, RawGovernanceMetaData<?, ?>> governances = new HashMap<String, RawGovernanceMetaData<?, ?>>();
		governances.put("GOV_ZERO",
				this.createMock(RawGovernanceMetaData.class));
		governances.put("GOV_ONE", rawGovOne);
		governances
				.put("GOV_TWO", this.createMock(RawGovernanceMetaData.class));
		governances.put("GOV_THREE", rawGovTwo);

		// Record construct governance
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Record configuring governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { govOne, govTwo });
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(govOne, govOne.getGovernanceName(), "GOV_ONE");
		this.recordReturn(rawGovOne, rawGovOne.getGovernanceIndex(), 1);
		this.recordReturn(govTwo, govTwo.getGovernanceName(), "GOV_THREE");
		this.recordReturn(rawGovTwo, rawGovTwo.getGovernanceIndex(), 3);

		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Ensure correct required governance
		boolean[] requiredGovernance = metaData.getTaskMetaData()
				.getRequiredGovernance();
		assertFalse("First governance should not be active",
				requiredGovernance[0]);
		assertTrue("Second governance should be active", requiredGovernance[1]);
		assertFalse("Third governance should not be active",
				requiredGovernance[2]);
		assertTrue("Fourth governance should be active", requiredGovernance[3]);
	}

	/**
	 * Flagged to be manual {@link Governance} however {@link Governance}
	 * configured for the {@link ManagedFunction}.
	 */
	public void testGovernanceButFlaggedManual() {

		final ManagedFunctionGovernanceConfiguration gov = this
				.createMock(ManagedFunctionGovernanceConfiguration.class);

		// Record construct governance
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Record configuring governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[] { gov });
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), true);
		this.record_taskIssue("Manually manage Governance but Governance configured for OfficeFloor management");

		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure no required {@link Governance} if flagged for manual
	 * {@link Governance} control.
	 */
	public void testManualGovernance() {

		// Record construct governance
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Record configuring governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[0]);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), true);

		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawManagedFunctionMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Ensure manual governance
		boolean[] requiredGovernance = metaData.getTaskMetaData()
				.getRequiredGovernance();
		assertNull("Should not have governance array when manual",
				requiredGovernance);
	}

	/**
	 * Records obtaining {@link ManagedFunction} name, {@link ManagedFunctionFactory} and responsible
	 * {@link Team}.
	 */
	private void record_taskNameFactoryTeam() {
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getWorkName(), DEFAULT_WORK_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration,
				this.configuration.getDifferentiator(), null);
		this.recordReturn(this.configuration,
				this.configuration.getOfficeTeamName(), "TEAM");
		this.recordReturn(this.rawWorkMetaData,
				this.rawWorkMetaData.getRawOfficeMetaData(),
				this.rawOfficeMetaData);
		Map<String, TeamManagement> teams = new HashMap<String, TeamManagement>();
		teams.put("TEAM", this.responsibleTeam);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getTeams(), teams);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getContinueTeam(), this.continueTeam);
	}

	/**
	 * Records no {@link ManagedObject} instances.
	 */
	private void record_NoManagedObjects() {
		this.recordReturn(this.configuration,
				this.configuration.getObjectConfiguration(),
				new ManagedFunctionObjectConfiguration[0]);
	}

	/**
	 * Records no {@link Administrator} {@link Duty} instances for {@link ManagedFunction}.
	 */
	private void record_NoAdministration() {
		this.recordReturn(this.configuration,
				this.configuration.getPreTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
		this.recordReturn(this.configuration,
				this.configuration.getPostTaskAdministratorDutyConfiguration(),
				new ManagedFunctionDutyConfiguration[0]);
	}

	/**
	 * Duty key for testing.
	 */
	private static enum DutyKey {
		KEY
	}

	/**
	 * Records no {@link Governance} instances for the {@link ManagedFunction}.
	 */
	private void record_NoGovernance() {
		final Map<?, ?> governances = this.createMock(Map.class);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceConfiguration(),
				new ManagedFunctionGovernanceConfiguration[0]);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.isManuallyManageGovernance(), false);
		this.recordReturn(this.rawOfficeMetaData,
				this.rawOfficeMetaData.getGovernanceMetaData(), governances);
		this.recordReturn(governances, governances.size(), 0);
	}

	/**
	 * Records obtaining the {@link ManagedFunctionLocator}.
	 */
	private void record_createWorkSpecificTaskMetaDataLocator() {
		this.recordReturn(
				this.inputTaskMetaDataLocator,
				this.inputTaskMetaDataLocator
						.createWorkSpecificOfficeMetaDataLocator(this.workMetaData),
				this.taskLocator);
		this.recordReturn(this.workMetaData, this.workMetaData.getWorkName(),
				DEFAULT_WORK_NAME);
	}

	/**
	 * Records obtaining the {@link RawManagedObjectMetaData} for a
	 * {@link RawBoundManagedObjectMetaData} with a single
	 * {@link RawBoundManagedObjectInstanceMetaData}.
	 * 
	 * @param rawMo
	 *            {@link RawBoundManagedObjectMetaData}.
	 * @param rawInstanceMo
	 *            Single {@link RawBoundManagedObjectInstanceMetaData}.
	 * @param rawMo
	 *            {@link RawManagedObjectMetaData}.
	 */
	private void record_singleInstance_getRawManagedObjectMetaData(
			RawBoundManagedObjectMetaData rawBoundMo,
			RawBoundManagedObjectInstanceMetaData<?> rawBoundMoInstance,
			RawManagedObjectMetaData<?, ?> rawMo) {
		this.recordReturn(
				rawBoundMo,
				rawBoundMo.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { rawBoundMoInstance });
		this.recordReturn(rawBoundMoInstance,
				rawBoundMoInstance.getRawManagedObjectMetaData(), rawMo);
	}

	/**
	 * Records sorting the {@link RawBoundManagedObjectMetaData} for
	 * coordinating.
	 * 
	 * @param boundManagedObjects
	 *            Listing of {@link RawBoundManagedObjectMetaData} to be sorted.
	 */
	private void record_dependencySortingForCoordination(
			LoadDependencies... boundManagedObjects) {
		for (LoadDependencies boundManagedObject : boundManagedObjects) {
			this.record_loadDependencies(boundManagedObject);
		}
	}

	/**
	 * Records loading the dependencies.
	 * 
	 * @param mo
	 *            {@link LoadDependencies}.
	 */
	private void record_loadDependencies(LoadDependencies mo) {
		this.record_loadDependencies(mo, new HashSet<ManagedObjectIndex>());
	}

	/**
	 * Records loading the dependencies.
	 * 
	 * @param mo
	 *            {@link LoadDependencies}.
	 * @param loadedDependencies
	 *            Set of loaded dependencies.
	 */
	private void record_loadDependencies(LoadDependencies mo,
			Set<ManagedObjectIndex> loadedDependencies) {

		// Record checking whether loaded
		this.recordReturn(mo.boundMo, mo.boundMo.getManagedObjectIndex(),
				mo.index);
		if (loadedDependencies.contains(mo.index)) {
			// Dependency already loaded
			return;
		}

		// Create the listing of dependencies
		RawBoundManagedObjectMetaData[] dependencies = new RawBoundManagedObjectMetaData[mo.dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			dependencies[i] = mo.dependencies[i].boundMo;
		}

		// Record loading the direct dependencies
		this.recordReturn(
				mo.boundMo,
				mo.boundMo.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { mo.boundMoInstance });
		this.recordReturn(mo.boundMoInstance,
				mo.boundMoInstance.getDependencies(), dependencies);

		// Flag the dependency as loaded
		loadedDependencies.add(mo.index);

		// Record loading the transient dependencies
		for (LoadDependencies dependency : mo.dependencies) {
			this.record_loadDependencies(dependency, loadedDependencies);
		}
	}

	/**
	 * Struct to contain details for recording loading dependencies.
	 */
	private static class LoadDependencies {

		public final RawBoundManagedObjectMetaData boundMo;

		public final ManagedObjectIndex index;

		public final RawBoundManagedObjectInstanceMetaData<?> boundMoInstance;

		public final LoadDependencies[] dependencies;

		public LoadDependencies(RawBoundManagedObjectMetaData boundMo,
				ManagedObjectIndex index,
				RawBoundManagedObjectInstanceMetaData<?> boundMoInstance,
				LoadDependencies... dependencies) {
			this.boundMo = boundMo;
			this.index = index;
			this.boundMoInstance = boundMoInstance;
			this.dependencies = dependencies;
		}
	}

	/**
	 * Records no {@link Flow}.
	 */
	private void record_NoFlows() {
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedFunctionFlowConfiguration[0]);
	}

	/**
	 * Records no next {@link ManagedFunction}.
	 */
	private void record_NoNextTask() {
		this.recordReturn(this.configuration,
				this.configuration.getNextTaskInFlow(), null);
	}

	/**
	 * Records no {@link EscalationFlow}.
	 */
	private void record_NoEscalations() {
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new ManagedFunctionEscalationConfiguration[0]);
	}

	/**
	 * Records an issue on the {@link OfficeFloorIssues} about the {@link ManagedFunction}.
	 * 
	 * @param issueDescription
	 *            Issue description expected.
	 */
	private void record_taskIssue(String issueDescription) {
		this.issues.addIssue(AssetType.TASK, TASK_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawManagedFunctionMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            If expected to be constructed.
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private RawManagedFunctionMetaData<W, D, F> constructRawTaskMetaData(
			boolean isExpectConstruct) {

		// Construct the raw task meta-data
		RawManagedFunctionMetaData<W, ?, ?> metaData = RawManagedFunctionMetaDataImpl.getFactory()
				.constructRawTaskMetaData(this.configuration, this.issues,
						this.rawWorkMetaData);
		if (isExpectConstruct) {
			assertNotNull("Expected to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return (RawManagedFunctionMetaData<W, D, F>) metaData;
	}

	/**
	 * Fully constructs the {@link RawManagedFunctionMetaData} by ensuring remaining state
	 * is loaded. Will always expect to construct the {@link RawManagedFunctionMetaData}.
	 * 
	 * @return {@link RawManagedFunctionMetaData}.
	 */
	private RawManagedFunctionMetaData<W, D, F> fullyConstructRawTaskMetaData() {

		// Construct the raw task meta-data
		RawManagedFunctionMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);

		// Other tasks and work expected to be constructed between these steps

		// Link the tasks and load remaining state to task meta-data
		metaData.linkTasks(this.inputTaskMetaDataLocator, this.workMetaData,
				this.assetManagerFactory, this.issues);

		// Return the fully constructed meta-data
		return metaData;
	}

}